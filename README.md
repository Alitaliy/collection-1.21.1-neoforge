# Collection

`Collection` 是一个面向 Minecraft 1.21.1 / NeoForge 的考古与探索模组。玩法灵感来自《荒野大镖客 2 Online》的收藏家：玩家探索遗迹、用刷子清理可疑沙子或可疑砂砾，并收集按地点、主题与稀有度分布的收藏品。

## 0.4.0：收藏线扩展与轮换线索

当前版本继续沿着 0.3.0 之后的“下一步”推进，并把剩余路线中的内容型部分先做成一轮新里程碑：

1. 新增两条收藏线：
   - `化石`：菊石化石、琥珀化石、肋骨化石
   - `神像`：玉质神像、太阳偶像、黑曜石护符
2. 新增两类可定位遗迹：
   - `collection:frozen_fossil_site`
   - `collection:jungle_idol_shrine`
3. 收藏家手册现在拥有“今日线索”轮换：
   - 每天轮换一个重点套组
   - 潜行右键会优先给出当前轮值套组的线索地图
4. 新增阶段奖励：
   - 每个套组在 `1/3` 进度时获得野外补给
   - 每个套组在 `2/3` 进度时获得收藏家报酬
5. 当玩家在“今日线索”轮值期间完成对应套组时，会额外获得一笔绿宝石奖励。
6. 独立美术资源本轮仍然跳过，继续复用原版素材，后续再统一补齐。

## 当前内容

- 收藏品类别：
  - 古币：1792 Quarter、1792 Nickel、1789 Penny
  - 箭头：燧石箭头、黑曜石箭头、骨制箭头
  - 遗珍：圣甲虫吊坠、绿松石戒指、银质怀饰
  - 化石：菊石化石、琥珀化石、肋骨化石
  - 神像：玉质神像、太阳偶像、黑曜石护符
- 套组奖励：
  - 古币展示盒
  - 箭头展示盒
  - 遗珍名录
  - 化石展示架
  - 神龛档案
- 原版考古兼容地点：
  - 沙漠神殿
  - 沙漠井
  - 温暖海洋废墟
  - 寒冷海洋废墟
  - 古迹废墟常见层
  - 古迹废墟稀有层
- 自定义可定位遗迹：
  - `collection:buried_coin_ruin`
  - `collection:badlands_arrowhead_site`
  - `collection:shoreline_relic_cache`
  - `collection:frozen_fossil_site`
  - `collection:jungle_idol_shrine`

原版考古结果仍然通过全局掉落修改器做“单物品替换”：命中收藏品时，用一件对应地点的收藏品替换原有考古产物；未命中时保持原版奖励不变。这样每个可疑方块始终只会产出一件战利品。

## 数据驱动调整

不需要改 Java 代码：

- `src/main/resources/data/collection/loot_modifiers/`
  - 控制原版考古地点的收藏品替换概率。
- `src/main/resources/data/collection/loot_table/archaeology/`
  - 控制各地点和各自定义遗迹中可疑方块的收藏品权重。
- `src/main/resources/data/collection/worldgen/structure/`
  - 控制自定义遗迹的结构类型与生成步骤。
- `src/main/resources/data/collection/worldgen/structure_set/`
  - 控制遗迹的分布频率、间距和 salt。
- `src/main/resources/data/collection/worldgen/template_pool/`
  - 控制每类遗迹会从哪些结构模板中抽取布局。
- `src/main/resources/data/collection/structure/`
  - 存放实际的 `.nbt` 遗迹模板。
- `src/main/resources/data/collection/tags/worldgen/biome/has_structure/`
  - 控制各类遗迹出现在哪些生物群系。
- `src/main/resources/data/collection/tags/worldgen/structure/`
  - 控制线索地图会追踪哪些结构目标。
- `src/main/resources/data/collection/tags/item/`
  - 划分收藏类别标签，方便后续扩展更多系统。

如果你修改了自定义遗迹模板，可以重新执行：

```powershell
& .\scripts\generate_structures.ps1
```

这些均为数据包资源，整合包和服务器可用同路径数据包直接覆盖。

## 玩家使用

- `收藏家手册`
  - 右键：打开 GUI 手册，查看总进度、各套组详情与今日线索
  - 潜行右键：领取当前轮值或最近未完成套组的线索地图
- 当玩家第一次获得某件收藏品时，会记录到玩家进度中。
- 每个套组在 `1/3` 与 `2/3` 进度时都会分别发放一次阶段奖励。
- 当某个套组 3 件都发现后，会自动发放对应奖励；死亡后进度会保留。
- 如果该套组恰好是“今日线索”，完成时还会额外获得一笔绿宝石奖励。
- 完成收藏流程会逐步解锁专属成就。

## 开发与验证

需要 Java 21。

```powershell
.\gradlew.bat build
.\gradlew.bat runServer
```

在开发环境中可以使用以下命令快速验证：

- `/locate structure collection:buried_coin_ruin`
- `/locate structure collection:badlands_arrowhead_site`
- `/locate structure collection:shoreline_relic_cache`
- `/locate structure collection:frozen_fossil_site`
- `/locate structure collection:jungle_idol_shrine`
- `/place structure collection:buried_coin_ruin`
- `/place structure collection:badlands_arrowhead_site`
- `/place structure collection:shoreline_relic_cache`
- `/place structure collection:frozen_fossil_site`
- `/place structure collection:jungle_idol_shrine`

## 下一步

- 为收藏品、手册与奖励物补充独立美术资源，而不是继续复用原版贴图。
- 增加真正的收藏家委托 / 交付循环，例如 NPC、订单、套组上交与重复报酬。
- 扩展更大型的主题建筑群、营地链条和更深的地图追踪逻辑，让不同收藏线拥有更鲜明的区域玩法。
