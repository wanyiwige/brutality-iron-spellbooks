# Brutality 法术迁移附属

当前 1.4.0：原有 30 个法术，加上批注后保留的 23 个，共 53 个；影焰斩已在 1.3.2 删除。全部 53 个法术已接入用户选定的原生 16×16 图标。图标接入记录见 `revision-1.4.0.md`，第二轮法术修改记录保留在 `revision-1.3.1.md`。

Forge 1.20.1，模组 ID `brutality_iron`。构建产物为
`build/libs/brutality_iron_spellbooks-1.4.0.jar`。

## 学派和标志物

| 原学派 | 铁魔法学派 | 标志物 |
|---|---|---|
| VOIDWALKER | `brutality_iron:nihility` 虚无 | `terramity:empty_essence` |
| CELESTIA | `brutality_iron:celestia` 天界 | `terramity:angel_feather` |
| BRIMWIELDER | `brutality_iron:inferno` 炼狱 | `terramity:chthonic_crystal` |
| COSMIC | `alex_caves_spellsbook:hazen_cosmic` 宇宙 | 沿用现有学派 |
| UMBRANCY | `alex_caves_spellsbook:hazen_shadow` 暗影 | 沿用现有学派 |

每派六个法术，共 30 个，包括原版存在实现但未注册的四个。
完整 ID、原类路径及分类见 `spell-inventory.json`。

## 使用

客户端与服务端安装相同 JAR，同时保留当前 Brutality 0.8.5、Terramity
0.9.8、铁魔法 1.20.1-3.16.3、万氏铁魔法拓展及它们的前置。
本附属复用原模组的实体基类、效果、声音和模型，并非无前置替代版。
本附属使用独立模组 ID，与原 Brutality 一起安装。

进入创造页“Brutality：铁魔法法术”获取卷轴，再通过铁魔法铭文台写入法术书，
使用铁魔法原生施法键。三个新标志物支持卷轴锻造台；宇宙与暗影沿用已有学派。
同一创造页还包含六本史诗品质空白法术书，属性见 `spellbook-design.md`。
恶魔魔典对应 Bielgg 暗影，黑暗之书对应 Echo 幽匿；其余为宇宙、虚无、炼狱与天界。
六本可使用原生铭文流程写入各学派法术，装备于法术书饰品槽时获得属性。
GUI 使用定稿动态贴图，手持引用 Brutality 的原模型、原贴图及原发光层。
可在工作台用普通书与 Terramity 材料合成，六本配方详见 `book-recipes.md`。
获得对应核心材料后解锁配方书提示。Bielgg 暗影内容或 Echo 未安装/启用时跳过对应书和配方。
被弃用的第七本暗影之书没有注册。天界书采用最终无翅膀、仅光环与书面动效的版本。
三张卷轴使用先前确认的最终动画贴图。没有改动原法书及其原施法体系。
其中虚无卷轴已采用新选定的银白金属环版本，来源记录于 `scroll-selection.json`。

最高等级按法术分别设为 III、V、VIII、X，共 209 个等级卷轴。各法术的
上限与实际逐级稀有度见 `spell-progression.md`。起始稀有度分别为史诗、稀有、
罕见、普通，使用铁魔法本体的稀有度权重计算，最后两级固定为传说。
锻造台按实际稀有度的首个等级出卷轴，不存在的稀有度不会生成零级卷轴。
服务器配置仍可覆盖默认上限和起始稀有度；旧存档如保存过旧的十级配置，
需要在其铁魔法 serverconfig 中调整 `brutality_iron` 的对应条目才能使用新默认值。
基础伤害、距离、持续时间和等级成长沿用原实现；
冷却采用原基础冷却，使用铁魔法的统一配置覆盖机制。伤害接入铁魔法总法强、
对应学派法强、抗性及法术伤害事件；法力与冷却由铁魔法管理。
持续技每次原生持续施法结算都消耗法力，数值仍需实玩平衡。
当前施法逻辑以玩家为施法者，不支持法师怪物自动施放这些法术。

## 原版缺漏的处理

- `destruction` 原版仅产生特效，已补上半径 3 格的单次范围伤害，沿用其 10+2×等级伤害参数。
- `intangible` 原版为瞬发却只实现 tick 回调，已修正为瞬发添加虚化效果。
- `duo_lunaris` 原版实现与 `moonlit_mending` 重复，首版保留治疗行为。
- `sacred_oath` 保留原有相位效果；没有依据与实现不符的旧文案虚构新技能。
- 神圣光柱补上施法者归属与等级伤害；陨星移除写死伤害，使用学派法强。
- 夜幕使用存档持久化的时间恢复状态，连续施法会延长期限。
- 解离术以原生命阈值判定后，通过铁魔法伤害处理，移除直接删除生物和强制置零生命的路径。
- 原版缺失的法术选择图标使用补充像素图标；三个学派卷轴仍为用户确认的定稿。

## 构建与验证

本机使用 `./build-local.ps1 -Tasks build`，Java 17、ForgeGradle 6、Forge 47.4.18。
`libs` 是本机编译前置，不打入发布 JAR。`reobfJar` 随 jar 任务执行。
`ServerAudit` 仅在显式传入 `-Dbrutality_iron.audit=true` 时执行，用于一次性独立测试存档，
会执行法术回调、检查卷轴保存与新学派锻造，随后关闭服务器。不可对正式存档启用。
客户端渲染、实际按键施法、多人同步与全等级战斗平衡仍需游戏验证。

第三方来源见 `THIRD_PARTY_NOTICES.md`。

## 1.2.0 技能扩展

新增 60 个武器、饰品与实体技能法术，共 90 个法术。新增学派优先使用虚无、天界、炼狱、流风与岩土，并新增疫病；其余使用本体冰霜、猩红、自然或唤魔、邪术。原 30 个法术的学派归属保留。新增全部 ID、默认参数见 `skill-expansion.json`。

流风和岩土现在是必要依赖。疫病卷轴采用 ROACW 原图衍生的 24 帧气泡动画；当前标志物兼容 Terramity 幽冥水晶与 Mortality 疫病核心。原 Lethality/Mortality 不需要作为编译/运行类依赖，所需模型与贴图随此本地集成打包。

此批是面向玩家施法重写的版本，调整了 Boss 数值、友军过滤、召唤数量与持续时间；不是给原武器模拟右键。疫病移除原版随机给予增益的分支，作为纯敌对感染处理。召唤物没有掉落与经验，离线、死亡或换维度时清理；重启后已有临时实体快速消散，不延续尚未结束的法术会话。

梦境唤回只处理近期死亡事件后进入旁观状态的玩家，拒绝普通旁观玩家及极限模式。它不是通用倒地系统，也不恢复物品。多人与实际死亡/重生流程需要另行实玩验证。

重生成资源时依次运行 `prepare_resources.py`、`prepare_books.py`、`prepare_skill_expansion.py`、`prepare_skill_visuals.py`、`prepare_selected_scrolls.py`。

## 1.2.1 客户端模型热修复

修复陨星蓄放等新法术实体渲染时的 GeckoLib 模型加载崩溃。复制的模型、动画和贴图现在分别置于 `geo/ported/`、`animations/ported/`、`textures/ported/`，使其进入 GeckoLib 资源扫描。渲染前检查模型与动画缓存；若资源缺失则记录错误并暂不绘制该实体。此保护不改变服务端伤害或施法逻辑。
