package dev.fluxium.scheduler;

/**
 * Lightweight snapshot of current client workload + real optimization counters.
 * Updated each frame with minimal overhead. All counters are real measurements.
 */
public final class WorkloadMetrics {
    public float fps;
    public float frameTimeMs;
    public float onePercentLowFps;
    public float cpuLoadEstimate;
    public long usedMemoryMb;
    public int chunkQueueSize;
    public int renderQueueSize;
    public int entityCount;
    public int networkPacketsThisTick;
    public PlayerMode mode = PlayerMode.IDLE;

    public long tasksProcessed;
    public long tasksDeferred;
    public long chunkTasksPrioritized;
    public long renderTasksOptimized;
    public long cacheHits;
    public long cacheMisses;
    public long workDeferredSuccessfully;
    public long allocationsAvoided;
    public long packetsTracked;

    public void resetTickCounters() {
        networkPacketsThisTick = 0;
    }

    public void resetAllCounters() {
        tasksProcessed = 0;
        tasksDeferred = 0;
        chunkTasksPrioritized = 0;
        renderTasksOptimized = 0;
        cacheHits = 0;
        cacheMisses = 0;
        workDeferredSuccessfully = 0;
        allocationsAvoided = 0;
        packetsTracked = 0;
    }
}
