package com.collection.Item;

import com.collection.Collection;
import com.collection.block.ModBlocks;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Collection.MODID);

    public static final DeferredItem<Item> COLLECTOR_JOURNAL =
            ITEMS.register("journal/collector_journal", () -> new CollectorJournalItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
            ));

    public static final DeferredItem<?> COLLECTOR_WORKSTATION =
            ITEMS.registerSimpleBlockItem(ModBlocks.COLLECTOR_WORKSTATION);

    public static final DeferredItem<Item> COIN_25CENT_1792 = collectible("coin/coin_25cent_1792");
    public static final DeferredItem<Item> COIN_NICKEL_1792 = collectible("coin/coin_nickel_1792");
    public static final DeferredItem<Item> COIN_1CENT_1789 = collectible("coin/coin_1cent_1789");

    public static final DeferredItem<Item> FLINT_ARROWHEAD = collectible("arrowhead/flint_arrowhead");
    public static final DeferredItem<Item> OBSIDIAN_ARROWHEAD = collectible("arrowhead/obsidian_arrowhead");
    public static final DeferredItem<Item> BONE_ARROWHEAD = collectible("arrowhead/bone_arrowhead");

    public static final DeferredItem<Item> SCARAB_PENDANT = collectible("relic/scarab_pendant");
    public static final DeferredItem<Item> TURQUOISE_RING = collectible("relic/turquoise_ring");
    public static final DeferredItem<Item> SILVER_LOCKET = collectible("relic/silver_locket");

    public static final DeferredItem<Item> AMMONITE_FOSSIL = collectible("fossil/ammonite_fossil");
    public static final DeferredItem<Item> AMBER_FOSSIL = collectible("fossil/amber_fossil");
    public static final DeferredItem<Item> RIB_FRAGMENT = collectible("fossil/rib_fragment");

    public static final DeferredItem<Item> JADE_EFFIGY = collectible("effigy/jade_effigy");
    public static final DeferredItem<Item> SUN_IDOL = collectible("effigy/sun_idol");
    public static final DeferredItem<Item> OBSIDIAN_TALISMAN = collectible("effigy/obsidian_talisman");

    public static final DeferredItem<Item> COIN_DISPLAY_CASE = reward("reward/coin_display_case");
    public static final DeferredItem<Item> ARROWHEAD_DISPLAY_CASE = reward("reward/arrowhead_display_case");
    public static final DeferredItem<Item> RELIC_LEDGER = reward("reward/relic_ledger");
    public static final DeferredItem<Item> FOSSIL_DISPLAY_RACK = reward("reward/fossil_display_rack");
    public static final DeferredItem<Item> SHRINE_RECORD = reward("reward/shrine_record");

    private ModItems() {
    }

    private static DeferredItem<Item> collectible(String path) {
        return ITEMS.register(path, () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    }

    private static DeferredItem<Item> reward(String path) {
        return ITEMS.register(path, () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
