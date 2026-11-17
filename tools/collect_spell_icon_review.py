"""Collect the current 53 review variants; does not write runtime resources or installed JARs."""
from pathlib import Path
import json,hashlib,shutil,zipfile
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'pixel-art'
OUT=ART/'spell-icons-16-review-all53';(OUT/'native').mkdir(parents=True,exist_ok=True)
spells=json.loads((ROOT/'spell-inventory.json').read_text(encoding='utf8'))+json.loads((ROOT/'skill-expansion.json').read_text(encoding='utf8'))
assert len(spells)==53 and all(row['id'].split(':')[-1]!='shadowflame_slash' for row in spells)
batches=[(0,20,'spell-icons-16-detail-first20'),(20,30,'spell-icons-16-detail-third10'),(30,40,'spell-icons-16-fresh-fourth10'),(40,50,'spell-icons-16-fresh-fifth10'),(50,53,'spell-icons-16-fresh-last3')]
selection=[]
for start,end,folder in batches:
    batch=ART/folder;checks={row['id']:row for row in json.loads((batch/'validation.json').read_text(encoding='utf8'))}
    for index in range(start,end):
        row=spells[index];sid=row['id'].split(':')[-1];source=batch/'native'/(sid+'.png')
        digest=hashlib.sha256(source.read_bytes()).hexdigest();assert digest==checks[sid]['sha256']
        image=Image.open(source).convert('RGBA');assert image.size==(16,16) and image.getchannel('A').getextrema()==(255,255)
        target=OUT/'native'/source.name;shutil.copy2(source,target)
        selection.append(dict(number=index+1,id='brutality_iron:'+sid,name=row['name'],school=row['school'],source_batch=folder,width=16,height=16,opaque=True,sha256=digest))
assert len(selection)==len({row['id'] for row in selection})==53
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',13)
sheet=Image.new('RGB',(960,1050),'#242733');draw=ImageDraw.Draw(sheet)
for index,row in enumerate(selection):
    image=Image.open(OUT/'native'/(row['id'].split(':')[-1]+'.png')).convert('RGB')
    x=index%8*120+12;y=index//8*150+6
    sheet.paste(image.resize((96,96),Image.Resampling.NEAREST),(x,y))
    draw.text((x-3,y+105),f"{row['number']:02d} {row['name']}",font=font,fill='#eeeeee')
    draw.text((x-3,y+125),'16×16',font=font,fill='#aab6ca')
sheet.save(OUT/'contact-all53.png')
(OUT/'selection.json').write_text(json.dumps({'status':'review variants; not installed','count':53,'icons':selection},ensure_ascii=False,indent=2),encoding='utf8')
(OUT/'README.md').write_text('当前53个法术的图标审阅合集。原生图均为16×16，背景不透明；总览仅用6倍最近邻放大。selection.json记录每张的来源批次与哈希。未修改游戏资源或安装JAR，绘制完成不等于所有版本已获用户最终确认。\n',encoding='utf8')
with zipfile.ZipFile(OUT/'all53-native-16x16.zip','w',zipfile.ZIP_DEFLATED) as z:
    for row in selection:
        name=row['id'].split(':')[-1]+'.png';z.write(OUT/'native'/name,'native/'+name)
    for name in ['contact-all53.png','selection.json','README.md']:z.write(OUT/name,name)
print('Collected 53 unique native 16x16 review icons. Runtime and mods untouched.')
