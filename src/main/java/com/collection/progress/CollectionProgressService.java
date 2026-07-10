package com.collection.progress;

import com.collection.collectible.CollectibleCatalog;
import com.collection.collectible.CollectibleDefinition;
import com.collection.collectible.CollectibleSetDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class CollectionProgressService {
    private CollectionProgressService() {
    }

    public static void handleCollectedStack(ServerPlayer player, ItemStack stack, boolean announceDiscoveries) {
        CollectibleCatalog.fromStack(stack).ifPresent(collectible -> {
            PlayerCollectionProgress progress = player.getData(ModAttachments.PLAYER_COLLECTION_PROGRESS);
            if (progress.discover(collectible.id()) && announceDiscoveries) {
                player.sendSystemMessage(Component.translatable("collection.progress.discovered", collectible.name()));
            }
            thisGrantRewards(player, progress);
        });
    }

    public static void syncInventory(ServerPlayer player, boolean announceDiscoveries) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                handleCollectedStack(player, stack, announceDiscoveries);
            }
        }
    }

    public static void showJournal(ServerPlayer player, boolean detailed) {
        syncInventory(player, false);
        PlayerCollectionProgress progress = player.getData(ModAttachments.PLAYER_COLLECTION_PROGRESS);

        player.sendSystemMessage(Component.translatable(
                detailed ? "collection.journal.detail_header" : "collection.journal.summary_header"
        ).withStyle(ChatFormatting.GOLD));

        for (CollectibleSetDefinition set : CollectibleCatalog.SETS) {
            int discovered = progress.discoveredCount(set);
            player.sendSystemMessage(Component.translatable("collection.journal.set_progress", set.name(), discovered, set.size()));

            if (detailed) {
                for (CollectibleDefinition collectible : set.collectibles()) {
                    if (progress.hasDiscovered(collectible.id())) {
                        player.sendSystemMessage(Component.translatable("collection.journal.item_found", collectible.name())
                                .withStyle(ChatFormatting.DARK_GREEN));
                    } else {
                        player.sendSystemMessage(Component.translatable("collection.journal.item_missing", collectible.name())
                                .withStyle(ChatFormatting.GRAY));
                        player.sendSystemMessage(Component.translatable("collection.journal.clue", collectible.clue())
                                .withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            } else if (discovered < set.size()) {
                set.collectibles().stream()
                        .filter(collectible -> !progress.hasDiscovered(collectible.id()))
                        .findFirst()
                        .ifPresent(collectible -> player.sendSystemMessage(
                                Component.translatable("collection.journal.next_clue", collectible.clue())
                                        .withStyle(ChatFormatting.DARK_GRAY)
                        ));
            } else {
                player.sendSystemMessage(Component.translatable("collection.journal.set_complete", set.name())
                        .withStyle(ChatFormatting.GREEN));
            }
        }
    }

    private static void thisGrantRewards(ServerPlayer player, PlayerCollectionProgress progress) {
        for (CollectibleSetDefinition set : CollectibleCatalog.SETS) {
            if (progress.isSetComplete(set) && !progress.hasClaimedReward(set.id()) && progress.claimReward(set.id())) {
                ItemStack reward = set.createRewardStack();
                if (!player.addItem(reward)) {
                    player.drop(reward, false);
                }
                player.sendSystemMessage(Component.translatable("collection.progress.reward_granted", set.name(), reward.getHoverName())
                        .withStyle(ChatFormatting.AQUA));
            }
        }
    }
}
