package dev.fluxium.logic;

import dev.fluxium.config.FluxiumConfig;
import dev.fluxium.scheduler.FluxScheduler;

public final class GameLogicOptimizer {
    public static final GameLogicOptimizer INSTANCE = new GameLogicOptimizer();

    private boolean initialized = false;

    private GameLogicOptimizer() {}

    public boolean isEnabled() {
        boolean enabled = FluxiumConfig.get().gameLogicOptimization;
        if (enabled) initialized = true;
        return enabled;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void recordCacheHit() {
        FluxScheduler.INSTANCE.getMetrics().cacheHits++;
    }

    public void recordCacheMiss() {
        FluxScheduler.INSTANCE.getMetrics().cacheMisses++;
    }
}
