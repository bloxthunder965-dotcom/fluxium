package dev.fluxium.scheduler;

import dev.fluxium.config.FluxiumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class FluxScheduler {
    public static final FluxScheduler INSTANCE = new FluxScheduler();

    private final WorkloadMetrics metrics = new WorkloadMetrics();
    private long lastFrameNanos = System.nanoTime();
    private final float[] frameTimeHistory = new float[64];
    private int frameHistoryIndex = 0;
    private int frameHistoryCount = 0;
    private Vec3 lastPlayerPos = Vec3.ZERO;
    private long lastModeCheckMs = 0;
    private int combatTicksRemaining = 0;
    private boolean active = false;
    private boolean initialized = false;

    private FluxScheduler() {}

    public void tick() {
        if (!FluxiumConfig.get().fluxScheduler) {
            active = false;
            return;
        }
        active = true;
        initialized = true;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            metrics.mode = PlayerMode.IDLE;
            return;
        }

        long now = System.nanoTime();
        float frameMs = (now - lastFrameNanos) / 1_000_000f;
        lastFrameNanos = now;

        frameTimeHistory[frameHistoryIndex] = frameMs;
        frameHistoryIndex = (frameHistoryIndex + 1) % frameTimeHistory.length;
        if (frameHistoryCount < frameTimeHistory.length) frameHistoryCount++;

        metrics.frameTimeMs = frameMs;
        metrics.fps = frameMs > 0.1f ? 1000f / frameMs : 0f;

        if (frameHistoryCount > 8) {
            float[] sorted = new float[frameHistoryCount];
            System.arraycopy(frameTimeHistory, 0, sorted, 0, frameHistoryCount);
            java.util.Arrays.sort(sorted);
            int idx = Math.max(0, (int) (frameHistoryCount * 0.99) - 1);
            float worst = sorted[Math.min(idx, sorted.length - 1)];
            metrics.onePercentLowFps = worst > 0.1f ? 1000f / worst : 0f;
        }

        Runtime rt = Runtime.getRuntime();
        metrics.usedMemoryMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        metrics.cpuLoadEstimate = Math.min(100f, (frameMs / 16.67f) * 100f);

        long nowMs = System.currentTimeMillis();
        if (nowMs - lastModeCheckMs > 250) {
            lastModeCheckMs = nowMs;
            updatePlayerMode(mc.player);
        }

        metrics.tasksProcessed++;
        metrics.resetTickCounters();
    }

    private void updatePlayerMode(LocalPlayer player) {
        if (combatTicksRemaining > 0) {
            combatTicksRemaining--;
            metrics.mode = PlayerMode.COMBAT;
            return;
        }

        Vec3 pos = player.position();
        double dist = pos.distanceTo(lastPlayerPos);
        lastPlayerPos = pos;

        boolean moving = dist > 0.05;
        boolean lookingAround = Math.abs(player.getXRot() - player.xRotO) > 0.1
                || Math.abs(player.getYRot() - player.yRotO) > 0.1;

        if (player.getLastAttacker() != null || player.hurtTime > 0) {
            combatTicksRemaining = 40;
            metrics.mode = PlayerMode.COMBAT;
        } else if (!moving && !lookingAround) {
            metrics.mode = PlayerMode.IDLE;
        } else if (player.isShiftKeyDown()) {
            metrics.mode = PlayerMode.BUILDING;
        } else {
            metrics.mode = PlayerMode.EXPLORING;
        }
    }

    public void notifyCombat() {
        combatTicksRemaining = 60;
        metrics.mode = PlayerMode.COMBAT;
    }

    public WorkloadMetrics getMetrics() {
        return metrics;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean shouldDeferBackgroundWork() {
        if (!FluxiumConfig.get().frameTimeProtection) return false;
        boolean defer = metrics.frameTimeMs > 20f || metrics.onePercentLowFps < 45f;
        if (defer) {
            metrics.tasksDeferred++;
            metrics.workDeferredSuccessfully++;
        }
        return defer;
    }

    public float getChunkPriorityMultiplier() {
        return switch (metrics.mode) {
            case EXPLORING -> 1.4f;
            case BUILDING -> 1.1f;
            case COMBAT -> 0.6f;
            case IDLE -> 0.4f;
        };
    }

    public float getEntityPriorityMultiplier() {
        return switch (metrics.mode) {
            case COMBAT -> 1.5f;
            case BUILDING -> 1.0f;
            case EXPLORING -> 0.9f;
            case IDLE -> 0.5f;
        };
    }
}
