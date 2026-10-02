package dev.fluxium.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.fluxium.Fluxium;
import dev.fluxium.config.FluxiumConfig;
import dev.fluxium.diagnostics.BenchmarkSystem;
import dev.fluxium.diagnostics.DiagnosticsSystem;
import dev.fluxium.scheduler.FluxScheduler;
import dev.fluxium.scheduler.WorkloadMetrics;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public final class FluxiumCommands {
    private FluxiumCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommands.literal("fluxium");

        root.then(ClientCommands.literal("debug")
                .executes(ctx -> {
                    FluxiumConfig cfg = FluxiumConfig.get();
                    cfg.debugHud = !cfg.debugHud;
                    cfg.save();
                    String state = cfg.debugHud ? "enabled" : "disabled";
                    ctx.getSource().sendFeedback(Component.literal("§b[Fluxium]§r Debug HUD " + state));
                    if (cfg.debugHud) sendLiveSnapshot(ctx.getSource());
                    return 1;
                }));

        root.then(ClientCommands.literal("diagnostics")
                .executes(ctx -> {
                    for (String line : DiagnosticsSystem.INSTANCE.formatReport().split("\n")) {
                        ctx.getSource().sendFeedback(Component.literal("§b" + line));
                    }
                    return 1;
                }));

        LiteralArgumentBuilder<FabricClientCommandSource> bench = ClientCommands.literal("benchmark");

        bench.then(ClientCommands.literal("start").executes(ctx -> {
            if (BenchmarkSystem.INSTANCE.isRunning()) {
                ctx.getSource().sendFeedback(Component.literal("§e[Fluxium] Benchmark already running."));
                return 0;
            }
            BenchmarkSystem.INSTANCE.start();
            ctx.getSource().sendFeedback(Component.literal("§a[Fluxium] Benchmark started."));
            return 1;
        }));

        bench.then(ClientCommands.literal("stop").executes(ctx -> {
            if (!BenchmarkSystem.INSTANCE.isRunning()) {
                ctx.getSource().sendFeedback(Component.literal("§e[Fluxium] No benchmark running."));
                return 0;
            }
            BenchmarkSystem.INSTANCE.stop();
            var result = BenchmarkSystem.INSTANCE.getLastResult();
            if (result != null) {
                for (String line : result.format().split("\n")) {
                    ctx.getSource().sendFeedback(Component.literal("§b" + line));
                }
            }
            return 1;
        }));

        bench.executes(ctx -> {
            if (BenchmarkSystem.INSTANCE.isRunning()) {
                ctx.getSource().sendFeedback(Component.literal("§a[Fluxium] Benchmark RUNNING"));
                var live = BenchmarkSystem.INSTANCE.getLiveResult();
                if (live != null) for (String line : live.format().split("\n"))
                    ctx.getSource().sendFeedback(Component.literal("§b" + line));
            } else {
                var last = BenchmarkSystem.INSTANCE.getLastResult();
                if (last == null)
                    ctx.getSource().sendFeedback(Component.literal("§e[Fluxium] No benchmark data."));
                else {
                    ctx.getSource().sendFeedback(Component.literal("§a[Fluxium] Last result:"));
                    for (String line : last.format().split("\n"))
                        ctx.getSource().sendFeedback(Component.literal("§b" + line));
                }
            }
            return 1;
        });

        root.then(bench);

        root.then(ClientCommands.literal("status").executes(ctx -> {
            sendLiveSnapshot(ctx.getSource());
            return 1;
        }));

        root.then(ClientCommands.literal("counters")
                .then(ClientCommands.literal("reset").executes(ctx -> {
                    FluxScheduler.INSTANCE.getMetrics().resetAllCounters();
                    ctx.getSource().sendFeedback(Component.literal("§a[Fluxium] Counters reset."));
                    return 1;
                })));

        dispatcher.register(root);
        Fluxium.LOGGER.info("Registered /fluxium client commands");
    }

    private static void sendLiveSnapshot(FabricClientCommandSource source) {
        WorkloadMetrics m = FluxScheduler.INSTANCE.getMetrics();
        source.sendFeedback(Component.literal("§b=== Fluxium Live ==="));
        source.sendFeedback(Component.literal(String.format("§7FPS §f%.0f  §7Frame §f%.1f ms  §71%% Low §f%.0f", m.fps, m.frameTimeMs, m.onePercentLowFps)));
        source.sendFeedback(Component.literal(String.format("§7CPU est §f%.0f%%  §7Memory §f%d MB", m.cpuLoadEstimate, m.usedMemoryMb)));
        source.sendFeedback(Component.literal(String.format("§7Chunks §f%d  §7Render §f%d  §7Entities §f%d  §7Net §f%d",
                m.chunkQueueSize, m.renderQueueSize, m.entityCount, m.networkPacketsThisTick)));
        source.sendFeedback(Component.literal(String.format("§7Mode §f%s  §7Scheduler §f%s",
                m.mode.name(), FluxScheduler.INSTANCE.isActive() ? "ACTIVE" : "OFF")));
        source.sendFeedback(Component.literal(String.format(
                "§7Tasks §f%d  §7Deferred §f%d  §7ChunkPrio §f%d  §7RenderOpt §f%d",
                m.tasksProcessed, m.tasksDeferred, m.chunkTasksPrioritized, m.renderTasksOptimized)));
        source.sendFeedback(Component.literal(String.format(
                "§7Cache H/M §f%d/%d  §7Alloc avoided §f%d  §7Net tracked §f%d",
                m.cacheHits, m.cacheMisses, m.allocationsAvoided, m.packetsTracked)));
    }
}
