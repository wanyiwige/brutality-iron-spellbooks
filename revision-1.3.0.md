# 1.3.0 用户批注实施记录

按 spell-revision-checklist.md 的用户批注实施。原文不改。

原有 30 个法术保留；新增批次保留 27 个、删除 33 个，合计 57 个法术、397 个默认等级卷轴。原有六本书、配方及已选卷轴贴图保留。

## 逐项结果

| 编号 | 原 ID | 处理 |
|---|---|---|
| 01 | `brutality_iron:event_horizon` | 复用原版模型与贴图，补回原版 FullbrightNoDepth 发光层。 |
| 02 | `brutality_iron:backstep` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 03 | `brutality_iron:telekinetic_grasp` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 04 | `brutality_iron:abyssal_volley` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 05 | `brutality_iron:void_exchange` | 改为 irons_spellbooks:ender，沿用末影学派的法强、抗性与锻造标志物。 |
| 06 | `brutality_iron:ceiling_ascent` | 保持原机制。 |
| 07 | `brutality_iron:vanishing_decoy` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 08 | `brutality_iron:pursuit_anchor` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 09 | `brutality_iron:binding_hook` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 10 | `brutality_iron:stillness_ward` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 11 | `brutality_iron:eclipse_charge` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 12 | `brutality_iron:cruel_sun` | 复用原版模型与贴图，补回 FullbrightNoDepth 与 FullbrightAlpha 两个附加层。 |
| 13 | `brutality_iron:sacred_lance` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 14 | `brutality_iron:stellar_marks` | 改为朝准星的四颗小范围散射，保留附着与二次引爆。 |
| 15 | `brutality_iron:crystal_sanctuary` | 接入粉紫水晶法阵与逐步显现；中心使用指定的 Terramity 晶体方块与交叉平面晶簇。 |
| 16 | `brutality_iron:triple_judgment` | 在身前三处局部位置布阵，锁定施法时准星方向，依次释放平行圣光；光束遇方块结束。 |
| 17 | `brutality_iron:starlight_flurry` | 参照上游 StarlightItem：5–6 道星刺同时从视线横截面的随机位置释放；七色纹理，不再单列连续射出。 |
| 18 | `brutality_iron:ironwill_recovery` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 19 | `brutality_iron:dawn_triad` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 20 | `brutality_iron:meteor_reservoir` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 21 | `brutality_iron:dream_recall` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 22 | `brutality_iron:shadowflame_slash` | 使用重绘黑紫影焰贴图，斩击面正对运动方向，保留穿透与单目标去重；Terramity 影焰状态按实际注册情况使用。 |
| 23 | `brutality_iron:seeking_blades` | 原剑光纹理改为单平面；四个独立发射点，短暂直飞后限角转向，优先分配不同可见目标。 |
| 24 | `brutality_iron:infernal_crosscut` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 25 | `brutality_iron:charged_detonation` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 26 | `brutality_iron:burning_zeal` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 27 | `brutality_iron:stygian_souls` | 6–10 枚游魂从身后周围多个位置先后发出，展开后追踪；限制转弯并检查视线，增加随实际路径弯曲的尾带。 |
| 28 | `brutality_iron:hellrok_slam` | 接入紫红火焰裂纹贴图及蓄势/冲击过程，只伤害同层可达的地面目标。 |
| 29 | `brutality_iron:soulreaping_scythes` | 原贴图分离为单平面，镰刃在飞行坐标内持续自转；追踪限角，单目标去重。 |
| 30 | `brutality_iron:devil_armament` | 三镰两矛先在前方展开，再按间隔发射；原纹理拆分为单层，镰刃自转、叉矛尖端朝前。 |
| 31 | `brutality_iron:ashen_skulls` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 32 | `brutality_iron:hellish_bolt` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 33 | `brutality_iron:brimstone_pact` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 34 | `brutality_iron:infernal_spirits` | 接入本体 PlayerRecasts、SummonedEntitiesCastData 与 SummonManager；第二段取消该次召唤。火灵燃烧改在弹体命中时施加。 |
| 35 | `brutality_iron:gale_fist` | 冲刺经过的轨迹按约 0.6 格间隔生成白色云雾，累计不足一段的移动距离。 |
| 36 | `brutality_iron:inertia_reversal` | 保持原机制。 |
| 37 | `brutality_iron:gale_recoil` | 保持原机制。 |
| 38 | `brutality_iron:homeward_gale` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 39 | `brutality_iron:gale_blitz` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 40 | `brutality_iron:blade_current` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 41 | `brutality_iron:guardian_hand` | 守护之手生成在准星前方，锁定施法方向并有独立旋转；显示射线与实际遇墙长度对应。 |
| 42 | `brutality_iron:resonant_roar` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 43 | `brutality_iron:stone_retinue` | 沿用原召唤数量，接入本体原生二段取消与召唤生命周期。 |
| 44 | `brutality_iron:seismic_pressure` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 45 | `brutality_iron:stone_body_slam` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 46 | `brutality_iron:tremor_sense` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 47 | `brutality_iron:core_shatter` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 48 | `brutality_iron:caustic_wave` | 接入黑绿腐蚀刃波；使用准星局部坐标散射，统一首帧方向。 |
| 49 | `brutality_iron:plague_infusion` | 命中目标生成黑绿感染雾团，保留感染传播机制。 |
| 50 | `brutality_iron:pestilent_mist` | 以十帧 16×16 黑绿毒雾纹理形成多团错帧的雾区，替代原通用粒子堆。 |
| 51 | `brutality_iron:corrosive_claws` | 三次爪击接入独立黑绿爪痕贴图，保留原感染与削甲机制。 |
| 52 | `brutality_iron:soulfrost_wave` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 53 | `brutality_iron:stray_legion` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 54 | `brutality_iron:ice_cube_barrage` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 55 | `brutality_iron:sanguine_slash` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 56 | `brutality_iron:vital_covenant` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 57 | `brutality_iron:gaia_bloom` | 接入低像素藤叶法阵，按帧由内向外逐步浮现。 |
| 58 | `brutality_iron:arcane_retinue` | 已从法术注册、创造页和新生成卷轴/锻造中移除。 |
| 59 | `brutality_iron:spirit_bomb` | 保留原施法与伤害机制；与其他弹体一并修正生成时的初始化顺序。 |
| 60 | `brutality_iron:triple_profane_beam` | 与三重圣裁共用方向/布局规则，使用独立邪术法阵和紫黑光柱，替换原 holy_beam 外观。 |

## 实现说明

- 新像素贴图直接来自用户要求使用的 spell-revision-v2 原生稿；全部复制到模组自身命名空间。晶体主体引用实际 Terramity 方块模型。
- 武器弹体使用一个双面可见的四边形，并按原图比例设置尺寸；光束采用相交平面保证侧面可见，二者不共用模型结构。
- 弹体入世界之前设置模型、位置、速度、朝向及显示参数，避免首次生成时方向默认为零。新增角色、大小、寿命与发射轴同步。
- 跨墙追踪、定向光束、地面重击与取消召唤加入隔离服回归；具体结果以 audit/skill-release-verification.json 和 audit/validation-1.3.0.md 为准。
- 删除旧注册后，旧存档中本附属的相应短期效果实体在服务端 tick 时清除。移除的法术 ID 不再可获取；本次不修改任何存档文件。

## 验证边界

构建、服务端测试与资源检查不等同于客户端画面验收。尚未完成重启整合包后的实际施法、光影渲染、多人旁观及平衡验收。
