package com.collection.village;

import com.collection.Collection;
import com.collection.Item.ModItems;
import com.collection.collectible.CollectibleCatalog;
import com.collection.collectible.CollectibleDefinition;
import com.collection.collectible.CollectibleSetDefinition;
import com.collection.progress.CollectionProgressService;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

@EventBusSubscriber(modid = Collection.MODID)
public final class ModVillagerTrades {
    private static final float PRICE_MULTIPLIER = 0.05F;
    private static final List<ItemRecyclingOption> VANILLA_ARCHAEOLOGY_RECYCLING_OPTIONS = List.of(
            new ItemRecyclingOption(Items.BRICK, 4, 1, 12, 4),
            new ItemRecyclingOption(Items.STICK, 8, 1, 12, 4),
            new ItemRecyclingOption(Items.COAL, 4, 1, 12, 4),
            new ItemRecyclingOption(Items.WHEAT, 6, 1, 12, 4),
            new ItemRecyclingOption(Items.GOLD_NUGGET, 4, 1, 12, 4),
            new ItemRecyclingOption(Items.GUNPOWDER, 4, 1, 12, 4),
            new ItemRecyclingOption(Items.ANGLER_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.ARCHER_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.ARMS_UP_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.BLADE_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.BREWER_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.BURN_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.DANGER_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.EXPLORER_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.FLOW_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.FRIEND_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.GUSTER_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.HEART_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.HEARTBREAK_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.HOWL_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.MINER_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.MOURNER_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.PLENTY_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.PRIZE_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.SCRAPE_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.SHEAF_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.SHELTER_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.SKULL_POTTERY_SHERD, 1, 2, 8, 5),
            new ItemRecyclingOption(Items.SNORT_POTTERY_SHERD, 1, 2, 8, 5)
    );
    private static final List<ItemRecyclingOption> RARE_VANILLA_ARCHAEOLOGY_RECYCLING_OPTIONS = List.of(
            new ItemRecyclingOption(Items.DIAMOND, 1, 6, 6, 10),
            new ItemRecyclingOption(Items.MUSIC_DISC_RELIC, 1, 10, 3, 18),
            new ItemRecyclingOption(Items.SNIFFER_EGG, 1, 12, 3, 20),
            new ItemRecyclingOption(Items.FLOW_POTTERY_SHERD, 1, 3, 8, 7),
            new ItemRecyclingOption(Items.GUSTER_POTTERY_SHERD, 1, 3, 8, 7),
            new ItemRecyclingOption(Items.SCRAPE_POTTERY_SHERD, 1, 3, 8, 7)
    );
    private static final List<ClueMapOption> APPRENTICE_CLUE_MAP_OPTIONS = List.of(
            new ClueMapOption(CollectibleCatalog.COIN_SET, 8, 8, 8),
            new ClueMapOption(CollectibleCatalog.ARROWHEAD_SET, 10, 8, 8),
            new ClueMapOption(CollectibleCatalog.RELIC_SET, 11, 8, 10)
    );
    private static final List<ClueMapOption> JOURNEYMAN_CLUE_MAP_OPTIONS = List.of(
            new ClueMapOption(CollectibleCatalog.COIN_SET, 8, 8, 8),
            new ClueMapOption(CollectibleCatalog.ARROWHEAD_SET, 10, 8, 10),
            new ClueMapOption(CollectibleCatalog.RELIC_SET, 12, 8, 12),
            new ClueMapOption(CollectibleCatalog.FOSSIL_SET, 14, 6, 14)
    );
    private static final List<ClueMapOption> EXPERT_CLUE_MAP_OPTIONS = List.of(
            new ClueMapOption(CollectibleCatalog.ARROWHEAD_SET, 9, 8, 12),
            new ClueMapOption(CollectibleCatalog.RELIC_SET, 11, 8, 14),
            new ClueMapOption(CollectibleCatalog.FOSSIL_SET, 14, 6, 18),
            new ClueMapOption(CollectibleCatalog.EFFIGY_SET, 16, 6, 20)
    );
    private static final List<ClueMapOption> MASTER_CLUE_MAP_OPTIONS = List.of(
            new ClueMapOption(CollectibleCatalog.COIN_SET, 7, 10, 10),
            new ClueMapOption(CollectibleCatalog.ARROWHEAD_SET, 9, 10, 12),
            new ClueMapOption(CollectibleCatalog.RELIC_SET, 11, 8, 14),
            new ClueMapOption(CollectibleCatalog.FOSSIL_SET, 13, 8, 18),
            new ClueMapOption(CollectibleCatalog.EFFIGY_SET, 15, 8, 24)
    );

    private ModVillagerTrades() {
    }

    @SubscribeEvent
    public static void addCollectorTrades(VillagerTradesEvent event) {
        if (!event.getType().equals(ModVillagers.COLLECTOR.get())) {
            return;
        }

        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        tradesForLevel(trades, 1).add(new ItemForEmeraldsTrade(ModItems.COLLECTOR_JOURNAL.toStack(), 4, 8, 2));
        tradesForLevel(trades, 1).add(new RandomCollectibleRecyclingTrade(1, 3, 12, 5));
        tradesForLevel(trades, 2).add(new RandomClueMapTrade(APPRENTICE_CLUE_MAP_OPTIONS));
        tradesForLevel(trades, 2).add(new RandomArchaeologyRecyclingTrade(1, 3, 12, 5, VANILLA_ARCHAEOLOGY_RECYCLING_OPTIONS));
        tradesForLevel(trades, 3).add(new RandomClueMapTrade(JOURNEYMAN_CLUE_MAP_OPTIONS));
        tradesForLevel(trades, 3).add(new RandomArchaeologyRecyclingTrade(1, 4, 10, 8, VANILLA_ARCHAEOLOGY_RECYCLING_OPTIONS));
        tradesForLevel(trades, 4).add(new RandomClueMapTrade(EXPERT_CLUE_MAP_OPTIONS));
        tradesForLevel(trades, 4).add(new RandomArchaeologyRecyclingTrade(1, 5, 8, 12, RARE_VANILLA_ARCHAEOLOGY_RECYCLING_OPTIONS));
        tradesForLevel(trades, 5).add(new RandomClueMapTrade(MASTER_CLUE_MAP_OPTIONS));
        tradesForLevel(trades, 5).add(new RandomArchaeologyRecyclingTrade(1, 6, 8, 16, RARE_VANILLA_ARCHAEOLOGY_RECYCLING_OPTIONS));
    }

    @SubscribeEvent
    public static void ensureNoviceCollectorTrades(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()
                || event.getHand() != InteractionHand.MAIN_HAND
                || event.getEntity().isSecondaryUseActive()
                || !(event.getTarget() instanceof Villager villager)
                || !villager.getVillagerData().getProfession().equals(ModVillagers.COLLECTOR.get())
                || villager.getVillagerData().getLevel() != 1) {
            return;
        }

        ensureNoviceCollectorOffers(villager);
    }

    private static List<VillagerTrades.ItemListing> tradesForLevel(
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades,
            int level
    ) {
        return trades.computeIfAbsent(level, ignored -> new ArrayList<>());
    }

    private static void ensureNoviceCollectorOffers(Villager villager) {
        MerchantOffers offers = villager.getOffers();
        RandomSource random = villager.getRandom();

        if (!hasJournalTrade(offers)) {
            offers.add(new ItemForEmeraldsTrade(ModItems.COLLECTOR_JOURNAL.toStack(), 4, 8, 2).getOffer(villager, random));
        }

        if (!hasCollectibleRecyclingTrade(offers)) {
            offers.add(new RandomCollectibleRecyclingTrade(1, 3, 12, 5).getOffer(villager, random));
        }

        if (!hasVanillaArchaeologyRecyclingTrade(offers)) {
            offers.add(new RandomVanillaArchaeologyRecyclingTrade().getOffer(villager, random));
        }
    }

    private static boolean hasJournalTrade(MerchantOffers offers) {
        for (MerchantOffer offer : offers) {
            if (offer.getResult().is(ModItems.COLLECTOR_JOURNAL.get())) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasCollectibleRecyclingTrade(MerchantOffers offers) {
        for (MerchantOffer offer : offers) {
            if (offer.getResult().is(Items.EMERALD) && CollectibleCatalog.fromStack(offer.getBaseCostA()).isPresent()) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasVanillaArchaeologyRecyclingTrade(MerchantOffers offers) {
        for (MerchantOffer offer : offers) {
            if (!offer.getResult().is(Items.EMERALD)) {
                continue;
            }

            for (ItemRecyclingOption option : VANILLA_ARCHAEOLOGY_RECYCLING_OPTIONS) {
                if (offer.getBaseCostA().is(option.item().asItem())) {
                    return true;
                }
            }
        }

        return false;
    }

    private record ItemForEmeraldsTrade(ItemStack result, int emeraldCost, int maxUses, int xp)
            implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(
                    new ItemCost(Items.EMERALD, this.emeraldCost),
                    this.result.copy(),
                    this.maxUses,
                    this.xp,
                    PRICE_MULTIPLIER
            );
        }
    }

    private record RandomCollectibleRecyclingTrade(int collectibleCost, int emeraldReward, int maxUses, int xp)
            implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            if (CollectibleCatalog.COLLECTIBLES.isEmpty()) {
                return null;
            }

            CollectibleDefinition collectible = CollectibleCatalog.COLLECTIBLES.get(
                    random.nextInt(CollectibleCatalog.COLLECTIBLES.size())
            );

            return new MerchantOffer(
                    new ItemCost(collectible.item().get(), this.collectibleCost),
                    new ItemStack(Items.EMERALD, this.emeraldReward),
                    this.maxUses,
                    this.xp,
                    PRICE_MULTIPLIER
            );
        }
    }

    private record RandomVanillaArchaeologyRecyclingTrade()
            implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            if (VANILLA_ARCHAEOLOGY_RECYCLING_OPTIONS.isEmpty()) {
                return null;
            }

            return VANILLA_ARCHAEOLOGY_RECYCLING_OPTIONS.get(
                    random.nextInt(VANILLA_ARCHAEOLOGY_RECYCLING_OPTIONS.size())
            ).createOffer();
        }
    }

    private record RandomArchaeologyRecyclingTrade(
            int collectibleCost,
            int collectibleEmeraldReward,
            int maxUses,
            int xp,
            List<ItemRecyclingOption> vanillaOptions
    ) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            boolean useCollectible = !CollectibleCatalog.COLLECTIBLES.isEmpty()
                    && (this.vanillaOptions.isEmpty() || random.nextBoolean());
            if (useCollectible) {
                return new RandomCollectibleRecyclingTrade(
                        this.collectibleCost,
                        this.collectibleEmeraldReward,
                        this.maxUses,
                        this.xp
                ).getOffer(trader, random);
            }

            if (this.vanillaOptions.isEmpty()) {
                return null;
            }

            return this.vanillaOptions.get(random.nextInt(this.vanillaOptions.size())).createOffer();
        }
    }

    private record ItemRecyclingOption(ItemLike item, int itemCost, int emeraldReward, int maxUses, int xp) {
        private MerchantOffer createOffer() {
            return new MerchantOffer(
                    new ItemCost(this.item, this.itemCost),
                    new ItemStack(Items.EMERALD, this.emeraldReward),
                    this.maxUses,
                    this.xp,
                    PRICE_MULTIPLIER
            );
        }
    }

    private record RandomClueMapTrade(List<ClueMapOption> options)
            implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            if (this.options.isEmpty()) {
                return null;
            }

            return this.options.get(random.nextInt(this.options.size())).createOffer(trader);
        }
    }

    private record ClueMapOption(CollectibleSetDefinition set, int emeraldCost, int maxUses, int xp) {
        private MerchantOffer createOffer(Entity trader) {
            if (!(trader.level() instanceof ServerLevel level)) {
                return null;
            }

            ItemStack map = CollectionProgressService.createClueMap(level, trader.blockPosition(), this.set);
            if (map.isEmpty()) {
                return null;
            }

            return new MerchantOffer(
                    new ItemCost(Items.EMERALD, this.emeraldCost),
                    map,
                    this.maxUses,
                    this.xp,
                    PRICE_MULTIPLIER
            );
        }
    }
}
