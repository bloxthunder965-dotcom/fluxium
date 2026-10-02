package dev.fluxium.mixin;

import dev.fluxium.config.FluxiumConfig;
import dev.fluxium.scheduler.FluxScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "tickEntities", at = @At("HEAD"), require = 0)
    private void fluxium$onTickEntities(CallbackInfo ci) {
        if (!FluxiumConfig.get().fluxScheduler) return;
        try {
            ClientLevel self = (ClientLevel) (Object) this;
            FluxScheduler.INSTANCE.getMetrics().entityCount =
                    (int) Math.min(Integer.MAX_VALUE, self.entitiesForRendering().spliterator().estimateSize());
        } catch (Throwable ignored) {}
    }
}
