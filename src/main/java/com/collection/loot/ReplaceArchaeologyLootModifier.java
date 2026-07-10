package com.collection.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Replaces the generated loot with one result rolled from a configured loot table.
 * Conditions are evaluated by {@link LootModifier}; use a loot-table-id condition and
 * a random-chance condition in data to target archaeology loot safely.
 */
public final class ReplaceArchaeologyLootModifier extends LootModifier {
    public static final MapCodec<ReplaceArchaeologyLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).and(
                    ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").forGetter(ReplaceArchaeologyLootModifier::table)
            ).apply(instance, ReplaceArchaeologyLootModifier::new)
    );

    private final ResourceKey<LootTable> table;

    public ReplaceArchaeologyLootModifier(LootItemCondition[] conditions, ResourceKey<LootTable> table) {
        super(conditions);
        this.table = table;
    }

    public ResourceKey<LootTable> table() {
        return this.table;
    }

    @SuppressWarnings("deprecation") // Deliberately avoid applying global modifiers to the replacement table again.
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ObjectArrayList<ItemStack> replacementLoot = new ObjectArrayList<>();

        context.getResolver().get(Registries.LOOT_TABLE, this.table).ifPresent(replacementTable ->
                replacementTable.value().getRandomItemsRaw(
                        context,
                        LootTable.createStackSplitter(context.getLevel(), stack -> {
                            if (!stack.isEmpty()) {
                                replacementLoot.add(stack);
                            }
                        })
                )
        );

        // A malformed or missing replacement table should not turn a brushed block into an empty find.
        if (!replacementLoot.isEmpty()) {
            generatedLoot.clear();
            generatedLoot.add(replacementLoot.get(context.getRandom().nextInt(replacementLoot.size())));
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.REPLACE_ARCHAEOLOGY_LOOT.get();
    }
}
