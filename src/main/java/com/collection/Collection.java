package com.collection;

import com.collection.Item.ModCreativeModeTabs;
import com.collection.Item.ModItems;
import com.collection.block.ModBlocks;
import com.collection.loot.ModLootModifiers;
import com.collection.progress.ModAttachments;
import com.collection.village.ModVillagers;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Collection.MODID)
public final class Collection {
    public static final String MODID = "collection";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Collection(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModVillagers.register(modEventBus);
    }
}
