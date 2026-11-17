"""Review batch 21-30, authored on a true 16x16 grid. Existing icons are read-only."""
from pathlib import Path
import sys,json,hashlib,zipfile
sys.path.insert(0,r'C:/Users/WANYIWIGE/.codex/skills/pixel-art-studio/scripts')
from pixelstudio import Sprite
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent
NATIVE=ROOT/'native';PREVIEW=ROOT/'preview'
for folder in [NATIVE,PREVIEW]:folder.mkdir(parents=True,exist_ok=True)
SHADOW=['#0c1220','#18243a','#34445f','#6c7a9c','#b4c9db','#eef3de','#25314f','#4b5b7d','#8da7bf','#d3dfd6','#8b77a4','#faf5dc']
NIHILITY=['#100e19','#201a2d','#3c334b','#615672','#9585a7','#d6cadc','#30283f','#51435f','#b4a1c3','#f3e9e1','#756c89','#16131f']
def dots(s,color,*coords):
    for x,y in coords:
        assert type(x)==int and type(y)==int and 0<=x<16 and 0<=y<16
        s.px(x,y,color)
def line(s,color,*points):
    for a,b in zip(points,points[1:]):s.line(*a,*b,color)
def blank(palette,center=(8,8),pattern='none'):
    s=Sprite(16,16,palette=palette);cx,cy=center
    for y in range(16):
        for x in range(16):
            distance=(x-cx)**2+(y-cy)**2
            s.px(x,y,6 if distance<26 else 1 if distance<95 else 0)
    if pattern=='orbit':
        line(s,6,(1,11),(3,13),(8,14),(13,11),(14,7))
    elif pattern=='ray':
        line(s,6,(1,13),(5,10),(10,6),(14,2));line(s,1,(1,15),(6,12),(12,7),(15,5))
    elif pattern=='fracture':
        line(s,2,(1,4),(4,5),(5,7));line(s,2,(10,9),(12,10),(14,13))
    elif pattern=='phase':
        s.line(1,6,5,6,6);s.line(10,10,14,10,6);s.line(2,13,7,13,1)
    return s

def duo_lunaris():
    s=blank(SHADOW,(8,8),'orbit')
    # Separate diagonal discs keep the two lunar phases readable at 1x.
    s.ellipse(1,2,7,8,2,fill=True);s.ellipse(1,2,7,7,3,fill=True)
    s.polygon([(3,3),(5,3),(6,5),(5,7),(3,7),(2,5)],4)
    line(s,5,(2,5),(3,3),(5,2));dots(s,9,(4,3),(3,4))
    dots(s,8,(5,4),(4,6),(6,6));dots(s,3,(5,5),(5,7))
    s.ellipse(8,7,14,13,3,fill=True);s.ellipse(10,6,15,11,0,fill=True)
    line(s,4,(10,7),(9,8),(8,10),(9,12),(11,13))
    line(s,5,(10,7),(9,8),(8,10));dots(s,8,(9,11),(10,12),(12,12))
    dots(s,7,(9,12),(12,13));dots(s,4,(13,2),(14,3))
    return s
def moonlit_mending():
    s=blank(SHADOW,(8,8))
    s.polygon([(8,1),(5,2),(2,5),(1,9),(3,12),(6,14),(10,14),(11,12),(7,12),(5,10),(4,7),(6,4)],3)
    line(s,4,(7,2),(4,4),(2,7),(2,10),(4,12),(7,13),(10,13))
    line(s,5,(6,2),(4,3),(3,5),(2,7));dots(s,9,(3,6),(3,9))
    dots(s,8,(4,11),(6,12),(8,13));dots(s,7,(4,5),(4,8),(5,11))
    s.polygon([(8,6),(10,6),(11,7),(12,6),(13,6),(14,7),(14,9),(11,12),(8,9)],2)
    s.polygon([(8,7),(10,7),(11,8),(12,7),(13,7),(13,9),(11,11),(9,9)],8)
    dots(s,5,(9,7),(10,8),(12,7));dots(s,4,(10,9),(11,10),(13,8))
    s.line(12,2,12,4,4);s.line(11,3,13,3,4);s.px(12,3,5)
    return s
def nightfall():
    s=blank(SHADOW,(10,4))
    # A quiet midnight sky with a descending dark horizon, not a second moon weapon.
    s.ellipse(8,1,14,7,3,fill=True);s.ellipse(10,0,15,5,0,fill=True)
    line(s,4,(10,1),(9,2),(8,4),(9,6),(11,7));dots(s,5,(9,3),(9,4),(10,6))
    dots(s,4,(2,2),(3,5),(6,3));dots(s,8,(5,7),(13,9))
    s.polygon([(0,11),(3,8),(6,11),(8,9),(11,11),(14,8),(15,9),(15,15),(0,15)],2)
    s.polygon([(0,13),(2,12),(5,13),(7,11),(10,13),(12,12),(15,13),(15,15),(0,15)],0)
    line(s,7,(1,11),(3,9),(5,11));line(s,7,(11,11),(14,9),(15,10))
    s.line(3,14,11,14,1);s.line(5,13,8,13,6)
    return s
def piercing_moonlight():
    s=blank(SHADOW,(7,8),'ray')
    s.polygon([(5,2),(2,4),(1,7),(2,10),(5,11),(7,10),(4,9),(3,7),(4,4)],3)
    line(s,4,(4,3),(2,5),(2,8),(4,10),(6,10));dots(s,5,(3,4),(2,6),(3,9))
    s.polygon([(3,10),(5,8),(13,2),(14,2),(14,5),(6,11)],2)
    s.polygon([(4,10),(7,7),(14,2),(14,4),(7,10)],8)
    s.line(4,10,14,3,5);s.line(6,10,14,4,4)
    dots(s,9,(5,9),(9,6),(13,3));dots(s,7,(5,11),(10,7),(13,5))
    return s
def dematerialize():
    s=blank(NIHILITY,(7,8),'fracture')
    s.rect(4,2,7,5,3);s.rect(4,2,6,4,4);s.line(4,2,5,2,9)
    dots(s,5,(4,3),(5,3));dots(s,10,(7,3),(6,5))
    s.polygon([(4,6),(7,6),(8,7),(7,10),(4,10),(3,8)],3)
    s.line(4,6,4,9,5);s.rect(5,6,6,8,8);dots(s,7,(6,9),(7,8))
    s.line(2,7,2,9,4);s.line(3,6,3,8,8);s.line(8,6,9,7,4)
    s.rect(4,11,5,14,3);s.line(4,11,4,13,5)
    dots(s,4,(7,11),(8,13),(10,4),(11,8));dots(s,8,(12,5),(13,5),(12,6),(13,6))
    dots(s,5,(10,10),(13,11));dots(s,3,(11,12),(14,9));dots(s,9,(11,3))
    return s
def edge_of_oblivion():
    s=blank(NIHILITY,(8,8))
    s.polygon([(2,5),(3,3),(6,2),(10,2),(13,4),(10,4),(7,4),(4,5)],3)
    line(s,5,(2,5),(4,3),(7,2),(10,2));dots(s,8,(5,3),(8,3),(11,3),(12,4))
    s.polygon([(14,6),(14,9),(12,12),(9,13),(9,11),(11,10),(12,7)],3)
    line(s,8,(13,6),(13,9),(11,11),(9,12));dots(s,5,(13,7),(12,10),(10,12))
    s.polygon([(1,7),(3,8),(4,10),(7,11),(7,13),(3,12),(1,10)],3)
    line(s,5,(1,7),(2,10),(4,11),(6,12));dots(s,8,(3,10),(5,11))
    dots(s,2,(6,5),(10,6),(9,10),(5,8));dots(s,4,(1,2),(14,13))
    return s
def gravitic_implosion():
    s=blank(NIHILITY,(8,8))
    # Four inward vectors distinguish compression from the outward broken shock ring.
    s.polygon([(1,2),(3,1),(6,4),(7,3),(7,7),(3,7),(4,6)],3)
    s.polygon([(12,1),(14,2),(11,6),(12,7),(8,7),(8,3),(9,4)],3)
    s.polygon([(1,12),(2,14),(6,11),(7,12),(7,8),(3,8),(4,9)],3)
    s.polygon([(12,14),(14,12),(11,9),(12,8),(8,8),(8,12),(9,11)],3)
    for a,b in [((2,2),(5,5)),((13,2),(10,5)),((2,13),(5,10)),((13,13),(10,10))]:s.line(*a,*b,8)
    dots(s,9,(5,5),(6,6),(10,5),(9,6),(5,10),(6,9),(10,10),(9,9))
    dots(s,4,(4,6),(10,6),(4,9),(10,9));s.rect(7,7,8,8,0)
    dots(s,7,(6,7),(9,8),(8,6),(7,9))
    return s
def intangible():
    s=blank(NIHILITY,(8,8),'phase')
    # A complete hollow phase body; no outward-flying fragments.
    line(s,2,(3,4),(4,2),(7,1),(10,3),(11,6),(13,9))
    s.rect(6,2,9,5,3);s.line(6,2,8,2,8);s.line(6,3,6,4,5)
    s.rect(7,3,8,4,1);s.px(9,4,7)
    s.polygon([(5,6),(10,6),(12,8),(12,10),(10,10),(9,9),(6,9),(5,10),(3,10),(3,8)],3)
    line(s,8,(5,6),(4,7),(4,9));line(s,4,(10,6),(11,8),(11,9))
    s.rect(6,7,9,8,0);dots(s,5,(6,6),(7,6));dots(s,7,(9,8),(10,7))
    s.rect(5,10,6,13,3);s.rect(9,10,10,13,7)
    s.line(5,11,5,13,8);s.line(9,11,9,13,4)
    s.line(4,14,6,14,5);s.line(9,14,11,14,4)
    s.line(5,10,9,10,1);dots(s,8,(6,9),(9,9))
    return s
def spatial_rupture():
    s=blank(NIHILITY,(8,8),'fracture')
    outer=[(8,1),(6,4),(8,5),(5,8),(7,9),(5,13),(8,12),(9,14),(10,10),(9,8),(12,5),(10,4),(11,2)]
    s.polygon(outer,3)
    s.polygon([(8,2),(7,4),(9,5),(6,8),(8,9),(7,12),(9,11),(9,9),(8,8),(11,5),(9,4),(10,2)],0)
    line(s,5,(8,1),(6,4),(8,5),(5,8),(7,9),(5,13))
    line(s,8,(11,2),(10,4),(12,5),(9,8),(10,10),(9,13))
    dots(s,9,(7,3),(6,7),(6,11));dots(s,7,(10,6),(9,10),(8,12))
    dots(s,4,(2,5),(3,5),(13,10),(13,11));dots(s,8,(3,6),(12,12))
    return s
def void_walk():
    s=blank(NIHILITY,(8,9),'phase')
    # Two portals and a three-step path: entry and exit, rather than swapping figures.
    s.ellipse(1,6,6,14,3,fill=True);s.ellipse(2,8,5,12,0,fill=True)
    line(s,8,(3,6),(2,7),(1,9),(1,11),(2,13));dots(s,5,(2,8),(2,9),(3,6))
    dots(s,7,(5,7),(6,10),(5,13));s.px(4,14,4)
    s.ellipse(9,1,14,10,4,fill=True);s.ellipse(10,3,13,8,0,fill=True)
    line(s,5,(11,1),(10,2),(9,4),(9,7));line(s,8,(14,4),(14,7),(13,9),(11,10))
    dots(s,9,(10,3),(10,4));dots(s,7,(13,2),(13,8))
    dots(s,4,(6,12),(7,11),(8,10));dots(s,8,(7,13),(8,12),(9,11))
    return s

SPECS=[('duo_lunaris','双月',duo_lunaris),('moonlit_mending','月光修补',moonlit_mending),('nightfall','黄昏',nightfall),('piercing_moonlight','贯穿月光',piercing_moonlight),('dematerialize','非物质化',dematerialize),('edge_of_oblivion','遗忘边缘',edge_of_oblivion),('gravitic_implosion','重力内爆',gravitic_implosion),('intangible','无形',intangible),('spatial_rupture','空间断裂',spatial_rupture),('void_walk','虚空漫步',void_walk)]
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',16)
sheet=Image.new('RGB',(800,420),'#242733');draw=ImageDraw.Draw(sheet)
grid=Image.new('RGB',(800,420),'#242733');gd=ImageDraw.Draw(grid)
small=Image.new('RGB',(400,210),'#242733');sd=ImageDraw.Draw(small)
smallfont=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',10)
audit=[]
for index,(sid,name,paint) in enumerate(SPECS):
    sprite=paint();im=sprite.composite(1)
    assert im.size==(16,16) and im.getchannel('A').getextrema()==(255,255) and len(im.getcolors())<=12
    sprite.save_png(str(NATIVE/(sid+'.png')));sprite.save_png(str(PREVIEW/(sid+'.png')),scale=8)
    zoom=Image.open(PREVIEW/(sid+'.png')).convert('RGB')
    for y in range(16):
        for x in range(16):assert zoom.crop((x*8,y*8,x*8+8,y*8+8)).getcolors()==[(64,im.getpixel((x,y))[:3])]
    x=index%5*160+16;y=index//5*210+10
    sheet.paste(zoom,(x,y));grid.paste(zoom,(x,y))
    for n in range(17):
        gd.line((x+n*8,y,x+n*8,y+128),fill='#434957');gd.line((x,y+n*8,x+128,y+n*8),fill='#434957')
    for d in [draw,gd]:
        d.text((x,y+140),f'{index+21:02d} {name}',font=font,fill='#eeeeee');d.text((x,y+166),'16×16 · 细化版',font=font,fill='#b7c4d6')
    sx=index%5*80+8;sy=index//5*105+5;small.paste(im.resize((64,64),Image.Resampling.NEAREST),(sx,sy));sd.text((sx,sy+70),f'{index+21} {name}',font=smallfont,fill='#eeeeee')
    audit.append(dict(number=index+21,id=sid,name=name,width=16,height=16,colors=len(im.getcolors()),opaque=True,equal_pixel_blocks=True,preview_scale=8,sha256=hashlib.sha256((NATIVE/(sid+'.png')).read_bytes()).hexdigest()))
# Check the previous batch without modifying it.
previous=ROOT.parent/'spell-icons-16-detail-first20'
for row in json.loads((previous/'validation.json').read_text(encoding='utf8')):
    assert hashlib.sha256((previous/'native'/(row['id']+'.png')).read_bytes()).hexdigest()==row['sha256']
sheet.save(ROOT/'contact-sheet.png');grid.save(ROOT/'grid-proof.png');small.save(ROOT/'readability-4x.png')
(ROOT/'validation.json').write_text(json.dumps(audit,ensure_ascii=False,indent=2),encoding='utf8')
with zipfile.ZipFile(ROOT/'third10-native-16x16.zip','w',zipfile.ZIP_DEFLATED) as z:
    for sid,_,_ in SPECS:z.write(NATIVE/(sid+'.png'),'native/'+sid+'.png')
    for name in ['contact-sheet.png','grid-proof.png','validation.json']:z.write(ROOT/name,name)
print('PASS: 10 native 16x16 icons, opaque backgrounds, <=12 colors, equal 8x8 preview blocks; previous 20 unchanged; not installed.')
