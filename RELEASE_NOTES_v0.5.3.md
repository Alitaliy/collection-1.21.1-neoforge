# Collection 0.5.3

`0.5.3` adds biome-specific Collector village houses and a dedicated Collector house loot chest.

## Villages

- Added dedicated Collector house templates for plains, desert, savanna, snowy, and taiga villages.
- Each house uses vanilla blocks and a matching village-biome style.
- Collector houses include the Collector's Workstation, a bed, shelves, decoration, and a loot chest.
- Collector houses no longer include other vanilla job-site blocks, preventing villagers from taking the wrong profession.
- Window blocks now use full glass instead of single glass panes.
- Removed indoor hanging lanterns from the Collector house templates.
- Fixed wall torch placement so the torches attach to interior walls.
- Replaced the first entrance plank with a biome-appropriate stair final state.
- Fixed the entrance stair orientation so the step faces into the house.
- Fixed bed orientation and restored the bed-side exterior wall block to each house's own wall material.
- Reduced Collector house village pool weight from 2 to 1 to make duplicate Collector houses less frequent in one village.
- Added `/function collection:debug/place_collector_houses` for quick visual testing of all five house variants.

## Loot

- Added the `collection:chests/collector_house` loot table.
- Collector house chests always contain one brush.
- Random chest loot is weighted toward vanilla archaeology finds, with a smaller chance for mod collectibles.

## Asset

- `collection-0.5.3.jar`
