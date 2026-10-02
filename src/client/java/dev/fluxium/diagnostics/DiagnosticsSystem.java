package dev.fluxium.diagnostics;

import dev.fluxium.client.chunk.SmartChunkPipeline;
import dev.fluxium.client.render.AdaptiveRendering;
import dev.fluxium.config.FluxiumConfig;
import dev.fluxium.logic.GameLogicOptimizer;
import dev.fluxium.memory.MemoryOptimizer;
import dev.fluxium.network.NetworkOptimizer;
import dev.fluxium.scheduler.FluxScheduler;

public final class DiagnosticsSystem {
    public static final DiagnosticsSystem INSTANCE = new DiagnosticsSystem();

    private DiagnosticsSystem() {}

    public record SystemStatus(String name, boolean active) {}

    public SystemStatus[] getStatuses() {
        FluxiumConfig cfg = FluxiumConfig.get();
        return new SystemStatus[]{
                new SystemStatus("Flux Scheduler", cfg.fluxScheduler && FluxScheduler.INSTANCE.isInitialized()),
                new SystemStatus("Adaptive Rendering", cfg.adaptiveRendering && AdaptiveRendering.INSTANCE.isInitialized()),
                new SystemStatus("Chunk Pipeline", cfg.smartChunkPipeline && SmartChunkPipeline.INSTANCE.isInitialized()),
                new SystemStatus("Memory Optimizer", cfg.memoryOptimization && MemoryOptimizer.INSTANCE.isInitialized()),
                new SystemStatus("Logic Optimizer", cfg.gameLogicOptimization && GameLogicOptimizer.INSTANCE.isInitialized()),
                new SystemStatus("Network Optimizer", cfg.networkOptimization && NetworkOptimizer.INSTANCE.isInitialized()),
                new SystemStatus("Frame Protection", cfg.frameTimeProtection && FluxScheduler.INSTANCE.isInitialized())
        };
    }

    public String formatReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Fluxium Diagnostics ===\n");
        for (SystemStatus s : getStatuses()) {
            sb.append(String.format("%-22s %s%n", s.name(), s.active() ? "ACTIVE" : "INACTIVE"));
        }
        return sb.toString();
    }
}
