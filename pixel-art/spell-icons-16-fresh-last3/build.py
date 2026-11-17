"""Last three native spell icons, created independently from mechanics, not existing art."""
from pathlib import Path
import sys,json,hashlib,zipfile
sys.path.insert(0,r'C:/Users/WANYIWIGE/.codex/skills/pixel-art-studio/scripts')
from pixelstudio import Sprite
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent
for folder in ['native','preview']:(ROOT/folder).mkdir(parents=True,exist_ok=True)
PLAGUE=['#0b1515','#11221f','#1e382b','#273a30','#426445','#689044','#92b749','#c5d56e','#52675c','#7b9a83','#b2c5a0','#f1edc6']
FLAME=['#180e1a','#2d1424','#4b1c31','#743048','#aa3b42','#d35d3e','#f6994c','#ffd176','#57424e','#b28674','#ffe0a0','#fff3ce']
def blank(palette):
    s=Sprite(16,16,palette=palette)
    for y in range(16):
        for x in range(16):s.px(x,y,1 if x+2*y<32 else 0)
    return s
def dots(s,c,*points):
    for x,y in points:
        assert type(x)==int and type(y)==int and 0<=x<16 and 0<=y<16
        s.px(x,y,c)
def path(s,c,*points):
    for a,b in zip(points,points[1:]):s.line(*a,*b,c)

def plague_infusion():
    s=blank(PLAGUE)
    # An infected chest and branching contagion, with two secondary nodes.
    path(s,2,(2,4),(4,6),(6,7));path(s,2,(9,7),(11,5),(13,4))
    s.polygon([(6,2),(9,2),(10,4),(9,6),(6,6),(5,4)],8)
    s.polygon([(6,2),(8,2),(8,4),(7,5),(6,4)],9)
    dots(s,10,(6,2),(6,3));dots(s,3,(9,3),(8,5))
    dots(s,8,(9,4));dots(s,3,(7,6))
    s.polygon([(5,6),(10,6),(12,8),(12,11),(10,12),(9,14),(5,14),(4,11),(3,10),(3,8)],3)
    s.polygon([(5,7),(7,6),(9,7),(10,10),(9,12),(6,12),(4,10)],8)
    path(s,9,(4,8),(5,7),(6,7));path(s,4,(10,7),(11,9),(10,11))
    s.polygon([(7,7),(9,8),(9,10),(7,11),(6,9)],5)
    path(s,6,(7,8),(8,8),(8,10));dots(s,11,(7,8));dots(s,7,(7,9),(8,10))
    path(s,5,(6,9),(4,8),(2,8));path(s,6,(9,9),(11,10),(13,10))
    path(s,4,(7,11),(6,13));path(s,5,(8,11),(9,13))
    s.rect(1,7,2,9,4);dots(s,7,(1,7),(2,8))
    s.rect(12,9,14,11,4);s.rect(12,9,13,10,6);dots(s,11,(12,9))
    s.rect(12,2,13,3,4);dots(s,7,(12,2));dots(s,2,(2,13),(13,13))
    return s
def pestilent_mist():
    s=blank(PLAGUE)
    # Three overlapping cloud lobes, lit upper surfaces and a heavy lower shadow.
    s.polygon([(2,9),(2,6),(4,4),(7,5),(9,4),(12,5),(13,8),(15,10),(14,13),(11,14),(5,14),(1,12)],3)
    s.ellipse(2,5,9,12,4,fill=True);s.ellipse(6,5,13,12,4,fill=True)
    s.polygon([(2,7),(4,5),(6,5),(8,7),(7,9),(4,10),(2,9)],5)
    s.polygon([(4,5),(6,6),(6,7),(4,8),(3,7)],6)
    dots(s,7,(4,6),(5,6));dots(s,10,(3,7))
    s.polygon([(8,7),(11,6),(13,8),(13,10),(11,12),(8,11),(7,9)],5)
    path(s,6,(9,7),(11,7),(12,8));dots(s,7,(9,7),(10,7))
    s.ellipse(4,9,11,14,4,fill=True);s.ellipse(4,9,10,12,5,fill=True)
    path(s,6,(5,10),(7,9),(9,10));dots(s,7,(6,10));dots(s,3,(5,13),(8,14),(11,13))
    # Air bubbles are rings; all interiors are still opaque pixels.
    s.ellipse(10,1,14,5,5,fill=False);path(s,7,(11,1),(10,2),(10,3));dots(s,11,(11,1));dots(s,4,(13,4))
    s.ellipse(2,1,4,3,5,fill=False);dots(s,7,(2,1),(3,1));dots(s,6,(1,5),(7,2))
    return s
def infernal_spirits():
    s=blank(FLAME)
    s.ellipse(2,12,13,15,2,fill=False);path(s,4,(3,13),(6,14),(10,14),(12,13))
    # Smaller rear spirits stand apart from the large foreground flame.
    s.polygon([(2,5),(4,7),(4,5),(6,8),(5,11),(3,12),(1,10),(1,8)],3)
    s.polygon([(2,6),(3,8),(4,7),(5,9),(4,11),(2,10)],5)
    path(s,6,(2,7),(2,9),(3,10));dots(s,10,(2,7));dots(s,2,(2,8),(4,8),(3,9))
    s.polygon([(12,4),(13,7),(14,6),(15,9),(14,11),(12,12),(10,10),(11,7)],3)
    s.polygon([(12,6),(12,8),(14,7),(14,10),(12,11),(11,9)],5)
    path(s,6,(12,7),(11,9),(12,10));dots(s,10,(12,8));dots(s,2,(11,9),(13,9));dots(s,3,(12,11))
    s.polygon([(7,1),(8,4),(10,3),(9,6),(11,8),(11,11),(9,13),(6,13),(4,10),(5,7),(5,4)],3)
    s.polygon([(7,3),(7,6),(9,5),(8,7),(10,9),(9,11),(7,12),(5,10),(6,7)],4)
    s.polygon([(7,5),(7,7),(9,8),(9,10),(7,11),(6,9)],6)
    s.polygon([(7,7),(8,8),(8,10),(7,10),(6,9)],7)
    dots(s,11,(7,7));dots(s,10,(7,8),(8,10));dots(s,2,(6,8),(9,8))
    dots(s,5,(5,10),(6,12),(9,12));dots(s,4,(1,13),(13,13));dots(s,6,(4,3),(12,2))
    return s

SPECS=[('plague_infusion','疫病注入',plague_infusion),('pestilent_mist','疫雾爆发',pestilent_mist),('infernal_spirits','火灵集群',infernal_spirits)]
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',16)
sheet=Image.new('RGB',(480,210),'#242733');draw=ImageDraw.Draw(sheet)
grid=Image.new('RGB',(480,210),'#242733');gd=ImageDraw.Draw(grid)
small=Image.new('RGB',(240,105),'#242733');sd=ImageDraw.Draw(small);sf=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',10)
report=[]
for i,(sid,name,paint) in enumerate(SPECS):
    s=paint();im=s.composite(1)
    assert im.size==(16,16) and im.getchannel('A').getextrema()==(255,255) and len(im.getcolors())<=12
    s.save_png(str(ROOT/'native'/(sid+'.png')));s.save_png(str(ROOT/'preview'/(sid+'.png')),scale=8)
    zoom=im.convert('RGB').resize((128,128),Image.Resampling.NEAREST)
    for y in range(16):
        for x in range(16):assert zoom.crop((8*x,8*y,8*x+8,8*y+8)).getcolors()==[(64,im.getpixel((x,y))[:3])]
    x=i*160+16;y=10;sheet.paste(zoom,(x,y));grid.paste(zoom,(x,y))
    for n in range(17):
        gd.line((x+n*8,y,x+n*8,y+128),fill='#434957');gd.line((x,y+n*8,x+128,y+n*8),fill='#434957')
    for d in [draw,gd]:
        d.text((x,y+140),f'{i+51:02d} {name}',font=font,fill='#eeeeee');d.text((x,y+166),'16×16 · 全新绘制',font=font,fill='#b7c4d6')
    small.paste(im.resize((64,64),Image.Resampling.NEAREST),(i*80+8,5));sd.text((i*80+8,75),f'{i+51} {name}',font=sf,fill='#eeeeee')
    report.append(dict(number=i+51,id=sid,name=name,width=16,height=16,opaque=True,equal_pixel_blocks=True,colors=len(im.getcolors()),old_art_inputs=[],sha256=hashlib.sha256((ROOT/'native'/(sid+'.png')).read_bytes()).hexdigest()))
sheet.save(ROOT/'contact-sheet.png');grid.save(ROOT/'grid-proof.png');small.save(ROOT/'readability-4x.png')
(ROOT/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
with zipfile.ZipFile(ROOT/'fresh-51-53-native16.zip','w',zipfile.ZIP_DEFLATED) as z:
    for sid,_,_ in SPECS:z.write(ROOT/'native'/(sid+'.png'),'native/'+sid+'.png')
    for name in ['contact-sheet.png','grid-proof.png','validation.json']:z.write(ROOT/name,name)
print('PASS: final three native 16x16 icons; fully opaque; equal pixels; <=12 colors; no old art inputs; not installed.')
