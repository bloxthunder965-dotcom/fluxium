package dev.fluxium.client.chunk;

import dev.fluxium.config.FluxiumConfig;
import dev.fluxium.scheduler.FluxScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;

public final class SmartChunkPipeline {
    public static final SmartChunkPipeline INSTANCE = new SmartChunkPipeline();
    private int pendingRebuildBudget = 4;
    private long lastBudgetResetMs = 0;
    private boolean initialized = false;
    private SmartChunkPipeline() {}

    public void tick() {
        if (!FluxiumConfig.get().smartChunkPipeline) return;
        initialized = true;
        long now = System.currentTimeMillis();
        if (now - lastBudgetResetMs > 50) {
            lastBudgetResetMs = now;
            float mult = FluxScheduler.INSTANCE.getChunkPriorityMultiplier();
            if (FluxScheduler.INSTANCE.shouldDeferBackgroundWork()) {
                pendingRebuildBudget = Math.max(1, (int) (2 * mult));
            } else {
                pendingRebuildBudget = Math.max(2, (int) (6 * mult));
            }
        }
    }

    public boolean isEnabled() { return FluxiumConfig.get().smartChunkPipeline; }
    public boolean isInitialized() { return initialized; }

    public float computePriority(SectionPos section) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0f;
        Vec3 eye = mc.player.getEyePosition(1f);
        double dx = (section.x() << 4) + 8 - eye.x;
        double dy = (section.y() << 4) + 8 - eye.y;
        double dz = (section.z() << 4) + 8 - eye.z;
        double distSq = dx * dx + dy * dy + dz * dz;
        float distScore = (float) (1.0 / (1.0 + Math.sqrt(distSq) / 32.0));
        Vec3 look = mc.player.getViewVector(1f);
        double dot = (dx * look.x + dy * look.y + dz * look.z) / (Math.sqrt(distSq) + 1e-6);
        float viewBonus = (float) Math.max(0, dot) * 0.5f;
        FluxScheduler.INSTANCE.getMetrics().chunkTasksPrioritized++;
        return (distScore + viewBonus) * FluxScheduler.INSTANCE.getChunkPriorityMultiplier();
    }

    public boolean tryConsumeRebuildBudget() {
        if (!isEnabled()) return true;
        if (pendingRebuildBudget > 0) {
            pendingRebuildBudget--;
            FluxScheduler.INSTANCE.getMetrics().tasksProcessed++;
            return true;
        }
        FluxScheduler.INSTANCE.getMetrics().tasksDeferred++;
        FluxScheduler.INSTANCE.getMetrics().workDeferredSuccessfully++;
        return false;
    }

    public void reportQueueSize(int size) {
        FluxScheduler.INSTANCE.getMetrics().chunkQueueSize = size;
    }
}
