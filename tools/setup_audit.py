from pathlib import Path
import shutil, zipfile, tomllib, json, sys, hashlib
root=Path(__file__).resolve().parents[1]; pack=root.parent
old=pack/'wans-iron-expansion/development-checks/pure-darkness-dev.10/isolated-server'
out=root/'audit/isolated-server'; (out/'mods').mkdir(parents=True,exist_ok=True)
for jar in (old/'mods').glob('*.jar'):
    if jar.name.startswith('wans_iron_expansion'): continue
    shutil.copy2(jar,out/'mods'/jar.name)
patterns=['wans_iron_expansion-*.jar','hazennstuff*.jar','bielgg_spells*.jar','*wind_spellbooks*.jar']
if '--with-roaring' in sys.argv: patterns.append('roaring*.jar')
if '--with-echo' in sys.argv: patterns.append('Echo*jar')
for pattern in patterns:
    for jar in (pack/'mods').glob(pattern): shutil.copy2(jar,out/'mods'/jar.name)
version=(root/'gradle.properties').read_text().split('mod_version=')[1].splitlines()[0].strip()
jar=root/'build/libs'/('brutality_iron_spellbooks-'+version+'.jar')
for previous in (out/'mods').glob('brutality_iron_spellbooks-*.jar'):
    if previous.name != jar.name:
        archive=root/'audit/previous-versions'; archive.mkdir(exist_ok=True)
        shutil.move(str(previous), str(archive/previous.name))
shutil.copy2(jar,out/'mods'/jar.name)
for name in ['forge_args.txt','eula.txt']:
    shutil.copy2(old/name,out/name)
audit_world='verification-'+version+'-'+hashlib.sha256(jar.read_bytes()).hexdigest()[:8]
properties=(old/'server.properties').read_text().replace('server-port=25596','server-port=25607').replace('level-name=verification-world','level-name='+audit_world)
(out/'server.properties').write_text(properties)
found=[]
for jar in (out/'mods').glob('*.jar'):
    with zipfile.ZipFile(jar) as z:
        if 'META-INF/mods.toml' in z.namelist():
            data=tomllib.loads(z.read('META-INF/mods.toml').decode('utf-8-sig',errors='replace'))
            found.extend(m['modId'] for m in data['mods'])
(root/'audit/mod-ids.json').write_text(json.dumps(found,indent=2))
print('Prepared',out,'mods',len(list((out/'mods').glob('*.jar'))))
