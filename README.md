# Collection

`Collection` 是一个面向 Minecraft 1.21.1 / NeoForge 的考古与探索模组。玩法灵感来自《荒野大镖客 2 Online》的收藏家：玩家探索遗迹、用刷子清理可疑沙子或可疑砂砾，并收集按地点、主题与稀有度分布的收藏品。

当前版本：`0.5.2`

## 当前核心玩法

- 在原版和模组新增遗迹中寻找可疑方块。
- 使用刷子清理可疑沙子 / 可疑砂砾，概率获得收藏品。
- 通过收藏家手册查看收藏进度、套组详情与线索提示。
- 在村庄中寻找收藏家村民，购买手册和线索地图。
- 将模组收藏品或原版考古物品卖给收藏家，用来获得绿宝石并提升收藏家等级。
- 完成收藏套组后获得对应奖励，并解锁相关进度。

## 0.5.x：收藏家村民与交易循环

`0.5.x` 开始加入收藏家 NPC 玩法，让线索地图从“手册直接领取”改为“与收藏家交易获得”。

### 新增内容

- 新增村民职业：`收藏家`
- 新增职业方块：`收藏家工作台`
- 新增收藏家手册交易
- 新增线索地图交易
- 新增收藏品 / 考古物品回收交易
- 新增统一样式的收藏家小屋
- 收藏家小屋会加入以下原版村庄房屋池：
  - 平原村庄
  - 沙漠村庄
  - 热带草原村庄
  - 雪原村庄
  - 针叶林村庄

### 收藏家交易设计

#### 1 级收藏家

1 级收藏家会提供稳定的早期交易入口：

- 固定出售收藏家手册
- 随机收购一个本模组收藏品
- 随机收购一个原版考古产物

这样玩家不需要反复购买手册来升级收藏家；即使暂时刷不到指定模组收藏品，也可以通过原版考古产物推动升级。

#### 2–5 级收藏家

从 2 级开始，收藏家不再固定出售某一个套组的线索地图，而是使用随机交易池：

- 每次升级新增一个随机线索地图交易
- 每次升级新增一个随机考古物品回收交易
- 回收交易可能要求：
  - 本模组收藏品
  - 原版考古常见产物
  - 高等级原版稀有考古产物

高等级收藏家可能收购更稀有的原版考古物品，例如 `music_disc_relic` 或 `sniffer_egg`。

## 收藏品套组

当前已有 5 条收藏线：

- 古币
  - `1792 Quarter`
  - `1792 Nickel`
  - `1789 Penny`
- 箭头
  - 燧石箭头
  - 黑曜石箭头
  - 骨制箭头
- 遗珍
  - 圣甲虫吊坠
  - 绿松石戒指
  - 银质怀饰
- 化石
  - 菊石化石
  - 琥珀化石
  - 肋骨碎片
- 神像
  - 玉质神像
  - 太阳偶像
  - 黑曜石护符

## 套组奖励

完成对应套组后会获得专属奖励：

- 古币展示盒
- 箭头展示盒
- 遗珍名录
- 化石展示架
- 神龛档案

收藏进度会保存在玩家数据中，死亡后不会丢失。

## 可探索遗迹

### 原版考古兼容地点

模组会通过全局掉落修改器接入原版考古地点。命中收藏品时，会用一件对应地点的收藏品替换原本考古产物；未命中时保持原版奖励不变。

- 沙漠神殿
- 沙漠井
- 温暖海洋废墟
- 寒冷海洋废墟
- 古迹废墟常见层
- 古迹废墟稀有层

### 模组新增结构

当前可通过 `/locate structure` 与 `/place structure` 测试的结构：

- `collection:buried_coin_ruin`
- `collection:badlands_arrowhead_site`
- `collection:shoreline_relic_cache`
- `collection:frozen_fossil_site`
- `collection:jungle_idol_shrine`

这些结构中会包含可疑方块，并接入对应的收藏品掉落池。

## 收藏家手册

收藏家手册用于查看进度，而不再直接发放线索地图。

- 右键：打开手册 GUI
- 潜行右键：提示玩家线索地图需要通过收藏家村民交易获得
- 手册 GUI 可查看：
  - 总收藏进度
  - 各收藏套组进度
  - 套组奖励
  - 收藏品线索说明

## 数据驱动资源

多数玩法参数可以通过数据包资源调整。

- `src/main/resources/data/collection/loot_modifiers/`
  - 控制原版考古地点的收藏品替换概率。
- `src/main/resources/data/collection/loot_table/archaeology/`
  - 控制各地点和各模组遗迹中可疑方块的收藏品权重。
- `src/main/resources/data/collection/worldgen/structure/`
  - 控制自定义遗迹的结构类型。
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
  - 收藏品分类标签。
- `src/main/resources/data/minecraft/worldgen/template_pool/village/*/houses.json`
  - 将收藏家小屋注入原版村庄房屋池。

如果修改了自定义遗迹模板，可以重新执行：

```powershell
.\scripts\generate_structures.ps1
```

如果修改收藏家村庄小屋模板，可以重新执行：

```powershell
.\scripts\generate_village_collector_house.ps1
```

## 开发与验证

需要 Java 21。

```powershell
.\gradlew.bat build
.\gradlew.bat runServer
```

开发环境中可使用以下命令快速验证结构：

```mcfunction
/locate structure collection:buried_coin_ruin
/locate structure collection:badlands_arrowhead_site
/locate structure collection:shoreline_relic_cache
/locate structure collection:frozen_fossil_site
/locate structure collection:jungle_idol_shrine

/place structure collection:buried_coin_ruin
/place structure collection:badlands_arrowhead_site
/place structure collection:shoreline_relic_cache
/place structure collection:frozen_fossil_site
/place structure collection:jungle_idol_shrine
```

测试收藏家交易时，建议新生成一个收藏家村民，或让无职业村民认领收藏家工作台。旧世界中已经生成并升级过的收藏家，其已有交易不会完全重洗；新生成交易会使用当前版本的随机池。

## 版本历史

- `0.5.2`
  - 优化收藏家 1 级交易为手册 + 模组收藏品回收 + 原版考古物回收。
  - 将 2–5 级收藏家交易改为随机线索地图 + 随机考古物回收。
- `0.5.1`
  - 为 1 级收藏家加入模组收藏品回收交易。
- `0.5.0`
  - 新增收藏家村民、收藏家工作台和村庄收藏家小屋。
  - 线索地图改为通过收藏家交易获得。
- `0.4.x`
  - 重做收藏家手册 GUI。
  - 修复结构模板放置与 GUI 显示问题。
- `0.3.0`
  - 将自定义发掘点升级为可定位、可放置的结构系统。
- `0.2.0`
  - 扩展箭头与遗珍收藏线，加入收藏进度与奖励。
- `0.1.0`
  - 首个可发布里程碑，完成古币收藏线和基础考古掉落接入。

## 下一步计划

- 补充独立美术资源，替换当前复用的原版素材。
- 继续扩展收藏家委托 / 订单 / 上交流程。
- 增加更大型的主题建筑群、营地链条和区域化收藏玩法。
- 为不同生物群系村庄制作差异化收藏家小屋。
