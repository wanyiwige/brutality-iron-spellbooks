"""Fresh compositions for spells 31-40. No old icon/image inputs or paint-function imports."""
from pathlib import Path
import sys,json,hashlib,zipfile
sys.path.insert(0,r'C:/Users/WANYIWIGE/.codex/skills/pixel-art-studio/scripts')
from pixelstudio import Sprite
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent
for folder in ['native','preview']:(ROOT/folder).mkdir(parents=True,exist_ok=True)
# Index roles: 0-2 backdrop, 3-7 material shadow -> light, 8-10 secondary ramp, 11 glint.
VOID=['#0a0a12','#171321','#2d223b','#392f49','#605474','#88749e','#b7a0cb','#dfd1e4','#443354','#77639b','#b5a8d7','#fff2e9']
HOLY=['#16151e','#272334','#433342','#653b35','#996142','#cc914e','#f4bc63','#ffe3a1','#48556a','#8491a4','#d5dce0','#fff8d4']
INFERNO=['#130b13','#2b101d','#51162c','#5d233e','#96364e','#c45a69','#ef8b8d','#ffe2c2','#71647b','#a59bb3','#d5d0d0','#fff5de']
WIND=['#0b141c','#182935','#254553','#334b58','#567180','#86a5ae','#bbd3d3','#e4eee3','#286578','#4c9dae','#8cd2d6','#fbf7df']
ENDER=['#11091c','#271332','#3d1d4f','#4c285c','#794590','#a46bb2','#d19dd6','#efd3ea','#51466f','#8886b6','#c7c5e1','#fff1ed']
EARTH=['#11151b','#252b31','#3d4444','#363c43','#586262','#818d83','#b2b7a0','#e3ddbd','#725f41','#b79752','#e9ca7d','#fff1c8']
def canvas(palette):
    s=Sprite(16,16,palette=palette)
    for y in range(16):
        for x in range(16):s.px(x,y,1 if x+y<20 else 0)
    return s
def px(s,c,*points):
    for x,y in points:
        assert type(x)==int and type(y)==int and 0<=x<16 and 0<=y<16
        s.px(x,y,c)
def stroke(s,c,*points):
    for a,b in zip(points,points[1:]):s.line(*a,*b,c)

def event_horizon():
    s=canvas(VOID)
    # Face-on, three curved infalling streams. The core stays truly dark.
    s.polygon([(3,2),(9,1),(13,4),(14,9),(11,13),(5,14),(1,10),(1,5)],2)
    stroke(s,4,(1,6),(2,3),(5,2),(9,2),(11,4),(11,6))
    stroke(s,6,(2,6),(3,4),(6,3),(9,4),(10,5));px(s,7,(4,3),(5,3));px(s,11,(4,4))
    stroke(s,4,(14,7),(14,10),(12,13),(8,14),(6,12),(6,10))
    stroke(s,5,(13,7),(13,10),(11,12),(8,13),(7,11));px(s,6,(12,11),(10,12))
    stroke(s,4,(4,14),(2,12),(1,9),(3,7),(5,7))
    stroke(s,7,(3,12),(2,10),(3,8),(5,8));px(s,6,(4,12),(5,11))
    s.ellipse(5,5,10,10,3,fill=True);s.ellipse(6,6,10,10,0,fill=True)
    px(s,5,(5,6),(6,5),(8,5));px(s,8,(10,7),(9,10))
    return s
def cruel_sun():
    s=canvas(HOLY)
    s.polygon([(2,4),(5,1),(10,2),(14,5),(13,12),(9,15),(3,13),(0,8)],2)
    # An incandescent sphere: broad hot upper face, amber lower-right volume.
    corona=[[(3,5),(1,3),(5,4)],[(6,3),(7,0),(9,3)],[(10,4),(14,2),(12,6)],[(12,8),(15,9),(12,11)],[(9,12),(11,15),(7,13)],[(4,11),(1,13),(3,9)]]
    for q in corona:s.polygon(q,4)
    s.ellipse(2,3,12,13,4,fill=True);s.ellipse(2,3,11,11,5,fill=True)
    s.polygon([(4,4),(8,3),(11,6),(10,9),(7,11),(3,9),(2,6)],6)
    s.polygon([(4,4),(7,4),(9,5),(8,7),(5,8),(3,7)],7)
    s.rect(4,5,6,6,11);px(s,11,(3,6),(6,4));px(s,7,(9,6),(4,9))
    px(s,5,(9,8),(8,10),(5,10));px(s,3,(10,10),(9,12),(11,9))
    stroke(s,6,(2,4),(3,5));stroke(s,7,(11,4),(13,3));px(s,6,(13,9),(9,14))
    return s
def seeking_blades():
    s=canvas(INFERNO)
    # Four separately oriented daggers steer towards a central target.
    for q in [[(1,8),(1,3),(3,1)],[(9,1),(13,2),(14,4)],[(14,9),(14,13),(12,14)],[(5,14),(1,13),(1,10)]]:stroke(s,2,*q)
    blades=[([(2,2),(6,4),(6,6),(4,5)],[(3,2),(5,4),(5,5)]),
            ([(12,2),(13,5),(10,7),(10,5)],[(12,3),(12,5),(11,6)]),
            ([(13,13),(9,11),(9,9),(11,10)],[(12,13),(10,11),(10,10)]),
            ([(2,12),(3,8),(6,8),(4,10)],[(3,11),(4,9),(5,9)])]
    for shape,edge in blades:
        s.polygon(shape,8);stroke(s,10,*edge);s.px(*edge[0],11)
    stroke(s,4,(2,4),(4,2));stroke(s,4,(11,2),(14,4));stroke(s,4,(11,13),(13,11));stroke(s,4,(1,10),(3,12))
    px(s,6,(3,3),(13,3),(12,12),(2,11));s.rect(7,7,8,8,3);px(s,5,(7,7),(8,8))
    return s
def stellar_marks():
    s=canvas(HOLY)
    # Three suspended seals lead into a fourth, detonating star at the right.
    stroke(s,2,(2,3),(4,7),(3,11),(6,13),(10,9))
    for x,y in [(3,3),(2,8),(5,13)]:
        s.line(x-1,y,x+1,y,5);s.line(x,y-1,x,y+1,6);px(s,11,(x,y));px(s,4,(x+1,y+1))
    s.polygon([(11,3),(12,6),(15,6),(13,8),(14,12),(11,10),(8,12),(9,9),(6,7),(9,6)],4)
    s.polygon([(11,5),(12,7),(14,7),(12,8),(12,10),(10,9),(8,8),(10,7)],6)
    px(s,7,(10,6),(11,7),(12,8),(10,8));px(s,11,(10,7),(11,8))
    px(s,5,(8,5),(13,5),(13,11));stroke(s,3,(5,5),(7,6));stroke(s,5,(5,10),(7,9))
    return s
def gale_fist():
    s=canvas(WIND)
    stroke(s,2,(0,7),(2,4),(5,3),(8,4));stroke(s,8,(1,10),(3,11),(6,10))
    # Side view: foreshortened forearm into a three-knuckle gauntlet.
    s.polygon([(2,2),(6,2),(10,6),(12,6),(14,8),(14,12),(12,14),(9,13),(6,9),(3,8),(1,5)],3)
    s.polygon([(2,3),(5,3),(9,7),(10,10),(8,10),(5,7),(3,7),(2,5)],5)
    stroke(s,7,(2,3),(4,3),(8,7));stroke(s,6,(2,5),(4,6),(6,8))
    s.polygon([(10,6),(12,7),(13,8),(12,9),(14,10),(13,12),(11,13),(8,10),(8,8)],4)
    s.polygon([(10,7),(12,8),(11,9),(13,10),(12,11),(10,11),(9,9)],6)
    px(s,11,(10,7),(12,8));px(s,7,(11,10),(12,10))
    stroke(s,3,(9,9),(10,9),(12,11));px(s,5,(13,12),(10,12))
    stroke(s,9,(1,1),(3,1),(5,2));px(s,10,(1,8),(2,9),(4,9))
    return s
def void_exchange():
    s=canvas(ENDER)
    # The positions are diagonally opposed on two lit pads, with bent transfer paths.
    s.ellipse(1,12,7,14,3,fill=True);s.line(2,12,6,12,6)
    s.ellipse(9,7,14,9,3,fill=True);s.line(10,7,13,7,6)
    s.rect(3,7,4,8,6);s.rect(3,9,5,11,4);px(s,7,(3,7),(3,9));px(s,5,(2,10),(6,10),(3,12),(5,12))
    s.rect(11,2,12,3,7);s.rect(10,4,12,6,5);px(s,11,(11,2));px(s,6,(9,5),(13,5),(10,7),(12,7))
    stroke(s,4,(3,5),(3,3),(5,1),(7,1));s.polygon([(6,0),(8,1),(6,3)],6);px(s,7,(7,1))
    stroke(s,4,(10,10),(12,11),(12,13),(10,14));s.polygon([(11,12),(8,14),(11,15)],6);px(s,7,(9,14))
    px(s,8,(6,7),(8,9));px(s,10,(7,6),(8,10))
    return s
def ceiling_ascent():
    s=canvas(VOID)
    # Cutaway stone shelf with an emerging figure; no central arrow template.
    s.polygon([(1,7),(5,5),(13,5),(15,7),(11,9),(1,9)],5)
    s.polygon([(1,9),(11,9),(15,7),(15,10),(11,12),(1,12)],3)
    stroke(s,6,(2,7),(5,6),(12,6));stroke(s,4,(2,10),(5,10),(5,11));stroke(s,2,(11,9),(11,11))
    stroke(s,2,(3,9),(3,11));stroke(s,4,(12,10),(14,9));px(s,7,(3,7),(12,6))
    stroke(s,2,(4,8),(4,9),(7,9));px(s,6,(2,8),(13,7));px(s,7,(4,6),(12,6))
    s.rect(7,1,9,3,5);px(s,11,(7,1),(8,1));px(s,7,(7,2),(8,2))
    s.polygon([(7,4),(10,4),(11,6),(10,9),(7,10),(6,7)],5)
    stroke(s,7,(7,4),(7,7),(8,8));stroke(s,6,(10,5),(10,7))
    stroke(s,6,(6,5),(5,3));stroke(s,6,(11,5),(12,3));px(s,11,(5,2),(12,2))
    px(s,4,(7,11),(9,11));px(s,6,(7,13),(9,12));px(s,7,(8,14))
    return s
def inertia_reversal():
    s=canvas(WIND)
    # Strong outgoing vector opposes a darker incoming velocity, at different scales.
    s.polygon([(1,5),(5,1),(5,3),(13,3),(13,6),(5,6),(5,8)],4)
    s.polygon([(2,5),(4,3),(4,4),(12,4),(12,5),(4,5),(4,6)],6)
    stroke(s,11,(3,4),(4,3));s.line(5,4,11,4,7);px(s,5,(12,6),(6,6))
    s.polygon([(3,10),(10,10),(10,8),(14,12),(10,15),(10,13),(3,13)],3)
    s.polygon([(4,11),(11,11),(11,10),(13,12),(11,13),(11,12),(4,12)],9)
    px(s,10,(12,12),(10,11));s.line(4,12,8,12,8)
    px(s,2,(1,9),(2,10),(13,1),(14,2));s.line(6,8,8,8,2)
    return s
def gale_recoil():
    s=canvas(WIND)
    # An airborne whole figure, tilted away from a forceful right-hand gust.
    s.rect(3,2,5,4,5);px(s,7,(3,2),(4,2));px(s,6,(3,3))
    s.polygon([(4,5),(6,5),(8,7),(7,9),(5,8),(3,6)],4)
    stroke(s,6,(4,5),(5,6),(6,7));px(s,7,(4,5))
    stroke(s,5,(3,6),(1,6),(1,4));stroke(s,5,(6,5),(8,4))
    stroke(s,5,(5,8),(4,10),(2,11));stroke(s,6,(7,8),(8,10),(10,10));px(s,7,(2,11),(9,10))
    s.polygon([(10,3),(13,4),(15,7),(14,11),(11,14),(7,14),(7,12),(10,12),(12,10),(13,7),(11,6),(9,6)],8)
    stroke(s,10,(10,4),(13,5),(14,7),(13,10),(10,13),(8,13))
    stroke(s,7,(11,5),(12,6),(12,8));px(s,6,(13,9),(11,11))
    s.polygon([(7,5),(10,3),(10,7)],9);px(s,11,(8,5))
    return s
def guardian_hand():
    s=canvas(EARTH)
    s.polygon([(3,8),(8,4),(13,5),(14,9),(10,13),(5,15),(1,12)],2)
    # Reaching diagonal back of a hand, with four foreshortened stone fingers.
    s.polygon([(1,12),(4,10),(7,12),(5,15),(2,14)],3)
    s.polygon([(2,12),(4,11),(6,12),(4,14),(2,13)],5);px(s,7,(2,12),(3,11))
    s.polygon([(4,7),(8,5),(12,7),(11,11),(7,13),(3,10)],4)
    s.polygon([(4,7),(8,6),(10,7),(9,10),(6,11),(4,9)],5)
    s.polygon([(4,7),(8,6),(8,8),(6,9),(4,8)],6)
    stroke(s,3,(5,11),(7,12),(10,10))
    fingers=[[(6,6),(7,3),(8,2),(9,3),(8,6)],[(9,5),(10,2),(12,2),(12,4),(11,6)],[(11,6),(13,4),(14,5),(14,7),(12,8)],[(12,8),(14,7),(15,8),(14,10),(12,10)]]
    for q in fingers:s.polygon(q,4)
    stroke(s,6,(7,5),(8,3));stroke(s,7,(10,3),(11,2));stroke(s,6,(12,6),(13,5));px(s,6,(14,8))
    stroke(s,3,(7,5),(8,6));stroke(s,3,(10,4),(11,5));stroke(s,3,(12,7),(13,8))
    s.polygon([(2,6),(4,6),(6,8),(5,10),(3,8),(2,8)],4);stroke(s,6,(2,6),(4,7),(5,8))
    s.polygon([(7,7),(10,8),(9,11),(6,10)],8);s.polygon([(7,8),(9,8),(9,10),(7,10)],9)
    px(s,10,(7,8),(8,8),(7,9));px(s,11,(7,8));px(s,3,(5,10),(9,11))
    return s

SPECS=[('event_horizon','引力黑洞',event_horizon),('cruel_sun','残酷太阳',cruel_sun),('seeking_blades','追踪剑光',seeking_blades),('stellar_marks','星印引爆',stellar_marks),('gale_fist','火箭拳',gale_fist),('void_exchange','末影换位',void_exchange),('ceiling_ascent','穿顶升行',ceiling_ascent),('inertia_reversal','惯性逆转',inertia_reversal),('gale_recoil','烈风退跃',gale_recoil),('guardian_hand','守护者之手',guardian_hand)]
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',16)
sheet=Image.new('RGB',(800,420),'#242733');draw=ImageDraw.Draw(sheet)
grid=Image.new('RGB',(800,420),'#242733');gd=ImageDraw.Draw(grid)
small=Image.new('RGB',(400,210),'#242733');sd=ImageDraw.Draw(small);sf=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',10)
report=[]
for i,(sid,name,paint) in enumerate(SPECS):
    s=paint();im=s.composite(1)
    assert im.size==(16,16) and im.getchannel('A').getextrema()==(255,255) and len(im.getcolors())<=12
    s.save_png(str(ROOT/'native'/(sid+'.png')));s.save_png(str(ROOT/'preview'/(sid+'.png')),scale=8)
    zoom=im.convert('RGB').resize((128,128),Image.Resampling.NEAREST)
    for y in range(16):
        for x in range(16):assert zoom.crop((x*8,y*8,x*8+8,y*8+8)).getcolors()==[(64,im.getpixel((x,y))[:3])]
    x=i%5*160+16;y=i//5*210+10
    for target in [sheet,grid]:target.paste(zoom,(x,y))
    for n in range(17):
        gd.line((x+n*8,y,x+n*8,y+128),fill='#434957');gd.line((x,y+n*8,x+128,y+n*8),fill='#434957')
    for d in [draw,gd]:
        d.text((x,y+140),f'{i+31:02d} {name}',font=font,fill='#eeeeee');d.text((x,y+166),'16×16 · 重新绘制',font=font,fill='#b7c4d6')
    sx=i%5*80+8;sy=i//5*105+5;small.paste(im.resize((64,64),Image.Resampling.NEAREST),(sx,sy));sd.text((sx,sy+70),f'{i+31} {name}',font=sf,fill='#eeeeee')
    report.append(dict(number=i+31,id=sid,name=name,width=16,height=16,opaque=True,uniform_pixels=True,colors=len(im.getcolors()),old_art_inputs=[],sha256=hashlib.sha256((ROOT/'native'/(sid+'.png')).read_bytes()).hexdigest()))
sheet.save(ROOT/'contact-sheet.png');grid.save(ROOT/'grid-proof.png');small.save(ROOT/'readability-4x.png')
(ROOT/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
with zipfile.ZipFile(ROOT/'fresh-31-40-native16.zip','w',zipfile.ZIP_DEFLATED) as z:
    for sid,_,_ in SPECS:z.write(ROOT/'native'/(sid+'.png'),'native/'+sid+'.png')
    for name in ['contact-sheet.png','grid-proof.png','validation.json']:z.write(ROOT/name,name)
print('PASS: ten new native-grid compositions; no previous art inputs; opaque 16x16; 12-color limit; uniform pixels; not installed.')
