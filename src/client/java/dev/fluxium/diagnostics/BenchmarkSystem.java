package dev.fluxium.diagnostics;

import dev.fluxium.scheduler.FluxScheduler;
import dev.fluxium.scheduler.WorkloadMetrics;

public final class BenchmarkSystem {
    public static final BenchmarkSystem INSTANCE = new BenchmarkSystem();

    private boolean running = false;
    private long startTimeMs;
    private long sampleCount;
    private double sumFps;
    private double sumFrameTime;
    private float minFps = Float.MAX_VALUE;
    private float maxFrameTime;
    private final float[] frameTimeSamples = new float[4096];
    private int sampleIndex;
    private long sumMemoryMb;
    private long sumChunkQueue;
    private long sumRenderQueue;
    private BenchmarkResult lastResult;

    private BenchmarkSystem() {}

    public boolean isRunning() { return running; }

    public void start() {
        running = true;
        startTimeMs = System.currentTimeMillis();
        sampleCount = 0;
        sumFps = 0; sumFrameTime = 0;
        minFps = Float.MAX_VALUE; maxFrameTime = 0;
        sampleIndex = 0;
        sumMemoryMb = 0; sumChunkQueue = 0; sumRenderQueue = 0;
        lastResult = null;
    }

    public void stop() {
        if (!running) return;
        running = false;
        lastResult = computeResult();
    }

    public void sample() {
        if (!running) return;
        WorkloadMetrics m = FluxScheduler.INSTANCE.getMetrics();
        sampleCount++;
        sumFps += m.fps;
        sumFrameTime += m.frameTimeMs;
        if (m.fps > 0 && m.fps < minFps) minFps = m.fps;
        if (m.frameTimeMs > maxFrameTime) maxFrameTime = m.frameTimeMs;
        if (sampleIndex < frameTimeSamples.length) frameTimeSamples[sampleIndex++] = m.frameTimeMs;
        sumMemoryMb += m.usedMemoryMb;
        sumChunkQueue += m.chunkQueueSize;
        sumRenderQueue += m.renderQueueSize;
    }

    public BenchmarkResult getLastResult() { return lastResult; }

    public BenchmarkResult getLiveResult() {
        if (!running || sampleCount == 0) return lastResult;
        return computeResult();
    }

    private BenchmarkResult computeResult() {
        if (sampleCount == 0) {
            return new BenchmarkResult(0f, 0f, 0f, 0f, 0f, 0L, 0f, 0f, sampleCount, System.currentTimeMillis() - startTimeMs);
        }
        float avgFps = (float) (sumFps / sampleCount);
        float avgFrame = (float) (sumFrameTime / sampleCount);
        float onePercentLow = computeOnePercentLow();
        long avgMem = sumMemoryMb / sampleCount;
        float avgChunk = (float) sumChunkQueue / sampleCount;
        float avgRender = (float) sumRenderQueue / sampleCount;
        long duration = System.currentTimeMillis() - startTimeMs;
        return new BenchmarkResult(avgFps, minFps == Float.MAX_VALUE ? 0 : minFps, onePercentLow, avgFrame, maxFrameTime, avgMem, avgChunk, avgRender, sampleCount, duration);
    }

    private float computeOnePercentLow() {
        int n = sampleIndex;
        if (n < 8) return 0;
        float[] sorted = new float[n];
        System.arraycopy(frameTimeSamples, 0, sorted, 0, n);
        java.util.Arrays.sort(sorted);
        int idx = Math.max(0, (int) (n * 0.99) - 1);
        float worst = sorted[Math.min(idx, sorted.length - 1)];
        return worst > 0.1f ? 1000f / worst : 0f;
    }

    public record BenchmarkResult(
            float avgFps, float minFps, float onePercentLowFps,
            float avgFrameTimeMs, float maxFrameTimeMs,
            long avgMemoryMb, float avgChunkQueue, float avgRenderQueue,
            long sampleCount, long durationMs
    ) {
        public String format() {
            return String.format(
                    "=== Fluxium Benchmark ===%nDuration: %.1f s (%d samples)%nAvg FPS: %.1f%nMin FPS: %.1f%n1%% Low FPS: %.1f%nAvg Frame: %.2f ms%nMax Frame: %.2f ms%nAvg Memory: %d MB%nAvg Chunk Queue: %.1f%nAvg Render Load: %.1f%n",
                    durationMs / 1000.0, sampleCount, avgFps, minFps, onePercentLowFps,
                    avgFrameTimeMs, maxFrameTimeMs, avgMemoryMb, avgChunkQueue, avgRenderQueue);
        }
    }
}
