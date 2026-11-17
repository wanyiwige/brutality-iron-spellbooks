"""Second review batch: ten original icons painted at native 16x16."""
from pathlib import Path
import sys,json,zipfile,hashlib
sys.path.insert(0,r'C:/Users/WANYIWIGE/.codex/skills/pixel-art-studio/scripts')
from pixelstudio import Sprite
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent
NATIVE=ROOT/'native';PREVIEW=ROOT/'preview'
for folder in [NATIVE,PREVIEW]:folder.mkdir(parents=True,exist_ok=True)
CELESTIA=['#101722','#1c2b40','#50647f','#a87538','#f4ce77','#faf3db']
COSMIC=['#0b1023','#1b2248','#49316e','#8152bd','#4da9d1','#d9f3ef']
SHADOW=['#0c1220','#18243a','#34445f','#6c7a9c','#b4c9db','#eef3de']
def blank(palette):
    s=Sprite(16,16,palette=palette);s.rect(0,0,15,15,0)
    s.polygon([(0,0),(15,0),(15,2),(2,15),(0,15)],1)
    return s
def pixels(s,points,color):
    for x,y in points:
        assert type(x)==int and type(y)==int and 0<=x<16 and 0<=y<16
        s.px(x,y,color)
def path(s,points,color):
    for a,b in zip(points,points[1:]):s.line(*a,*b,color)
def sparkle(s,x,y,color=5):
    s.line(x-1,y,x+1,y,color);s.line(x,y-1,x,y+1,color)

def sacred_oath():
    s=blank(CELESTIA)
    s.ellipse(3,2,12,10,3,fill=False);path(s,[(4,4),(5,3),(10,3),(11,4)],4)
    s.polygon([(8,1),(10,4),(9,9),(7,9),(6,4)],2)
    s.polygon([(8,1),(8,8),(7,8),(7,4)],5);s.line(9,4,9,8,4)
    s.rect(4,9,11,10,3);s.line(5,9,10,9,4)
    s.rect(7,10,8,13,3);s.px(7,11,4);s.rect(7,14,8,14,4)
    return s
def sacrifice():
    s=blank(CELESTIA)
    s.polygon([(3,5),(5,3),(7,3),(8,5),(9,3),(11,3),(13,5),(13,8),(8,13),(3,8)],3)
    s.polygon([(4,5),(6,4),(7,5),(8,6),(10,4),(12,5),(12,7),(8,11),(4,7)],4)
    s.rect(4,5,5,6,5);s.px(6,4,5);s.line(5,7,7,9,5)
    s.line(1,10,3,11,2);s.line(12,2,14,1,2)
    pixels(s,[(1,12),(2,12),(2,13),(13,1),(14,1),(14,2)],5)
    return s
def cosmic_cataclysm():
    s=blank(COSMIC)
    s.polygon([(4,8),(7,6),(10,3),(14,1),(13,5),(11,7),(8,10)],2)
    s.polygon([(5,9),(8,6),(12,3),(10,7),(7,11)],3)
    s.line(8,8,13,2,4)
    s.polygon([(4,7),(7,7),(9,9),(8,12),(5,13),(2,11),(2,9)],3)
    s.polygon([(3,8),(6,8),(8,10),(6,12),(3,11)],4)
    s.rect(3,9,5,10,5);s.px(4,8,5)
    path(s,[(0,12),(2,13),(3,14),(4,15)],4)
    path(s,[(0,14),(1,14),(2,15)],2)
    pixels(s,[(2,3),(11,12)],3)
    return s
def gravitokinesis():
    s=blank(COSMIC)
    path(s,[(12,5),(11,3),(9,2),(5,2),(3,4)],3)
    s.polygon([(2,4),(5,2),(5,5)],4)
    path(s,[(3,10),(4,12),(6,13),(10,13),(12,11)],3)
    s.polygon([(13,11),(10,10),(10,13)],4)
    s.polygon([(6,5),(9,5),(11,7),(9,10),(6,10),(5,8)],2)
    s.polygon([(6,5),(9,5),(9,7),(7,9),(5,8)],3)
    s.polygon([(6,6),(8,5),(9,7),(7,8),(6,8)],4)
    s.line(6,5,8,5,5);s.px(5,7,4)
    return s
def meteor_shower():
    s=blank(COSMIC)
    for x,y,length in [(2,5,4),(6,9,6),(11,12,3)]:
        s.line(x,y,x+length,y-length,2)
        s.line(x+1,y,x+length,y-length+1,3)
        s.rect(x,y,x+2,y+2,4)
        pixels(s,[(x,y+1),(x+1,y+1),(x+1,y+2)],5)
    pixels(s,[(1,12),(11,2)],2)
    return s
def singularity_shift():
    s=blank(COSMIC)
    s.ellipse(4,4,11,11,2,fill=True)
    s.ellipse(5,5,10,10,0,fill=True)
    path(s,[(1,9),(2,6),(6,3),(10,2),(13,3),(14,5)],3)
    path(s,[(1,9),(2,12),(6,12),(11,9),(14,5)],3)
    path(s,[(2,11),(5,11),(10,8),(12,6)],4)
    sparkle(s,13,4,5)
    pixels(s,[(6,4),(7,4)],4)
    return s
def star_burst():
    s=blank(COSMIC)
    tips=[(3,2),(8,3),(12,6),(13,10),(10,13)]
    for x,y in tips:s.line(2,12,x,y,2)
    for x,y in tips:
        s.line(x,y,x-1,y+1,4);sparkle(s,x,y,5)
    s.rect(1,11,3,13,4);s.px(2,12,5)
    return s
def star_stream():
    s=blank(COSMIC)
    points=[(1,14),(2,11),(4,9),(7,8),(10,8),(12,6),(12,3),(14,1)]
    path(s,points,2);path(s,[(x+1,y) for x,y in points if x<14],3)
    path(s,[(2,12),(4,10),(7,9),(10,9),(13,6),(13,4)],4)
    for x,y in [(3,12),(7,8),(12,4)]:sparkle(s,x,y,5)
    return s
def crescent_dart():
    s=blank(SHADOW)
    moon=[(1,0),(3,0),(4,1),(4,3),(3,4),(1,4),(2,3),(2,1)]
    for x,y in [(2,1),(6,5),(10,9)]:
        s.polygon([(x+a,y+b) for a,b in moon],3)
        path(s,[(x+1,y),(x+3,y),(x+4,y+1),(x+4,y+2)],5)
        s.line(x+3,y+3,x+2,y+4,4)
    path(s,[(1,8),(2,7),(3,7)],2)
    path(s,[(4,13),(5,12),(6,12)],2)
    return s
def crescent_scythe():
    s=blank(SHADOW)
    s.line(3,14,10,5,2);s.line(4,14,10,6,3)
    s.polygon([(5,2),(9,1),(12,2),(14,5),(13,8),(11,10),(9,10),(11,8),(11,5),(9,3)],3)
    path(s,[(5,2),(9,1),(12,2),(14,5),(13,7)],4)
    path(s,[(6,2),(9,2),(11,3),(12,4)],5)
    s.line(11,5,11,7,4);s.px(10,8,4)
    s.line(2,13,4,14,4)
    return s

SPECS=[('sacred_oath','神圣誓言',sacred_oath),('sacrifice','牺牲',sacrifice),('cosmic_cataclysm','宇宙大灾变',cosmic_cataclysm),('gravitokinesis','重力运动',gravitokinesis),('meteor_shower','流星雨',meteor_shower),('singularity_shift','奇点偏移',singularity_shift),('star_burst','星爆',star_burst),('star_stream','星流',star_stream),('crescent_dart','新月飞镖',crescent_dart),('crescent_scythe','新月之镰',crescent_scythe)]
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',16)
sheet=Image.new('RGB',(800,420),'#242733');draw=ImageDraw.Draw(sheet)
grid=Image.new('RGB',(800,420),'#242733');gd=ImageDraw.Draw(grid)
report=[]
for index,(sid,name,paint) in enumerate(SPECS):
    sprite=paint();im=sprite.composite(1)
    assert im.size==(16,16) and im.getchannel('A').getextrema()==(255,255) and len(im.getcolors())<=6
    sprite.save_png(str(NATIVE/(sid+'.png')));sprite.save_png(str(PREVIEW/(sid+'.png')),scale=8)
    zoom=Image.open(PREVIEW/(sid+'.png')).convert('RGB')
    for y in range(16):
        for x in range(16):assert zoom.crop((8*x,8*y,8*x+8,8*y+8)).getcolors()==[(64,im.getpixel((x,y))[:3])]
    x=index%5*160+16;y=index//5*210+10
    sheet.paste(zoom,(x,y));grid.paste(zoom,(x,y))
    for n in range(17):
        gd.line((x+n*8,y,x+n*8,y+128),fill='#3d4252');gd.line((x,y+n*8,x+128,y+n*8),fill='#3d4252')
    for d in [draw,gd]:
        d.text((x,y+140),f'{index+11:02d} {name}',font=font,fill='#f1f1f1');d.text((x,y+166),'16×16',font=font,fill='#aab3c6')
    report.append(dict(id=sid,name=name,width=16,height=16,colors=len(im.getcolors()),opaque=True,equal_pixel_blocks=True,preview_scale=8,sha256=hashlib.sha256((NATIVE/(sid+'.png')).read_bytes()).hexdigest()))
sheet.save(ROOT/'contact-sheet.png');grid.save(ROOT/'grid-proof.png')
(ROOT/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
approved=ROOT.parent/'spell-icons-16-redraw-first10'
for row in json.loads((approved/'approved-manifest.json').read_text(encoding='utf8')):
    assert hashlib.sha256((approved/'native'/(row['id']+'.png')).read_bytes()).hexdigest()==row['sha256']
with zipfile.ZipFile(ROOT/'second10-native-16x16.zip','w',zipfile.ZIP_DEFLATED) as z:
    for sid,_,_ in SPECS:z.write(NATIVE/(sid+'.png'),'native/'+sid+'.png')
    for filename in ['contact-sheet.png','grid-proof.png','validation.json']:z.write(ROOT/filename,filename)
print('PASS: second ten native 16x16 icons; first ten approved files unchanged; no mod integration.')
