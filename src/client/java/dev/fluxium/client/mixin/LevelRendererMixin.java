package dev.fluxium.client.mixin;

import dev.fluxium.client.render.AdaptiveRendering;
import dev.fluxium.config.FluxiumConfig;
import dev.fluxium.scheduler.FluxScheduler;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At("HEAD"), require = 0)
    private void fluxium$onRenderLevel(CallbackInfo ci) {
        if (FluxiumConfig.get().adaptiveRendering) {
            FluxScheduler.INSTANCE.getMetrics().renderQueueSize =
                    AdaptiveRendering.INSTANCE.isUnderFramePressure() ? 8 : 2;
        }
    }
}
