"""Install the user's final 53 review variants; never use the rejected generated batch."""
from pathlib import Path
import json,shutil,hashlib,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'pixel-art/spell-icons-16-review-all53'
ASSET=ROOT/'src/main/resources/assets/brutality_iron'
active=json.loads((ROOT/'spell-inventory.json').read_text(encoding='utf8'))+json.loads((ROOT/'skill-expansion.json').read_text(encoding='utf8'))
ids={d['id'].split(':')[-1] for d in active}
assert len(ids)==53 and 'shadowflame_slash' not in ids
selection=json.loads((ART/'selection.json').read_text(encoding='utf8'))
audit=selection['icons']
assert selection['count']==len(audit)==53
assert {r['id'].split(':')[-1] for r in audit}==ids
assert {r['source_batch'] for r in audit}=={'spell-icons-16-detail-first20','spell-icons-16-detail-third10','spell-icons-16-fresh-fourth10','spell-icons-16-fresh-fifth10','spell-icons-16-fresh-last3'}
# Validate every source before changing any runtime resource.
for row in audit:
    source=ART/'native'/(row['id'].split(':')[-1]+'.png')
    assert hashlib.sha256(source.read_bytes()).hexdigest()==row['sha256']
    image=Image.open(source).convert('RGBA')
    assert image.size==(16,16) and image.getchannel('A').getextrema()==(255,255)
    assert len(image.getcolors())<=12
backup=ROOT/'audit/icons-before-1.4.0';backup.mkdir(exist_ok=True)
for relative in ['gradle.properties','src/main/resources/META-INF/mods.toml']:
    target=backup/relative
    if not target.exists():
        target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(ROOT/relative,target)
manifest=[]
removed_metadata=[]
for row in audit:
    sid=row['id'].split(':')[-1];source=ART/'native'/f'{sid}.png';target=ASSET/'textures/gui/spell_icons'/source.name
    image=Image.open(source).convert('RGBA')
    assert image.size==(16,16) and image.getchannel('A').getextrema()==(255,255)
    assert len(image.getcolors())<=12
    for old in [target,Path(str(target)+'.mcmeta')]:
        if old.exists() and not (backup/old.name).exists():shutil.copy2(old,backup/old.name)
    shutil.copy2(source,target)
    metadata=Path(str(target)+'.mcmeta')
    if metadata.exists():
        assert metadata.resolve().parent==(ASSET/'textures/gui/spell_icons').resolve()
        removed_metadata.append(metadata.name);metadata.unlink()
    manifest.append(dict(id='brutality_iron:'+sid,name=row['name'],source_batch=row['source_batch'],width=16,height=16,opaque_background=True,sha256=hashlib.sha256(target.read_bytes()).hexdigest()))
(ASSET/'spell_icon_manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
for relative in ['gradle.properties','src/main/resources/META-INF/mods.toml']:
    file=ROOT/relative;text=file.read_text(encoding='utf8').replace('mod_version=1.3.2','mod_version=1.4.0').replace('version="1.3.2"','version="1.4.0"');file.write_text(text,encoding='utf8')
(ROOT/'spell-icon-selection.json').write_text(json.dumps({'version':'1.4.0','source':str(ART.relative_to(ROOT)),'count':53,'icons':manifest},ensure_ascii=False,indent=2)+'\n',encoding='utf8')
(ROOT/'audit/icon-install-1.4.0.json').write_text(json.dumps({'count':53,'source':str(ART),'backup':str(backup),'removed_animation_metadata':removed_metadata,'runtime_resource_ids':[r['id'] for r in manifest]},ensure_ascii=False,indent=2),encoding='utf8')
print(f'Installed 53 final opaque 16x16 icons; removed {len(removed_metadata)} obsolete animation metadata files; source backups saved; version 1.4.0')
