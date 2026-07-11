# Places all Collector village house templates in a row for quick visual testing.
# Run in a flat/open area:
# /function collection:debug/place_collector_houses

place template collection:village/collector_house_plains ~ ~ ~
setblock ~ ~ ~4 minecraft:oak_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]
setblock ~4 ~ ~4 minecraft:oak_planks

place template collection:village/collector_house_desert ~12 ~ ~
setblock ~12 ~ ~4 minecraft:smooth_sandstone_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]
setblock ~16 ~ ~4 minecraft:cut_sandstone

place template collection:village/collector_house_savanna ~24 ~ ~
setblock ~24 ~ ~4 minecraft:acacia_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]
setblock ~28 ~ ~4 minecraft:acacia_planks

place template collection:village/collector_house_snowy ~36 ~ ~
setblock ~36 ~ ~4 minecraft:spruce_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]
setblock ~40 ~ ~4 minecraft:spruce_planks

place template collection:village/collector_house_taiga ~48 ~ ~
setblock ~48 ~ ~4 minecraft:spruce_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]
setblock ~52 ~ ~4 minecraft:spruce_planks
