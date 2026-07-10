package com.collection.collectible;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record CollectibleSetDefinition(
        String id,
        String nameKey,
        Supplier<Item> rewardItem,
        List<CollectibleDefinition> collectibles
) {
    public Component name() {
        return Component.translatable(this.nameKey);
    }

    public int size() {
        return this.collectibles.size();
    }

    public ItemStack createRewardStack() {
        return new ItemStack(this.rewardItem.get());
    }
}
