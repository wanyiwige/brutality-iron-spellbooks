"""Regression for GeckoLib 4.8.4's geo/ and animations/ resource discovery.

Mirrors the prefixes in the installed GeckoLibCache.loadModels/loadAnimations;
checks discovery, JSON hierarchy and clip references, not GPU rendering.
"""
from pathlib import Path
import json,zipfile,sys
ROOT=Path(__file__).resolve().parents[1]
version=(ROOT/'gradle.properties').read_text().split('mod_version=')[1].splitlines()[0].strip()
target=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'build/libs'/f'brutality_iron_spellbooks-{version}.jar'
with zipfile.ZipFile(target) as jar:
    names=set(jar.namelist())
    def discover(prefix):
        found=set()
        for n in names:
            parts=n.split('/',2)
            if len(parts)==3 and parts[0]=='assets' and parts[2].startswith(prefix+'/') and n.endswith('.json'):
                found.add(parts[1]+':'+parts[2])
        return found
    geo,animations=discover('geo'),discover('animations')
    refs=json.loads(jar.read('assets/brutality_iron/skill_visuals.json'))
    missing=[];bones=0
    for alias,v in refs.items():
        for key,discovered in [('model',geo),('animation',animations)]:
            if v[key] not in discovered:missing.append({'alias':alias,'kind':key,'resource':v[key]})
        if v['model'] in geo:
            model=json.loads(jar.read('assets/'+v['model'].replace(':','/',1)))
            geometries=model.get('minecraft:geometry',[]);assert geometries,alias
            for geometry in geometries:
                # Brutality's original Abyss projectile intentionally has no
                # geometry; its appearance is supplied by particles.
                nodes=geometry.get('bones',[]);bones+=len(nodes)
                node_names={b['name'] for b in nodes}
                for node in nodes:assert not node.get('parent') or node['parent'] in node_names,(alias,node['name'])
        if v['animation'] in animations:
            clips=json.loads(jar.read('assets/'+v['animation'].replace(':','/',1))).get('animations',{})
            assert not v['clip'] or v['clip'] in clips,(alias,v['clip'])
        assert 'assets/'+v['texture'].replace(':','/',1) in names,alias
    result={'jar':str(target),'aliases':len(refs),'missing_count':len(missing),'missing':missing,'geometry_bones_checked':bones,'gpu_rendering_tested':False}
    if '--expect-failure' in sys.argv:
        assert missing,'Expected the old broken release to fail discovery'
        print(json.dumps({'old_release_regression_reproduced':True,'aliases':len(refs),'missing_resources':len(missing)},indent=2))
    else:
        assert not missing,json.dumps(missing,indent=2)
        (ROOT/'audit/gecko-discovery-verification.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
        print(json.dumps(result,ensure_ascii=False,indent=2))
