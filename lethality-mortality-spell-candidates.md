# Lethality / Mortality 法术候选初审

范围为实际安装 JAR 的静态代码与资源审查，未新增注册、绘制素材、构建或部署。用户已允许为独特机制创建新学派；原有五学派仍优先，其他附属学派不接入。

## 当前文件

- Lethality：`lethality_0.4.1_1.20.1.jar`，mods.toml 内版本为 `0.3-1.20.1`。
- Mortality：`mortality-0.0.45.6.11-forge-1.20.1.jar`，mods.toml 内版本为 `0.0.45.7`。
- 两个文件名和内置版本不同，后续以 audit 中保存的实际路径与 SHA-256 确定来源。

## 新学派建议：疫病

拟议 ID：`brutality_iron:plague`（尚未注册）。身份是感染、传播、酸蚀与破甲；与本体自然的植物/常规毒、炼狱的火焰诅咒区分。首批可由腐蚀刃波、疫病注入、疫雾爆发、腐甲连爪构成。这里只是给新法术分组，不宣称原模组已经拥有该学派。对现有 JAR 的中英文 school 翻译键检索未找到同名疫病学派，但这不等于已完成所有注册路径审计。

## 候选

| 来源 | 原物品/实体 | 拟定法术 | 学派 | 核对到的机制 | 改造和证据边界 |
|---|---|---|---|---|---|
| Lethality | Blighted Cleaver / Defiled Greatsword | 腐蚀刃波 | 疫病（新建建议） | 单道或三道穿透斩击，叠加 Acid Venom 和 Hellfire。 | 合并成同一法术随等级增加弹道；双重毒火状态需保留或明确改造。 |
| Lethality | Bladecrest / Forbidden / Exalted Oathblade | 追魂魔镰 | 炼狱 | 射出追踪/穿透镰刃，附加燃烧与影焰。 | 系列武器合并成长曲线，不每把剑做一份同质法术。 |
| Lethality | Devil’s Devastation | 恶魔兵装 | 炼狱 | 一次放出三道镰刃和两支叉矛。 | 保留镰刃与叉矛两种形态，与普通影焰斩区分。 |
| Lethality | Gael’s Greatsword | 灰烬追魂 | 炼狱 | 发射三枚寻敌骷髅，接触目标后触发爆裂表现。 | 需要重写武器攻击力依赖、目标过滤与伤害归属。 |
| Lethality | Vehemence | 地狱爆弹 | 炼狱 | 发射地狱能量弹，命中触发爆裂。 | 与灰烬追魂区分弹道及范围；最终可二选一避免重复。 |
| Lethality | Grievance | 硫火献祭 | 炼狱 | 以持续自伤换取攻击强化与吸血。 | 原自伤效果每 tick 执行，不能原数值照搬；强化和吸血依赖事件，需同时迁移。 |
| Lethality | Starlight | 星芒连刺 | 天界 | 连续生成快速的星光刺击实体。 | 穿甲为文本描述，完整伤害类型链仍需检查；不得仅凭文案宣称已实现穿甲法术。 |
| Lethality | Nightfall | 铁意回生 | 天界 | 积攒护盾后开启吸收护盾，再施放将剩余护盾的一部分转为治疗。 | 实际依赖 ShieldCharge 累积，不是描述所说的每次固定最大生命 30%；法术可设计独立护盾资源。 |
| Lethality | Foreign Hook | 拘魂钩索 | 虚无 | 投掷钩索命中并拉回敌人。 | 以束缚牵引承接虚无，不增加未证实的穿墙效果。 |
| Lethality | Pale Branch | 静止庇护 | 虚无 | 静止 60 tick 后不断刷新高等级抗性并抵抗击退，移动时解除。 | 代码是抗性 V 与击退处理，不能直接描述为对所有伤害绝对免疫；建议有限时或引导。 |
| Lethality | Warden Spine | 震动感知 | 岩土 | 检测附近移动生物并给予发光标记。 | 原范围 50 格；法术化降低初级范围并缩短持续时间。 |
| Lethality | HF Meowrasama | 刃域加速 | 流风 | 进入强化状态，提高自身机动与攻击节奏，同时干扰附近敌人。 | 需进一步逐项核对攻击速度与减速事件；列第二批，不当成已经完整审完的直接移植项。 |
| Mortality | 瘟疫武器 / Plague Core | 疫病注入 | 疫病（新建建议） | 对目标施加可累积发作并向附近生物传播的 PLAGUE。 | 原疫病存在随机增益分支；设计时明确是否保留，不得误称永远只有减益。 |
| Mortality | Plague Reaper | 疫雾爆发 | 疫病（新建建议） | 击杀触发生成携带疫病的范围效果云。 | 原函数主要生成效果云及爆裂粒子，没有直接爆炸伤害；玩家法术化改为主动释放。 |
| Mortality | Venoshopod | 腐甲连爪 | 疫病（新建建议） | 连爪造成伤害并附加疫病与 Armor Crunch。 | 两套连爪流程合并为等级成长；原破甲过程只针对 ServerPlayer，必须补全普通怪物适用性。 |
| Mortality | Champion Of Elbram / Spear Of The Champion | 裂核重击 | 岩土 | 攻击附加 Shattered Core，降低护甲与最大生命。 | 原减属性过程只支持 ServerPlayer；玩家法术需要针对 LivingEntity 改写并设 Boss 上限。 |
| Mortality | Infernal Core | 火灵集群 | 炼狱 | 生成多只 Fire Spirit，由其自身流程持续活动。 | 移植需改阵营、主人和寿命，不能把原敌对实体原样当友方召唤。 |
| Mortality | Dawnmaker | 晨曦三叠 | 天界 | 攻击积累 Dawn 状态，满足条件后触发额外打击。 | 改为短连段或印记叠层法术，保留原日光特效；触发时序需要运行测试。 |
| Mortality | Dawn Devestated | 蚀月蓄击 | 虚无 | 攻击积累 Fading Fate，满足条件后消耗层数进行强化打击。 | 以月蚀与命运消散承接虚无；不要与已移植的月系法术仅换名重复。 |
| Mortality | Deliverance onto a New World | 陨星蓄放 | 天界 | 积累 Deliverance 层数，释放时按层数生成 Meteor。 | 已有流星雨，应强调命中蓄层后一次释放；最终保留与否需去重。 |
| Mortality | Rächer of Solnari | 生命契约 | 猩红（本体） | 以降低最大生命为代价增加攻击力，叠层增加收益与代价。 | 保留本体猩红；所有属性在结束、死亡、离线后需正确恢复。 |
| Mortality | Iridium Dreamtwister + Power Over Revival | 梦境唤回 | 天界 | 消耗复苏道具，寻找附近旁观玩家，并在演出后改为生存模式。 | 原逻辑不是通用死亡复活系统；不应直接改变正常旁观管理玩家的模式，暂缓正式移植。 |

## 对应代码路径

- 腐蚀刃波：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/BlightedCleaverItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/DefiledGreatswordItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/entity/AcidicSlashEntity.java`
- 追魂魔镰：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/BladecrestOathswordItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/ForbiddenOathbladeItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/ExaltedOathbladeItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/entity/ForbiddenScytheEntity.java`
- 恶魔兵装：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/DevilsDevastationItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/entity/DevilsScytheEntity.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/entity/DevilsPitchforkEntity.java`
- 灰烬追魂：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/GaelsGreatswordItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/entity/GaelSkullEntity.java`
- 地狱爆弹：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/VehemenceItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/entity/VehemenceBoltEntity.java`
- 硫火献祭：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/GrievanceItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/potion/BrimstoneFlamesBuffEffect.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/client/ModEffectEvents.java`
- 星芒连刺：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/StarlightItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/entity/StarlightStabEntity.java`
- 铁意回生：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/NightfallItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/events/NightfallEvents.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/client/ModEffectEvents.java`
- 拘魂钩索：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/ForeignHookItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/entity/HookEntity.java`
- 静止庇护：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/curios/PaleBranchItem.java`
- 震动感知：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/curios/WardenSpineItem.java`
- 刃域加速：`audit/terramity-addons-review/lethality/source/net/daphne/lethality/item/custom/HFMeowrasamaItem.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/potion/BladeModeEffect.java`；`audit/terramity-addons-review/lethality/source/net/daphne/lethality/client/BladeModeEvents.java`
- 疫病注入：`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/ToolUsedToAttackProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/PlagueCoreTicksProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/PlagueActivityProcedure.java`
- 疫雾爆发：`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/PlagueScytheKillsProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/ExplosionAndStainProcedure.java`
- 腐甲连爪：`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/VenoshopodAttackAndSpecialsProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/VenoshopodSuperattackProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/ArmorCrunchStartsProcedure.java`
- 裂核重击：`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/ChampionOfElbramAttacksProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/ToolUsedToAttackProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/ShatteredCoreStartsProcedure.java`
- 火灵集群：`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/FurnaceCoreAttacksProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/FurnaceCoreMechanicProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/FireSpiritOnEntityTickUpdateProcedure.java`
- 晨曦三叠：`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/DawnmakerAttackProcedureProcedure.java`
- 蚀月蓄击：`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/DawnUnmadeSpecialSequenceProcedure.java`
- 陨星蓄放：`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/DeliveranceProcProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/DeliveranceSummonsMeteorsProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/MeteorTicksProcedure.java`
- 生命契约：`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/RacherUseProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/BondOfLifeAppliedProcedure.java`；`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/BondOfLifeExpiresProcedure.java`
- 梦境唤回：`audit/terramity-addons-review/mortality/source/net/mcreator/mortality/procedures/DreamtwisterRightclickProcedure.java`

## 本轮结论

- 22 项候选，其中刃域加速需要继续核查事件，梦境唤回暂缓；同系列镰刃、连爪已经合并，普通材质升级武器不逐个计数。
- 新建学派只建议疫病一个；硫火、诅咒、灵魂剑技继续交给炼狱，避免再拆出一组相似学派。
- Lethality 本次类清单以武器、弹射物、饰品和状态为主；不凭模组名称宣称它一定有 Boss。Mortality 实体候选也不能一律称作 Boss。
- 原弹射物普遍依赖武器攻击属性、物品栈、网络包或原事件；接入铁魔法时应改为法强、等级、法力与冷却，不伪造持剑输入。
- 静态反编译用于核对逻辑，不等于编译可用源码；原 Mortality 部分反编译方法含 LambdaMetafactory 等不适合直接复用的输出，需要结合 javap/运行验证。

## 疫病卷轴定稿

用户已确认采用 ROACW 原版 `assets/roacw/textures/item/scroll_plague.png`，原生 16×16、静态，保持原配色与轮廓。定稿位于 `../generated-art/brutality-iron-scrolls/final/疫病/scroll_plague.png`，已复制到本工程资源并配置物品模型；新学派与法术尚未注册，未构建部署。本次仅复用该素材，不接入 ROACW 的其他附属学派。

疫病卷轴动画审看版：`../generated-art/brutality-iron-scrolls/review/plague-animated-v1/`。24 帧，每帧 2 tick，2.4 秒循环；原生 16×16，字迹明暗流动与两侧疫病粒子。首尾帧保持原图；已加入工程资源，未构建部署，等待视觉反馈。原静态定稿保留。
