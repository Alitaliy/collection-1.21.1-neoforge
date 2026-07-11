package com.collection.village;

import com.collection.Collection;
import com.collection.Item.ModItems;
import com.collection.collectible.CollectibleCatalog;
import com.collection.collectible.CollectibleSetDefinition;
import com.collection.progress.CollectionProgressService;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

@EventBusSubscriber(modid = Collection.MODID)
public final class ModVillagerTrades {
    private static final float PRICE_MULTIPLIER = 0.05F;

    private ModVillagerTrades() {
    }

    @SubscribeEvent
    public static void addCollectorTrades(VillagerTradesEvent event) {
        if (!event.getType().equals(ModVillagers.COLLECTOR.get())) {
            return;
        }

        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        tradesForLevel(trades, 1).add(new ItemForEmeraldsTrade(ModItems.COLLECTOR_JOURNAL.toStack(), 4, 8, 2));
        tradesForLevel(trades, 2).add(new ClueMapTrade(CollectibleCatalog.COIN_SET, 8, 8, 8));
        tradesForLevel(trades, 3).add(new ClueMapTrade(CollectibleCatalog.ARROWHEAD_SET, 10, 8, 12));
        tradesForLevel(trades, 3).add(new ClueMapTrade(CollectibleCatalog.RELIC_SET, 12, 8, 12));
        tradesForLevel(trades, 4).add(new ClueMapTrade(CollectibleCatalog.FOSSIL_SET, 14, 6, 18));
        tradesForLevel(trades, 5).add(new ClueMapTrade(CollectibleCatalog.EFFIGY_SET, 16, 6, 24));
    }

    private static List<VillagerTrades.ItemListing> tradesForLevel(
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades,
            int level
    ) {
        return trades.computeIfAbsent(level, ignored -> new ArrayList<>());
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

    private record ClueMapTrade(CollectibleSetDefinition set, int emeraldCost, int maxUses, int xp)
            implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
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
