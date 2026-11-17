# 六本学派法术书方案

状态：用户已确认，现按本表接入 1.1.0；构建、测试及部署结果分别记录在 audit 目录。

仅纳入已经保留的六种外观。`brutality:umbral_tome` 暗影之书的第七张贴图已被用户弃用，不在接入范围内。

## 学派与建议属性

定位为后期学派专精书，物品稀有度统一为史诗。下表法力是额外最大法力，百分比是对应属性加成。

| 书本外观与原模型 | 学派 | 法术槽 | 最大法力 | 学派法强 | 其他属性 | 用途 |
|---|---|---:|---:|---:|---|---|
| 恶魔魔典 `brutality:daemonic_tome` | 暗影 `bielgg_spells:shadow` | 10 | +200 | +20% | 施法速度 +15% | 快速施法与连招 |
| 黑暗之书 `brutality:darkist_tome` | 幽匿 `echo_spellbooks:echo` | 10 | +220 | +20% | 施法移动速度 +20% | 走位施法与幽匿控制 |
| 宇宙魔典 `brutality:cosmic_tome` | 宇宙 `alex_caves_spellsbook:hazen_cosmic` | 12 | +300 | +15% | 法力回复 +10% | 大法力池与多法术配置 |
| 虚空魔典 `brutality:void_tome` | 虚无 `brutality_iron:nihility` | 8 | +160 | +25% | 冷却缩减属性 +15% | 用较少槽位换取更强的学派输出与技能周转 |
| 持剑者宝典 `brutality:brimwielder_tome` | 炼狱 `brutality_iron:inferno` | 10 | +200 | +25% | 法力回复 +15% | 高耗能法术与分轮输出后的补充 |
| 塞拉斯蒂娅之书 `brutality:celestia_tome` | 天界 `brutality_iron:celestia` | 12 | +240 | +20% | 通用法术抗性 +10% | 攻击、保护、治疗等法术的综合配置 |

幽匿与暗影两本的学派来自用户明确指定。其余四本沿用本次移植已经确认的对应关系。
这两本暗色书不与 `alex_caves_spellsbook:hazen_shadow` 混用；后者仍是已移植月系法术的归属，不能通过一本书的外观选择自动更改那些法术的学派。

## 共用规则

- 每本均可使用铁魔法原生铭文流程写入各学派法术；对应学派法强仅加成自己的学派。
- 建议采用原生空白可写法术书，不预装或锁定法术，槽数均为可写总槽数。
- 装入铁魔法法术书饰品槽时按原生装备规则提供属性；背包中携带不生效，手持仅用于使用与 3D 展示，不额外重复叠加同一本书的装备属性。
- 法力加成为固定增加上限。学派法强、回蓝、冷却、抗性等使用原生百分比属性；例如“冷却缩减属性 +15%”不承诺最终冷却时间恰好减少 15%，实际值由本体叠加和软上限算法决定。
- 法力回复加成遵循本体现有回复限制，不保证持续施法时也能回蓝。
- 学派法强不自动增强写死的治疗比例、位移距离、增益持续时间或控制效果。首版不额外改写法术机制。
- 背包、物品栏等 GUI 使用确认后的 16×16 动态贴图；手持沿用对应 Brutality 原模型、原 UV 与原有纹理效果。
- 创造标签页增加六本书；被弃用的第七本不注册。
- Bielgg 与 Echo 采用可选依赖思路：相应模组及学派不存在时跳过该本书及其配方，而不是注册失效属性或悄悄改为其他学派。当前整合包两者均已安装。

## 数值依据与实际核对

当前安装铁魔法 `irons_spellbooks-1.20.1-3.16.3.jar` 的 `ItemRegistry` 中：

- 钻石法术书为 10 槽、+100 最大法力。
- 下界合金法术书为 12 槽、+200 最大法力和 +20% 冷却缩减属性。
- 烈焰与德鲁伊法术书为 10 槽、+200 最大法力和 +10% 对应学派法强。
- 龙皮法术书为 12 槽、+200 最大法力和 +10% 末影法强。

本方案比这些本体书略强，按后期专精定位设计，尚未做战斗平衡测试。配方与获取途径应在接入前单独确定，不默认无成本获取。

实际学派属性键：

| 书 | 学派法强属性 |
|---|---|
| 恶魔魔典 | `bielgg_spells:shadow_spell_power` |
| 黑暗之书 | `echo_spellbooks:echo_spell_power` |
| 宇宙魔典 | `alex_caves_spellsbook:hazen_cosmic_spell_power` |
| 虚空魔典 | `brutality_iron:nihility_spell_power` |
| 持剑者宝典 | `brutality_iron:inferno_spell_power` |
| 塞拉斯蒂娅之书 | `brutality_iron:celestia_spell_power` |

通用属性来自铁魔法：`max_mana`、`cast_time_reduction`、`casting_movespeed`、`mana_regen`、`cooldown_reduction`、`spell_resist`。

核对材料：已安装 Bielgg/Echo JAR 的注册类及语言文件、移植附属 `Content.java`、万氏拓展 `HazenSchools.java`/`HazenAttributes.java`，以及从当前铁魔法 JAR 提取的 `ItemRegistry`、`SpellBook`、`UniqueSpellBook`、`AttributeRegistry`。
