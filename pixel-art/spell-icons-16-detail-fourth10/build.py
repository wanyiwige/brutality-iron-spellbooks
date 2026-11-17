"""Native-grid spell icon review batch 31-40. No installed resources are modified."""
from pathlib import Path
import sys,json,hashlib,zipfile
sys.path.insert(0,r'C:/Users/WANYIWIGE/.codex/skills/pixel-art-studio/scripts')
from pixelstudio import Sprite
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent
NATIVE=ROOT/'native';PREVIEW=ROOT/'preview'
for folder in [NATIVE,PREVIEW]:folder.mkdir(parents=True,exist_ok=True)
NIHILITY=['#100e19','#201a2d','#3c334b','#615672','#9585a7','#d6cadc','#30283f','#51435f','#b4a1c3','#f3e9e1','#756c89','#16131f']
CELESTIA=['#101722','#1c2b40','#50647f','#a87538','#f4ce77','#faf3db','#2f4055','#6e88a3','#d5a454','#fffbe9','#a4c1c8','#645237']
INFERNO=['#140c19','#2b1226','#641b37','#ae2e4d','#ef6870','#ffe6d4','#45182a','#83223f','#d34657','#ff9e9a','#b98d8d','#fae0b0']
WIND=['#0c1920','#172e38','#32505c','#5e7b87','#afcbd0','#edf4e8','#22414a','#436371','#88adb8','#d7e5dc','#61a9b4','#94d4d3']
ENDER=['#140b23','#29143a','#4a235d','#75378e','#b967c7','#f4d9ee','#381b4d','#9652a9','#d78edc','#fff0ed','#62407f','#b195d2']
GEO=['#19151a','#30272a','#4b4346','#736b68','#a59c8d','#e4dcc7','#44363a','#8b8276','#c5b89e','#f5e8c5','#b78a44','#f1cc77']
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
            r=(x-cx)**2+(y-cy)**2;s.px(x,y,6 if r<27 else 1 if r<95 else 0)
    if pattern=='wind':
        line(s,6,(1,4),(4,3),(7,4));line(s,2,(1,12),(3,14),(7,14))
    elif pattern=='rays':
        for end in [(1,1),(14,1),(1,14),(14,14)]:s.line(cx,cy,*end,6)
    elif pattern=='phase':
        s.line(1,6,5,6,6);s.line(10,10,14,10,6)
    return s

def event_horizon():
    s=blank(NIHILITY,(8,8))
    line(s,3,(1,10),(2,7),(6,3),(10,2),(13,3),(14,5))
    s.ellipse(4,3,12,11,3,fill=True);s.ellipse(5,4,11,10,0,fill=True)
    line(s,8,(5,5),(6,4),(8,3),(10,4));dots(s,4,(11,6),(11,8))
    s.polygon([(1,10),(3,12),(7,12),(12,8),(14,4),(12,8),(6,10),(3,10)],3)
    line(s,5,(1,10),(3,11),(6,11),(10,9),(12,7),(14,4))
    line(s,8,(3,12),(7,12),(11,9));dots(s,9,(5,11),(6,10),(11,8))
    dots(s,7,(2,8),(9,2),(13,4));dots(s,4,(7,13),(8,12))
    return s
def cruel_sun():
    s=blank(CELESTIA,(8,8),'rays')
    flames=[[(7,4),(7,1),(9,2),(9,5)],[(10,4),(13,2),(12,6)],[(11,7),(15,6),(14,9),(11,9)],[(10,11),(14,13),(11,13),(9,12)],[(6,11),(8,15),(5,14),(5,11)],[(4,10),(1,13),(2,9)],[(4,7),(0,6),(2,4),(5,5)]]
    for shape in flames:s.polygon(shape,3)
    for a,b in [((8,2),(8,5)),((12,4),(11,6)),((12,8),(14,7)),((10,11),(12,12)),((6,11),(7,13)),((3,10),(2,11)),((2,5),(4,6))]:s.line(*a,*b,4)
    s.ellipse(3,3,12,12,3,fill=True);s.ellipse(3,3,11,11,8,fill=True)
    s.polygon([(5,4),(9,4),(11,6),(11,9),(9,11),(5,10),(4,7)],4)
    s.polygon([(6,6),(9,5),(10,7),(10,9),(8,10),(6,9)],3)
    line(s,5,(4,7),(4,5),(6,4),(8,4));dots(s,9,(5,5),(5,6))
    dots(s,8,(8,7),(9,8),(6,10));dots(s,11,(7,8),(8,9))
    return s
def seeking_blades():
    s=blank(INFERNO,(7,8))
    for points in [[(2,13),(2,9),(3,6)],[(3,13),(5,10),(7,7)],[(3,13),(7,11),(10,9)],[(3,14),(8,14),(11,12)]]:line(s,2,*points)
    for points in [[(2,11),(3,8),(4,6)],[(4,12),(6,9),(8,7)],[(5,13),(8,11),(10,10)],[(7,14),(10,13)]]:line(s,7,*points)
    for x,y in [(1,1),(6,2),(10,5),(10,10)]:
        dots(s,3,(x+3,y+1),(x+2,y+2),(x+1,y+3),(x,y+4))
        dots(s,5,(x+3,y),(x+2,y+1),(x+1,y+2))
        dots(s,4,(x,y+2),(x+1,y+3),(x+2,y+4));dots(s,9,(x+2,y+1))
    return s
def stellar_marks():
    s=blank(CELESTIA,(8,8))
    for x,y in [(3,3),(12,3),(3,12),(12,12)]:
        s.line(x-1,y,x+1,y,4);s.line(x,y-1,x,y+1,4);dots(s,5,(x,y));dots(s,8,(x-1,y+1))
    line(s,11,(4,4),(6,6));line(s,11,(11,4),(9,6));line(s,11,(4,11),(6,9));line(s,11,(11,11),(9,9))
    s.polygon([(7,4),(8,6),(11,6),(9,8),(10,11),(7,9),(5,11),(6,8),(4,6),(7,6)],3)
    s.polygon([(7,6),(8,7),(10,7),(8,8),(8,10),(7,8),(5,7)],8)
    s.rect(7,7,8,8,5);dots(s,4,(7,5),(9,8),(6,9));dots(s,9,(7,7))
    return s
def gale_fist():
    s=blank(WIND,(9,8),'wind')
    line(s,3,(1,5),(3,5),(4,6));line(s,8,(1,10),(2,12),(4,13))
    s.polygon([(6,5),(8,3),(10,2),(12,3),(12,4),(14,5),(14,9),(12,11),(8,12),(5,10),(4,7)],2)
    s.polygon([(6,6),(8,4),(10,3),(11,4),(12,5),(13,5),(13,9),(11,10),(8,11),(6,9)],3)
    s.polygon([(7,5),(9,4),(10,4),(10,6),(12,5),(13,6),(13,8),(11,9),(8,8),(6,7)],4)
    line(s,5,(7,5),(9,3),(10,3));s.line(11,5,12,5,5)
    s.line(9,5,9,7,7);s.line(12,6,12,8,7)
    s.polygon([(7,8),(9,7),(11,8),(11,10),(9,11),(7,10)],8)
    dots(s,9,(8,8),(9,8));dots(s,2,(8,10),(10,10))
    s.polygon([(3,10),(5,9),(8,11),(6,14),(4,14),(2,12)],3)
    s.line(3,11,5,13,4);dots(s,11,(2,5),(4,6),(2,11))
    return s
def void_exchange():
    s=blank(ENDER,(8,8),'phase')
    # Two complete silhouettes; short opposed arrows never close into a generic ring.
    for x,y,shade in [(1,4,4),(10,5,8)]:
        s.rect(x+1,y,x+2,y+1,shade);s.rect(x+1,y+2,x+2,y+4,3)
        dots(s,shade,(x,y+3),(x+3,y+3),(x+1,y+5),(x+2,y+5));s.px(x+1,y,5)
        s.line(x+1,y+2,x+1,y+4,8)
    s.line(5,2,10,2,4);s.polygon([(9,1),(11,2),(9,3)],8);dots(s,5,(10,2))
    s.line(5,13,10,13,4);s.polygon([(6,12),(4,13),(6,14)],8);dots(s,5,(5,13))
    return s
def ceiling_ascent():
    s=blank(NIHILITY,(8,7),'phase')
    s.rect(1,5,14,8,2)
    for points in [[(1,5),(5,5),(5,7)],[(6,5),(10,5)],[(11,6),(14,6)],[(2,8),(4,8)],[(10,8),(13,8)]]:line(s,3,*points)
    dots(s,7,(2,6),(6,8),(12,5));dots(s,4,(3,5),(11,6))
    s.polygon([(8,1),(12,4),(9,4),(9,10),(7,10),(7,4),(4,4)],3)
    s.polygon([(8,1),(10,3),(8,3),(8,9),(7,9),(7,3),(6,3)],8)
    s.line(8,2,8,7,9);dots(s,5,(7,3),(8,8))
    s.rect(6,11,8,12,4);s.rect(5,13,9,14,3);dots(s,8,(6,11),(6,13));dots(s,2,(7,14))
    return s
def inertia_reversal():
    s=blank(WIND,(9,7),'wind')
    s.polygon([(4,2),(10,2),(13,5),(13,10),(10,13),(7,13),(7,10),(9,10),(10,8),(10,6),(8,5),(4,5),(4,7),(1,4),(4,1)],3)
    line(s,4,(4,3),(9,3),(12,5),(12,9),(10,11),(8,11))
    s.polygon([(2,4),(4,2),(4,3),(8,3),(8,4),(4,4),(4,5)],5)
    dots(s,9,(3,4),(9,3));dots(s,11,(11,5),(12,7),(10,11))
    dots(s,7,(13,6),(12,10),(9,13));s.line(1,10,5,10,2);s.line(2,13,5,13,7)
    return s
def gale_recoil():
    s=blank(WIND,(8,9),'wind')
    s.polygon([(6,3),(9,3),(9,7),(11,9),(11,11),(7,12),(3,12),(2,10),(6,8)],2)
    s.polygon([(6,4),(8,4),(8,8),(10,9),(10,10),(7,11),(3,11),(3,10),(7,8)],3)
    s.line(6,4,6,7,4);line(s,5,(6,8),(4,9),(3,10),(5,10))
    dots(s,8,(7,4),(7,6),(8,8),(9,10));dots(s,7,(8,5),(8,7),(6,11))
    s.line(3,12,8,12,4);s.px(9,11,9)
    line(s,4,(12,6),(14,7),(14,11),(12,13),(9,14));dots(s,5,(14,8),(13,12))
    line(s,7,(1,5),(3,5),(4,6));s.polygon([(1,5),(3,3),(3,6)],8)
    return s
def guardian_hand():
    s=blank(GEO,(8,9))
    s.polygon([(4,7),(12,7),(14,9),(13,12),(10,14),(5,14),(3,11)],2)
    s.polygon([(4,8),(11,8),(13,10),(11,12),(9,13),(5,13),(4,11)],3)
    for x,y,w in [(4,3,1),(7,1,1),(10,3,1),(13,6,1)]:
        s.rect(x,y,x+w,y+6,3);s.line(x,y+1,x,y+4,4);s.line(x,y,x+w-1,y,5)
        s.line(x,y+3,x+w,y+3,2);dots(s,7,(x+1,y+4))
    s.polygon([(1,7),(3,7),(5,10),(4,12),(2,11),(1,9)],3)
    line(s,4,(1,8),(2,8),(3,10));dots(s,5,(1,7),(4,4),(7,2))
    s.rect(5,13,10,14,3);s.line(5,13,7,13,8)
    s.ellipse(6,7,11,12,10,fill=True);s.ellipse(7,8,10,11,11,fill=True)
    dots(s,9,(8,8),(8,9),(9,9));dots(s,8,(7,11),(10,10))
    line(s,10,(11,7),(13,5),(14,4));dots(s,11,(13,5),(14,4))
    return s

SPECS=[('event_horizon','引力黑洞',event_horizon),('cruel_sun','残酷太阳',cruel_sun),('seeking_blades','追踪剑光',seeking_blades),('stellar_marks','星印引爆',stellar_marks),('gale_fist','火箭拳',gale_fist),('void_exchange','末影换位',void_exchange),('ceiling_ascent','穿顶升行',ceiling_ascent),('inertia_reversal','惯性逆转',inertia_reversal),('gale_recoil','烈风退跃',gale_recoil),('guardian_hand','守护者之手',guardian_hand)]
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
        for x in range(16):assert zoom.crop((8*x,8*y,8*x+8,8*y+8)).getcolors()==[(64,im.getpixel((x,y))[:3])]
    x=index%5*160+16;y=index//5*210+10
    sheet.paste(zoom,(x,y));grid.paste(zoom,(x,y))
    for n in range(17):
        gd.line((x+n*8,y,x+n*8,y+128),fill='#434957');gd.line((x,y+n*8,x+128,y+n*8),fill='#434957')
    for d in [draw,gd]:
        d.text((x,y+140),f'{index+31:02d} {name}',font=font,fill='#eeeeee');d.text((x,y+166),'16×16 · 细化版',font=font,fill='#b7c4d6')
    sx=index%5*80+8;sy=index//5*105+5;small.paste(im.resize((64,64),Image.Resampling.NEAREST),(sx,sy));sd.text((sx,sy+70),f'{index+31} {name}',font=smallfont,fill='#eeeeee')
    audit.append(dict(number=index+31,id=sid,name=name,width=16,height=16,colors=len(im.getcolors()),opaque=True,equal_pixel_blocks=True,preview_scale=8,sha256=hashlib.sha256((NATIVE/(sid+'.png')).read_bytes()).hexdigest()))
for name in ['spell-icons-16-detail-first20','spell-icons-16-detail-third10']:
    previous=ROOT.parent/name
    for row in json.loads((previous/'validation.json').read_text(encoding='utf8')):
        assert hashlib.sha256((previous/'native'/(row['id']+'.png')).read_bytes()).hexdigest()==row['sha256']
sheet.save(ROOT/'contact-sheet.png');grid.save(ROOT/'grid-proof.png');small.save(ROOT/'readability-4x.png')
(ROOT/'validation.json').write_text(json.dumps(audit,ensure_ascii=False,indent=2),encoding='utf8')
with zipfile.ZipFile(ROOT/'fourth10-native-16x16.zip','w',zipfile.ZIP_DEFLATED) as z:
    for sid,_,_ in SPECS:z.write(NATIVE/(sid+'.png'),'native/'+sid+'.png')
    for name in ['contact-sheet.png','grid-proof.png','validation.json']:z.write(ROOT/name,name)
print('PASS: ten native 16x16 opaque icons; equal pixel blocks; prior thirty untouched; not installed.')
