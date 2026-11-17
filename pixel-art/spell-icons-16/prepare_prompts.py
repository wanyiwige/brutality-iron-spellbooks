from pathlib import Path
import json
OUT=Path(__file__).resolve().parent
ROOT=OUT.parents[1]
designs=json.loads((OUT/'designs.json').read_text(encoding='utf8'))
rows=json.loads((ROOT/'spell-inventory.json').read_text(encoding='utf8'))+json.loads((ROOT/'skill-expansion.json').read_text(encoding='utf8'))
palettes={
 'brutality_iron:inferno':'black cherry background, crimson red and magenta primary light, pale bone highlights',
 'brutality_iron:celestia':'deep midnight blue background, white and warm gold primary light, pale azure secondary accents',
 'brutality_iron:nihility':'near-black charcoal violet background, muted silver and gray-lilac primary light, black negative spaces',
 'alex_caves_spellsbook:hazen_cosmic':'deep navy-indigo background, cyan-white and violet primary light, sparse pink star accents',
 'alex_caves_spellsbook:hazen_shadow':'deep ink-blue background, silver moonlight and desaturated lavender primary light, cool blue secondary tones',
 'wind_spellbooks:wind':'dark storm-teal background, soft white and pale blue-gray primary light, cyan accents',
 'gtbcs_geomancy_plus:geo':'deep earthy brown background, carved gray-brown stone primary forms, pale gold or jade accents',
 'brutality_iron:plague':'near-black olive background, dark moss green shadows, vivid lime highlights',
 'irons_spellbooks:ender':'deep purple-black background, saturated violet and pink-white primary light',
 'irons_spellbooks:evocation':'deep blue-gray background, icy white and brilliant cyan primary light',
 'irons_spellbooks:eldritch':'near-black plum background, sinister vivid violet pillars with lilac-white highlights'}
base='Create exactly ONE square Minecraft spell icon in the style of the provided Iron Spellbooks and addon references. The reference contact sheet is STYLE ONLY: output a single icon, never a sheet or any text. The final asset is a REAL 16x16 pixel icon, displayed enlarged with hard square pixels: think in exactly 16 columns and 16 rows, each logical pixel a large uniform square. Very simple bold clusters and recognizable silhouette, around 20 colors maximum. Fill the entire square with an opaque colored background with only subtle two-tone pixel shading. Subject occupies roughly 13x13 pixels with a one-pixel breathing margin. No frame, no lettering, no UI labels, no fine decorative glyphs, no photorealism, no antialiasing, no blur, no smooth glow, no transparent background. Bright readable central form over dark school-colored background. Prioritize recognition at actual 16x16 size over detail. '
manifest=[]
for row in rows:
    sid=row['id'].split(':')[-1]
    manifest.append(dict(id=sid,name=row['name'],school=row['school'],prompt=base+'SPELL: '+row['name']+' ('+sid+'). MOTIF: '+designs[sid]+' PALETTE: '+palettes[row['school']]+'. Preserve the action and recognizable motif while using a strict coarse 16x16 pixel grid.'))
assert len(manifest)==53 and len(designs)==53
(OUT/'prompts.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps(manifest,ensure_ascii=False))
