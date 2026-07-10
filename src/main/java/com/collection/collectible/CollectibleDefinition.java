package com.collection.collectible;

import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record CollectibleDefinition(
        ResourceLocation id,
        Supplier<Item> item,
        String setId,
        String clueKey
) {
    public boolean matches(ItemStack stack) {
        return stack.is(this.item.get());
    }

    public Component name() {
        return this.item.get().getDefaultInstance().getHoverName();
    }

    public Component clue() {
        return Component.translatable(this.clueKey);
    }
}
