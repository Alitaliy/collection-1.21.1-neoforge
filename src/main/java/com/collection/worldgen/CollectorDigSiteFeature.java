package com.collection.worldgen;

import com.collection.Collection;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Procedural archaeology sites used by the collector gameplay loop.
 * Each theme owns several hand-authored block templates and common/rare brush loot.
 */
public final class CollectorDigSiteFeature extends Feature<NoneFeatureConfiguration> {
    private final SiteTheme theme;

    public CollectorDigSiteFeature(Codec<NoneFeatureConfiguration> codec, SiteTheme theme) {
        super(codec);
        this.theme = theme;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos base = context.origin().below();
        if (!this.isSuitableSite(level, base)) {
            return false;
        }

        SiteTemplate template = this.theme.templates().get(context.random().nextInt(this.theme.templates().size()));
        this.placeTemplate(level, base, template, context.random());
        return true;
    }

    private boolean isSuitableSite(WorldGenLevel level, BlockPos base) {
        for (int x = -this.theme.radius(); x <= this.theme.radius(); x++) {
            for (int z = -this.theme.radius(); z <= this.theme.radius(); z++) {
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, base.getX() + x, base.getZ() + z) - 1;
                BlockPos surface = new BlockPos(base.getX() + x, surfaceY, base.getZ() + z);
                BlockState state = level.getBlockState(surface);
                if (surfaceY != base.getY() || !this.theme.allowedSurface().contains(state.getBlock()) || !state.getFluidState().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void placeTemplate(WorldGenLevel level, BlockPos base, SiteTemplate template, RandomSource random) {
        for (TemplateBlock block : template.blocks()) {
            level.setBlock(base.offset(block.x(), block.y(), block.z()), block.state(), 2);
        }

        for (BrushPlacement brush : template.brushPlacements()) {
            if (brush.chanceDenominator() > 1 && random.nextInt(brush.chanceDenominator()) != 0) {
                continue;
            }
            this.placeBrushableBlock(
                    level,
                    base.offset(brush.x(), brush.y(), brush.z()),
                    brush.state(),
                    brush.lootTier() == LootTier.COMMON ? this.theme.commonLoot() : this.theme.rareLoot(),
                    random
            );
        }
    }

    private void placeBrushableBlock(
            WorldGenLevel level,
            BlockPos position,
            BlockState state,
            ResourceKey<LootTable> lootTable,
            RandomSource random
    ) {
        level.setBlock(position, state, 2);
        if (level.getBlockEntity(position) instanceof BrushableBlockEntity brushableBlockEntity) {
            brushableBlockEntity.setLootTable(lootTable, random.nextLong());
            brushableBlockEntity.setChanged();
        }
    }

    public enum SiteTheme {
        DESERT_COIN_RUIN(
                Set.of(Blocks.SAND, Blocks.RED_SAND, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.GRAVEL),
                2,
                loot("archaeology/buried_coin_ruin_common"),
                loot("archaeology/buried_coin_ruin_rare"),
                List.of(
                        new SiteTemplate(
                                List.of(
                                        block(-2, 0, -2, Blocks.CUT_SANDSTONE),
                                        block(-2, 0, -1, Blocks.SANDSTONE),
                                        block(-2, 0, 0, Blocks.CHISELED_SANDSTONE),
                                        block(-2, 0, 1, Blocks.SANDSTONE),
                                        block(-2, 0, 2, Blocks.CUT_SANDSTONE),
                                        block(-1, 0, -2, Blocks.SANDSTONE),
                                        block(-1, 0, 2, Blocks.SANDSTONE),
                                        block(0, 0, -2, Blocks.CHISELED_SANDSTONE),
                                        block(0, 0, 0, Blocks.CUT_SANDSTONE),
                                        block(0, 0, 2, Blocks.CHISELED_SANDSTONE),
                                        block(1, 0, -2, Blocks.SANDSTONE),
                                        block(1, 0, 2, Blocks.SANDSTONE),
                                        block(2, 0, -2, Blocks.CUT_SANDSTONE),
                                        block(2, 0, -1, Blocks.SANDSTONE),
                                        block(2, 0, 0, Blocks.CHISELED_SANDSTONE),
                                        block(2, 0, 1, Blocks.SANDSTONE),
                                        block(2, 0, 2, Blocks.CUT_SANDSTONE),
                                        block(0, 1, 0, Blocks.SAND)
                                ),
                                List.of(
                                        brush(-1, 1, 0, Blocks.SUSPICIOUS_SAND, LootTier.COMMON, 1),
                                        brush(1, 1, 0, Blocks.SUSPICIOUS_SAND, LootTier.COMMON, 2),
                                        brush(0, 1, 1, Blocks.SUSPICIOUS_GRAVEL, LootTier.RARE, 4)
                                )
                        ),
                        new SiteTemplate(
                                List.of(
                                        block(-2, 0, -1, Blocks.CUT_SANDSTONE),
                                        block(-2, 0, 0, Blocks.SANDSTONE),
                                        block(-2, 0, 1, Blocks.CUT_SANDSTONE),
                                        block(-1, 0, -2, Blocks.SANDSTONE),
                                        block(-1, 0, 2, Blocks.SANDSTONE),
                                        block(0, 0, -2, Blocks.CUT_SANDSTONE),
                                        block(0, 0, -1, Blocks.SAND),
                                        block(0, 0, 0, Blocks.CHISELED_SANDSTONE),
                                        block(0, 0, 1, Blocks.SAND),
                                        block(0, 0, 2, Blocks.CUT_SANDSTONE),
                                        block(1, 0, -2, Blocks.SANDSTONE),
                                        block(1, 0, 2, Blocks.SANDSTONE),
                                        block(2, 0, -1, Blocks.CUT_SANDSTONE),
                                        block(2, 0, 0, Blocks.SANDSTONE),
                                        block(2, 0, 1, Blocks.CUT_SANDSTONE),
                                        block(-1, 1, -1, Blocks.SANDSTONE),
                                        block(1, 1, 1, Blocks.SANDSTONE)
                                ),
                                List.of(
                                        brush(0, 1, -1, Blocks.SUSPICIOUS_SAND, LootTier.COMMON, 1),
                                        brush(0, 1, 1, Blocks.SUSPICIOUS_SAND, LootTier.COMMON, 2),
                                        brush(1, 1, 0, Blocks.SUSPICIOUS_GRAVEL, LootTier.RARE, 3)
                                )
                        ),
                        new SiteTemplate(
                                List.of(
                                        block(-2, 0, 0, Blocks.CHISELED_SANDSTONE),
                                        block(-1, 0, -1, Blocks.SANDSTONE),
                                        block(-1, 0, 0, Blocks.CUT_SANDSTONE),
                                        block(-1, 0, 1, Blocks.SANDSTONE),
                                        block(0, 0, -2, Blocks.CHISELED_SANDSTONE),
                                        block(0, 0, -1, Blocks.CUT_SANDSTONE),
                                        block(0, 0, 0, Blocks.SAND),
                                        block(0, 0, 1, Blocks.CUT_SANDSTONE),
                                        block(0, 0, 2, Blocks.CHISELED_SANDSTONE),
                                        block(1, 0, -1, Blocks.SANDSTONE),
                                        block(1, 0, 0, Blocks.CUT_SANDSTONE),
                                        block(1, 0, 1, Blocks.SANDSTONE),
                                        block(2, 0, 0, Blocks.CHISELED_SANDSTONE),
                                        block(-2, 1, 0, Blocks.SANDSTONE),
                                        block(2, 1, 0, Blocks.SANDSTONE)
                                ),
                                List.of(
                                        brush(-1, 1, 0, Blocks.SUSPICIOUS_SAND, LootTier.COMMON, 1),
                                        brush(1, 1, 0, Blocks.SUSPICIOUS_SAND, LootTier.COMMON, 2),
                                        brush(0, 1, -1, Blocks.SUSPICIOUS_GRAVEL, LootTier.RARE, 4)
                                )
                        )
                )
        ),
        BADLANDS_ARROWHEAD_SITE(
                Set.of(Blocks.RED_SAND, Blocks.RED_SANDSTONE, Blocks.TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GRAVEL),
                2,
                loot("archaeology/badlands_arrowhead_site_common"),
                loot("archaeology/badlands_arrowhead_site_rare"),
                List.of(
                        new SiteTemplate(
                                List.of(
                                        block(-2, 0, -2, Blocks.RED_SANDSTONE),
                                        block(-2, 0, -1, Blocks.TERRACOTTA),
                                        block(-2, 0, 0, Blocks.CUT_RED_SANDSTONE),
                                        block(-2, 0, 1, Blocks.TERRACOTTA),
                                        block(-2, 0, 2, Blocks.RED_SANDSTONE),
                                        block(-1, 0, -2, Blocks.TERRACOTTA),
                                        block(-1, 0, 2, Blocks.TERRACOTTA),
                                        block(0, 0, -2, Blocks.CUT_RED_SANDSTONE),
                                        block(0, 0, 0, Blocks.RED_SAND),
                                        block(0, 0, 2, Blocks.CUT_RED_SANDSTONE),
                                        block(1, 0, -2, Blocks.TERRACOTTA),
                                        block(1, 0, 2, Blocks.TERRACOTTA),
                                        block(2, 0, -2, Blocks.RED_SANDSTONE),
                                        block(2, 0, -1, Blocks.TERRACOTTA),
                                        block(2, 0, 0, Blocks.CUT_RED_SANDSTONE),
                                        block(2, 0, 1, Blocks.TERRACOTTA),
                                        block(2, 0, 2, Blocks.RED_SANDSTONE)
                                ),
                                List.of(
                                        brush(-1, 1, 0, Blocks.SUSPICIOUS_GRAVEL, LootTier.COMMON, 1),
                                        brush(1, 1, 0, Blocks.SUSPICIOUS_GRAVEL, LootTier.COMMON, 2),
                                        brush(0, 1, 1, Blocks.SUSPICIOUS_SAND, LootTier.RARE, 4)
                                )
                        ),
                        new SiteTemplate(
                                List.of(
                                        block(-2, 0, 0, Blocks.RED_SANDSTONE),
                                        block(-1, 0, -1, Blocks.ORANGE_TERRACOTTA),
                                        block(-1, 0, 0, Blocks.CUT_RED_SANDSTONE),
                                        block(-1, 0, 1, Blocks.ORANGE_TERRACOTTA),
                                        block(0, 0, -2, Blocks.RED_SANDSTONE),
                                        block(0, 0, -1, Blocks.RED_SAND),
                                        block(0, 0, 0, Blocks.GRAVEL),
                                        block(0, 0, 1, Blocks.RED_SAND),
                                        block(0, 0, 2, Blocks.RED_SANDSTONE),
                                        block(1, 0, -1, Blocks.BROWN_TERRACOTTA),
                                        block(1, 0, 0, Blocks.CUT_RED_SANDSTONE),
                                        block(1, 0, 1, Blocks.BROWN_TERRACOTTA),
                                        block(2, 0, 0, Blocks.RED_SANDSTONE),
                                        block(0, 1, -2, Blocks.CUT_RED_SANDSTONE),
                                        block(0, 1, 2, Blocks.CUT_RED_SANDSTONE)
                                ),
                                List.of(
                                        brush(0, 1, -1, Blocks.SUSPICIOUS_GRAVEL, LootTier.COMMON, 1),
                                        brush(0, 1, 1, Blocks.SUSPICIOUS_GRAVEL, LootTier.COMMON, 2),
                                        brush(1, 1, 0, Blocks.SUSPICIOUS_SAND, LootTier.RARE, 3)
                                )
                        ),
                        new SiteTemplate(
                                List.of(
                                        block(-2, 0, -1, Blocks.RED_SANDSTONE),
                                        block(-2, 0, 1, Blocks.RED_SANDSTONE),
                                        block(-1, 0, -2, Blocks.ORANGE_TERRACOTTA),
                                        block(-1, 0, -1, Blocks.RED_SAND),
                                        block(-1, 0, 0, Blocks.CUT_RED_SANDSTONE),
                                        block(-1, 0, 1, Blocks.RED_SAND),
                                        block(-1, 0, 2, Blocks.BROWN_TERRACOTTA),
                                        block(0, 0, -1, Blocks.RED_SAND),
                                        block(0, 0, 0, Blocks.GRAVEL),
                                        block(0, 0, 1, Blocks.RED_SAND),
                                        block(1, 0, -2, Blocks.BROWN_TERRACOTTA),
                                        block(1, 0, -1, Blocks.RED_SAND),
                                        block(1, 0, 0, Blocks.CUT_RED_SANDSTONE),
                                        block(1, 0, 1, Blocks.RED_SAND),
                                        block(1, 0, 2, Blocks.ORANGE_TERRACOTTA),
                                        block(2, 0, -1, Blocks.RED_SANDSTONE),
                                        block(2, 0, 1, Blocks.RED_SANDSTONE)
                                ),
                                List.of(
                                        brush(-1, 1, 0, Blocks.SUSPICIOUS_GRAVEL, LootTier.COMMON, 1),
                                        brush(1, 1, 0, Blocks.SUSPICIOUS_GRAVEL, LootTier.COMMON, 2),
                                        brush(0, 1, 0, Blocks.SUSPICIOUS_SAND, LootTier.RARE, 4)
                                )
                        )
                )
        ),
        SHORELINE_RELIC_CACHE(
                Set.of(Blocks.SAND, Blocks.GRAVEL, Blocks.SANDSTONE, Blocks.STONE, Blocks.COBBLESTONE),
                2,
                loot("archaeology/shoreline_relic_cache_common"),
                loot("archaeology/shoreline_relic_cache_rare"),
                List.of(
                        new SiteTemplate(
                                List.of(
                                        block(-2, 0, -2, Blocks.COBBLESTONE),
                                        block(-2, 0, 2, Blocks.COBBLESTONE),
                                        block(-1, 0, -1, Blocks.SANDSTONE),
                                        block(-1, 0, 0, Blocks.GRAVEL),
                                        block(-1, 0, 1, Blocks.SANDSTONE),
                                        block(0, 0, -1, Blocks.GRAVEL),
                                        block(0, 0, 0, Blocks.SAND),
                                        block(0, 0, 1, Blocks.GRAVEL),
                                        block(1, 0, -1, Blocks.SANDSTONE),
                                        block(1, 0, 0, Blocks.GRAVEL),
                                        block(1, 0, 1, Blocks.SANDSTONE),
                                        block(2, 0, -2, Blocks.COBBLESTONE),
                                        block(2, 0, 2, Blocks.COBBLESTONE),
                                        block(0, 1, -2, Blocks.MOSSY_COBBLESTONE)
                                ),
                                List.of(
                                        brush(-1, 1, 0, Blocks.SUSPICIOUS_SAND, LootTier.COMMON, 1),
                                        brush(1, 1, 0, Blocks.SUSPICIOUS_GRAVEL, LootTier.COMMON, 2),
                                        brush(0, 1, 1, Blocks.SUSPICIOUS_SAND, LootTier.RARE, 4)
                                )
                        ),
                        new SiteTemplate(
                                List.of(
                                        block(-2, 0, 0, Blocks.COBBLESTONE),
                                        block(-1, 0, -1, Blocks.SANDSTONE),
                                        block(-1, 0, 1, Blocks.SANDSTONE),
                                        block(0, 0, -2, Blocks.MOSSY_COBBLESTONE),
                                        block(0, 0, -1, Blocks.GRAVEL),
                                        block(0, 0, 0, Blocks.SAND),
                                        block(0, 0, 1, Blocks.GRAVEL),
                                        block(0, 0, 2, Blocks.MOSSY_COBBLESTONE),
                                        block(1, 0, -1, Blocks.SANDSTONE),
                                        block(1, 0, 1, Blocks.SANDSTONE),
                                        block(2, 0, 0, Blocks.COBBLESTONE),
                                        block(-2, 1, 0, Blocks.OAK_PLANKS),
                                        block(2, 1, 0, Blocks.OAK_PLANKS)
                                ),
                                List.of(
                                        brush(0, 1, -1, Blocks.SUSPICIOUS_SAND, LootTier.COMMON, 1),
                                        brush(0, 1, 1, Blocks.SUSPICIOUS_GRAVEL, LootTier.COMMON, 2),
                                        brush(1, 1, 0, Blocks.SUSPICIOUS_SAND, LootTier.RARE, 3)
                                )
                        ),
                        new SiteTemplate(
                                List.of(
                                        block(-2, 0, -1, Blocks.SANDSTONE),
                                        block(-2, 0, 1, Blocks.SANDSTONE),
                                        block(-1, 0, -2, Blocks.COBBLESTONE),
                                        block(-1, 0, -1, Blocks.GRAVEL),
                                        block(-1, 0, 1, Blocks.GRAVEL),
                                        block(-1, 0, 2, Blocks.COBBLESTONE),
                                        block(0, 0, -2, Blocks.MOSSY_COBBLESTONE),
                                        block(0, 0, -1, Blocks.GRAVEL),
                                        block(0, 0, 0, Blocks.SAND),
                                        block(0, 0, 1, Blocks.GRAVEL),
                                        block(0, 0, 2, Blocks.MOSSY_COBBLESTONE),
                                        block(1, 0, -2, Blocks.COBBLESTONE),
                                        block(1, 0, -1, Blocks.GRAVEL),
                                        block(1, 0, 1, Blocks.GRAVEL),
                                        block(1, 0, 2, Blocks.COBBLESTONE),
                                        block(2, 0, -1, Blocks.SANDSTONE),
                                        block(2, 0, 1, Blocks.SANDSTONE)
                                ),
                                List.of(
                                        brush(-1, 1, 0, Blocks.SUSPICIOUS_SAND, LootTier.COMMON, 1),
                                        brush(1, 1, 0, Blocks.SUSPICIOUS_GRAVEL, LootTier.COMMON, 2),
                                        brush(0, 1, 0, Blocks.SUSPICIOUS_SAND, LootTier.RARE, 4)
                                )
                        )
                )
        );

        private final Set<Block> allowedSurface;
        private final int radius;
        private final ResourceKey<LootTable> commonLoot;
        private final ResourceKey<LootTable> rareLoot;
        private final List<SiteTemplate> templates;

        SiteTheme(
                Set<Block> allowedSurface,
                int radius,
                ResourceKey<LootTable> commonLoot,
                ResourceKey<LootTable> rareLoot,
                List<SiteTemplate> templates
        ) {
            this.allowedSurface = allowedSurface;
            this.radius = radius;
            this.commonLoot = commonLoot;
            this.rareLoot = rareLoot;
            this.templates = templates;
        }

        public Set<Block> allowedSurface() {
            return allowedSurface;
        }

        public int radius() {
            return radius;
        }

        public ResourceKey<LootTable> commonLoot() {
            return commonLoot;
        }

        public ResourceKey<LootTable> rareLoot() {
            return rareLoot;
        }

        public List<SiteTemplate> templates() {
            return templates;
        }
    }

    private enum LootTier {
        COMMON,
        RARE
    }

    private record SiteTemplate(List<TemplateBlock> blocks, List<BrushPlacement> brushPlacements) {
    }

    private record TemplateBlock(int x, int y, int z, BlockState state) {
    }

    private record BrushPlacement(int x, int y, int z, BlockState state, LootTier lootTier, int chanceDenominator) {
    }

    private static TemplateBlock block(int x, int y, int z, Block block) {
        return new TemplateBlock(x, y, z, block.defaultBlockState());
    }

    private static BrushPlacement brush(int x, int y, int z, Block block, LootTier lootTier, int chanceDenominator) {
        return new BrushPlacement(x, y, z, block.defaultBlockState(), lootTier, chanceDenominator);
    }

    private static ResourceKey<LootTable> loot(String path) {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(Collection.MODID, path)
        );
    }
}
