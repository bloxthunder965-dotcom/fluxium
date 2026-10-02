package dev.fluxium.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into the main client loop.
 * Primary scheduler tick is driven from Fabric ClientTickEvents for safety.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void fluxium$onTick(CallbackInfo ci) {
        // Observation only - real work is in ClientTickEvents
    }
}
