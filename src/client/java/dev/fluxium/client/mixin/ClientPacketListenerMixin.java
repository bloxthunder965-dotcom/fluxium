package dev.fluxium.client.mixin;

import dev.fluxium.network.NetworkOptimizer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), require = 0)
    private void fluxium$onSend(Packet<?> packet, CallbackInfo ci) {
        if (NetworkOptimizer.INSTANCE.isEnabled()) {
            NetworkOptimizer.INSTANCE.onPacketProcessed();
        }
    }
}
