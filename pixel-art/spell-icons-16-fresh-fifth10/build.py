"""Fresh spell icon compositions 41-50, drawn directly on 16x16 integer grids."""
from pathlib import Path
import sys,json,hashlib,zipfile
sys.path.insert(0,r'C:/Users/WANYIWIGE/.codex/skills/pixel-art-studio/scripts')
from pixelstudio import Sprite
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent
for folder in ['native','preview']:(ROOT/folder).mkdir(parents=True,exist_ok=True)
CRYSTAL=['#151324','#25233d','#3b3151','#482848','#734272','#a66aa0','#d29ac4','#f5d4e6','#765a40','#bd9c63','#f0d695','#fff4e9']
HOLY=['#16151e','#272334','#433342','#653b35','#996142','#cc914e','#f4bc63','#ffe3a1','#48556a','#8491a4','#d5dce0','#fff8d4']
INFERNO=['#130b13','#2b101d','#51162c','#4c203c','#843752','#bd536d','#ec8491','#ffe0cf','#5d4a6b','#99819e','#d3b7c7','#fff4e5']
STONE=['#10181d','#202c30','#344143','#3b4948','#586860','#80917e','#b5bfa3','#e2dfc1','#384d45','#647f68','#b5bb78','#f5edbd']
EVOCATION=['#0b1225','#172746','#294468','#234777','#326db0','#5c9fd2','#94d5e5','#d1f3ef','#42475c','#758c9e','#a1b7be','#fff9d9']
ELDRITCH=['#120c21','#211238','#3a2056','#452368','#69388a','#9c65b8','#c699db','#ead4ee','#542b62','#875176','#c084ad','#fff0e1']
PLAGUE=['#0b1515','#142723','#263c2b','#243c25','#3c612d','#628c3b','#94bb52','#c8d981','#263333','#506441','#9bbf77','#edf1bf']
RAINBOW=['#0d1528','#172944','#2a3c57','#ed8493','#f4cc77','#9acb96','#8dcbd8','#a798d9','#dee5dd','#699dc5','#586878','#fff3db']
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

def crystal_sanctuary():
    s=blank(CRYSTAL)
    s.ellipse(1,9,14,14,8,fill=False);path(s,10,(2,11),(4,10),(11,10),(13,11))
    path(s,9,(2,13),(5,14),(10,14),(13,12));dots(s,11,(3,11),(12,11))
    s.polygon([(7,3),(9,1),(11,4),(10,10),(8,12),(6,9)],3)
    s.polygon([(9,2),(10,4),(9,10),(8,11),(8,5)],5)
    s.polygon([(8,3),(9,2),(8,6),(8,10),(7,8)],7)
    path(s,6,(10,5),(10,8),(9,10));dots(s,11,(8,3),(8,4));dots(s,4,(6,8),(7,10))
    s.polygon([(2,8),(4,5),(6,8),(6,11),(4,12),(2,10)],4)
    s.polygon([(4,6),(4,9),(5,11),(3,10)],6);dots(s,7,(3,7),(3,8));dots(s,3,(5,8),(5,9))
    s.polygon([(11,7),(13,5),(14,8),(12,11),(10,11)],4)
    path(s,6,(13,6),(12,8),(11,10));dots(s,7,(13,6),(12,7))
    s.line(2,2,2,4,9);s.line(1,3,3,3,10);dots(s,11,(2,3))
    return s
def triple_judgment():
    s=blank(HOLY)
    for x,y,w,h in [(2,1,2,6),(6,2,3,8),(10,3,4,11)]:
        s.rect(x,y,x+w-1,h,3)
        s.rect(x,y,x+w-2,h,6)
        s.line(x,y,x,h,11)
        if w>2:s.line(x+1,y+1,x+1,h-1,7)
        s.ellipse(x-1,h,x+w,h+2,4,fill=False)
        s.line(x,h,x+w-1,h,7)
    dots(s,10,(4,2),(9,4));dots(s,5,(5,11),(9,13))
    path(s,2,(0,4),(2,3));path(s,2,(7,1),(9,2),(14,1));dots(s,11,(11,13),(12,13))
    return s
def stygian_souls():
    s=blank(INFERNO)
    # Three distinct faces, unequal scale and curling rather than flame-only tails.
    s.polygon([(6,2),(9,2),(11,4),(10,6),(9,7),(10,9),(8,8),(7,6),(5,5)],3)
    s.polygon([(7,2),(9,3),(10,4),(9,6),(7,6),(6,4)],5)
    dots(s,10,(7,3),(8,3),(9,4),(7,5));dots(s,3,(7,4),(9,4));dots(s,6,(9,6),(9,7))
    s.polygon([(2,6),(5,6),(7,8),(6,10),(5,11),(7,13),(9,14),(5,14),(3,12),(1,9)],4)
    s.polygon([(2,7),(5,7),(6,8),(5,10),(3,10),(2,9)],6)
    dots(s,7,(2,7),(3,7),(3,9),(5,8));dots(s,3,(3,8),(5,8));path(s,5,(4,11),(5,12),(7,13));dots(s,10,(3,7))
    s.polygon([(11,8),(13,8),(14,9),(14,11),(12,12),(14,14),(11,14),(10,12),(10,10)],3)
    s.polygon([(11,9),(13,9),(13,11),(11,11)],5)
    dots(s,10,(11,9),(12,9));dots(s,3,(12,10));dots(s,6,(11,12),(12,13));dots(s,2,(1,13),(12,3))
    return s
def stone_retinue():
    s=blank(STONE)
    # Rear guardian is smaller; front guardian has a carved brow and a blocky chest.
    s.polygon([(1,5),(3,3),(6,4),(7,8),(6,11),(2,11),(0,9)],3)
    s.polygon([(2,5),(3,4),(5,4),(6,6),(5,8),(2,7)],5)
    path(s,6,(2,5),(3,4),(5,5));s.line(2,6,5,6,3);dots(s,10,(3,6),(5,6));dots(s,8,(4,8))
    s.rect(2,9,5,12,4);s.line(2,10,4,10,6);s.line(1,13,3,13,3);s.line(5,13,6,13,3)
    dots(s,1,(4,12),(4,13));dots(s,5,(2,13),(5,12))
    s.polygon([(8,3),(11,2),(14,4),(14,8),(12,10),(8,9),(7,6)],3)
    s.polygon([(8,4),(11,3),(13,4),(12,7),(9,8),(8,6)],5)
    s.polygon([(8,4),(10,3),(10,5),(8,6)],6);dots(s,7,(9,3),(8,4))
    s.line(9,5,13,5,3);dots(s,10,(9,6),(12,6));dots(s,3,(11,7));dots(s,6,(10,8),(12,8))
    s.polygon([(8,9),(12,9),(14,11),(13,13),(8,14),(6,12)],4)
    s.polygon([(8,9),(11,10),(11,12),(8,12),(7,11)],5)
    path(s,6,(7,10),(8,9),(10,10));s.line(12,11,12,13,3);s.line(7,14,9,14,6);s.line(11,14,13,14,5)
    dots(s,1,(10,13),(10,14));dots(s,3,(11,6));dots(s,7,(9,4))
    return s
def spirit_bomb():
    s=blank(EVOCATION)
    s.ellipse(2,0,14,12,2,fill=True);s.ellipse(3,1,13,11,3,fill=True)
    s.ellipse(3,1,12,10,4,fill=True)
    s.polygon([(5,2),(9,2),(12,5),(11,8),(8,10),(4,8),(3,5)],5)
    s.polygon([(5,2),(8,2),(10,4),(9,6),(6,7),(4,5)],6)
    s.polygon([(5,3),(8,3),(9,4),(7,5),(5,5)],7);dots(s,11,(5,3),(6,3),(4,4))
    path(s,4,(8,9),(10,8),(11,6));dots(s,6,(10,4),(6,8));dots(s,3,(11,9),(9,10))
    path(s,7,(4,6),(5,7),(7,7));dots(s,5,(12,3),(3,8))
    path(s,8,(1,11),(3,13),(6,14));path(s,9,(2,11),(4,13),(6,13))
    path(s,8,(14,11),(12,13),(10,14));path(s,9,(13,11),(11,13),(10,13))
    s.rect(7,12,8,13,8);s.rect(6,14,9,15,8);dots(s,10,(7,12),(3,12),(12,12))
    return s
def triple_profane_beam():
    s=blank(ELDRITCH)
    # Near beam at the left, with staggered diminishing columns receding to the right.
    for x,y,w,base in [(2,2,3,12),(7,1,2,9),(12,0,2,5)]:
        s.polygon([(x,y),(x+w-1,y),(x+w,base-1),(x+w-1,base),(x,base),(x-1,base-2)],3)
        s.line(x,y,x,base-1,6)
        s.line(x+1,y+1,x+1,base-1,4)
        s.line(x,y+2,x,base-2,7)
        dots(s,11,(x,y+2),(x,base-2))
        s.ellipse(x-1,base,x+w,base+2,5,fill=False);s.line(x,base,x+w-1,base,6)
    path(s,8,(1,13),(2,14),(5,14),(6,12));dots(s,10,(6,10),(10,8),(14,4))
    dots(s,2,(0,7),(5,4),(10,2));dots(s,6,(3,13),(8,10))
    return s
def caustic_wave():
    s=blank(PLAGUE)
    # Corrosive crescent, with a thick dark underside and a sharp lit upper face.
    s.polygon([(2,13),(7,10),(10,7),(11,3),(14,1),(14,6),(12,10),(8,13),(4,14)],3)
    s.polygon([(3,13),(8,10),(11,7),(12,4),(14,2),(13,7),(11,10),(7,12)],5)
    path(s,7,(4,13),(8,11),(11,8),(13,5),(14,2))
    path(s,6,(6,12),(10,10),(12,7));dots(s,11,(10,9),(12,6),(13,4))
    dots(s,4,(3,13),(8,12),(11,11),(13,8));dots(s,8,(10,7),(11,5))
    path(s,4,(1,8),(3,9),(5,8),(6,6));path(s,5,(2,11),(3,10),(4,10))
    s.rect(2,3,3,4,4);dots(s,7,(2,3));dots(s,5,(6,3),(7,4));dots(s,6,(4,6))
    return s
def soulreaping_scythes():
    s=blank(INFERNO)
    # Sweeping reaping edge with a long haft and a distinct spectral recoil trail.
    path(s,3,(1,7),(1,10),(3,13),(6,14),(9,13));path(s,4,(2,9),(3,11),(6,13))
    s.line(3,14,10,5,3);s.line(4,14,10,6,5);dots(s,6,(5,12),(7,10))
    s.polygon([(3,3),(6,1),(10,1),(13,4),(14,7),(12,10),(9,11),(11,8),(11,5),(8,3)],8)
    s.polygon([(4,3),(6,2),(10,2),(12,4),(13,7),(11,9),(12,6),(10,4),(8,3)],5)
    path(s,10,(4,3),(7,1),(10,2),(12,4),(13,6));dots(s,11,(6,2),(8,2))
    dots(s,4,(12,7),(11,9),(9,10));dots(s,7,(11,4),(12,5));dots(s,9,(7,3),(10,3))
    dots(s,6,(2,10),(4,12),(8,14));dots(s,2,(1,3),(14,12))
    return s
def devil_armament():
    s=blank(INFERNO)
    # Two differently shaped main weapons rise from a conjuration flare.
    s.ellipse(2,11,13,15,3,fill=False);path(s,4,(3,13),(6,14),(10,14),(12,12))
    s.line(4,14,12,4,3);s.line(5,14,12,5,9)
    s.line(3,4,11,14,8);s.line(4,5,11,13,5)
    s.polygon([(10,1),(11,4),(12,5),(13,3),(14,1),(14,5),(12,7),(10,5)],4)
    s.line(12,0,12,5,10);path(s,7,(10,1),(10,3),(11,4));path(s,6,(14,2),(13,4));dots(s,11,(12,1))
    s.polygon([(1,4),(3,2),(6,2),(8,4),(7,7),(6,5),(4,4)],8)
    path(s,10,(1,4),(3,2),(6,3),(7,4));dots(s,11,(3,3));dots(s,6,(6,4),(7,5))
    dots(s,7,(6,11),(8,9));dots(s,4,(4,10),(9,12));dots(s,6,(6,14),(8,14))
    return s
def starlight_flurry():
    s=blank(RAINBOW)
    shots=[((1,4),(4,1),3),((1,8),(7,2),4),((2,12),(11,3),6),((5,14),(13,6),5),((9,14),(14,9),7),((12,14),(14,12),9)]
    for a,b,c in shots:
        s.line(*a,*b,c)
        if a[0]+1<16 and a[1]+1<16:s.line(a[0]+1,a[1]+1,b[0],b[1]+1,2)
        dots(s,11,b);dots(s,8,(b[0]-1,b[1]+1))
    path(s,11,(5,9),(7,7),(9,5));dots(s,8,(3,11),(8,6))
    return s

SPECS=[('crystal_sanctuary','水晶疗愈阵',crystal_sanctuary),('triple_judgment','三重圣裁',triple_judgment),('stygian_souls','冥河游魂',stygian_souls),('stone_retinue','石像卫队',stone_retinue),('spirit_bomb','聚能灵弹',spirit_bomb),('triple_profane_beam','三重邪光',triple_profane_beam),('caustic_wave','腐蚀刃波',caustic_wave),('soulreaping_scythes','追魂魔镰',soulreaping_scythes),('devil_armament','恶魔兵装',devil_armament),('starlight_flurry','星芒连刺',starlight_flurry)]
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
    sheet.paste(zoom,(x,y));grid.paste(zoom,(x,y))
    for n in range(17):
        gd.line((x+n*8,y,x+n*8,y+128),fill='#434957');gd.line((x,y+n*8,x+128,y+n*8),fill='#434957')
    for d in [draw,gd]:
        d.text((x,y+140),f'{i+41:02d} {name}',font=font,fill='#eeeeee');d.text((x,y+166),'16×16 · 全新绘制',font=font,fill='#b7c4d6')
    sx=i%5*80+8;sy=i//5*105+5;small.paste(im.resize((64,64),Image.Resampling.NEAREST),(sx,sy));sd.text((sx,sy+70),f'{i+41} {name}',font=sf,fill='#eeeeee')
    report.append(dict(number=i+41,id=sid,name=name,width=16,height=16,opaque=True,equal_pixel_blocks=True,colors=len(im.getcolors()),old_art_inputs=[],sha256=hashlib.sha256((ROOT/'native'/(sid+'.png')).read_bytes()).hexdigest()))
sheet.save(ROOT/'contact-sheet.png');grid.save(ROOT/'grid-proof.png');small.save(ROOT/'readability-4x.png')
(ROOT/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
with zipfile.ZipFile(ROOT/'fresh-41-50-native16.zip','w',zipfile.ZIP_DEFLATED) as z:
    for sid,_,_ in SPECS:z.write(ROOT/'native'/(sid+'.png'),'native/'+sid+'.png')
    for name in ['contact-sheet.png','grid-proof.png','validation.json']:z.write(ROOT/name,name)
print('PASS: ten fresh native 16x16 opaque icons; uniform pixels; <=12 colors; no old artwork inputs; not installed.')
