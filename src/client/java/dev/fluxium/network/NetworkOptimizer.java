package dev.fluxium.network;

import dev.fluxium.config.FluxiumConfig;
import dev.fluxium.scheduler.FluxScheduler;

public final class NetworkOptimizer {
    public static final NetworkOptimizer INSTANCE = new NetworkOptimizer();

    private boolean initialized = false;

    private NetworkOptimizer() {}

    public boolean isEnabled() {
        return FluxiumConfig.get().networkOptimization;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void onPacketProcessed() {
        initialized = true;
        FluxScheduler.INSTANCE.getMetrics().networkPacketsThisTick++;
        FluxScheduler.INSTANCE.getMetrics().packetsTracked++;
    }

    public boolean shouldDeferNonCritical() {
        boolean defer = FluxScheduler.INSTANCE.shouldDeferBackgroundWork();
        if (defer) {
            FluxScheduler.INSTANCE.getMetrics().tasksDeferred++;
            FluxScheduler.INSTANCE.getMetrics().workDeferredSuccessfully++;
        }
        return defer;
    }
}
