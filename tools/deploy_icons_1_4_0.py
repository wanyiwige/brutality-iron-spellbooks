"""Deploy the exact tested 1.4.0 icon release with a verified 1.3.2 rollback JAR."""
from pathlib import Path
import json,hashlib,datetime,shutil,zipfile,re,tomllib
ROOT=Path(__file__).resolve().parents[1]
INSTANCE=ROOT.parent;VERSION='1.4.0'
SOURCE=ROOT/'build/libs'/f'brutality_iron_spellbooks-{VERSION}.jar'
OLD_SHA='9376cfac3510710ef386b91102ec58aabaf4ac3752203d30708dd7d0f1ace452'
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def active():
    found=[]
    for path in (INSTANCE/'mods').glob('*.jar'):
        with zipfile.ZipFile(path) as z:
            if 'META-INF/mods.toml' not in z.namelist():continue
            raw=z.read('META-INF/mods.toml')
            if re.search(rb'(?m)^\s*modId\s*=\s*[\"\x27]brutality_iron[\"\x27]',raw):
                if any(m.get('modId')=='brutality_iron' for m in tomllib.loads(raw.decode('utf8')).get('mods',[])):found.append(path)
    return found
digest=sha(SOURCE)
for name in ['release-verification.json','skill-release-verification.json','icon-release-verification.json']:
    report=json.loads((ROOT/'audit'/name).read_text(encoding='utf8'))
    assert report['sha256']==digest,(name,'does not cover the release JAR')
icons=json.loads((ROOT/'audit/icon-release-verification.json').read_text(encoding='utf8'))
assert icons['icons']==53 and icons['all_native_16x16'] and icons['all_opaque'] and icons['changes_limited_to_icons_version_and_credits']
discovery=json.loads((ROOT/'audit/gecko-discovery-verification.json').read_text(encoding='utf8'))
assert discovery['missing_count']==0 and discovery['aliases']==28
current=active();assert len(current)==1,current
old=current[0];assert sha(old)==OLD_SHA,'Installed JAR changed since preflight; inspect before replacing'
assert old.resolve().parent==(INSTANCE/'mods').resolve()
stamp=datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).strftime('%Y%m%d-%H%M%S')
backup_dir=INSTANCE/'mod-backups'/f'brutality-iron-1.4.0-icons-{stamp}';backup_dir.mkdir(parents=True,exist_ok=False)
backup=backup_dir/old.name;shutil.copy2(old,backup);assert sha(backup)==OLD_SHA
target=INSTANCE/'mods'/SOURCE.name;staging=INSTANCE/'mods'/('.'+SOURCE.name+'.pending')
assert not target.exists() and not staging.exists()
shutil.copy2(SOURCE,staging);assert sha(staging)==digest
old.unlink()
try:staging.rename(target)
except BaseException:
    shutil.copy2(backup,old)
    raise
assert sha(target)==digest and active()==[target]
receipt=dict(version=VERSION,installed=str(target),bytes=target.stat().st_size,sha256=digest,backup=str(backup),previous_sha256=OLD_SHA,active_mod_id_count=1,spell_icons=53,native_size=[16,16],opaque_backgrounds=True,obsolete_animation_metadata_removed=8,gameplay_classes_unchanged=icons['gameplay_classes_unchanged'],total_spells=53,build_and_reobf=True,isolated_server_audit=True,client_rendering_verified=False,game_restarted=False)
(ROOT/'audit/deployment-1.4.0.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print(json.dumps(receipt,ensure_ascii=False,indent=2))
