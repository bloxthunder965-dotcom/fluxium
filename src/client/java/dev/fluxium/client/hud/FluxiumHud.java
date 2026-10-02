package dev.fluxium.client.hud;

import dev.fluxium.Fluxium;
import dev.fluxium.config.FluxiumConfig;
import dev.fluxium.diagnostics.BenchmarkSystem;
import dev.fluxium.diagnostics.DiagnosticsSystem;
import dev.fluxium.scheduler.FluxScheduler;
import dev.fluxium.scheduler.WorkloadMetrics;

public final class FluxiumHud {
    private FluxiumHud() {}

    public static boolean isVisible() {
        return FluxiumConfig.get().debugHud;
    }

    public static String formatTextSnapshot() {
        WorkloadMetrics m = FluxScheduler.INSTANCE.getMetrics();
        StringBuilder sb = new StringBuilder();
        for (String line : buildLines(m)) sb.append(line).append('\n');
        return sb.toString();
    }

    public static String[] buildLines(WorkloadMetrics m) {
        String scheduler = FluxScheduler.INSTANCE.isActive() ? "ACTIVE" : "OFF";
        String bench = BenchmarkSystem.INSTANCE.isRunning() ? "RUNNING" : "idle";
        DiagnosticsSystem.SystemStatus[] statuses = DiagnosticsSystem.INSTANCE.getStatuses();
        StringBuilder systems = new StringBuilder();
        for (var s : statuses) {
            if (s.active()) {
                if (!systems.isEmpty()) systems.append(", ");
                systems.append(s.name());
            }
        }
        if (systems.isEmpty()) systems.append("none");
        return new String[]{
                "FLUXIUM " + Fluxium.VERSION + "  DEBUG",
                String.format("FPS          %.0f", m.fps),
                String.format("FRAME        %.1f ms", m.frameTimeMs),
                String.format("1%% LOW       %.0f", m.onePercentLowFps),
                String.format("MEMORY       %d MB", m.usedMemoryMb),
                "MODE         " + m.mode.name(),
                "SCHEDULER    " + scheduler,
                "BENCHMARK    " + bench,
                "SYSTEMS      " + systems
        };
    }
}
