package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.util.collection.LongObjectStorage;
import net.minecraft.world.chunk.ClientChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkProvider.class)
public interface ClientChunkProviderAccessor {
    @Accessor("chunkStorage")
    LongObjectStorage getChunkMap();
}