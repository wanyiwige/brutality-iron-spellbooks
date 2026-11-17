from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw
p=Path('brutality-iron-spellbooks/pixel-art/triple-profane-beam')
# Static UV preview of the original model at its expanded animation pose (1.2 s).
# This renders existing texture artwork; it does not repaint the texture.
W,H=500,720
right=np.array([.866,0,-.5]);up=np.array([.15,.954,.26]);depth=np.cross(right,up)
def render(path):
 tex=np.array(Image.open(path).convert('RGBA'));out=np.zeros((H,W,4),dtype=np.uint8);out[:]=[27,29,39,255]
 faces=[]
 def face(v,uv): faces.append((sum(np.dot(x,depth) for x in v)/4,np.array(v),np.array(uv)))
 for y in [0.2525,199.2525]:
  face([[-64,y,-64],[64,y,-64],[64,y,64],[-64,y,64]],[[0,0],[63,0],[63,63],[0,63]])
 # Animated original cube becomes width 72, height about 198 at 1.2 seconds.
 for a,b in [((-36,-36),(36,-36)),((36,-36),(36,36)),((36,36),(-36,36)),((-36,36),(-36,-36))]:
  face([[a[0],0,a[1]],[b[0],0,b[1]],[b[0],198,b[1]],[a[0],198,a[1]]],[[48,79],[63,79],[63,64],[48,64]])
 for _,v,uv in sorted(faces,key=lambda f:f[0]):
  pts=np.stack([250+v@right*2.65,625-v@up*2.65],axis=1)
  for inds in [[0,1,2],[0,2,3]]:
   q=pts[inds];u=uv[inds];xmin=max(0,int(q[:,0].min()));xmax=min(W-1,int(q[:,0].max())+1);ymin=max(0,int(q[:,1].min()));ymax=min(H-1,int(q[:,1].max())+1)
   xx,yy=np.meshgrid(np.arange(xmin,xmax+1)+.5,np.arange(ymin,ymax+1)+.5)
   m=np.vstack([q.T,np.ones(3)])
   if abs(np.linalg.det(m))<1e-6:continue
   weights=np.linalg.inv(m)@np.stack([xx.ravel(),yy.ravel(),np.ones(xx.size)])
   valid=(weights.min(axis=0)>=-1e-6);t=(u.T@weights).round().astype(int);src=tex[np.clip(t[1],0,79),np.clip(t[0],0,63)]
   region=out[ymin:ymax+1,xmin:xmax+1];dst=region.reshape(-1,4).copy();al=src[:,3:4]/255*valid[:,None];dst[:,:3]=(src[:,:3]*al+dst[:,:3]*(1-al)).astype('uint8');region[:]=dst.reshape(region.shape)
 return Image.fromarray(out)
a=render(p/'original-holy-beam.png');b=render(p/'triple-profane-beam-64x80.png');canvas=Image.new('RGBA',(1000,720));canvas.paste(a,(0,0));canvas.paste(b,(500,0));canvas.save(p/'original-model-uv-comparison.png')
