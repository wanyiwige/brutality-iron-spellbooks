"""Refine the first twenty editable native-grid icons; preserve earlier review files."""
from pathlib import Path
import sys,ast,json,hashlib,zipfile
sys.path.insert(0,r'C:/Users/WANYIWIGE/.codex/skills/pixel-art-studio/scripts')
from pixelstudio import Sprite
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent
NATIVE=ROOT/'native';PREVIEW=ROOT/'preview'
for folder in [NATIVE,PREVIEW]:folder.mkdir(exist_ok=True)

# Load editable drawing functions only; never run old export code or overwrite approved files.
def recipes(folder):
    script=folder/'build.py';tree=ast.parse(script.read_text(encoding='utf8'))
    nodes=[n for n in tree.body if isinstance(n,ast.FunctionDef) or isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id in {'INFERNO','CELESTIA','COSMIC','SHADOW'} for t in n.targets)]
    scope={'Sprite':Sprite};exec(compile(ast.Module(body=nodes,type_ignores=[]),str(script),'exec'),scope)
    return scope
FIRST=ROOT.parent/'spell-icons-16-redraw-first10';SECOND=ROOT.parent/'spell-icons-16-redraw-second10'
R1=recipes(FIRST);R2=recipes(SECOND)
SPECS=[
 ('annihilation','歼灭','inferno',R1),('brimspike','尖刺','inferno',R1),('chthonic_capsule','通灵胶囊','inferno',R1),('destruction','毁灭','inferno',R1),('extinction','消光','inferno',R1),('stygian_step','冥河步','inferno',R1),
 ('divine_retribution','神圣报应','celestia',R1),('heavenly_flight','天飞','celestia',R1),('holy_mantle','圣衣','celestia',R1),('light_binding','光缚','celestia',R1),
 ('sacred_oath','神圣誓言','celestia',R2),('sacrifice','牺牲','celestia',R2),('cosmic_cataclysm','宇宙大灾变','cosmic',R2),('gravitokinesis','重力运动','cosmic',R2),('meteor_shower','流星雨','cosmic',R2),('singularity_shift','奇点偏移','cosmic',R2),('star_burst','星爆','cosmic',R2),('star_stream','星流','cosmic',R2),('crescent_dart','新月飞镖','shadow',R2),('crescent_scythe','新月之镰','shadow',R2)]
EXTRA={
 'inferno':['#45182a','#83223f','#d34657','#ff9e9a','#b98d8d','#fae0b0'],
 'celestia':['#2f4055','#6e88a3','#d5a454','#fffbe9','#a4c1c8','#645237'],
 'cosmic':['#2d2858','#634394','#a184dd','#8cd9e0','#427392','#eefbeb'],
 'shadow':['#25314f','#4b5b7d','#8da7bf','#d3dfd6','#8b77a4','#faf5dc']}
CENTERS=[(8,8),(8,8),(8,8),(5,8),(7,8),(9,9),(8,11),(8,7),(8,8),(8,8),(8,7),(8,7),(5,10),(8,8),(8,9),(8,8),(3,11),(8,8),(8,8),(10,5)]
def dots(s,color,*coords):
    for x,y in coords:
        assert type(x)==int and type(y)==int and 0<=x<16 and 0<=y<16
        s.px(x,y,color)
def line(s,color,*points):
    for a,b in zip(points,points[1:]):s.line(*a,*b,color)
def background(s,index):
    cx,cy=CENTERS[index]
    for y in range(16):
        for x in range(16):
            distance=(x-cx)**2+(y-cy)**2
            s.px(x,y,6 if distance<28 else 1 if distance<95 else 0)
    if index==0:
        s.ellipse(1,1,14,14,1,fill=False);s.ellipse(3,3,12,12,2,fill=False)
    elif index in [1,4,12,14]:
        for offset in [-3,3]:line(s,6,(1,12+offset),(5,9+offset),(10,5+offset),(14,2+offset))
    elif index in [3,5]:
        line(s,2,(1,5),(4,5),(6,7));line(s,1,(0,13),(5,13),(8,11))
    elif index==6:
        s.line(4,1,4,9,6);s.line(11,2,11,9,6);s.line(2,14,13,14,11)
    elif index==10:
        line(s,6,(3,13),(5,11),(10,11),(12,13));s.line(5,14,10,14,11)
    elif index==7:
        for end in [(1,1),(14,1),(1,11),(14,11)]:s.line(8,7,*end,6)
    elif index==8:
        line(s,11,(2,9),(2,5),(5,1),(10,1),(13,5),(13,9))
    elif index==9:
        for y in [5,11]:s.line(1,y,14,y,6)
    elif index==11:
        line(s,11,(1,12),(3,11),(4,10));line(s,11,(11,4),(13,3),(14,1))
        line(s,6,(3,14),(6,13),(8,14),(11,13))
    elif index==13:
        s.ellipse(1,1,14,14,6,fill=False)
    elif index==15:
        s.ellipse(2,3,13,12,6,fill=False)
    elif index==16:
        for end in [(1,1),(9,1),(14,5),(14,12)]:s.line(2,12,*end,6)
    elif index==17:
        line(s,6,(1,15),(1,10),(4,7),(9,6),(11,3),(14,1))
    elif index in [18,19]:
        line(s,6,(1,13),(4,10),(7,7),(10,4),(14,2));line(s,1,(1,14),(5,11),(9,7),(14,4))

def refine(s,sid):
    if sid=='annihilation':
        dots(s,9,(7,3),(9,4),(3,5),(4,7),(12,5),(12,7),(3,12),(5,11),(11,12))
        dots(s,7,(7,6),(5,6),(10,6),(6,11),(10,11))
        dots(s,10,(7,4),(4,6),(11,6),(4,12),(11,13));dots(s,0,(8,8));dots(s,8,(6,8),(9,8),(8,6),(8,10))
    elif sid=='brimspike':
        line(s,8,(3,12),(5,9),(8,7),(11,4));dots(s,9,(7,7),(9,5),(11,3))
        dots(s,7,(4,9),(6,7),(9,8),(10,6));dots(s,5,(4,8),(7,5),(12,3))
        line(s,3,(2,6),(3,4),(5,3));line(s,4,(10,13),(12,12),(13,10))
    elif sid=='chthonic_capsule':
        dots(s,9,(4,5),(4,7),(5,8),(7,8),(9,5));dots(s,8,(8,4),(10,5),(11,7),(7,10))
        line(s,2,(8,5),(9,6),(8,7));line(s,7,(9,8),(10,9),(9,10))
        dots(s,5,(5,4),(5,5));dots(s,3,(4,12),(5,13),(11,11))
    elif sid=='destruction':
        dots(s,9,(4,6),(3,8),(6,9),(8,8),(11,6),(12,9))
        dots(s,8,(7,5),(9,6),(9,10),(11,11));dots(s,7,(10,4),(12,7),(10,12))
        line(s,4,(1,5),(2,6));line(s,8,(2,11),(3,12));dots(s,5,(5,7),(6,8))
    elif sid=='extinction':
        line(s,9,(3,10),(10,5));dots(s,8,(4,12),(6,10),(8,9),(11,6),(13,5))
        dots(s,7,(2,10),(5,8),(8,6),(11,3));dots(s,5,(1,11),(2,10),(2,12),(13,3))
        line(s,3,(6,1),(7,3));line(s,8,(10,10),(12,10))
    elif sid=='stygian_step':
        dots(s,7,(9,4),(9,6),(8,8),(6,10),(12,12));dots(s,8,(9,5),(8,7),(9,9),(12,10))
        dots(s,9,(7,5),(8,9),(11,11));s.line(7,12,11,12,7)
        line(s,3,(1,7),(3,7),(5,8));dots(s,4,(2,10),(3,11),(4,12))
    elif sid=='divine_retribution':
        s.line(6,2,6,7,8);s.line(9,2,9,7,3);dots(s,9,(7,2),(7,5),(8,8),(7,11))
        dots(s,8,(4,4),(11,4),(5,13),(10,13));dots(s,10,(2,10),(3,11),(12,10),(13,9))
        s.line(5,14,10,14,11);dots(s,5,(4,12),(11,12))
    elif sid=='heavenly_flight':
        dots(s,10,(3,6),(4,8),(5,10),(12,6),(11,8),(10,10))
        dots(s,7,(4,9),(5,11),(11,9),(10,11));dots(s,5,(2,4),(3,7),(4,10),(13,4),(12,7),(11,10))
        dots(s,8,(7,3),(8,6),(8,10));dots(s,9,(6,1),(7,7))
    elif sid=='holy_mantle':
        line(s,7,(4,6),(4,9),(3,11));line(s,7,(10,6),(11,9),(12,11))
        dots(s,10,(4,5),(3,9),(4,12),(11,6),(12,12));dots(s,9,(6,2),(4,6))
        s.line(5,10,5,12,2);s.line(10,10,10,12,2)
        dots(s,8,(6,8),(7,10),(8,11));dots(s,11,(9,9),(9,10));dots(s,5,(6,7),(7,8))
    elif sid=='light_binding':
        dots(s,7,(6,3),(7,5),(9,6),(5,11));dots(s,10,(7,2),(8,3))
        for x in [2,10]:
            dots(s,9,(x,6),(x+1,6));dots(s,8,(x,8),(x+3,9),(x+1,10));dots(s,11,(x+2,10))
        dots(s,5,(6,7),(7,8),(8,7),(9,8));dots(s,3,(6,8),(8,8))
    elif sid=='sacred_oath':
        s.line(6,4,6,7,10);dots(s,9,(8,2),(7,5));dots(s,7,(9,5),(9,7))
        dots(s,8,(4,4),(5,3),(11,5),(10,9),(7,12));dots(s,11,(12,7),(10,10),(8,13))
        dots(s,5,(5,9),(7,14));dots(s,10,(7,6))
    elif sid=='sacrifice':
        dots(s,8,(3,7),(5,9),(7,11),(9,10),(11,8),(12,5));dots(s,11,(9,11),(10,10),(12,8))
        dots(s,9,(5,4),(6,5),(4,6));dots(s,10,(5,8),(7,9))
        dots(s,4,(1,11),(3,13),(13,2));dots(s,9,(2,12),(14,1));dots(s,8,(11,4),(10,5))
    elif sid=='cosmic_cataclysm':
        dots(s,9,(3,8),(3,10),(4,11),(6,8));dots(s,10,(6,10),(7,10),(6,11))
        line(s,2,(6,8),(5,9),(6,10));dots(s,8,(8,7),(9,6),(11,4),(13,2))
        dots(s,11,(3,9),(5,8));dots(s,7,(7,12),(8,11))
        dots(s,9,(1,13),(3,14));dots(s,10,(1,14),(3,15));dots(s,3,(10,12),(11,13))
    elif sid=='gravitokinesis':
        dots(s,10,(8,6),(9,7),(7,9));dots(s,8,(6,6),(7,7),(9,8));dots(s,11,(6,5))
        dots(s,9,(3,3),(4,3),(11,12),(12,11));dots(s,8,(7,2),(9,2),(5,13),(7,13))
        dots(s,7,(12,4),(3,11))
    elif sid=='meteor_shower':
        for x,y in [(2,5),(6,9),(11,12)]:
            dots(s,9,(x,y),(x+2,y+1));dots(s,10,(x+2,y+2));dots(s,11,(x,y+1))
        dots(s,8,(4,3),(5,2),(8,7),(9,6),(12,11),(13,10));dots(s,7,(6,2),(11,4),(14,10))
    elif sid=='singularity_shift':
        line(s,9,(4,11),(6,10),(9,9),(11,7));dots(s,8,(2,7),(3,5),(6,3),(10,2))
        dots(s,7,(2,11),(6,12),(10,10));dots(s,4,(5,5),(5,6),(9,10))
        dots(s,11,(13,4));dots(s,9,(12,3),(14,4))
    elif sid=='star_burst':
        for x,y in [(3,2),(8,3),(12,6),(13,10),(10,13)]:
            dots(s,9,(x,y+1));dots(s,8,(x-1,y+1))
        dots(s,3,(3,9),(5,8),(6,7),(8,9),(7,12));dots(s,11,(2,12));dots(s,9,(1,12),(3,13))
    elif sid=='star_stream':
        dots(s,8,(1,13),(2,10),(4,9),(5,9),(8,9),(10,8),(12,6),(13,3))
        dots(s,7,(2,14),(6,10),(10,10),(14,5));dots(s,9,(4,12),(8,8),(12,5))
        dots(s,11,(3,12),(7,8),(12,4))
    elif sid=='crescent_dart':
        for x,y in [(2,1),(6,5),(10,9)]:
            dots(s,9,(x+2,y),(x+4,y+1));dots(s,8,(x+3,y+3));dots(s,7,(x+2,y+4))
            dots(s,10,(x+1,y+3))
        line(s,7,(1,8),(2,7),(3,7));line(s,6,(4,14),(5,13),(7,12))
    elif sid=='crescent_scythe':
        dots(s,9,(6,2),(9,1),(12,2),(13,4));dots(s,8,(10,3),(12,5),(12,7),(10,9))
        dots(s,7,(9,3),(13,6),(11,9));dots(s,11,(9,2),(11,3))
        dots(s,8,(4,13),(6,11),(8,8));dots(s,3,(5,12),(7,10));dots(s,4,(3,14),(7,11))

font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',16)
sheets=[Image.new('RGB',(800,420),'#242733') for _ in range(2)]
compare=Image.new('RGB',(960,1000),'#242733');cd=ImageDraw.Draw(compare)
gridproof=Image.new('RGB',(800,840),'#242733');gd=ImageDraw.Draw(gridproof)
audit=[]
for index,(sid,name,school,scope) in enumerate(SPECS):
    original=scope[sid]().composite(1);old_palette=scope[school.upper()]
    palette=old_palette+EXTRA[school];s=Sprite(16,16,palette=palette)
    background(s,index)
    old_rgba=[tuple(bytes.fromhex(c.lstrip('#')))+(255,) for c in old_palette]
    for y in range(16):
        for x in range(16):
            color=original.getpixel((x,y))
            if color not in old_rgba[:2]:s.px(x,y,color)
    refine(s,sid);im=s.composite(1)
    assert im.size==(16,16) and im.getchannel('A').getextrema()==(255,255) and len(im.getcolors())<=12
    s.save_png(str(NATIVE/(sid+'.png')));s.save_png(str(PREVIEW/(sid+'.png')),scale=8)
    zoom=Image.open(PREVIEW/(sid+'.png')).convert('RGB')
    for y in range(16):
        for x in range(16):assert zoom.crop((x*8,y*8,x*8+8,y*8+8)).getcolors()==[(64,im.getpixel((x,y))[:3])]
    sheet=sheets[index//10];d=ImageDraw.Draw(sheet);i=index%10;x=i%5*160+16;y=i//5*210+10
    sheet.paste(zoom,(x,y));d.text((x,y+140),f'{index+1:02d} {name}',font=font,fill='#eeeeee');d.text((x,y+166),'16×16 · 细化版',font=font,fill='#b7c4d6')
    gy=y+(index//10)*420;gridproof.paste(zoom,(x,gy))
    for n in range(17):
        gd.line((x+n*8,gy,x+n*8,gy+128),fill='#434957');gd.line((x,gy+n*8,x+128,gy+n*8),fill='#434957')
    gd.text((x,gy+140),f'{index+1:02d} {name}',font=font,fill='#eeeeee')
    cx=(index%4)*240;cy=(index//4)*200
    compare.paste(original.resize((96,96),Image.Resampling.NEAREST),(cx+12,cy+28));compare.paste(im.resize((96,96),Image.Resampling.NEAREST),(cx+128,cy+28))
    cd.text((cx+12,cy+4),'原稿',font=font,fill='#b7c4d6');cd.text((cx+128,cy+4),'细化',font=font,fill='#eeeeee');cd.text((cx+12,cy+132),f'{index+1:02d} {name}',font=font,fill='#eeeeee')
    before=FIRST if index<10 else SECOND
    assert Image.open(before/'native'/(sid+'.png')).convert('RGBA').tobytes()==original.tobytes()
    audit.append(dict(id=sid,name=name,size=[16,16],colors=len(im.getcolors()),opaque=True,equal_pixel_blocks=True,original_unchanged=True,changed_pixels=sum(im.getpixel((x,y))!=original.getpixel((x,y)) for y in range(16) for x in range(16)),sha256=hashlib.sha256((NATIVE/(sid+'.png')).read_bytes()).hexdigest()))
for i,sheet in enumerate(sheets):sheet.save(ROOT/f'contact-{i+1}.png')
both=Image.new('RGB',(800,840));both.paste(sheets[0],(0,0));both.paste(sheets[1],(0,420));both.save(ROOT/'contact-all20.png')
gridproof.save(ROOT/'grid-proof.png');compare.save(ROOT/'before-after.png')
(ROOT/'validation.json').write_text(json.dumps(audit,ensure_ascii=False,indent=2),encoding='utf8')
with zipfile.ZipFile(ROOT/'first20-detailed-16x16.zip','w',zipfile.ZIP_DEFLATED) as z:
    for sid,_,_,_ in SPECS:z.write(NATIVE/(sid+'.png'),'native/'+sid+'.png')
    for name in ['contact-all20.png','before-after.png','grid-proof.png','validation.json']:z.write(ROOT/name,name)
print('PASS: 20 refined native 16x16 files; max 12 colors; all pixels expand equally; originals preserved; not installed.')
