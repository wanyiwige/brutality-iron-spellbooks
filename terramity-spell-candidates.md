# Terramity 武器、饰品与 Boss 技能法术化初审

依据当前安装的 Terramity 0.9.8 Forge 1.20.1 JAR 反编译检查；已读武器调用、主动饰品总入口及多组 Boss 技能流程。仅设计与静态审查，未新增法术、学派或部署。

## 学派规则

优先使用虚无 `brutality_iron:nihility`、天界 `brutality_iron:celestia`、炼狱 `brutality_iron:inferno`、流风 `wind_spellbooks:wind`、岩土 `gtbcs_geomancy_plus:geo`。这五个学派不适合承接的候选，使用铁魔法本体学派；不使用其他附属学派，不额外创建学派。只调整这批新增候选，已完成的法术保持原归属。理由见 `candidate-school-assignments.md`。

| 类型 | 原来源 | 拟定法术 | 建议学派 | 代码依据与实现边界 |
|---|---|---|---|---|
| 武器/法器 | 末影换位法杖 | 末影换位 | 虚无 | 弹射物命中后互换双方位置，并调用延时回换逻辑；可设计两阶段换位。 |
| 武器/法器 | 飞升法典 | 穿顶升行 | 虚无 | 扫描上方方块，找到穿过顶层后的可站立位置并传送；不是普通飞行。 |
| 武器/法器 | 反转速度法典 | 惯性逆转 | 流风 | 延时反转并放大自身速度，可用于急停折返；设计时需设速度上限。 |
| 武器/法器 | 烈风弹反法典 | 烈风退跃 | 流风 | 实际右键逻辑给自身反方向速度，属于风力位移；不能仅凭中文名认定有弹反。 |
| 武器/法器 | 守卫者魔典 | 守护者之手 | 岩土 | 召唤 TG_HAND；手部实体旋转并沿射线造成能量伤害，原版也可能伤害使用者。 |
| 饰品 | 水晶之心 | 水晶疗愈阵 | 天界 | 生成水晶之心法阵，实体 tick 为周围生物施加再生 II；移植需过滤敌方。 |
| 饰品 | 盖亚的恩典 | 盖亚催生阵 | 自然 | 生成催生法阵，tick 检查可骨粉催生植物并促生。 |
| 饰品 | 忍者替身 | 替身遁形 | 虚无 | 生成诱饵并给予自身 60 tick 隐身；诱饵与仇恨转移要另接入法术主人。 |
| 饰品 | 混沌之心戒指 | 追猎印记 | 虚无 | 原饰品记录最后命中目标，可传送至其位置；法术化使用先标记后追击。 |
| 饰品 | 燃烧的灵魂指环 | 燃魂加速 | 炼狱 | 以自身着火为代价获得急迫增益；法术化可保留代价，避免直接扩展为未证实的攻击加速。 |
| 饰品 | 传送笛 | 归途旋风 | 流风 | 工具提示描述返回出生点并留下可供他人使用的旋风；此行只核对物品文本，过程类需进一步追踪，不列首批直接实现。 |
| Boss | 德天使 Virtue | 三重圣裁 | 天界 | 记录目标位置并分次生成三个 HOLY_BEAM；适合预警后落下的定点圣光柱。 |
| Boss | 女巫·喀耳刻 Circe | 冥河游魂 | 炼狱 | 施法生成 stygian_soul；移植时需重写阵营与归属，不能直接照搬 summon 命令。 |
| Boss | 女巫·喀耳刻 Circe | 魔仆召集 | 唤魔 | 召唤 Conjurling / Thunker；玩家版改成受控召唤物。 |
| Boss | 魔法师·梅林 Merlin | 冰块投射 | 冰霜 | 随机施法池含 IceCube 弹射物；与火球、闪电、换位分开，按元素各归本体学派。 |
| Boss | 试炼守护者 | 震慑咆哮 | 岩土 | 范围施加 TREMBLING；原技能本身不应仅凭名称解释为伤害吐息。 |
| Boss | 试炼守护者 | 石像卫队 | 岩土 | 召唤地牢石像鬼，需改为玩家受控、有限寿命与数量。 |
| Boss | 超级嗅探兽 | 聚能灵弹 | 唤魔 | 长时间蓄能，在上方生成 SPIRIT_BOMB；重做玩家施放过程与伤害数值。 |
| Boss | 超级嗅探兽 | 震地威压 | 岩土 | 震地流程含广域 TREMBLING；当前效果函数主要是震慑与声效，新增地裂伤害属于设计扩展。 |
| Boss | 黄昏洛克 Duskrok | 岩躯震击 | 岩土 | 砸地后对附近目标结算范围伤害，与现有岩土法术区分效果。 |
| Boss | 地狱洛克 Hellrok | 炼狱重击 | 炼狱 | 近距离砸地伤害；移植需保留其攻击演出并另审炽焰相关联动。 |
| Boss | 大法师·甘道夫 Gundalf | 疾风乱斩 | 流风 | 连续突进并在周围随机位置调用斩击伤害；重写面向玩家的瞄准与位移。 |
| Boss | 大法师·甘道夫 Gundalf | 三重邪光 | 邪术 | 记录目标位置并分次生成 UNHOLY_BEAM；与德天使光柱二选一或做明确不同的区域压制。 |

## 代码路径

- 末影换位：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/EnderswapProjectileProjectileHitsLivingEntityProcedure.java`
- 穿顶升行：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/TomeOfAscensionRightclickedProcedure.java`
- 惯性逆转：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/VelocityFlipRightclickedProcedure.java`
- 烈风退跃：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/GalebounceTomeRightclickedProcedure.java`
- 守护者之手：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/GuardianGrimoireRightclickedProcedure.java`
- 水晶疗愈阵：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/CrystalHeartProcProcedure.java`
- 盖亚催生阵：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/GaiasGraceProcProcedure.java`
- 替身遁形：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/ShinobisSubstituteProcProcedure.java`
- 追猎印记：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/ChaosHeartRingTeleportProcedure.java`
- 燃魂加速：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/BurningSpiritRingProcProcedure.java`
- 归途旋风：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/WarpWhistleRightclickedProcedure.java`（该命名文件未找到，仅文本或相邻入口证据，需进一步追踪）
- 三重圣裁：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/VirtueCastProcedure.java`
- 冥河游魂：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/CirceCastProcedure.java`
- 魔仆召集：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/CirceSummonProcedure.java`
- 冰块投射：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/MerlinCastProcedure.java`
- 震慑咆哮：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/TrialGuardianAttackBreathProcedure.java`
- 石像卫队：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/TrialGuardianAttackSummonProcedure.java`
- 聚能灵弹：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/SuperSnifferSpiritBombProcedure.java`
- 震地威压：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/SuperSnifferStompEffectProcedure.java`
- 岩躯震击：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/DuskrokSlamAttackProcedure.java`
- 炼狱重击：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/HellrokSlamAttackProcedure.java`
- 疾风乱斩：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/GundalfGaleProcedure.java`
- 三重邪光：`audit/terramity-weapon-review/source/net/mcreator/terramity/procedures/GundalfLasersProcedure.java`

相关实体 tick：`CrystalHeartRingOnEntityTickUpdateProcedure`、`GaiasGraceRingOnEntityTickUpdateProcedure`、`TGHandOnEntityTickUpdateProcedure`。主动饰品总入口：`ActiveAbilityOnKeyPressedProcedure`。

## 去重与暂缓

- 用户明确已完成：终焉棱镜持续光束、超新星护体小行星。排除，Terra 的相似纯光束也不优先。
- 大天使光环的 Divine Flight 与现有天界“天飞”近似；不新增另一份飞行法术。
- 普通火球、电击等与铁魔法本体重复；不为每种武器建立同功能新法术。
- 骚动法典当前右键流程主要是轮播不同声音，不应当作高优先级战斗法术。
- 消亡权杖用于腐化/开采基岩，属于世界方块操作，暂不作为首批战斗法术。
- 计时码表标记 Creative Only / Experimental，不因其能改变昼夜就另建时间学派。
- 空间、隐身、闪避与已有虚无法术相似项，需要再逐项比较，避免只换名字。
- Boss 参数与脚本不能直接套给玩家；重设等级、法力、冷却、伤害、控制时长、友军过滤和召唤上限。
- 本次没有观看视频；用户已纠正为“饰品”，证据为实际 JAR 代码与资源。
