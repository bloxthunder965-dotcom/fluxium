package dev.fluxium.client;

import dev.fluxium.Fluxium;
import dev.fluxium.client.chunk.SmartChunkPipeline;
import dev.fluxium.client.command.FluxiumCommands;
import dev.fluxium.client.hud.FluxiumHud;
import dev.fluxium.client.render.AdaptiveRendering;
import dev.fluxium.config.FluxiumConfig;
import dev.fluxium.diagnostics.BenchmarkSystem;
import dev.fluxium.memory.MemoryOptimizer;
import dev.fluxium.scheduler.FluxScheduler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Client entrypoint. Initializes all Fluxium systems and diagnostics.
 */
public class FluxiumClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Fluxium.LOGGER.info("Initializing Fluxium {} – client workload management framework", Fluxium.VERSION);

        FluxiumConfig.get();

        FluxiumCommands.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FluxScheduler.INSTANCE.tick();
            MemoryOptimizer.INSTANCE.tick();
            SmartChunkPipeline.INSTANCE.tick();
            AdaptiveRendering.INSTANCE.tick();

            if (BenchmarkSystem.INSTANCE.isRunning()) {
                BenchmarkSystem.INSTANCE.sample();
            }
        });

        Fluxium.LOGGER.info("Fluxium ready. Scheduler, Adaptive Rendering, Smart Chunks, Memory, Network and Frame-Time Protection active.");
        Fluxium.LOGGER.info("Commands: /fluxium debug | diagnostics | benchmark start|stop | status | counters reset");
    }
}
