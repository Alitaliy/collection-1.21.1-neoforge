# Collection

`Collection` 是一个面向 Minecraft 1.21.1 / NeoForge 的考古与探索模组。玩法灵感来自《荒野大镖客 2 Online》的收藏家：玩家探索遗迹、用刷子清理可疑沙子或可疑砂砾，并收集按地点、主题与稀有度分布的收藏品。

## 0.2.0：收藏家原型

当前版本已经把 README 里最初列出的 4 个“后续方向”收敛为一轮可发布的原型：

1. 更多收藏类别、套组与奖励：除了古币，新增了箭头与遗珍两条收藏线；每条套组都能在集齐后自动发放对应奖励物品。
2. 多模板发掘点：原本的单一埋藏钱币遗迹升级为 3 种沙漠模板；同时新增恶地箭头发掘点与海岸遗珍缓存，两者也各自拥有多种模板。
3. 更多原版/新增地点兼容：原版 6 类考古地点的可疑方块现在会按地点产出更匹配的收藏品；新增 3 类自定义发掘点并通过生物群系修饰器自然生成。
4. 收藏册、线索与玩家进度：新增“收藏家手册”，右键查看套组进度，潜行右键查看详细线索；玩家的已发现藏品与已领取奖励会持久保存。

## 当前内容

- 收藏品类别：
  - 古币：1792 Quarter、1792 Nickel、1789 Penny
  - 箭头：燧石箭头、黑曜石箭头、骨制箭头
  - 遗珍：圣甲虫吊坠、绿松石戒指、银质怀饰
- 套组奖励：
  - 古币展示盒
  - 箭头展示盒
  - 遗珍名录
- 原版考古兼容地点：
  - 沙漠神殿
  - 沙漠井
  - 温暖海洋废墟
  - 寒冷海洋废墟
  - 古迹废墟常见层
  - 古迹废墟稀有层
- 自定义发掘点：
  - `collection:buried_coin_ruin`
  - `collection:badlands_arrowhead_site`
  - `collection:shoreline_relic_cache`

原版考古结果仍然通过全局掉落修改器做“单物品替换”：命中收藏品时，用一件对应地点的收藏品替换原有考古产物；未命中时保持原版奖励不变。这样每个可疑方块始终只会产出一件战利品。

## 数据驱动调整

不需要改 Java 代码：

- `src/main/resources/data/collection/loot_modifiers/` 控制原版考古地点替换概率。
- `src/main/resources/data/collection/loot_table/archaeology/` 控制各地点和各发掘点的收藏品权重。
- `src/main/resources/data/collection/neoforge/biome_modifier/` 控制自定义发掘点出现在哪些生物群系。
- `src/main/resources/data/collection/worldgen/placed_feature/` 控制它们的生成频率。
- `src/main/resources/data/collection/tags/item/` 划分收藏类别标签，方便后续扩展更多系统。

这些均为数据包资源，整合包和服务器可用同路径数据包直接覆盖。

## 玩家使用

- `收藏家手册`：右键查看套组进度，潜行右键查看详细线索。
- 当玩家第一次获得某件收藏品时，会记录到玩家进度中。
- 当某个套组 3 件都发现后，会自动发放对应奖励；死亡后进度会保留。

## 开发与验证

需要 Java 21。

```powershell
.\gradlew.bat runClient
.\gradlew.bat build
```

在开发环境中可以使用以下命令快速验证：

- `/locate structure minecraft:trail_ruins`
- `/place feature collection:buried_coin_ruin`
- `/place feature collection:badlands_arrowhead_site`
- `/place feature collection:shoreline_relic_cache`

## 下一步

- 为更多收藏线补充独立美术资源，而不是暂时复用原版物品贴图。
- 将多模板发掘点继续演进为真正的 NBT / 结构池建筑。
- 为收藏册补充 GUI 页面、地图线索与更强的成就反馈。
