package com.collection.collectible;

import com.collection.Collection;
import com.collection.Item.ModItems;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CollectibleCatalog {
    public static final CollectibleDefinition COIN_25CENT_1792 =
            collectible("coin_25cent_1792", ModItems.COIN_25CENT_1792, "coins", "collection.clue.coin_25cent_1792");
    public static final CollectibleDefinition COIN_NICKEL_1792 =
            collectible("coin_nickel_1792", ModItems.COIN_NICKEL_1792, "coins", "collection.clue.coin_nickel_1792");
    public static final CollectibleDefinition COIN_1CENT_1789 =
            collectible("coin_1cent_1789", ModItems.COIN_1CENT_1789, "coins", "collection.clue.coin_1cent_1789");

    public static final CollectibleDefinition FLINT_ARROWHEAD =
            collectible("flint_arrowhead", ModItems.FLINT_ARROWHEAD, "arrowheads", "collection.clue.flint_arrowhead");
    public static final CollectibleDefinition OBSIDIAN_ARROWHEAD =
            collectible("obsidian_arrowhead", ModItems.OBSIDIAN_ARROWHEAD, "arrowheads", "collection.clue.obsidian_arrowhead");
    public static final CollectibleDefinition BONE_ARROWHEAD =
            collectible("bone_arrowhead", ModItems.BONE_ARROWHEAD, "arrowheads", "collection.clue.bone_arrowhead");

    public static final CollectibleDefinition SCARAB_PENDANT =
            collectible("scarab_pendant", ModItems.SCARAB_PENDANT, "relics", "collection.clue.scarab_pendant");
    public static final CollectibleDefinition TURQUOISE_RING =
            collectible("turquoise_ring", ModItems.TURQUOISE_RING, "relics", "collection.clue.turquoise_ring");
    public static final CollectibleDefinition SILVER_LOCKET =
            collectible("silver_locket", ModItems.SILVER_LOCKET, "relics", "collection.clue.silver_locket");

    public static final CollectibleDefinition AMMONITE_FOSSIL =
            collectible("ammonite_fossil", ModItems.AMMONITE_FOSSIL, "fossils", "collection.clue.ammonite_fossil");
    public static final CollectibleDefinition AMBER_FOSSIL =
            collectible("amber_fossil", ModItems.AMBER_FOSSIL, "fossils", "collection.clue.amber_fossil");
    public static final CollectibleDefinition RIB_FRAGMENT =
            collectible("rib_fragment", ModItems.RIB_FRAGMENT, "fossils", "collection.clue.rib_fragment");

    public static final CollectibleDefinition JADE_EFFIGY =
            collectible("jade_effigy", ModItems.JADE_EFFIGY, "effigies", "collection.clue.jade_effigy");
    public static final CollectibleDefinition SUN_IDOL =
            collectible("sun_idol", ModItems.SUN_IDOL, "effigies", "collection.clue.sun_idol");
    public static final CollectibleDefinition OBSIDIAN_TALISMAN =
            collectible("obsidian_talisman", ModItems.OBSIDIAN_TALISMAN, "effigies", "collection.clue.obsidian_talisman");

    public static final List<CollectibleDefinition> COLLECTIBLES = List.of(
            COIN_25CENT_1792,
            COIN_NICKEL_1792,
            COIN_1CENT_1789,
            FLINT_ARROWHEAD,
            OBSIDIAN_ARROWHEAD,
            BONE_ARROWHEAD,
            SCARAB_PENDANT,
            TURQUOISE_RING,
            SILVER_LOCKET,
            AMMONITE_FOSSIL,
            AMBER_FOSSIL,
            RIB_FRAGMENT,
            JADE_EFFIGY,
            SUN_IDOL,
            OBSIDIAN_TALISMAN
    );

    public static final CollectibleSetDefinition COIN_SET =
            set("coins", "collection.set.coins", ModItems.COIN_DISPLAY_CASE, COIN_25CENT_1792, COIN_NICKEL_1792, COIN_1CENT_1789);
    public static final CollectibleSetDefinition ARROWHEAD_SET =
            set("arrowheads", "collection.set.arrowheads", ModItems.ARROWHEAD_DISPLAY_CASE, FLINT_ARROWHEAD, OBSIDIAN_ARROWHEAD, BONE_ARROWHEAD);
    public static final CollectibleSetDefinition RELIC_SET =
            set("relics", "collection.set.relics", ModItems.RELIC_LEDGER, SCARAB_PENDANT, TURQUOISE_RING, SILVER_LOCKET);
    public static final CollectibleSetDefinition FOSSIL_SET =
            set("fossils", "collection.set.fossils", ModItems.FOSSIL_DISPLAY_RACK, AMMONITE_FOSSIL, AMBER_FOSSIL, RIB_FRAGMENT);
    public static final CollectibleSetDefinition EFFIGY_SET =
            set("effigies", "collection.set.effigies", ModItems.SHRINE_RECORD, JADE_EFFIGY, SUN_IDOL, OBSIDIAN_TALISMAN);

    public static final List<CollectibleSetDefinition> SETS = List.of(COIN_SET, ARROWHEAD_SET, RELIC_SET, FOSSIL_SET, EFFIGY_SET);

    private CollectibleCatalog() {
    }

    public static Optional<CollectibleDefinition> fromStack(ItemStack stack) {
        return COLLECTIBLES.stream().filter(definition -> definition.matches(stack)).findFirst();
    }

    public static Optional<CollectibleSetDefinition> findSet(String setId) {
        return SETS.stream().filter(set -> set.id().equals(setId)).findFirst();
    }

    public static int featuredSetIndexForDay(long dayTime) {
        return (int) Math.floorMod(dayTime / 24000L, SETS.size());
    }

    public static CollectibleSetDefinition featuredSetForDay(long dayTime) {
        return SETS.get(featuredSetIndexForDay(dayTime));
    }

    private static CollectibleDefinition collectible(
            String path,
            Supplier<Item> item,
            String setId,
            String clueKey
    ) {
        return new CollectibleDefinition(
                ResourceLocation.fromNamespaceAndPath(Collection.MODID, path),
                item,
                setId,
                clueKey
        );
    }

    private static CollectibleSetDefinition set(
            String id,
            String nameKey,
            Supplier<Item> rewardItem,
            CollectibleDefinition... collectibles
    ) {
        return new CollectibleSetDefinition(id, nameKey, rewardItem, List.of(collectibles));
    }
}
