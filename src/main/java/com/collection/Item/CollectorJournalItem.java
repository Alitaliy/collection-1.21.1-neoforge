package com.collection.Item;

import com.collection.CollectionClient;
import com.collection.progress.CollectionProgressService;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class CollectorJournalItem extends Item {
    public CollectorJournalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            if (!player.isShiftKeyDown()) {
                CollectionClient.openJournalScreen();
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                CollectionProgressService.giveClueMap(serverPlayer);
            } else {
                CollectionProgressService.syncInventory(serverPlayer, false);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("collection.journal.tooltip.summary").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("collection.journal.tooltip.map").withStyle(ChatFormatting.DARK_GRAY));
    }
}
