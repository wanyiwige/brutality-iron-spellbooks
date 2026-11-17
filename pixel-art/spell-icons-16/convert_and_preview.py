"""Technical native-size conversion and contact-sheet assembly; no motif painting."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import json,shutil,hashlib
OUT=Path(__file__).resolve().parent
manifest=json.loads((OUT/'generated-files.json').read_text(encoding='utf8'))
native=OUT/'native';raw=OUT/'raw';preview=OUT/'preview'
for p in [native,raw,preview]:p.mkdir(exist_ok=True)
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',13)
sheet=Image.new('RGB',(960,max(1,(len(manifest)+7)//8)*158),(30,32,41));draw=ImageDraw.Draw(sheet)
audit=[]
for index,row in enumerate(manifest):
    source=Path(row['source']);shutil.copy2(source,raw/(row['id']+'.png'))
    im=Image.open(source).convert('RGB').resize((16,16),Image.Resampling.NEAREST)
    im=im.quantize(colors=24,method=Image.Quantize.MEDIANCUT,dither=Image.Dither.NONE).convert('RGBA')
    dest=native/(row['id']+'.png');im.save(dest)
    zoom=im.resize((96,96),Image.Resampling.NEAREST)
    zoom.save(preview/(row['id']+'.png'))
    x=index%8*120;y=index//8*158;sheet.paste(zoom,(x+12,y+4));sheet.paste(im,(x+98,y+105))
    draw.text((x+4,y+107),row['name'],font=font,fill='white');draw.text((x+4,y+129),row['id'][:17],font=font,fill='#b1b4c0')
    assert im.size==(16,16) and im.getchannel('A').getextrema()==(255,255)
    audit.append(dict(id=row['id'],name=row['name'],size=list(im.size),opaque=True,colors=len(im.getcolors()),sha256=hashlib.sha256(dest.read_bytes()).hexdigest()))
sheet.save(OUT/'contact-sheet.png')
(OUT/'native-validation.json').write_text(json.dumps(audit,ensure_ascii=False,indent=2),encoding='utf8')
print('Native icons converted:',len(audit),'16x16, opaque, <=24 colors')
