# 法术贴图重绘稿 v2

本批只包含美术素材与动画预览，尚未替换模组运行资源。原生 PNG 为实际低分辨率透明贴图；放大图仅供查看。

## 素材清单

| 贴图 | 对应法术 | 原生尺寸 | 动画 |
|---|---|---|---|
| [水晶疗愈阵](native/circle_crystal.png) | 15 水晶疗愈阵 | 64×64 | [GIF预览](preview/circle_crystal.gif) |
| [盖亚催生阵](native/circle_nature.png) | 57 盖亚催生阵 | 64×64 | [GIF预览](preview/circle_nature.gif) |
| [邪术法阵](native/circle_eldritch.png) | 60 三重邪光 | 64×64 | [GIF预览](preview/circle_eldritch.gif) |
| [疫病毒雾](native/plague_cloud.png) | 49 疫病注入 / 50 疫雾爆发 | 16×16 | [GIF预览](preview/plague_cloud.gif) |
| [腐蚀刃波](native/plague_wave.png) | 48 腐蚀刃波 | 32×48 | 静态稿 |
| [腐甲爪痕](native/plague_claws.png) | 51 腐甲连爪 | 32×32 | 静态稿 |
| [影焰斩](native/shadowflame_slash.png) | 22 影焰斩 | 24×48 | 静态稿 |
| [炼狱重击](native/inferno_impact.png) | 28 炼狱重击 | 64×64 | 静态稿 |
| [邪术光柱](native/column_eldritch.png) | 60 三重邪光 | 16×64 | 静态稿 |
| [圣光打击](native/column_holy.png) | 16 三重圣裁 | 16×64 | 静态稿 |

## 绘制依据与检查

- 使用内置图像生成工具分别生成各贴图底稿，再作原生网格取样、透明阈值清理和有限色整理；具体提示词见 generation.json 与 revision-prompts.json。原始生成图保留在 raw/。
- 水晶使用 Terramity 两张晶体贴图的七种实际不透明颜色。materials/ 中保留原版方块与晶簇贴图，后续用于水晶主体，未将其替换成其他配色。
- 疫病毒雾按 16×16 网格、六种颜色输出，其中五色来自 RoACW 原云雾，新增黑绿深色。刃波与爪痕增加少量酸绿高光，保留破碎轮廓。
- 三种法阵分别为粉紫晶体、绿叶催生和紫黑邪术主题；参考 Magic Circles 的同心环、符文字带与中心图案布置，未照搬其 1024px 线稿。
- 阴影、火焰与光柱分别使用独立形状与色板。影焰和炼狱采用黑紫/紫红；圣光以金白为主，邪光以紫黑与病白为主。
- 所有原生图仅使用 0/255 透明度、至多八种不透明色；尺寸与 SHA256 见 validation.json。放大图为整数倍最近邻。
- 两张光柱在首轮低分辨率预览中过细，已单独生成第二版并重新检查；并非直接把细线稿用于游戏。
- 法阵动画为一次显现后停留，preview/ 中 GIF 为方便观看而重复播放；native/ 中 _frames.png 和 _animation.json 保留运行时接入所需的帧信息。GIF 会合并相同画面，帧数可能少于原生帧条。
- 毒雾为十帧小幅像素漂动，画布和像素大小保持不变。已检查多帧对照；尚未在 Minecraft 中验证透明排序、世界大小、朝向及光影表现。

## 查看与文件用途

- [总览](preview/contact-sheet.png) / [动画多帧检查](preview/animation-check.png) / [浏览图册](gallery.html)
- native/：原生透明 PNG 与四组动画帧条；preview/：放大图与 GIF；materials/：原版水晶材质。
- 图像宽高不是世界显示尺寸。后续接入时须按模型、法术半径和地形选择实际尺寸，避免将小纹理无限撑大。
- build.py 可从保留的原图重复生成原生稿；素材包 ZIP 只打包可交付贴图、预览和说明，不包含大尺寸底稿。

## 参考出处

- Terramity 0.9.8 与 RoACW 1.2.4 的本地 JAR 原始纹理。
- 用户指定的 Magic Circles 1.2.2 JAR。
- https://calamitymod.wiki.gg/wiki/File:Plague_Cloud.png
- https://calamitymod.wiki.gg/wiki/File:Virulent_Wave.gif
- https://calamitymod.wiki.gg/wiki/File:Soul_Scythe_(projectile).png
- 更完整的原图与实现核对见 ../../revision-reference-review.md。
