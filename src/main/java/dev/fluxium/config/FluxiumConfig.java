package dev.fluxium.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.fluxium.Fluxium;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration for all Fluxium systems.
 * Safe defaults: all optimizations enabled, HUD disabled.
 */
public final class FluxiumConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("fluxium.json");

    public boolean fluxScheduler = true;
    public boolean adaptiveRendering = true;
    public boolean smartChunkPipeline = true;
    public boolean memoryOptimization = true;
    public boolean gameLogicOptimization = true;
    public boolean networkOptimization = true;
    public boolean frameTimeProtection = true;
    public boolean debugHud = false;
    public int hudKeyCode = 292;

    private static FluxiumConfig INSTANCE;

    public static FluxiumConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static FluxiumConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                FluxiumConfig cfg = GSON.fromJson(json, FluxiumConfig.class);
                if (cfg != null) {
                    Fluxium.LOGGER.info("Loaded Fluxium config from {}", CONFIG_PATH);
                    return cfg;
                }
            } catch (Exception e) {
                Fluxium.LOGGER.warn("Failed to load config, using defaults", e);
            }
        }
        FluxiumConfig defaults = new FluxiumConfig();
        defaults.save();
        return defaults;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            Fluxium.LOGGER.error("Failed to save config", e);
        }
    }
}
