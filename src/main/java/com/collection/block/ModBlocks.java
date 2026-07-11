package com.collection.block;

import com.collection.Collection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Collection.MODID);

    public static final DeferredBlock<Block> COLLECTOR_WORKSTATION =
            BLOCKS.registerSimpleBlock(
                    "collector_workstation",
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CARTOGRAPHY_TABLE)
                            .strength(2.5F)
                            .sound(SoundType.WOOD)
            );

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
