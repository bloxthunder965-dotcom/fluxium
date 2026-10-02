package dev.fluxium.memory;

import dev.fluxium.config.FluxiumConfig;
import dev.fluxium.scheduler.FluxScheduler;

public final class MemoryOptimizer {
    public static final MemoryOptimizer INSTANCE = new MemoryOptimizer();

    private long lastTrimMs = 0;
    private static final long TRIM_INTERVAL_MS = 30_000;
    private boolean initialized = false;

    private MemoryOptimizer() {}

    public void tick() {
        if (!FluxiumConfig.get().memoryOptimization) return;
        initialized = true;
        long now = System.currentTimeMillis();
        if (now - lastTrimMs > TRIM_INTERVAL_MS) {
            lastTrimMs = now;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void recordAllocationAvoided() {
        FluxScheduler.INSTANCE.getMetrics().allocationsAvoided++;
    }

    public void recordCacheHit() {
        FluxScheduler.INSTANCE.getMetrics().cacheHits++;
    }

    public void recordCacheMiss() {
        FluxScheduler.INSTANCE.getMetrics().cacheMisses++;
    }
}
