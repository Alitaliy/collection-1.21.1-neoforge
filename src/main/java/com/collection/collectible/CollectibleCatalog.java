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

    public static final List<CollectibleDefinition> COLLECTIBLES = List.of(
            COIN_25CENT_1792,
            COIN_NICKEL_1792,
            COIN_1CENT_1789,
            FLINT_ARROWHEAD,
            OBSIDIAN_ARROWHEAD,
            BONE_ARROWHEAD,
            SCARAB_PENDANT,
            TURQUOISE_RING,
            SILVER_LOCKET
    );

    public static final CollectibleSetDefinition COIN_SET =
            set("coins", "collection.set.coins", ModItems.COIN_DISPLAY_CASE, COIN_25CENT_1792, COIN_NICKEL_1792, COIN_1CENT_1789);
    public static final CollectibleSetDefinition ARROWHEAD_SET =
            set("arrowheads", "collection.set.arrowheads", ModItems.ARROWHEAD_DISPLAY_CASE, FLINT_ARROWHEAD, OBSIDIAN_ARROWHEAD, BONE_ARROWHEAD);
    public static final CollectibleSetDefinition RELIC_SET =
            set("relics", "collection.set.relics", ModItems.RELIC_LEDGER, SCARAB_PENDANT, TURQUOISE_RING, SILVER_LOCKET);

    public static final List<CollectibleSetDefinition> SETS = List.of(COIN_SET, ARROWHEAD_SET, RELIC_SET);

    private CollectibleCatalog() {
    }

    public static Optional<CollectibleDefinition> fromStack(ItemStack stack) {
        return COLLECTIBLES.stream().filter(definition -> definition.matches(stack)).findFirst();
    }

    public static Optional<CollectibleSetDefinition> findSet(String setId) {
        return SETS.stream().filter(set -> set.id().equals(setId)).findFirst();
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
