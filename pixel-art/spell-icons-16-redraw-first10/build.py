"""Ten NEW icons authored directly on a 16x16 integer grid; no source resizing."""
from pathlib import Path
import sys,json,zipfile,hashlib
sys.path.insert(0,r'C:/Users/WANYIWIGE/.codex/skills/pixel-art-studio/scripts')
from pixelstudio import Sprite
from PIL import Image,ImageDraw,ImageFont

ROOT=Path(__file__).resolve().parent
NATIVE=ROOT/'native';PREVIEW=ROOT/'preview'
for folder in [NATIVE,PREVIEW]:folder.mkdir(parents=True,exist_ok=True)
INFERNO=['#140c19','#2b1226','#641b37','#ae2e4d','#ef6870','#ffe6d4']
CELESTIA=['#101722','#1c2b40','#50647f','#a87538','#f4ce77','#faf3db']
def blank(palette):
    s=Sprite(16,16,palette=palette)
    s.rect(0,0,15,15,0)
    s.polygon([(0,0),(15,0),(15,2),(2,15),(0,15)],1)
    return s
def pixels(s,coords,color):
    for x,y in coords:
        assert isinstance(x,int) and isinstance(y,int) and 0<=x<16 and 0<=y<16
        s.px(x,y,color)
def polyline(s,points,color):
    for a,b in zip(points,points[1:]):s.line(*a,*b,color)

def annihilation():
    s=blank(INFERNO)
    # Five independent spear heads, not a generic filled star.
    for end in [(8,4),(4,6),(11,6),(4,11),(11,11)]:s.line(8,8,*end,3)
    heads=[[(8,1),(10,4),(9,5),(7,5),(6,4)],[(1,4),(5,5),(6,7),(3,8)],[(14,4),(13,8),(10,7),(11,5)],[(2,14),(3,10),(6,10),(6,12)],[(13,14),(9,12),(10,10),(12,10)]]
    for head in heads:s.polygon(head,3)
    for a,b in [((8,2),(8,4)),((2,5),(4,6)),((13,5),(12,6)),((3,13),(4,11)),((12,13),(11,11))]:s.line(*a,*b,5)
    pixels(s,[(7,5),(5,7),(10,7),(6,10),(9,10)],4)
    s.rect(7,7,8,8,2);s.px(7,7,4)
    return s
def brimspike():
    s=blank(INFERNO)
    polyline(s,[(2,8),(2,5),(4,3),(7,2)],2)
    polyline(s,[(8,13),(11,13),(13,11),(14,8)],3)
    s.polygon([(2,13),(5,7),(13,2),(11,8),(6,12)],2)
    s.polygon([(3,12),(6,7),(13,2),(9,8)],3)
    s.line(4,11,12,3,4);s.line(8,7,12,3,5)
    for points in [[(4,10),(3,7),(6,8)],[(7,7),(6,4),(9,5)],[(8,10),(11,9),(10,7)]]:s.polygon(points,3)
    pixels(s,[(4,8),(7,5),(11,9)],4)
    return s
def chthonic_capsule():
    s=blank(INFERNO)
    s.polygon([(6,2),(10,2),(13,5),(13,10),(10,13),(5,13),(2,10),(2,6)],2)
    s.polygon([(5,4),(9,3),(12,5),(12,9),(9,12),(5,11),(3,8)],3)
    s.polygon([(5,5),(9,4),(11,6),(10,8),(6,9),(4,8)],4)
    s.rect(5,4,7,5,5);s.px(4,6,5)
    polyline(s,[(4,10),(7,10),(9,9),(11,9)],2)
    pixels(s,[(1,12),(3,14),(14,8)],3)
    return s
def destruction():
    s=blank(INFERNO)
    s.polygon([(8,1),(11,3),(14,7),(14,9),(11,13),(8,14),(11,10),(12,8),(10,4)],2)
    polyline(s,[(9,2),(12,5),(13,7),(13,9),(11,12),(9,13)],4)
    s.polygon([(4,4),(7,6),(10,5),(9,8),(11,10),(7,10),(6,13),(4,10),(1,9),(3,7),(2,4)],3)
    s.polygon([(4,6),(6,7),(8,7),(7,9),(5,10),(4,8),(2,8)],4)
    s.rect(4,7,6,8,5);pixels(s,[(5,6),(7,8),(5,9)],5)
    pixels(s,[(1,3),(3,12),(11,1)],3)
    return s
def extinction():
    s=blank(INFERNO)
    s.polygon([(1,10),(3,8),(12,2),(14,2),(14,5),(4,13),(1,13)],2)
    s.polygon([(1,11),(4,9),(13,2),(14,4),(4,12),(1,12)],3)
    s.line(2,11,13,3,5);s.line(3,12,14,4,4)
    polyline(s,[(2,5),(4,6),(5,7)],3)
    polyline(s,[(8,10),(9,12),(12,12)],3)
    s.line(6,3,7,5,4);pixels(s,[(1,14),(12,7)],4)
    return s
def stygian_step():
    s=blank(INFERNO)
    polyline(s,[(1,5),(3,5),(5,7)],3)
    polyline(s,[(1,9),(3,9),(4,10)],4)
    polyline(s,[(2,13),(4,13),(5,12)],2)
    s.polygon([(8,2),(11,2),(10,7),(11,9),(14,10),(14,12),(6,12),(5,10),(6,6)],2)
    s.polygon([(8,3),(10,3),(9,7),(10,9),(13,10),(13,11),(7,11),(6,9)],3)
    polyline(s,[(8,3),(7,6),(7,8),(8,9),(11,10),(13,10)],4)
    s.rect(6,12,14,12,3);s.line(9,11,13,11,5);pixels(s,[(8,4),(7,5)],5)
    return s
def divine_retribution():
    s=blank(CELESTIA)
    s.ellipse(3,3,12,6,3,fill=False);s.line(4,3,11,3,4)
    s.rect(6,1,9,8,3);s.rect(7,1,8,9,5)
    s.polygon([(5,8),(10,8),(8,12),(7,12)],4)
    s.line(7,8,7,10,5)
    s.ellipse(2,12,13,14,3,fill=False);s.line(4,12,11,12,4)
    s.rect(6,12,9,13,5)
    for a,b in [((2,8),(4,10)),((12,9),(13,7)),((3,12),(1,11)),((11,12),(14,11))]:s.line(*a,*b,4)
    return s
def heavenly_flight():
    s=blank(CELESTIA)
    left=[(1,3),(3,4),(5,6),(7,8),(6,12),(4,11),(3,9),(2,8),(1,6)]
    s.polygon(left,2);s.polygon([(15-x,y) for x,y in left],2)
    for a,b in [((1,3),(3,6)),((2,6),(5,9)),((3,9),(5,11))]:
        s.line(*a,*b,5);s.line(15-a[0],a[1],15-b[0],b[1],5)
    s.line(4,6,6,9,5);s.line(11,6,9,9,5)
    s.ellipse(6,1,9,3,4,fill=False)
    s.rect(7,6,8,11,4);s.rect(7,7,8,9,5)
    return s
def holy_mantle():
    s=blank(CELESTIA)
    s.polygon([(6,2),(9,2),(11,5),(12,10),(14,13),(10,14),(5,14),(1,13),(3,9),(4,5)],2)
    polyline(s,[(6,2),(4,5),(3,9),(2,12),(4,13)],5)
    polyline(s,[(9,2),(11,5),(12,10),(13,12)],5)
    s.polygon([(6,4),(9,4),(10,6),(10,12),(8,13),(5,12),(5,6)],1)
    s.line(6,2,9,2,5)
    s.polygon([(6,7),(10,7),(10,10),(8,12),(6,10)],3)
    s.polygon([(6,7),(9,7),(9,9),(8,11),(7,10)],4)
    s.line(7,7,8,7,5)
    return s
def light_binding():
    s=blank(CELESTIA)
    s.rect(6,2,9,4,2);s.rect(5,5,10,10,2)
    s.line(6,11,5,14,2);s.line(9,11,10,14,2)
    # Broad, separated light cuffs and a short chain across the restrained body.
    for x in [2,10]:
        s.rect(x,6,x+3,10,3,fill=False)
        s.line(x,6,x+2,6,5);s.line(x,7,x,9,4);s.line(x+3,7,x+3,10,4)
    polyline(s,[(5,8),(6,7),(7,7),(8,8),(9,8),(10,7)],4)
    pixels(s,[(6,8),(8,7),(9,7)],5)
    return s

SPECS=[('annihilation','歼灭',annihilation),('brimspike','尖刺',brimspike),('chthonic_capsule','通灵胶囊',chthonic_capsule),('destruction','毁灭',destruction),('extinction','消光',extinction),('stygian_step','冥河步',stygian_step),('divine_retribution','神圣报应',divine_retribution),('heavenly_flight','天飞',heavenly_flight),('holy_mantle','圣衣',holy_mantle),('light_binding','光缚',light_binding)]
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',16)
sheet=Image.new('RGB',(800,420),'#242733');draw=ImageDraw.Draw(sheet)
grid_sheet=Image.new('RGB',(800,420),'#242733');grid_draw=ImageDraw.Draw(grid_sheet)
report=[]
for index,(sid,name,paint) in enumerate(SPECS):
    sprite=paint();image=sprite.composite(1)
    assert image.size==(16,16) and image.getchannel('A').getextrema()==(255,255)
    assert len(image.getcolors())<=6
    sprite.save_png(str(NATIVE/(sid+'.png')))
    sprite.save_png(str(PREVIEW/(sid+'.png')),scale=8)
    zoom=Image.open(PREVIEW/(sid+'.png')).convert('RGB')
    # Every logical pixel must expand into precisely one constant 8x8 block.
    for y in range(16):
        for x in range(16):assert zoom.crop((x*8,y*8,(x+1)*8,(y+1)*8)).getcolors()==[(64,image.getpixel((x,y))[:3])]
    x=(index%5)*160+16;y=(index//5)*210+10
    sheet.paste(zoom,(x,y));grid_sheet.paste(zoom,(x,y))
    for n in range(17):
        grid_draw.line((x+n*8,y,x+n*8,y+128),fill='#3d4252')
        grid_draw.line((x,y+n*8,x+128,y+n*8),fill='#3d4252')
    for d in [draw,grid_draw]:
        d.text((x,y+140),f'{index+1:02d} {name}',font=font,fill='#f1f1f1')
        d.text((x,y+166),'16×16',font=font,fill='#aab3c6')
    report.append(dict(id=sid,name=name,width=16,height=16,colors=len(image.getcolors()),opaque=True,preview_scale=8,equal_pixel_blocks=True,sha256=hashlib.sha256((NATIVE/(sid+'.png')).read_bytes()).hexdigest()))
sheet.save(ROOT/'contact-sheet.png');grid_sheet.save(ROOT/'grid-proof.png')
(ROOT/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
with zipfile.ZipFile(ROOT/'first10-native-16x16.zip','w',zipfile.ZIP_DEFLATED) as z:
    for sid,_,_ in SPECS:z.write(NATIVE/(sid+'.png'),'native/'+sid+'.png')
    for filename in ['contact-sheet.png','grid-proof.png','validation.json']:z.write(ROOT/filename,filename)
print('PASS: exactly ten original 16x16 icons; six colors each at most; uniform 8x8 preview blocks; not installed.')
