package com.collection.Item;

import com.collection.Collection;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Collection.MODID);

    public static final Supplier<CreativeModeTab> COLLECTION_TAB =
            CREATIVE_MODE_TAB.register("collector_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.COLLECTOR_JOURNAL.get()))
                    .title(Component.translatable("itemGroup.collection.collector_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.COLLECTOR_JOURNAL);
                        output.accept(ModItems.COLLECTOR_WORKSTATION);
                        output.accept(ModItems.COIN_25CENT_1792);
                        output.accept(ModItems.COIN_NICKEL_1792);
                        output.accept(ModItems.COIN_1CENT_1789);
                        output.accept(ModItems.FLINT_ARROWHEAD);
                        output.accept(ModItems.OBSIDIAN_ARROWHEAD);
                        output.accept(ModItems.BONE_ARROWHEAD);
                        output.accept(ModItems.SCARAB_PENDANT);
                        output.accept(ModItems.TURQUOISE_RING);
                        output.accept(ModItems.SILVER_LOCKET);
                        output.accept(ModItems.AMMONITE_FOSSIL);
                        output.accept(ModItems.AMBER_FOSSIL);
                        output.accept(ModItems.RIB_FRAGMENT);
                        output.accept(ModItems.JADE_EFFIGY);
                        output.accept(ModItems.SUN_IDOL);
                        output.accept(ModItems.OBSIDIAN_TALISMAN);
                        output.accept(ModItems.COIN_DISPLAY_CASE);
                        output.accept(ModItems.ARROWHEAD_DISPLAY_CASE);
                        output.accept(ModItems.RELIC_LEDGER);
                        output.accept(ModItems.FOSSIL_DISPLAY_RACK);
                        output.accept(ModItems.SHRINE_RECORD);
                    }).build());

    private ModCreativeModeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
