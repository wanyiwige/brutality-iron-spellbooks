from pathlib import Path
import json,html
OUT=Path(__file__).resolve().parent
manifest=json.loads((OUT/'prompts.json').read_text(encoding='utf8'))
schools={'brutality_iron:nihility':'虚无','brutality_iron:celestia':'天界','brutality_iron:inferno':'炼狱','alex_caves_spellsbook:hazen_cosmic':'宇宙','alex_caves_spellsbook:hazen_shadow':'暗影','wind_spellbooks:wind':'流风','gtbcs_geomancy_plus:geo':'岩土','brutality_iron:plague':'疫病','irons_spellbooks:ender':'末影','irons_spellbooks:evocation':'唤魔','irons_spellbooks:eldritch':'邪术'}
page=['<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>Brutality 铁魔法 · 53 张法术图标</title><style>body{background:#1e2029;color:#eee;font:15px "Microsoft YaHei",sans-serif;max-width:1100px;margin:32px auto;padding:0 20px}h1{font-size:24px}h2{font-size:19px;margin-top:30px}.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(150px,1fr));gap:12px}article{background:#292c38;border-radius:7px;padding:14px;text-align:center}img{image-rendering:pixelated;vertical-align:middle}.large{width:96px;height:96px;display:block;margin:0 auto 14px}.native{width:16px;height:16px;margin-left:8px}p{margin:6px 0}.id{font:11px monospace;color:#b7bdcf;overflow-wrap:anywhere}a{color:#b9dfff}.intro{color:#bbc1cf;line-height:1.8}</style><h1>53 张法术图标</h1><p class="intro">每张原生 16×16，背景不透明。大图为整数倍最近邻放大；名称旁同时显示原始尺寸。点击大图可打开独立 PNG。</p>']
for school,label in schools.items():
    rows=[r for r in manifest if r['school']==school]
    if not rows:continue
    page.append('<h2>'+label+' · '+str(len(rows))+'</h2><div class="grid">')
    for row in rows:
        sid=html.escape(row['id']);name=html.escape(row['name'])
        page.append(f'<article><a href="native/{sid}.png"><img class="large" src="native/{sid}.png" alt="{name}"></a><p>{name}<img class="native" src="native/{sid}.png" alt="原始尺寸"></p><p class="id">{sid}</p></article>')
    page.append('</div>')
(OUT/'gallery.html').write_text(''.join(page)+'</html>',encoding='utf8')
print('Gallery written')
