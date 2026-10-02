package dev.fluxium.client.mixin;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Soft prioritization of chunk rebuilds.
 */
@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher")
public class ChunkRenderDispatcherMixin {
}
