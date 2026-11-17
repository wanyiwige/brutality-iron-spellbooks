"""Apply the second hand-edited checklist, preserving both review documents."""
from pathlib import Path
import json,re,shutil,hashlib,copy
ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/'src/main/resources/assets/brutality_iron'
def write(p,x):
    p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(json.dumps(x,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
document=ROOT/'spell-revision-checklist-1.3.0.md'
rows=[]
for block in re.split(r'(?m)^### ',document.read_text(encoding='utf8'))[1:]:
    sid=re.search(r'brutality_iron:([a-z_]+)',block).group(1)
    request=block.split('本轮修改要求：',1)[1].split('\n## ',1)[0].strip('* \r\n')
    rows.append(dict(number=int(block[:2]),id=sid,requirement=request,delete=request.startswith('删')))
removed={r['id'] for r in rows if r['delete']}
assert len(rows)==27 and removed=={'hellrok_slam','corrosive_claws','gaia_bloom'}
removed.update(json.loads((ROOT/'additional-spell-exclusions.json').read_text(encoding='utf8')))
write(ROOT/'audit/revision-round2-requirements.json',dict(checklist_sha256=hashlib.sha256(document.read_bytes()).hexdigest(),rows=rows))
baseline=ROOT/'audit/revision-round2-before'
definitions=[d for d in json.loads((baseline/'skill-expansion.json').read_text(encoding='utf8')) if d['id'] not in removed]
updates={
    'event_horizon':'召出跟随自身的黑洞，牵引并持续伤害附近敌人；二段施法取消黑洞。',
    'cruel_sun':'头顶升起微型太阳，每秒对附近敌人造成受法强加成的天界法术伤害；二段施法取消太阳。',
    'triple_judgment':'在身前地面由近到远依次展开三处法阵，再从上方降下原版立体圣光柱。',
    'triple_profane_beam':'在身前地面由近到远依次展开三处邪术阵，再从上方降下紫黑立体邪光柱。',
    'stygian_souls':'所有游魂同时在身后列阵，短暂蓄势后依次发射，追踪可见敌人，命中施加虚弱。',
    'crystal_sanctuary':'在稍高于地面的较大范围内布置水晶法阵，为自身、队友与所属召唤物提供再生。',
    'pestilent_mist':'在瞄准位置生成持续翻涌的立体疫雾区域，感染并伤害其中的敌人。'}
for d in definitions:
    if d['id'] in updates:d['description']=updates[d['id']]
assert len(definitions)==23
write(ROOT/'skill-expansion.json',definitions);write(ASSET/'skill_expansion.json',definitions)
for lang in ['zh_cn','en_us']:
    p=ASSET/'lang'/f'{lang}.json';data=json.loads(p.read_text(encoding='utf8'))
    for sid in removed:
        for suffix in ['', '.guide']:data.pop('spell.brutality_iron.'+sid+suffix,None)
    if lang=='zh_cn':
        for sid,desc in updates.items():data['spell.brutality_iron.'+sid+'.guide']=desc
    else:
        descriptions={
            'event_horizon':'Summon a following black hole that pulls and damages nearby enemies. Recast to dismiss it.',
            'cruel_sun':'Summon a sun above you that deals spell-power-scaled Celestia damage each second. Recast to dismiss it.',
            'triple_judgment':'Unfold three ground circles ahead, from near to far, then bring down the original three-dimensional holy columns.',
            'triple_profane_beam':'Unfold three eldritch ground circles ahead, from near to far, then bring down violet three-dimensional columns.',
            'stygian_souls':'Form all souls behind you at once, then launch them one by one at visible enemies, applying Weakness.',
            'crystal_sanctuary':'Place an enlarged crystal circle slightly above the ground, granting Regeneration to you and your allies.',
            'pestilent_mist':'Create a continuously billowing volume of plague mist at the aimed location, infecting and damaging enemies inside.'}
        for sid,desc in descriptions.items():data['spell.brutality_iron.'+sid+'.guide']=desc
    write(p,data)
manifest=json.loads((ASSET/'skill_revision.json').read_text(encoding='utf8'))
manifest['removed']=sorted(set(manifest['removed'])|removed)
manifest['retained']=[d['id'] for d in definitions]
manifest['revision_round']=2
write(ASSET/'skill_revision.json',manifest)
# Original holy geometry/UV/animation remain byte-for-byte unchanged.
catalog=json.loads((ASSET/'skill_visuals.json').read_text())
catalog['profane_beam']=dict(catalog['holy_beam'],texture='brutality_iron:textures/revision/profane_beam.png')
shutil.copy2(ROOT/'pixel-art/triple-profane-beam/triple-profane-beam-64x80.png',ASSET/'textures/revision/profane_beam.png')
# The source charge animation grows from zero and shifts by over 100 texels.
# Use only its rotation tracks for a stable, shared charge-to-flight visual.
animation=copy.deepcopy(json.loads((ASSET/'animations/ported/terramity/spirit_bomb.animation.json').read_text()))
animation['animations']['idle']['bones']['wholething']={'scale':{'vector':[3,3,3]},'position':{'vector':[0,-14,0]}}
write(ASSET/'animations/revision/spirit_bomb_flight.animation.json',animation)
catalog['spirit_bomb_flight']=dict(catalog['spirit_bomb'],animation='brutality_iron:animations/revision/spirit_bomb_flight.animation.json')
write(ASSET/'skill_visuals.json',catalog)
for name in ['gradle.properties','src/main/resources/META-INF/mods.toml']:
    p=ROOT/name;t=p.read_text(encoding='utf8').replace('mod_version=1.3.0','mod_version=1.3.1').replace('version="1.3.0"','version="1.3.1"').replace('57 native Iron spellbook spells, including 27 retained','54 native Iron spellbook spells, including 24 retained');p.write_text(t,encoding='utf8')
print(f'Round 2 prepared: {len(definitions)} retained, {len(manifest["removed"])} cumulative removed, {30+len(definitions)} total spells; original holy beam preserved')
