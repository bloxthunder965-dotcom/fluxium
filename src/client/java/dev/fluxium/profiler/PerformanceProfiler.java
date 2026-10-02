package dev.fluxium.profiler;

import dev.fluxium.scheduler.FluxScheduler;
import dev.fluxium.scheduler.WorkloadMetrics;

/**
 * Extremely low-overhead profiler.
 * Reads metrics already collected by FluxScheduler; adds almost nothing.
 */
public final class PerformanceProfiler {
    public static final PerformanceProfiler INSTANCE = new PerformanceProfiler();

    private PerformanceProfiler() {}

    public WorkloadMetrics snapshot() {
        return FluxScheduler.INSTANCE.getMetrics();
    }
}
