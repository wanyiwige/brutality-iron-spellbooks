from pathlib import Path
import hashlib, json, zipfile
root=Path(__file__).resolve().parents[1]
version=(root/'gradle.properties').read_text().split('mod_version=')[1].splitlines()[0].strip()
jar=root/'build/libs'/('brutality_iron_spellbooks-'+version+'.jar')
tested=root/'audit/isolated-server/mods'/jar.name
log=(root/'audit/isolated-server/logs/latest.log').read_text(encoding='utf-8',errors='replace')
report={'jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest()}
with zipfile.ZipFile(jar) as z, zipfile.ZipFile(tested) as test:
    assert z.testzip() is None
    names=z.namelist()
    for name in names:
        if name.endswith('.json') or name.endswith('.mcmeta'): json.loads(z.read(name))
    classes=[n for n in names if n.endswith('.class')]
    assert all(z.read(n)==test.read(n) for n in classes), 'runtime audit bytecode differs'
    report['tested_bytecode_matches_release']=True
    spells=json.loads((root/'spell-inventory.json').read_text(encoding='utf8'))
    assert len(spells)==30
    for spell in spells:
        assert 'assets/brutality_iron/textures/gui/spell_icons/'+spell['id'].split(':')[1]+'.png' in names
    report['spells']=len(spells)
    mapping={'nihility':('虚无','scroll_nihility'),'celestia':('天界','scroll_celestia'),'inferno':('炼狱','scroll_inferno')}
    selected_scrolls=json.loads((root/'scroll-selection.json').read_text(encoding='utf8')) if (root/'scroll-selection.json').exists() else {}
    for school,(folder,stem) in mapping.items():
        for suffix in ['.png','.png.mcmeta']:
            approved=root.parent/'generated-art/brutality-iron-scrolls/final'/folder/(stem+suffix)
            if school in selected_scrolls: approved=Path(str((root/selected_scrolls[school]['source']).resolve())+('' if suffix=='.png' else '.mcmeta'))
            assert z.read('assets/brutality_iron/textures/item/scroll_'+school+suffix)==approved.read_bytes()
    report['approved_scrolls_preserved']=3
    assert 'Brutality：铁魔法法术' in z.read('assets/brutality_iron/lang/zh_cn.json').decode('utf8')
    assert not any(n.endswith('.jar') for n in names)
    if (root/'book-assets.json').exists():
        assets=json.loads((root/'book-assets.json').read_text(encoding='utf8'))
        assert len(assets)==6
        for book in assets:
            name=book['id'].split(':')[1]
            texture='assets/brutality_iron/textures/item/'+name+'_inventory.png'
            assert hashlib.sha256(z.read(texture)).hexdigest()==book['texture_sha256']
            assert z.read(texture+'.mcmeta')==Path(book['selected_source']+'.mcmeta').read_bytes()
            model=json.loads(z.read('assets/brutality_iron/models/item/'+name+'.json'))
            assert model['loader']=='forge:separate_transforms'
            assert model['base']['parent']==book['original_model']
            assert model['perspectives']['gui']['parent']=='minecraft:item/generated'
        assert 'assets/brutality_iron/models/item/umbral_tome.json' not in names
        runtime_books=json.loads((root/'audit/isolated-server/book-report.json').read_text(encoding='utf8'))
        assert len(runtime_books)==4 and log.count('BRUTALITY_IRON_BOOK_PASS')==4
        report['books']=runtime_books
        report['book_runtime_coverage']='4 books; Bielgg Shadow and Echo books require client tests because upstream Roaring and Echo fail on dedicated server'
        report['book_gui_model_handheld_split_verified']=True
        report['selected_nihility_scroll_sha256']=hashlib.sha256(z.read('assets/brutality_iron/textures/item/scroll_nihility.png')).hexdigest()
        recipes=json.loads((root/'book-recipes.json').read_text(encoding='utf8'))
        assert len(recipes)==6
        for spec in recipes:
            name=spec['recipe'].split(':')[1]
            recipe=json.loads(z.read('data/brutality_iron/recipes/'+name+'.json'))
            assert recipe['pattern']==['IFI','CBC','IFI']
            assert recipe['result']=={'item':spec['recipe'],'count':1}
            assert recipe['key']['B']['item']=='minecraft:book'
            for k,v in [('I','primary'),('F','focus'),('C','binding')]:assert recipe['key'][k]['item']==spec[v]
            assert recipe['conditions']==[{'type':'forge:item_exists','item':spec['recipe']}]
        assert 'BRUTALITY_IRON_BOOK_RECIPES_PASS crafted=4 optional-skipped=2 negative=8 materials=24' in log
        report['book_recipes']='six recipes packaged; four native crafting/blank-container/unlock checks, eight negative cases, two optional skips, all 24 ingredient references registered'
report['runtime_callback_passes']=log.count('BRUTALITY_IRON_CAST_CALLBACK_PASS')
assert report['runtime_callback_passes']==30
progression=json.loads((root/'audit/isolated-server/progression-report.json').read_text(encoding='utf8'))
expected_levels=sum(s['max_level'] for s in spells)
assert sum(s['max_level'] for s in progression)==expected_levels
from collections import Counter
assert Counter(s['max_level'] for s in progression)=={3:5,5:8,8:8,10:9}
for entry in progression:
    assert entry['rarities'][-2:]==['LEGENDARY','LEGENDARY']
    assert len(entry['rarities'])==entry['max_level']
    inventory_entry=next(s for s in spells if s['id']==entry['id'])
    assert inventory_entry['max_level']==entry['max_level']
    assert len(entry['rarities'])==entry['max_level']
assert f'BRUTALITY_IRON_AUDIT_PASS spells=30 scrolls={expected_levels} server-ticks=240 beam-cleanup=pass' in log
assert 'BRUTALITY_IRON_AUDIT_FAILED' not in log
report['level_caps']=dict(Counter(s['max_level'] for s in progression))
report['runtime_checks']=f'30 cast callbacks, {expected_levels} scrolls, 150 ink/focus crafting combinations, 240 server ticks, beam cleanup, all final two ranks legendary'
assert 'crafting=150' in log
rarity_names={'COMMON':'普通','UNCOMMON':'罕见','RARE':'稀有','EPIC':'史诗','LEGENDARY':'传说'}
lines=['# 法术等级与稀有度分布（1.0.1）','',
       '基于隔离 Forge 服务端实际注册与锻造测试导出。使用铁魔法默认稀有度权重，末两级固定为传说。',
       '', '| 最高等级 | 法术数 | 各等级稀有度（从 I 级开始） |', '|---|---:|---|']
for cap in [3,5,8,10]:
    rows=[s for s in progression if s['max_level']==cap]
    assert all(s['rarities']==rows[0]['rarities'] for s in rows)
    lines.append(f"| {cap} | {len(rows)} | {' → '.join(rarity_names[r] for r in rows[0]['rarities'])} |")
lines+=['',f'共 30 个法术、{expected_levels} 个有效等级卷轴。创造标签页按各法术实际最高等级列出。','',
        '| 法术 | ID | 最高等级 | 各等级稀有度 |','|---|---|---:|---|']
for cap in [3,5,8,10]:
    for row in progression:
        if row['max_level']!=cap: continue
        entry=next(s for s in spells if s['id']==row['id'])
        lines.append(f"| {entry['name']} | `{row['id']}` | {cap} | {' / '.join(rarity_names[r] for r in row['rarities'])} |")
lines+=['','服务器端已有的铁魔法法术配置可以覆盖默认值；修改过稀有度权重时，前面的等级随本体算法变化，末两级仍为传说。',
        '锻造台使用对应稀有度出现的最低等级；不存在的稀有度不能锻造该法术。',
        '通过：30 个法术逐级检查、209 个卷轴保存检查、150 组墨水与标志物锻造检查、30 个施法回调和 240 个服务端 tick。',
        '尚未验证客户端显示、实际按键施法、多人同步与战斗平衡。','']
(root/'spell-progression.md').write_text('\n'.join(lines),encoding='utf8')
(root/'audit/release-verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print(json.dumps(report,ensure_ascii=False,indent=2))
