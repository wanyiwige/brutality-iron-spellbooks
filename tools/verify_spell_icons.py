"""Validate the final icon payload, resource scope and code identity against installed 1.3.2."""
from pathlib import Path
import sys,json,zipfile,hashlib,io,tomllib
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
version=(ROOT/'gradle.properties').read_text().split('mod_version=')[1].splitlines()[0].strip()
jar=ROOT/'build/libs'/f'brutality_iron_spellbooks-{version}.jar'
baseline=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT.parent/'mods/brutality_iron_spellbooks-1.3.2.jar'
if not baseline.exists() and (ROOT/'audit/deployment-1.4.0.json').exists():
    baseline=Path(json.loads((ROOT/'audit/deployment-1.4.0.json').read_text(encoding='utf8'))['backup'])
assert hashlib.sha256(baseline.read_bytes()).hexdigest()=='9376cfac3510710ef386b91102ec58aabaf4ac3752203d30708dd7d0f1ace452'
selection=json.loads((ROOT/'pixel-art/spell-icons-16-review-all53/selection.json').read_text(encoding='utf8'))['icons']
ids={r['id'] for r in selection};assert len(ids)==len(selection)==53
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(baseline) as old:
    assert z.testzip() is None
    names={n for n in z.namelist() if not n.endswith('/')};previous={n for n in old.namelist() if not n.endswith('/')}
    packed=json.loads(z.read('assets/brutality_iron/spell_icon_manifest.json'))
    assert {r['id'] for r in packed}==ids and len(packed)==53
    definitions=json.loads(z.read('assets/brutality_iron/skill_expansion.json'))
    legacy=json.loads((ROOT/'spell-inventory.json').read_text(encoding='utf8'))
    assert {r['id'] for r in legacy}|{'brutality_iron:'+r['id'] for r in definitions}==ids
    assert 'brutality_iron:shadowflame_slash' not in ids
    textures=set();metas=set();checks=[]
    for row in selection:
        sid=row['id'].split(':')[1];resource=f'assets/brutality_iron/textures/gui/spell_icons/{sid}.png'
        textures.add(resource);metas.add(resource+'.mcmeta')
        digest=hashlib.sha256(z.read(resource)).hexdigest();assert digest==row['sha256']
        assert next(r for r in packed if r['id']==row['id'])['sha256']==digest
        image=Image.open(io.BytesIO(z.read(resource))).convert('RGBA')
        assert image.size==(16,16) and image.getchannel('A').getextrema()==(255,255)
        assert len(image.getcolors())<=12 and resource+'.mcmeta' not in names
        checks.append(dict(id=row['id'],size=[16,16],opaque=True,sha256=digest,source_batch=row['source_batch']))
    classes={n for n in names if n.endswith('.class')}
    assert classes=={n for n in previous if n.endswith('.class')}
    assert all(z.read(n)==old.read(n) for n in classes),'Gameplay bytecode changed'
    allowed=textures|metas|{'META-INF/mods.toml','THIRD_PARTY_NOTICES.md','assets/brutality_iron/spell_icon_manifest.json'}
    added=names-previous;removed=previous-names
    changed={n for n in names&previous if z.read(n)!=old.read(n)}
    assert added<=allowed and removed<=metas and changed<=allowed,(added,removed,changed-allowed)
    old_meta=old.read('META-INF/mods.toml').decode('utf8');new_meta=z.read('META-INF/mods.toml').decode('utf8')
    assert old_meta.replace('version="1.3.2"',f'version="{version}"')==new_meta,'Unexpected dependency/metadata changes'
    assert tomllib.loads(new_meta)['mods'][0]['version']==version
    for n in names:
        if n.endswith('.json') or n.endswith('.mcmeta'):json.loads(z.read(n))
result=dict(version=version,jar=str(jar),bytes=jar.stat().st_size,sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),baseline=str(baseline),icons=53,all_native_16x16=True,all_opaque=True,obsolete_animation_metadata_removed=len(removed&metas),gameplay_classes_unchanged=len(classes),changes_limited_to_icons_version_and_credits=True,changed_files=sorted(changed),added_files=sorted(added),removed_files=sorted(removed),icon_checks=checks,client_rendering_verified=False)
(ROOT/'audit/icon-release-verification.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print(json.dumps({k:v for k,v in result.items() if k not in ['icon_checks','changed_files','added_files','removed_files']},ensure_ascii=False,indent=2))
