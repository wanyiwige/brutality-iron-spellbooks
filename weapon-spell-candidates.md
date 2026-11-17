# Brutality 武器技能法术化初审

依据当前安装的 `mods/brutality-0.8.5-1.20.1.jar` 重新提取并反编译武器和弹射物类。此报告仅提出候选，没有修改模组功能或重新部署。

用户确认已完成：终焉棱镜持续光束、超新星护体小行星。本轮剔除这两个效果。现有 30 个法术也已核对；机制相似的候选仍需设计时合并或区分。

下列为新增候选的设计归属，不表示原武器自带学派。按当前约定优先使用虚无、天界、炼狱、流风、岩土，其余仅用铁魔法本体学派；不使用其他附属学派。完整学派 ID 与理由见 `candidate-school-assignments.md`。

| 来源武器 | 物品 ID | 建议法术 | 建议学派 | 原代码效果 | 移植备注 |
|---|---|---|---|---|---|
| 事件视界 | `brutality:event_horizon` | 引力黑洞 | 虚无 | 生成跟随施法者的黑洞，持续牵引并伤害周围生物。 | 优先；改为限时实体并接入友军过滤。 |
| 神圣战斧·利塔 | `brutality:rhitta` | 残酷太阳 | 天界 | 头顶召唤跟随的小太阳，持续点燃附近生物；原武器限白天。 | 优先；原范围以太阳实体为中心，不是玩家为中心；白天限制是否保留待设计。 |
| 朗基努斯圣枪 | `brutality:rhongomyniad` | 圣枪贯光 | 天界 | 挥击触发直线圣光射线，并对沿线目标造成伤害。 | 优先；是短促射线，与用户已做的持续棱镜区分。 |
| 暗影步 | `brutality:shadowstep` | 暗影背袭 | 虚无 | 锁定视线中的目标，传送至目标背后。 | 优先；背刺增伤属于武器被动，不自动附加给法术。 |
| 霜之哀伤 | `brutality:frostmourne` | 冰魂震波 | 冰霜 | 向外扩散冰霜波，伤害、减速、冻结计时并按命中数恢复生命，附加自身增益。 | 优先；需要限制治疗与增益上限。 |
| 霜之哀伤 | `brutality:frostmourne` | 流浪者军团 | 冰霜 | 一次召唤 3–6 只受控流浪者，原逻辑同时最多 10 只。 | 优先；原实现复用铁魔法 SummonedStray，可接入原生召唤寿命和数量管理。 |
| 猩红剪刃 | `brutality:crimson_scissor_blade` | 猩红血斩 | 猩红 | 发射血色斩击，弹射物命中后治疗施法者。 | 优先；原中文描述中的耗血/恢复数值不能直接作为代码数值，当前弹射物命中治疗为 4 点生命。 |
| 影焰剪刃 | `brutality:shadowflame_scissor_blade` | 影焰斩 | 炼狱 | 发射影焰斩击，保留原暗影火焰效果。 | 优先；需统一命中伤害及 Terramity 效果归属。 |
| 异界之刃 | `brutality:exoblade` | 追踪剑光 | 炼狱 | 一次发射四道剑光，弹射物寻敌追踪并附加 Miracle Blight。 | 适合；需解除原武器伤害/物品栈依赖。 |
| 第七星 | `brutality:seventh_star` | 星印引爆 | 天界 | 三向/八向发射可附着星体，另一次操作引爆本人留下的星体和目标印记。 | 适合；需要法术两阶段施放和独立印记管理，避免与已有星爆同名。 |
| 创造折痕 | `brutality:crease_of_creation` | 念动力操纵 | 虚无 | 持续将选定实体拖向视线前方，可投掷并造成碰撞伤害。 | 适合；已有重力运动是瞬时推拉，此技能为持续操纵；需生命阈值和 Boss 抗性规则。 |
| 高频村正 | `brutality:hf_murasama` | 次元乱斩 | 炼狱 | 释放大量交错斩击粒子，对周围目标进行范围伤害。 | 适合；原代码松手结算一次范围伤害，不是持续多次伤害。 |
| 初始爆炸 | `brutality:first_explosion` | 蓄能爆裂 | 炼狱 | 锁定远处地面并蓄力，释放爆裂射线和大范围爆炸。 | 适合但改造较多；法阵生成函数实际没有生成实体，不能把文案当成完整特效；实例字段需改为每位施法者独立状态。 |
| 末日铁拳护手 | `brutality:doomfist_gauntlet` | 火箭拳 | 流风 | 蓄力后向前突进，碰撞目标时造成动能伤害和击退。 | 第二批；需要碰撞、位移及多人同步验证。 |
| 挑战者深渊/马里亚纳海沟 | `brutality:challenger_deep / marianas_trench` | 深渊追踪弹 | 虚无 | 释放 AbyssProjectile 弹幕，两把武器可合并为一个法术设计。 | 第二批；避免两把武器各移植出一个重复法术。 |

## 对应代码路径

- 引力黑洞：`audit/weapon-review/source/net/goo/brutality/item/weapon/spear/EventHorizonSpear.java`；`audit/weapon-review/source/net/goo/brutality/entity/projectile/generic/BlackHole.java`
- 残酷太阳：`audit/weapon-review/source/net/goo/brutality/item/weapon/axe/RhittaAxe.java`；`audit/weapon-review/source/net/goo/brutality/entity/projectile/generic/CruelSunEntity.java`
- 圣枪贯光：`audit/weapon-review/source/net/goo/brutality/item/weapon/spear/RhongomyniadSpear.java`；`audit/weapon-review/source/net/goo/brutality/entity/projectile/generic/RhongomyniadRay.java`
- 暗影背袭：`audit/weapon-review/source/net/goo/brutality/item/weapon/sword/ShadowstepSword.java`
- 冰魂震波：`audit/weapon-review/source/net/goo/brutality/item/weapon/sword/FrostmourneSword.java`
- 流浪者军团：`audit/weapon-review/source/net/goo/brutality/item/weapon/sword/FrostmourneSword.java`
- 猩红血斩：`audit/weapon-review/source/net/goo/brutality/item/weapon/sword/CrimsonScissorBlade.java`；`audit/weapon-review/source/net/goo/brutality/entity/projectile/generic/Bloodslash.java`
- 影焰斩：`audit/weapon-review/source/net/goo/brutality/item/weapon/sword/ShadowflameScissorBlade.java`；`audit/weapon-review/source/net/goo/brutality/entity/projectile/generic/ShadowflameSlash.java`
- 追踪剑光：`audit/weapon-review/source/net/goo/brutality/item/weapon/sword/ExobladeSword.java`；`audit/weapon-review/source/net/goo/brutality/entity/projectile/trident/ExobladeBeam.java`
- 星印引爆：`audit/weapon-review/source/net/goo/brutality/item/weapon/sword/SeventhStarSword.java`；`audit/weapon-review/source/net/goo/brutality/entity/projectile/generic/StarEntity.java`
- 念动力操纵：`audit/weapon-review/source/net/goo/brutality/item/weapon/generic/CreaseOfCreation.java`
- 次元乱斩：`audit/weapon-review/source/net/goo/brutality/item/weapon/sword/MurasamaSword.java`
- 蓄能爆裂：`audit/weapon-review/source/net/goo/brutality/item/weapon/generic/FirstExplosionStaff.java`；`audit/weapon-review/source/net/goo/brutality/entity/projectile/ray/ExplosionRay.java`
- 火箭拳：`audit/weapon-review/source/net/goo/brutality/item/weapon/generic/DoomfistGauntletItem.java`
- 深渊追踪弹：`audit/weapon-review/source/net/goo/brutality/item/weapon/sword/ChallengerDeepSword.java`；`audit/weapon-review/source/net/goo/brutality/entity/projectile/generic/AbyssProjectile.java`

## 暂缓项目

- 原子审判：可以做核爆/跃起砸地，但伤害入口分散在武器与事件代码，需另查事件与爆炸帮助类后再列为可直接移植项。
- 云：确有生命、饥饿、饱和度、氧气及效果存取代码，适合设计状态回溯，但需要限制可恢复内容及冷却，避免反复刷资源。
- 吸血鬼飞刀：已确认多枚飞刀投掷，但当前 VampireKnife 类本身没有治疗逻辑；不能只凭名称认定有吸血，需要继续追踪父类/事件。
- 命运卡组：确认五种随机命中效果（缓慢、短暂眩晕、点燃、中毒、凋零），可做趣味法术，第一批优先级较低。
- 纯属性被动、厨房工具和普通炸药投掷不作为首批。

## 通用改造

接入铁魔法法力、冷却、等级、学派法强与伤害归属；目标过滤复用本体友军逻辑。原物品 NBT、当前手持检查与 Item 单例状态应改为每位施法者独立数据。持续实体在结束、死亡、离线和换维度时清理。此处难度结论为静态代码审查，尚未做这些新增技能的运行验证。
