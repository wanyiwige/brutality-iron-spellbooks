# 1.4.0：接入 53 张原生法术图标

用户已要求将当前审阅合集接入模组。使用 `pixel-art/spell-icons-16-review-all53/selection.json` 中的版本：前二十张细化稿、第三批十张、重新绘制的第 31–40 张、第 41–50 张及最后三张。没有使用已否定的首轮高分辨率生成稿，也没有使用被后续重绘替换的第 31–40 张。

- 每张均为原生 16×16、不透明背景，直接复制原始 PNG，不作二次缩放。
- 通过法术 ID 一一对应 `assets/brutality_iron/textures/gui/spell_icons/<id>.png`，覆盖 30 个最初移植的法术和 23 个保留的新增法术。
- 删除这 53 个活动图标中遗留的 8 份 `.png.mcmeta`，避免将新静态图标按旧动画帧读取。
- 选择记录为 `spell-icon-selection.json`，JAR 内包含 `assets/brutality_iron/spell_icon_manifest.json`，逐张记录来源批次与 SHA-256。
- 修改前的源图标、动画配置、版本元数据备份在 `audit/icons-before-1.4.0/`；安装 JAR 的回滚位置见 `audit/deployment-1.4.0.json`。

与安装中的 1.3.2 比较，106 个运行类逐字节一致。法术注册、等级、属性、伤害逻辑、卷轴和法书资源均未因本次接入而改动。版本号升至 1.4.0。

构建、53 张图标的打包与 ID 对应、原生尺寸、不透明度、资源范围及 GeckoLib 资源发现检查已通过。完整验证与部署结果见 `audit/validation-1.4.0.md`。尚未用 Minecraft 客户端检查实际界面显示；安装后需由用户重启实例查看。
