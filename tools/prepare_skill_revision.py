"""Apply the user's 2026-11-14 checklist and selected low-pixel resources.

Kept separate from the original candidate inventory so re-generation cannot
silently re-enable rejected spells. The user's Markdown is never rewritten.
"""
from pathlib import Path
import json,re,hashlib,shutil,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/'src/main/resources/assets/brutality_iron'
DOC=(ROOT/'spell-revision-checklist.md').read_text(encoding='utf8')
rows=[]
for block in re.split(r'(?m)^### ',DOC)[1:]:
    sid=re.search(r'brutality_iron:([a-z_]+)',block).group(1)
    request=block.split('你的修改要求：',1)[1].split('\n## ',1)[0].strip('* \n')
    rows.append(dict(number=int(block[:2]),id=sid,requirement=request,delete=request.startswith('删')))
assert len(rows)==60 and sum(v['delete'] for v in rows)==33
removed={r['id'] for r in rows if r['delete']}
base=ROOT/'audit/revision-20261114-original/skill-expansion.json'
definitions=json.loads(base.read_text(encoding='utf8'))
definitions=[d for d in definitions if d['id'] not in removed]
updates={
 'void_exchange':('与锁定的敌人交换位置，片刻后安全换回。','irons_spellbooks:ender'),
 'stellar_marks':('朝准星散射四颗星体，命中后附着；再次施放引爆自己留下的星印。',None),
 'starlight_flurry':('从视线周围同时放出五至六道多色星刺，短距离刺击并穿透敌人。',None),
 'triple_judgment':('在身前布下三处圣光阵，沿施法时准星方向依次释放平行圣光。',None),
 'triple_profane_beam':('在身前布下三处邪术阵，沿施法时准星方向依次释放平行邪光。',None),
 'guardian_hand':('朝准星方向召出守护之手，旋转发光构件并沿施法时方向扫击。',None),
 'stygian_souls':('从身后不同位置先后放出更多游魂，展开后追踪可见敌人，命中施加虚弱。',None),
 'infernal_spirits':('召出火焰灵体协助作战；再次施放取消本次召唤。',None),
 'stone_retinue':('召出石像卫队协助作战；再次施放取消本次召唤。',None),
 'hellrok_slam':('蓄势后引发紫红火焰裂纹，灼烧并震起同层地面的敌人。',None)}
for d in definitions:
    if d['id'] in updates:
        description,school=updates[d['id']];d['description']=description
        if school:d['school']=school
assert len(definitions)==27
def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
write(ROOT/'skill-expansion.json',definitions);write(ASSET/'skill_expansion.json',definitions)
write(ROOT/'audit/revision-requirements.json',rows)
for lang in ['zh_cn','en_us']:
    p=ASSET/'lang'/f'{lang}.json';data=json.loads(p.read_text(encoding='utf8'))
    for sid in removed:
        for suffix in ['', '.guide']:data.pop('spell.brutality_iron.'+sid+suffix,None)
    for d in definitions:
        if lang=='zh_cn':data['spell.brutality_iron.'+d['id']+'.guide']=d['description']
    write(p,data)
art=ROOT/'pixel-art/spell-revision-v2/native';manifest=[]
for p in art.glob('*.png'):
    dest=ASSET/'textures/revision'/p.name;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,dest)
    manifest.append(dict(source=str(p.relative_to(ROOT)),target=str(dest.relative_to(ROOT/'src/main/resources')).replace('\\','/'),sha256=hashlib.sha256(p.read_bytes()).hexdigest()))
for p in art.glob('*_animation.json'):shutil.copy2(p,ASSET/'textures/revision'/p.name)
# Restore full original glow layers, not a repaint of the source models.
with zipfile.ZipFile(next((ROOT.parent/'mods').glob('brutality-*.jar'))) as jar:
    for alias in ['black_hole','cruel_sun']:
        for suffix in ['', '_fb', '_fba', '_fbnd']:
            source=f'assets/brutality/textures/entity/{alias}{suffix}.png'
            if source not in jar.namelist():continue
            target=ASSET/'textures/ported/brutality/entity'/Path(source).name;target.write_bytes(jar.read(source))
            if source+'.mcmeta' in jar.namelist():Path(str(target)+'.mcmeta').write_bytes(jar.read(source+'.mcmeta'))
            manifest.append(dict(source=source,target='assets/brutality_iron/'+str(target.relative_to(ASSET)).replace('\\','/'),sha256=hashlib.sha256(target.read_bytes()).hexdigest()))
# One quad uses one sprite: remove UV-atlas duplication from weapon projectiles.
with zipfile.ZipFile(next((ROOT.parent/'mods').glob('lethality*.jar'))) as jar:
    for alias in ['forbidden_scythe','devils_scythe','devils_pitchfork']+[f'starlight_stab_{i}' for i in range(7)]:
        import io
        image=Image.open(io.BytesIO(jar.read(f'assets/lethality/textures/entity/{alias}.png'))).convert('RGBA')
        # Each half is one side of the source thin cuboid.
        sprite=image.crop((0,0,image.width//2,image.height))
        dest=ASSET/'textures/revision'/(alias+'.png');sprite.save(dest)
        manifest.append(dict(source=f'lethality:{alias}:left_uv_half',target='assets/brutality_iron/textures/revision/'+dest.name,sha256=hashlib.sha256(dest.read_bytes()).hexdigest()))
with zipfile.ZipFile(next((ROOT.parent/'mods').glob('brutality-*.jar'))) as jar:
    dest=ASSET/'textures/revision/exoblade_beam.png';dest.write_bytes(jar.read('assets/brutality/textures/entity/exoblade_beam.png'))
    manifest.append(dict(source='brutality:exoblade_beam',target='assets/brutality_iron/textures/revision/'+dest.name,sha256=hashlib.sha256(dest.read_bytes()).hexdigest()))
write(ROOT/'revision-assets.json',manifest)
write(ASSET/'skill_revision.json',{'removed':sorted(removed),'retained':[d['id'] for d in definitions],'flat_weapon_layer_count':1,'art_version':'spell-revision-v2'})
print('Revision prepared: retained=27 removed=33 assets='+str(len(manifest)))
