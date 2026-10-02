package dev.fluxium.client.render;

import dev.fluxium.config.FluxiumConfig;
import dev.fluxium.scheduler.FluxScheduler;

public final class AdaptiveRendering {
    public static final AdaptiveRendering INSTANCE = new AdaptiveRendering();

    private boolean initialized = false;

    private AdaptiveRendering() {}

    public void tick() {
        if (FluxiumConfig.get().adaptiveRendering) {
            initialized = true;
        }
    }

    public boolean isEnabled() {
        return FluxiumConfig.get().adaptiveRendering;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean shouldSimplifyDistantEntity() {
        if (!isEnabled()) return false;
        boolean simplify = FluxScheduler.INSTANCE.getEntityPriorityMultiplier() < 0.8f
                && !FluxScheduler.INSTANCE.shouldDeferBackgroundWork();
        if (simplify) {
            FluxScheduler.INSTANCE.getMetrics().renderTasksOptimized++;
        }
        return simplify;
    }

    public boolean isUnderFramePressure() {
        return FluxScheduler.INSTANCE.shouldDeferBackgroundWork();
    }

    public void recordRenderOptimization() {
        FluxScheduler.INSTANCE.getMetrics().renderTasksOptimized++;
    }
}
