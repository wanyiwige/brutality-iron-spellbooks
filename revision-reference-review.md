# 法术重做：指定素材核对记录

日期：2026-11-14。只记录已读取的素材和后续绘制约束。本记录不代表重做已经实现或部署。

## 用户最新要求与范围

以用户编辑的 spell-revision-checklist.md 为准。实际逐项统计为删除 33 个、保留 27 个，先前口头说成删除 35 个、保留 25 个有误。保留项包括疫病四法术，按学派标题下的整体要求重绘；聚能灵弹的修改栏为空，暂不增加新机制。原有 30 个法术与六本书不在这次删改范围。

用户随后要求先看指定素材，再谈重绘；未经参考核对的九宫格方案不作为制作依据。

## Terramity 原始水晶素材

- 方块：assets/terramity/textures/block/prismatic_crystal_block.png，16×16，6 种含透明色在内的像素颜色。
- 晶簇：assets/terramity/textures/block/prismatic_crystal_cluster.png，16×16，8 种含透明色在内的像素颜色。
- 实际配色：浅粉白高光、粉紫亮部、紫色中间色、深紫暗部。不能擅自换成天蓝水晶。
- 晶簇原模型使用 block/cross，即两个交叉平面；方块与晶簇的立体呈现应参照实际模型，不能把纹理随便贴成球体。
- 提取原图和最近邻放大对照位于 audit/revision-references/crystal-native-reference.png。

## RoACW 疫病素材

已读取 roacw-1.2.4.jar 的瘟疫粒子定义及纹理：

- assets/roacw/particles/plague_cloud.json：按 plague_cloud_frame_0 至 frame_9 组成 10 帧。
- 每帧是 16×16，约 3–6 色，包括暗绿/黑绿阴影、草绿主体、黄绿亮部，轮廓由少数大像素簇形成。
- plague_nano_green.png 为 16×16，4 色；实际可见形状仅占小部分画布，不应把整张 16×16 当作一个巨大的方块粒子。
- 瘟疫导弹是 64×256 的模型 UV 图集，不能据此把毒雾也画成同尺寸或套用导弹的机械造型。
- 对照图：audit/revision-references/plague-native-reference.png。

## 灾厄官方维基：已实际打开的贴图

已使用浏览器进入官方 wiki.gg 文件页。初始网页抓取返回 403，浏览器访问成功。

| 素材 | 原文件尺寸 | 直接文件页 | 参考用途 |
|---|---|---|---|
| Plague Cloud | 28×24 PNG | https://calamitymod.wiki.gg/wiki/File:Plague_Cloud.png | 疫雾轮廓、绿色明暗簇、毒雾团块 |
| Virulent Wave | 46×84 GIF，4 帧，0.6 秒循环 | https://calamitymod.wiki.gg/wiki/File:Virulent_Wave.gif | 腐蚀刃波：弯曲波前与破碎拖尾 |
| Soul Scythe (projectile) | 68×66 PNG | https://calamitymod.wiki.gg/wiki/File:Soul_Scythe_(projectile).png | 镰刃轮廓、刃缘亮色、旋转残影参考 |
| Plague Fang | 16×28 PNG | https://calamitymod.wiki.gg/wiki/File:Plague_Fang.png | 尖锐毒性弹体的轮廓参考，不替代当前四法术的机制 |

条目出处：

- https://calamitymod.wiki.gg/wiki/Plagued_Fuel_Pack ：对应 Plague Cloud；条目描述冲刺留下瘟疫云。
- https://calamitymod.wiki.gg/wiki/Virulence ：对应 Virulent Wave；条目描述朝光标发射快速瘟疫波，并有使用动图。
- https://calamitymod.wiki.gg/wiki/Soul_Harvester ：对应旋转瘟疫镰刃。
- https://calamitymod.wiki.gg/wiki/Plague_Staff ：对应四枚毒牙。
- https://calamitymod.wiki.gg/wiki/Plague_Keeper ：页面明确标记已删除/被替代，仅作历史美术参考，不能冒充现行物品。

视觉观察：瘟疫云与毒浪并非单一亮绿轮廓加纯黑填色；内部也有分层绿簇，毒浪有破碎、不规则边缘。项目重绘遵守用户要求的黑绿基调，同时保留参考原图的分层与团块。原始文件尺寸不等于 Minecraft 中的世界显示尺寸，也不据此宣称每个源像素都是同一视觉像素颗粒。

## Lethality 原星芒连刺

已核对 StarlightItem.shootStarlightStab 与 StarlightStabEntity/Model：

- 上游一次在视线横截面周围随机位置生成 5–6 个弹体，不是前方一条直线按顺序射出。
- 位置使用视线、右向、局部上向组成的坐标，偏移半径 0.5–1.25。
- starlight_stab_0 至 _6 共 7 个纹理变体，每个 32×64；红、黄绿、青、黄、粉紫、蓝紫、绿。
- 同一 UV 图包含多个面使用的图案，不能把整张贴图当成两支独立弹体直接平铺。
- 重做需保留多色和周围散布方式，复核模型 UV、朝向与实际发射包。

## Magic Circles 法阵布置参考

已读取用户提供的 C:/Users/WANYIWIGE/Downloads/magiccircles-1.2.2-1.20.1.jar。

- 原法阵绝大部分为 1024×1024；circle_4_neon 为 1024×1053。它们是高分辨率线稿，不是用户要的低像素成稿。
- RendererUtils.drawQuad 使用一张平面四边形映射整张纹理，再通过姿态矩阵定位/旋转/缩放；不是用一圈随机粒子代替法阵。
- MagicCircleFactory 区分面向视线的手前法阵与朝地面的脚下法阵；前者有准星/视线坐标偏移，后者按实体底部位置偏移。
- 地面分支含淡入、逐步缩放、旋转及结束变换；具体 tick 参数随分支不同，不能把其中一组常量套给所有情况。
- 可参照结构：内外同心环、独立符文字带、方位装饰、中央几何图形，中心与效果主体对应。
- 项目中借鉴布置、分层、出现与消散方式，按水晶/自然/邪术主题重画低像素版本；不把 1024 线稿缩小后模糊使用。

## 后续绘制约束

1. 毒雾按原生 16×16 粒子级别控制颗粒；世界里通过多团不同大小、错帧、淡入淡出来形成雾区。
2. 斩击与爪痕按实际长宽比制作，不强行统一正方形画布；用阶梯轮廓和有限色，不加平滑模糊边。
3. 法阵先确定世界直径，再按目标像素密度选择画布。既不能把 16×16 小图无限撑大，也不能直接使用 1024 的细密纹理。
4. 水晶法阵遵循 Terramity 的粉紫晶体配色；疫病遵循黑绿/黄绿；邪术独立设计，不能复用天界光柱仅换粒子。
5. 先展示原生尺寸与整数倍最近邻放大的静态稿，检查风格与颗粒，再接动画及运行时。当前未将新草稿接入模组。
