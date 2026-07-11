package com.collection.village;

import com.collection.Collection;
import com.collection.block.ModBlocks;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModVillagers {
    public static final ResourceKey<PoiType> COLLECTOR_POI_KEY =
            ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, id("collector"));
    public static final ResourceKey<VillagerProfession> COLLECTOR_PROFESSION_KEY =
            ResourceKey.create(Registries.VILLAGER_PROFESSION, id("collector"));

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Collection.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, Collection.MODID);

    public static final DeferredHolder<PoiType, PoiType> COLLECTOR_POI =
            POI_TYPES.register("collector", () -> new PoiType(workstationStates(), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> COLLECTOR =
            PROFESSIONS.register("collector", () -> new VillagerProfession(
                    "collector",
                    ModVillagers::isCollectorPoi,
                    ModVillagers::isCollectorPoi,
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_CARTOGRAPHER
            ));

    private ModVillagers() {
    }

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        PROFESSIONS.register(eventBus);
    }

    private static boolean isCollectorPoi(Holder<PoiType> poiType) {
        return poiType.is(COLLECTOR_POI_KEY);
    }

    private static Set<BlockState> workstationStates() {
        return Set.copyOf(ModBlocks.COLLECTOR_WORKSTATION.get().getStateDefinition().getPossibleStates());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Collection.MODID, path);
    }
}
