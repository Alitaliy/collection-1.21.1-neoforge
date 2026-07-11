# Changelog

## 0.5.1

Release date: 2026-07-11

### Trading

- Added a novice Collector recycling trade.
- Level 1 Collectors now keep selling the Collector's Journal and can also buy one random collectible item for emeralds.
- The recycling trade grants villager XP, giving players an early upgrade path without repeatedly buying extra journals.

## 0.5.0

Release date: 2026-07-11

### Villagers

- Added the Collector villager profession.
- Added the Collector's Workstation job-site block.
- Added Collector villager trades:
  - Level 1: Collector's Journal
  - Level 2: Ancient Coin clue maps
  - Level 3: Arrowhead and Lost Relic clue maps
  - Level 4: Fossil clue maps
  - Level 5: Shrine Effigy clue maps

### Villages

- Added a unified Collector house template for vanilla villages.
- Added Collector houses to plains, desert, savanna, snowy, and taiga village house pools.
- Collector houses include the Collector's Workstation so villagers can claim the new profession.

### Gameplay

- Sneak-using the Collector's Journal no longer grants a free clue map.
- Clue maps are now obtained through Collector villager trades.

## 0.4.4

Release date: 2026-07-11

### UI

- Rebuilt the Collector Journal as a two-page handbook inspired by classic mod guide books.
- Moved set navigation into the right page chapter list with click-to-open entries.
- Moved set descriptions, reward preview, and progress bars onto the left page.
- Kept collectible clues inside hover tooltips so long localized text no longer spills outside the page.
- Added book frame, page divider, stitched spine, corner details, and page arrows drawn directly in the GUI.

## 0.4.3

Release date: 2026-07-11

### UI

- Fully disabled the default Minecraft screen background blur for the Collector Journal.
- Fixed a detail-page layout overlap where the final collectible card could run into the page footer.
- Added clipping around the detail card list so long translated clue text cannot draw outside the journal page.

## 0.4.2

Release date: 2026-07-11

### UI

- Reworked the Collector Journal into a handbook-style layout with a left navigation tab list and a dedicated right content page.
- Removed the dimmed full-screen background effect when opening the journal.
- Restyled the navigation controls to look like journal tabs instead of default Minecraft buttons.
- Constrained clue text and set details so long localized lines no longer spill outside the page frame.

## 0.4.1

Release date: 2026-07-11

### Fixes

- Fixed custom structure template NBT encoding so located structures now place correctly in the world.
- Regenerated all shipped structure templates for the five custom archaeology sites.
- Fixed Collector Journal ARGB text and panel colors so the GUI renders visibly in-game.

## 0.4.0

发布日期：2026-07-10

### 新增内容

- 新增两条收藏线：
  - `化石`
  - `神像`
- 新增两类可定位遗迹：
  - `collection:frozen_fossil_site`
  - `collection:jungle_idol_shrine`
- 新增两套完成奖励：
  - `化石展示架`
  - `神龛档案`
- 新增“今日线索”轮换逻辑，手册会按天切换当前重点收藏套组。
- 新增阶段奖励：
  - 套组进度 `1/3`
  - 套组进度 `2/3`

### 玩法与系统调整

- 潜行使用手册时，会优先发放当前轮值套组的线索地图。
- 在“今日线索”期间完成对应套组时，会额外获得一笔绿宝石奖励。
- 原版考古掉落分布进一步扩展，新增化石与神像在原版地点中的混合产出。
- 收藏家 GUI 总览页改为适配更多套组的紧凑布局。

### 工程与发布整理

- 更新结构模板生成脚本，支持 `frozen_fossil_site` 与 `jungle_idol_shrine` 两类新遗迹。
- 新增 `fossils` 与 `effigies` 物品标签。
- 新增两条套组完成成就。
- 版本号从 `0.3.0` 升级为 `0.4.0`。

## 0.3.0

发布日期：2026-07-10

### 新增内容

- 将三类自定义发掘点升级为真正的 `NBT / 结构池` 遗迹：
  - `collection:buried_coin_ruin`
  - `collection:badlands_arrowhead_site`
  - `collection:shoreline_relic_cache`
- 新增收藏家手册 GUI：
  - 分页总览页
  - 套组详情页
  - 缺失条目与线索显示
- 新增线索地图功能，潜行使用手册可直接领取对应未完成套组的地图。
- 新增客户端进度同步，用于 GUI 和地图线索展示。
- 新增收藏家成就链，包括首次发现、各套组完成与总收藏完成。

### 玩法与系统调整

- 自定义遗迹不再通过 biome modifier + placed feature 生成，而是改为可定位、可放置、可复用的原版结构系统。
- 手册的普通右键行为由聊天栏输出改为打开 GUI；潜行右键行为改为领取线索地图。
- 文档与验证命令同步切换为 `/locate structure` 与 `/place structure`。
- 独立美术资源继续沿用原版素材，本次版本专注功能闭环。

### 工程与发布整理

- 新增 `scripts/generate_structures.ps1`，用于重新生成自定义遗迹的 `.nbt` 模板。
- 版本号从 `0.2.0` 升级为 `0.3.0`。
- 更新 README、Release Notes 与版本历史说明。

## 0.2.0

发布日期：2026-07-10

### 新增内容

- 新增 `收藏家手册`，右键可查看套组进度，潜行右键可查看详细线索。
- 新增两条收藏线：`箭头` 与 `遗珍`，与古币共同组成三大收藏类别。
- 新增三套完成奖励：`古币展示盒`、`箭头展示盒`、`遗珍名录`。
- 新增玩家持久化收藏进度，记录首次获得的收藏品与已领取的套组奖励。
- 新增两类自定义发掘点：`badlands_arrowhead_site` 与 `shoreline_relic_cache`。

### 玩法与内容扩展

- 将原有 `buried_coin_ruin` 升级为多模板发掘点，不再只有单一遗迹布局。
- 扩展原版考古兼容地点的掉落分布：
  - 沙漠神殿、沙漠井更偏向古币与沙漠遗珍。
  - 温暖/寒冷海洋废墟更偏向遗珍。
  - 古迹废墟常见层/稀有层更偏向箭头与少量混合稀有收藏品。
- 新增 `collectibles`、`coins`、`arrowheads`、`relics` 物品标签，为后续系统扩展提供统一分类。

### 工程与发布整理

- 更新 README，将原先的“后续方向”收敛为已完成的 `0.2.0` 里程碑说明。
- 新增中英文文本、物品模型与发布说明文档。
- 版本号从 `0.1.0` 升级为 `0.2.0`。

## 0.1.0

### 首个可发布里程碑

- 完成首批三枚古币收藏品：
  - `1792 Quarter`
  - `1792 Nickel`
  - `1789 Penny`
- 完成独立创造模式页签与中英文本地化。
- 为 6 类原版考古地点接入定向替换掉落：
  - 沙漠神殿
  - 沙漠井
  - 温暖海洋废墟
  - 寒冷海洋废墟
  - 古迹废墟常见层
  - 古迹废墟稀有层
- 新增首个自定义发掘点 `buried_coin_ruin`，在沙漠中自然生成可刷取古币的可疑方块。
- 实现专用全局掉落修改器，确保考古结果仍保持“每个可疑方块只产出一件物品”的规则。

### 说明

- `0.1.0` 对应的是当前项目演进中的首个正式可发布节点。
- 当前仓库提交的是 `0.2.0` 状态源码，因此 `0.1.0` 在 GitHub Release 中将作为历史版本说明与二进制资产保留。
