"""Import generated designs onto native pixel grids; export review assets only.

The raw generated PNGs and prompts are retained. No live mod resources are edited.
Run with Python 3.13 and Pillow. All display enlargement is nearest-neighbor.
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import json, math, shutil, hashlib, zipfile

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'native'; PREVIEW=ROOT/'preview'; RAW=ROOT/'raw'
for folder in [OUT,PREVIEW,RAW]:folder.mkdir(exist_ok=True)
SPECS=json.loads((ROOT/'generation.json').read_text(encoding='utf8'))
PALETTES={
 # Exact opaque colors from Terramity's two prismatic crystal textures.
 'crystal':['#6d49ce','#9052e2','#b35fe9','#d67dee','#f49cf4','#f7bdf7','#ffeffb'],
 'nature':['#193d2c','#335c30','#588345','#89ad56','#b7ce78','#e7e9aa'],
 'eldritch':['#1b1428','#32203f','#56336e','#86509a','#b986b7','#e5d7b2'],
 # RoACW cloud ramp, with the requested black-green and a sparse acid highlight.
 'plague':['#101c19','#204240','#33634b','#498239','#80bc43','#91d948','#c7ee71'],
 'inferno':['#180e24','#36133d','#6b1856','#a62772','#dd3a92','#ff8dbd','#ffd1df'],
 'holy':['#8d4b20','#c37c26','#f1ad39','#ffdb6b','#fff3a4','#fffde1']}
LABELS={'circle_crystal':'水晶疗愈阵','circle_nature':'盖亚催生阵','circle_eldritch':'邪术法阵',
 'plague_cloud':'疫病毒雾','plague_wave':'腐蚀刃波','plague_claws':'腐甲爪痕',
 'shadowflame_slash':'影焰斩','inferno_impact':'炼狱重击','column_eldritch':'邪术光柱','column_holy':'圣光打击'}
def rgb(value):return tuple(bytes.fromhex(value[1:]))
def palette_for(id):
 key=('crystal' if 'crystal' in id else 'nature' if 'nature' in id else 'eldritch' if 'eldritch' in id else 'plague' if 'plague' in id else 'holy' if 'holy' in id else 'inferno')
 return [rgb(c) for c in PALETTES[key]]
def nearest(c,palette):return min(palette,key=lambda q:sum((c[i]-q[i])**2 for i in range(3)))
def clean(spec):
 id=spec['id']; source=Path(spec['raw_path']); dest=RAW/(id+('-v2' if spec.get('revision') else '')+'.png')
 if not dest.exists():shutil.copy2(source,dest)
 original=Image.open(dest).convert('RGBA'); w,h=spec['size']
 alpha=original.getchannel('A').point(lambda p:255 if p>=144 else 0)
 box=alpha.getbbox();assert box,id
 # A bounding box crop removes the generator's variable transparent canvas;
 # logical resolution and hard-alpha palette are fixed at export.
 art=original.crop(box); margin=2 if w<=32 else 3
 scale=min((w-margin*2)/art.width,(h-margin*2)/art.height)
 nw=max(1,round(art.width*scale));nh=max(1,round(art.height*scale))
 sampled=art.resize((nw,nh),Image.Resampling.NEAREST)
 native=Image.new('RGBA',(w,h));palette=palette_for(id)
 for y in range(nh):
  for x in range(nw):
   c=sampled.getpixel((x,y))
   if c[3]>=144:native.putpixel(((w-nw)//2+x,(h-nh)//2+y),nearest(c,palette)+(255,))
 # Ignore isolated anti-alias/noise remnants; deliberate 1px motes stay in fog.
 if id.startswith('circle_'):
  p=native.copy()
  for y in range(h):
   for x in range(w):
    if p.getpixel((x,y))[3] and not any(0<=x+dx<w and 0<=y+dy<h and p.getpixel((x+dx,y+dy))[3] for dx,dy in [(0,1),(0,-1),(1,0),(-1,0)]):native.putpixel((x,y),(0,0,0,0))
 if id=='plague_cloud':
  # Keep the small smoke puff on the reference's five opaque shades plus black.
  cloud_palette=palette[:-1]
  for y in range(h):
   for x in range(w):
    c=native.getpixel((x,y))
    if c[3]:native.putpixel((x,y),nearest(c,cloud_palette)+(255,))
 native.save(OUT/(id+'.png'))
 native.resize((w*6,h*6),Image.Resampling.NEAREST).save(PREVIEW/(id+'@6x.png'))
 return native
def circle_frames(im):
 # Stage concentric components into view, preserving their exact pixel grid.
 w,h=im.size;frames=[]
 for frame in range(20):
  phase=min(1,frame/15);pic=Image.new('RGBA',im.size)
  for y in range(h):
   for x in range(w):
    c=im.getpixel((x,y));radius=math.hypot(x-(w-1)/2,y-(h-1)/2)/(w*.5)
    if c[3] and radius<phase*1.45:pic.putpixel((x,y),c)
  frames.append(pic)
 frames.extend([im.copy() for _ in range(12)])
 return frames
def cloud_frames(im):
 frames=[];w,h=im.size
 for frame in range(10):
  pic=Image.new('RGBA',im.size)
  for y in range(h):
   shift=round(.7*math.sin(frame*math.tau/10+y*.6))
   for x in range(w):
    c=im.getpixel((x,y));xx=x+shift
    if c[3] and 1<=xx<w-1:pic.putpixel((xx,y),c)
  frames.append(pic)
 return frames
def export_frames(id,frames,ticks):
 w,h=frames[0].size;strip=Image.new('RGBA',(w,h*len(frames)))
 for i,f in enumerate(frames):strip.paste(f,(0,i*h))
 strip.save(OUT/(id+'_frames.png'))
 metadata={'width':w,'height':h,'frames':len(frames),'frametime_ticks':ticks,'loop':not id.startswith('circle_'),'animation':'reveal_once_then_hold' if id.startswith('circle_') else 'smoke_drift'}
 (OUT/(id+'_animation.json')).write_text(json.dumps(metadata,indent=2)+'\n',encoding='utf8')
 bg=(29,32,41);gif=[]
 for f in frames:
  opaque=Image.new('RGB',f.size,bg);opaque.paste(f,mask=f.getchannel('A'));gif.append(opaque.resize((w*6,h*6),Image.Resampling.NEAREST))
 gif[0].save(PREVIEW/(id+'.gif'),save_all=True,append_images=gif[1:],duration=ticks*50,loop=0,disposal=2,optimize=False)
 return metadata

if __name__=='__main__':
 images={};report=[]
 for spec in SPECS:
  if not spec.get('raw_path'):continue
  im=clean(spec);id=spec['id'];images[id]=im
  row={'id':id,'label':LABELS[id],'size':im.size,'colors':len({c for c in im.getdata() if c[3]}),'hard_alpha':all(c[3] in [0,255] for c in im.getdata()),'alpha_bbox':im.getbbox(),'sha256':hashlib.sha256((OUT/(id+'.png')).read_bytes()).hexdigest()}
  assert row['hard_alpha'] and row['colors']<=8,id
  if id.startswith('circle_'):row['animation']=export_frames(id,circle_frames(im),2)
  if id=='plague_cloud':row['animation']=export_frames(id,cloud_frames(im),2)
  report.append(row)
 fontpath=Path('C:/Windows/Fonts/msyh.ttc');font=ImageFont.truetype(str(fontpath),18)
 board=Image.new('RGB',(900,math.ceil(len(images)/3)*270),(29,32,41));draw=ImageDraw.Draw(board)
 for i,(id,im) in enumerate(images.items()):
  x=(i%3)*300;y=(i//3)*270;w,h=im.size;scale=min(8,220//w,205//h)
  large=im.resize((w*scale,h*scale),Image.Resampling.NEAREST)
  board.paste(large,(x+(300-large.width)//2,y+(210-large.height)//2),large)
  draw.text((x+14,y+216),LABELS[id],font=font,fill=(239,237,243))
  draw.text((x+14,y+242),f'{w} × {h}  /  {scale}x nearest',font=font,fill=(166,177,189))
 board.save(PREVIEW/'contact-sheet.png')
 (ROOT/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
 print(json.dumps(report,ensure_ascii=False,indent=2))
