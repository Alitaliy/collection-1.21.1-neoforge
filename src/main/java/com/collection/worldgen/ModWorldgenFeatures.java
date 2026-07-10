package com.collection.worldgen;

import com.collection.Collection;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Registers world-generation types without requiring the main mod class to own another
 * deferred register.  The configured and placed feature data lives under data/collection.
 */
@EventBusSubscriber(modid = Collection.MODID)
public final class ModWorldgenFeatures {
    private ModWorldgenFeatures() {
    }

    @SubscribeEvent
    public static void registerFeatures(RegisterEvent event) {
        event.register(
                Registries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Collection.MODID, "buried_coin_ruin"),
                () -> new CollectorDigSiteFeature(NoneFeatureConfiguration.CODEC, CollectorDigSiteFeature.SiteTheme.DESERT_COIN_RUIN)
        );
        event.register(
                Registries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Collection.MODID, "badlands_arrowhead_site"),
                () -> new CollectorDigSiteFeature(NoneFeatureConfiguration.CODEC, CollectorDigSiteFeature.SiteTheme.BADLANDS_ARROWHEAD_SITE)
        );
        event.register(
                Registries.FEATURE,
                ResourceLocation.fromNamespaceAndPath(Collection.MODID, "shoreline_relic_cache"),
                () -> new CollectorDigSiteFeature(NoneFeatureConfiguration.CODEC, CollectorDigSiteFeature.SiteTheme.SHORELINE_RELIC_CACHE)
        );
    }
}
