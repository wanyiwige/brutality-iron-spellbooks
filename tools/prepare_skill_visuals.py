"""Copy only the referenced original geometry, textures and clips into local assets."""
from pathlib import Path
import zipfile,json,hashlib
ROOT=Path(__file__).resolve().parents[1];ASSET=ROOT/'src/main/resources/assets/brutality_iron'
sources={mod:zipfile.ZipFile(next((ROOT.parent/'mods').glob(pattern))) for mod,pattern in [('brutality','brutality-0.8.5-*.jar'),('terramity','terramity-0.9.8-*.jar'),('lethality','lethality*.jar'),('mortality','mortality*.jar')]}
spec={}
for alias in ['black_hole','cruel_sun','rhongomyniad_ray','bloodslash','shadowflame_slash','exoblade_beam','star','abyss_projectile','ice_cube']:
    spec[alias]=('brutality','geo/entity/'+alias+'.geo.json','textures/entity/'+alias+'.png','animations/entity/'+alias+'.animation.json')
for alias in ['tg_hand','holy_beam','stygian_soul','spirit_bomb','decoy','conjurling','dungeon_effigy']:
    texture='trial_guardian' if alias=='tg_hand' else alias
    spec[alias]=('terramity','geo/'+alias+'.geo.json','textures/entities/'+texture+'.png','animations/'+alias+'.animation.json')
for alias in ['acidic_slash','forbidden_scythe','devils_scythe','devils_pitchfork','gael_skull','vehemence_bolt','hook_model','starlight_stab']:
    texture={'hook_model':'hook','starlight_stab':'starlight_stab_0'}.get(alias,alias)
    anim={'gael_skull':'gael_skull_idle','devils_scythe':'devils_scythe_idle','forbidden_scythe':'forbidden_scythe_idle'}.get(alias,alias)
    spec[alias]=('lethality','geo/'+alias+'.geo.json','textures/entity/'+texture+'.png','animations/'+anim+'.animation.json')
spec['meteor']=('mortality','geo/meteor.geo.json','textures/entities/meteortexture.png','animations/meteor.animation.json')
spec['fire_spirit']=('mortality','geo/firespirit.geo.json','textures/entities/firespirittexture.png','animations/firespirit.animation.json')
result={};manifest=[]
previous=json.loads((ROOT/'skill-visual-assets.json').read_text()) if (ROOT/'skill-visual-assets.json').exists() else []
empty=ASSET/'animations/ported/empty.animation.json';empty.parent.mkdir(parents=True,exist_ok=True);empty.write_text('{"format_version":"1.8.0","animations":{}}',encoding='utf8')
for alias,(mod,geo,tex,anim) in spec.items():
    jar=sources[mod];names=jar.namelist();entry={}
    for key,path in [('model',geo),('texture',tex),('animation',anim)]:
        source='assets/'+mod+'/'+path
        if key=='animation' and source not in names:
            entry[key]='brutality_iron:animations/ported/empty.animation.json';entry['clip']='';continue
        assert source in names,(alias,source)
        # GeckoLib scans only the top-level geo/ and animations/ directories.
        # Finding a JSON anywhere in the JAR does not make it discoverable.
        category,tail=path.split('/',1)
        relative=category+'/ported/'+mod+'/'+tail;dest=ASSET/relative;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(jar.read(source));entry[key]='brutality_iron:'+relative
        manifest.append({'source_mod':mod,'source':source,'target':'assets/brutality_iron/'+relative,'sha256':hashlib.sha256(jar.read(source)).hexdigest()})
        if key=='texture' and source+'.mcmeta' in names:Path(str(dest)+'.mcmeta').write_bytes(jar.read(source+'.mcmeta'))
        if key=='animation':
            clips=json.loads(jar.read(source)).get('animations',{});entry['clip']=next((c for c in clips if 'idle' in c.lower()),next(iter(clips),''))
    result[alias]=entry
(ASSET/'skill_visuals.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf8')
(ROOT/'skill-visual-assets.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf8')
current={entry['target'] for entry in manifest}
for entry in previous:
    old=(ROOT/'src/main/resources'/entry['target']).resolve()
    if entry['target'] not in current and old.is_relative_to((ASSET/'ported').resolve()) and old.exists() and hashlib.sha256(old.read_bytes()).hexdigest()==entry['sha256']:
        old.unlink()
        if Path(str(old)+'.mcmeta').exists():Path(str(old)+'.mcmeta').unlink()
old_empty=ASSET/'ported/empty.animation.json'
if old_empty.exists() and json.loads(old_empty.read_text()).get('animations')=={}:old_empty.unlink()
# Status icons reuse the approved plague material or original status art.
for effect in ['plague','corrosion','shattered_core','vital_covenant','brimstone_pact','stillness_ward']:
    mod='mortality' if effect in ['plague','shattered_core','vital_covenant'] else 'lethality'
    jar=sources[mod];paths=[n for n in jar.namelist() if '/textures/mob_effect/' in n and n.endswith('.png') and effect in n]
    if not paths:paths=[n for n in jar.namelist() if '/textures/mob_effect/' in n and n.endswith('.png')]
    if paths:
        dest=ASSET/'textures/mob_effect'/(effect+'.png');dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(jar.read(paths[0]))
print('Prepared',len(result),'source-model variants,',len(manifest),'copied resources')
