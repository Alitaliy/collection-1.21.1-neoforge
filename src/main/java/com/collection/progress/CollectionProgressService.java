package com.collection.progress;

import com.collection.Collection;
import com.collection.collectible.CollectibleCatalog;
import com.collection.collectible.CollectibleDefinition;
import com.collection.collectible.CollectibleSetDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public final class CollectionProgressService {
    private static final TagKey<Structure> COIN_CLUE_SITES = structureTag("coin_clue_sites");
    private static final TagKey<Structure> ARROWHEAD_CLUE_SITES = structureTag("arrowhead_clue_sites");
    private static final TagKey<Structure> RELIC_CLUE_SITES = structureTag("relic_clue_sites");
    private static final TagKey<Structure> FOSSIL_CLUE_SITES = structureTag("fossil_clue_sites");
    private static final TagKey<Structure> EFFIGY_CLUE_SITES = structureTag("effigy_clue_sites");

    private CollectionProgressService() {
    }

    public static void handleCollectedStack(ServerPlayer player, ItemStack stack, boolean announceDiscoveries) {
        PlayerCollectionProgress progress = player.getData(ModAttachments.PLAYER_COLLECTION_PROGRESS);
        if (processCollectedStack(player, progress, stack, announceDiscoveries)) {
            player.syncData(ModAttachments.PLAYER_COLLECTION_PROGRESS);
        }
    }

    public static void syncInventory(ServerPlayer player, boolean announceDiscoveries) {
        PlayerCollectionProgress progress = player.getData(ModAttachments.PLAYER_COLLECTION_PROGRESS);
        boolean changed = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                changed |= processCollectedStack(player, progress, stack, announceDiscoveries);
            }
        }
        if (changed) {
            player.syncData(ModAttachments.PLAYER_COLLECTION_PROGRESS);
        }
    }

    public static void giveClueMap(ServerPlayer player) {
        syncInventory(player, false);
        PlayerCollectionProgress progress = player.getData(ModAttachments.PLAYER_COLLECTION_PROGRESS);
        CollectibleSetDefinition targetSet = chooseLeadSet(player, progress);

        if (targetSet == null) {
            player.sendSystemMessage(Component.translatable("collection.journal.map_complete").withStyle(ChatFormatting.GREEN));
            return;
        }

        TagKey<Structure> structureTag = structureTagForSet(targetSet.id());
        BlockPos target = player.serverLevel().findNearestMapStructure(structureTag, player.blockPosition(), 128, false);
        if (target == null) {
            player.sendSystemMessage(Component.translatable("collection.journal.map_missing", targetSet.name()).withStyle(ChatFormatting.RED));
            return;
        }

        ItemStack clueMap = MapItem.create(player.serverLevel(), target.getX(), target.getZ(), (byte) 2, true, true);
        MapItem.renderBiomePreviewMap(player.serverLevel(), clueMap);

        MapItemSavedData.addTargetDecoration(clueMap, target, "+", MapDecorationTypes.TARGET_X);
        clueMap.set(DataComponents.CUSTOM_NAME, Component.translatable("collection.journal.map_name", targetSet.name()));

        giveItem(player, clueMap);
        player.sendSystemMessage(Component.translatable("collection.journal.map_given", targetSet.name()).withStyle(ChatFormatting.GOLD));
    }

    private static boolean processCollectedStack(
            ServerPlayer player,
            PlayerCollectionProgress progress,
            ItemStack stack,
            boolean announceDiscoveries
    ) {
        CollectibleDefinition collectible = CollectibleCatalog.fromStack(stack).orElse(null);
        if (collectible == null) {
            return false;
        }

        boolean changed = false;
        if (progress.discover(collectible.id())) {
            changed = true;
            if (announceDiscoveries) {
                player.sendSystemMessage(Component.translatable("collection.progress.discovered", collectible.name())
                        .withStyle(ChatFormatting.GOLD));
            }
            grantAdvancement(player, "first_discovery");
        }

        changed |= CollectibleCatalog.findSet(collectible.setId())
                .map(set -> grantMilestoneRewards(player, progress, set))
                .orElse(false);

        return grantRewards(player, progress) || changed;
    }

    private static boolean grantRewards(ServerPlayer player, PlayerCollectionProgress progress) {
        boolean changed = false;
        for (CollectibleSetDefinition set : CollectibleCatalog.SETS) {
            if (progress.isSetComplete(set) && !progress.hasClaimedReward(set.id()) && progress.claimReward(set.id())) {
                ItemStack reward = set.createRewardStack();
                giveItem(player, reward);
                player.sendSystemMessage(Component.translatable("collection.progress.reward_granted", set.name(), reward.getHoverName())
                        .withStyle(ChatFormatting.AQUA));
                if (isFeaturedSet(player, set)) {
                    ItemStack featuredBonus = new ItemStack(Items.EMERALD, 5);
                    giveItem(player, featuredBonus);
                    player.sendSystemMessage(Component.translatable("collection.progress.featured_bonus", set.name(), featuredBonus.getHoverName())
                            .withStyle(ChatFormatting.GOLD));
                }
                grantAdvancement(player, "complete_" + set.id());
                changed = true;
            }
        }
        if (changed && CollectibleCatalog.SETS.stream().allMatch(progress::isSetComplete)) {
            grantAdvancement(player, "master_collector");
        }
        return changed;
    }

    private static void grantAdvancement(ServerPlayer player, String path) {
        if (player.getServer() == null) {
            return;
        }
        ServerAdvancementManager advancements = player.getServer().getAdvancements();
        var advancement = advancements.get(ResourceLocation.fromNamespaceAndPath(Collection.MODID, path));
        if (advancement != null) {
            player.getAdvancements().award(advancement, "trigger");
        }
    }

    private static TagKey<Structure> structureTagForSet(String setId) {
        return switch (setId) {
            case "coins" -> COIN_CLUE_SITES;
            case "arrowheads" -> ARROWHEAD_CLUE_SITES;
            case "relics" -> RELIC_CLUE_SITES;
            case "fossils" -> FOSSIL_CLUE_SITES;
            case "effigies" -> EFFIGY_CLUE_SITES;
            default -> COIN_CLUE_SITES;
        };
    }

    private static CollectibleSetDefinition chooseLeadSet(ServerPlayer player, PlayerCollectionProgress progress) {
        int start = CollectibleCatalog.featuredSetIndexForDay(player.serverLevel().getDayTime());
        for (int offset = 0; offset < CollectibleCatalog.SETS.size(); offset++) {
            CollectibleSetDefinition candidate = CollectibleCatalog.SETS.get((start + offset) % CollectibleCatalog.SETS.size());
            if (!progress.isSetComplete(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean grantMilestoneRewards(ServerPlayer player, PlayerCollectionProgress progress, CollectibleSetDefinition set) {
        boolean changed = false;
        int discovered = progress.discoveredCount(set);

        if (discovered >= 1 && !progress.hasClaimedMilestone(set.id(), 1) && progress.claimMilestone(set.id(), 1)) {
            giveItem(player, new ItemStack(Items.BRUSH));
            giveItem(player, new ItemStack(Items.PAPER, 2));
            player.sendSystemMessage(Component.translatable("collection.progress.milestone_one", set.name()).withStyle(ChatFormatting.YELLOW));
            changed = true;
        }

        if (discovered >= 2 && !progress.hasClaimedMilestone(set.id(), 2) && progress.claimMilestone(set.id(), 2)) {
            giveItem(player, new ItemStack(Items.EMERALD, 3));
            player.sendSystemMessage(Component.translatable("collection.progress.milestone_two", set.name()).withStyle(ChatFormatting.GREEN));
            changed = true;
        }

        return changed;
    }

    private static boolean isFeaturedSet(ServerPlayer player, CollectibleSetDefinition set) {
        return CollectibleCatalog.featuredSetForDay(player.serverLevel().getDayTime()).id().equals(set.id());
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }

    private static TagKey<Structure> structureTag(String path) {
        return TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(Collection.MODID, path));
    }
}
