"""Deploy the verified 1.3.0 user revision with an exact-file rollback copy."""
from pathlib import Path
import datetime, hashlib, json, re, shutil, tomllib, zipfile

root = Path(__file__).resolve().parents[1]
instance = root.parent
version = '1.3.0'
source = root / 'build/libs' / f'brutality_iron_spellbooks-{version}.jar'
def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def active():
    found = []
    for path in (instance / 'mods').glob('*.jar'):
        with zipfile.ZipFile(path) as archive:
            if 'META-INF/mods.toml' not in archive.namelist():
                continue
            raw = archive.read('META-INF/mods.toml')
            # Some unrelated installed mods ship non-UTF8 comments. The mod ID
            # itself is ASCII; only parse TOML for a matching archive.
            if re.search(rb'(?m)^\s*modId\s*=\s*[\"\x27]brutality_iron[\"\x27]', raw):
                metadata = tomllib.loads(raw.decode('utf8'))
                if any(m.get('modId') == 'brutality_iron' for m in metadata.get('mods', [])):
                    found.append(path)
    return found

digest = sha(source)
for report in ['release-verification.json', 'skill-release-verification.json']:
    data = json.loads((root / 'audit' / report).read_text(encoding='utf8'))
    assert data['sha256'].lower() == digest, report
discovery = json.loads((root / 'audit/gecko-discovery-verification.json').read_text(encoding='utf8'))
assert discovery['missing_count'] == 0 and discovery['aliases'] == 26
assert Path(discovery['jar']).resolve() == source.resolve()
old_files = active()
assert len(old_files) == 1, old_files
old = old_files[0]
old_digest = sha(old)
assert old_digest == '2dd81b2bfabc6171c4daea9969f38ea476b44ce7b3019e2d8db200086b975f8e', 'Live JAR changed; inspect first'
backup_dir = instance / 'mod-backups' / ('brutality-iron-1.3.0-' + datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).strftime('%Y%m%d-%H%M%S'))
backup_dir.mkdir(parents=True, exist_ok=False)
backup = backup_dir / old.name
shutil.copy2(old, backup)
assert sha(backup) == old_digest
target = instance / 'mods' / source.name
staging = instance / 'mods' / ('.' + source.name + '.pending')
assert not target.exists() and not staging.exists()
shutil.copy2(source, staging)
assert sha(staging) == digest
old.unlink()
try:
    staging.rename(target)
except BaseException:
    shutil.copy2(backup, old)
    raise
assert sha(target) == digest and active() == [target]
receipt = dict(version=version, installed=str(target), sha256=digest,
               backup=str(backup), old_sha256=old_digest, active_mod_id_count=1,
               build_and_reobf=True, isolated_server_audit=True,
               gecko_aliases_verified=26, total_spells=57, retained_expansion_spells=27, removed_expansion_spells=33, native_direction_tests=80, game_restarted=False,
               client_rendering_verified=False)
(root / 'audit/deployment-1.3.0.json').write_text(json.dumps(receipt, ensure_ascii=False, indent=2)+'\n', encoding='utf8')
print(json.dumps(receipt, ensure_ascii=False, indent=2))
