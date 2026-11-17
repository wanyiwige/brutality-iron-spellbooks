from pathlib import Path
import json,hashlib,shutil
root=Path(__file__).resolve().parents[1]
for school,entry in json.loads((root/'scroll-selection.json').read_text(encoding='utf8')).items():
    source=(root/entry['source']).resolve()
    assert hashlib.sha256(source.read_bytes()).hexdigest()==entry['sha256']
    target=root/'src/main/resources/assets/brutality_iron/textures/item'/('scroll_'+school+'.png')
    shutil.copy2(source,target)
    metadata=Path(str(source)+'.mcmeta')
    target_metadata=Path(str(target)+'.mcmeta')
    if metadata.exists():
        shutil.copy2(metadata,target_metadata)
    elif target_metadata.exists():
        target_metadata.unlink()
    print('Selected scroll:',school,entry['sha256'])
