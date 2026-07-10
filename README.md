# Collection

`Collection` 是一个面向 Minecraft 1.21.1 / NeoForge 的考古与探索模组。玩法灵感来自《荒野大镖客 2 Online》的收藏家：玩家探索遗迹、用刷子清理可疑沙子或可疑砂砾，并收集按地点、主题与稀有度分布的收藏品。

## 0.3.0：结构遗迹与收藏册升级

当前版本已经把上一轮 README 中的两项核心“下一步”完整落地为可发布内容：

1. 自定义发掘点从 Feature 原型升级为真正的 `NBT / 结构池` 建筑：
   - `collection:buried_coin_ruin`
   - `collection:badlands_arrowhead_site`
   - `collection:shoreline_relic_cache`
2. 收藏家手册从聊天栏输出升级为真正的 GUI 页面：
   - 普通右键打开分页手册界面
   - 潜行右键领取线索地图
   - 每个套组都能查看缺失条目、线索和奖励状态
3. 玩家收藏进度现在会同步到客户端，用于 GUI、地图和后续反馈系统。
4. 新增完整的收藏家成就链：
   - 首次发现收藏品
   - 完成古币套组
   - 完成箭头套组
   - 完成遗珍套组
   - 完成全部收藏
5. 独立美术资源本轮暂时跳过，仍复用原版素材，后续再统一补齐。

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
- 自定义可定位遗迹：
  - `collection:buried_coin_ruin`
  - `collection:badlands_arrowhead_site`
  - `collection:shoreline_relic_cache`

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
  - 右键：打开 GUI 手册，查看总进度与各套组详情
  - 潜行右键：领取一张对应未完成套组的线索地图
- 当玩家第一次获得某件收藏品时，会记录到玩家进度中。
- 当某个套组 3 件都发现后，会自动发放对应奖励；死亡后进度会保留。
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
- `/place structure collection:buried_coin_ruin`
- `/place structure collection:badlands_arrowhead_site`
- `/place structure collection:shoreline_relic_cache`

## 下一步

- 为收藏品、手册与奖励物补充独立美术资源，而不是继续复用原版贴图。
- 继续扩展更多“收藏家风格”的地标与结构线索，例如更大型的废墟、营地、海岸残骸与主题建筑。
- 增加更多收藏线、阶段奖励，以及更完整的收藏家玩法循环（例如 NPC、委托、轮换线索等）。
