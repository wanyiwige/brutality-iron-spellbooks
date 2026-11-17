"""Validate the new skills in addition to the legacy release checks."""
from pathlib import Path
import hashlib,json,zipfile,re
from collections import Counter
root=Path(__file__).resolve().parents[1]
version=(root/'gradle.properties').read_text().split('mod_version=')[1].splitlines()[0].strip()
jar=root/'build/libs'/f'brutality_iron_spellbooks-{version}.jar'
tested=root/'audit/isolated-server/mods'/jar.name
log=(root/'audit/isolated-server/logs/latest.log').read_text(encoding='utf8',errors='replace')
with zipfile.ZipFile(jar) as release_payload,zipfile.ZipFile(tested) as tested_payload:
    assert set(release_payload.namelist())==set(tested_payload.namelist()),'Archive entries differ from the tested payload'
    changed=[n for n in release_payload.namelist() if release_payload.read(n)!=tested_payload.read(n)]
    # Updating credits does not warrant repeating combat fixtures. All code,
    # metadata and assets must still be byte-identical to the server-tested JAR.
    assert set(changed)<={'THIRD_PARTY_NOTICES.md'},('Untested payload changes',changed)
definitions=json.loads((root/'skill-expansion.json').read_text(encoding='utf8'))
review=json.loads((root/'candidate-school-assignments.json').read_text(encoding='utf8'))+json.loads((root/'lethality-mortality-spell-candidates.json').read_text(encoding='utf8'))
revision=json.loads((root/'audit/revision-requirements.json').read_text(encoding='utf8'))
round2=json.loads((root/'audit/revision-round2-requirements.json').read_text(encoding='utf8'))['rows']
removed={s['id'] for s in revision+round2 if s['delete']}|set(json.loads((root/'additional-spell-exclusions.json').read_text(encoding='utf8')))
retained={s['id'] for s in revision if not s['delete']}-removed
assert len(definitions)==23 and len(removed)==37 and len(review)==60
assert {s['id'] for s in definitions}==retained
assert {s['name'] for s in definitions}<={s['name'] for s in review}
legacy=json.loads((root/'spell-inventory.json').read_text(encoding='utf8'))
assert len({s['id'] for s in definitions}|{s['id'].split(':')[1] for s in legacy})==53
allowed={'brutality_iron:nihility','brutality_iron:celestia','brutality_iron:inferno','brutality_iron:plague','wind_spellbooks:wind','gtbcs_geomancy_plus:geo','irons_spellbooks:ender','irons_spellbooks:nature','irons_spellbooks:evocation','irons_spellbooks:eldritch'}
assert all(s['school'] in allowed for s in definitions)
report=json.loads((root/'audit/isolated-server/skill-report.json').read_text(encoding='utf8'))
assert len(report)==23
caps=Counter()
for row in report:
    d=next(s for s in definitions if 'brutality_iron:'+s['id']==row['id'])
    assert row['max_level']==d['max_level'] and row['school']==d['school']
    assert row['rarities'][-2:]==['LEGENDARY','LEGENDARY']
    assert d['max_level'] in [3,5,8,10];caps[d['max_level']]+=1
levels=sum(s['max_level'] for s in definitions)
assert f'BRUTALITY_IRON_SKILL_DATA_PASS spells=23 scrolls={levels} forging=115' in log
assert f'BRUTALITY_IRON_SKILL_AUDIT_PASS spells=23 positive-casts=23 revival-guard=0 scrolls={levels} forging=115 lifecycle=pass' in log
assert log.count('BRUTALITY_IRON_SKILL_CAST_PASS ')==23
assert 'BRUTALITY_IRON_REVISION_AUDIT_PASS direction-cases=80' in log
assert 'BRUTALITY_IRON_SKILL_AUDIT_FAILED' not in log and 'BRUTALITY_IRON_AUDIT_FAILED' not in log
with zipfile.ZipFile(jar) as z:
    assert z.testzip() is None
    assert json.loads(z.read('assets/brutality_iron/skill_expansion.json'))==definitions
    assert next(s for s in definitions if s['id']=='void_exchange')['school']=='irons_spellbooks:ender'
    zh=json.loads(z.read('assets/brutality_iron/lang/zh_cn.json'))
    assert all('spell.brutality_iron.'+sid not in zh for sid in removed)
    assets2=json.loads((root/'revision-assets.json').read_text(encoding='utf8'))
    for asset in assets2:assert hashlib.sha256(z.read(asset['target'])).hexdigest()==asset['sha256'],asset['target']
    for name,frames,size in [('circle_crystal',32,64),('circle_nature',32,64),('circle_eldritch',32,64),('plague_cloud',10,16)]:
        from PIL import Image
        import io
        image=Image.open(io.BytesIO(z.read(f'assets/brutality_iron/textures/revision/{name}_frames.png')))
        assert image.size==(size,size*frames)
    for alias in ['black_hole','cruel_sun']:
        assert f'assets/brutality_iron/textures/ported/brutality/entity/{alias}_fbnd.png' in z.namelist()
    for d in definitions:
        assert f"assets/brutality_iron/textures/gui/spell_icons/{d['id']}.png" in z.namelist()
    assets=json.loads((root/'skill-visual-assets.json').read_text())
    for asset in assets:assert hashlib.sha256(z.read(asset['target'])).hexdigest()==asset['sha256']
    visuals=json.loads(z.read('assets/brutality_iron/skill_visuals.json'))
    for alias,v in visuals.items():
        assert v['model'].split(':',1)[1].startswith('geo/'),(alias,'GeckoLib cannot discover model')
        assert v['animation'].split(':',1)[1].startswith('animations/'),(alias,'GeckoLib cannot discover animation')
        assert v['texture'].split(':',1)[1].startswith('textures/'),(alias,'texture path convention')
        for key in ['model','texture','animation']:
            location='assets/'+v[key].replace(':','/',1);assert location in z.namelist(),(alias,key)
    selected=json.loads((root/'scroll-selection.json').read_text(encoding='utf8'))['plague']
    assert hashlib.sha256(z.read('assets/brutality_iron/textures/item/scroll_plague.png')).hexdigest()==selected['sha256']
    meta=json.loads(z.read('assets/brutality_iron/textures/item/scroll_plague.png.mcmeta'))
    assert meta['animation']=={'frametime':2,'interpolate':False}
baseline=root/'audit/revision-round2-before'
for filename in ['spell-revision-checklist-1.3.0.md']:
    assert (root/filename).read_bytes()==(baseline/filename).read_bytes(),'User checklist was modified'
for resource in ['geo/ported/terramity/holy_beam.geo.json','animations/ported/terramity/holy_beam.animation.json','textures/ported/terramity/entities/holy_beam.png']:
    path='src/main/resources/assets/brutality_iron/'+resource
    assert (root/path).read_bytes()==(baseline/path).read_bytes(),'Original holy beam changed'
for token in ['effect-recasts=pass','ground-columns=pass','continuous-bomb=pass','interpolation=pass']:
    assert token in log,token
result={'version':version,'jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'server_tested_jar_sha256':hashlib.sha256(tested.read_bytes()).hexdigest(),'tested_payload_matches':True,'documentation_only_changes':changed,'legacy_spells':30,'retained_expansion_spells':23,'removed_expansion_spells':37,'total_spells':53,'added_level_scrolls':levels,'total_level_scrolls':levels+sum(s['max_level'] for s in legacy),'new_level_caps':dict(caps),'new_school_distribution':dict(Counter(s['school'] for s in definitions)),'native_forging_checks':115,'native_cast_fixtures':23,'direction_cases':80,'native_summon_cancel':True,'source_model_variants':len(visuals),'source_resources':len(assets),'revision_resources':len(assets2),'plague_scroll_sha256':selected['sha256'],'client_rendering_verified':False,'multiplayer_verified':False,'balance_verified':False}
(root/'audit/skill-release-verification.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print(json.dumps(result,ensure_ascii=False,indent=2))
