from pathlib import Path
import json,random,copy,math
import numpy as np
from PIL import Image,ImageDraw
root=Path(__file__).resolve().parents[1]
assets=root/'resources/assets/inner_dimension'
for lang,text in [('zh_cn','灵魂绑定：死亡后保留'),('en_us','Soulbound: kept on death')]:
 path=assets/'lang'/f'{lang}.json';data=json.loads(path.read_text(encoding='utf-8'));data['item.inner_dimension.domain_core.soulbound']=text;path.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
out=root/'texture-drafts/sheathed-infector';out.mkdir(parents=True,exist_ok=True)
rng=random.Random(234)
for name,base in [('rubber_grey',(220,217,192)),('rubber_grey_rim',(195,185,161)),('rubber_thin',(220,217,192))]:
 image=Image.new('RGB',(16,16))
 for y in range(16):
  for x in range(16):
   shade=rng.choice([-3,-2,-1,0,0,0,1,2,3])+(3 if x in [3,4] else 0)
   mix=(.10+.32*math.exp(-((x-7.5)**2+(y-7.5)**2)/26)) if name=='rubber_thin' else 0
   image.putpixel((x,y),tuple(int(v*(1-mix)+22*mix)+shade for v in base))
 image.save(assets/'textures/block'/f'{name}.png')
model=json.loads((assets/'models/block/infector.json').read_text())
model['textures'].update(rubber='inner_dimension:block/rubber_grey',rim='inner_dimension:block/rubber_grey_rim',thin='inner_dimension:block/rubber_thin',particle='inner_dimension:block/rubber_grey')
for e in model['elements']:
 for f in e['faces'].values():f['tintindex']=0
model['elements'][1]['to'][1]=15.25
def cube(a,b,texture):
 return {'from':a,'to':b,'faces':{face:{'texture':'#'+texture,'uv':[0,0,16,16]} for face in ['up','down','north','south','east','west']}}
model['elements'] += [
 cube([6.6,2.3,6.6],[9.4,14.75,9.4],'rubber'),
 cube([6.8,14.75,6.8],[9.2,15.45,9.2],'rubber'),
 cube([7.15,15.45,7.15],[8.85,16,8.85],'thin'),
 cube([5.85,1.3,5.85],[10.15,2.35,10.15],'rim'),
 cube([6.15,2.35,6.15],[9.85,2.65,9.85],'rubber')]
model['display'].update(gui={'rotation':[25,45,0],'translation':[0,0,0],'scale':[.95,.95,.95]},ground={'rotation':[0,0,0],'translation':[0,3,0],'scale':[.5,.5,.5]},fixed={'rotation':[0,180,0],'translation':[0,0,0],'scale':[.8,.8,.8]})
(assets/'models/block/sheathed_infector.json').write_text(json.dumps(model,indent=2),encoding='utf-8')
# Orthographic preview of the actual generated cuboids, using each material's base colour.
image=Image.new('RGB',(640,760),'#e8e8e4');draw=ImageDraw.Draw(image)
def point(x,y,z):return (320+(x-z)*33,650-y*33+(x+z-16)*12)
faces=[]
for e in model['elements']:
 x,y,z=e['from'];X,Y,Z=e['to']
 for face,vertices,shade in [('up',[(x,Y,z),(X,Y,z),(X,Y,Z),(x,Y,Z)],1.08),('south',[(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)],.95),('east',[(X,y,z),(X,y,Z),(X,Y,Z),(X,Y,z)],.76)]:
  material=e['faces'][face]['texture'];rgb=(22,22,25) if material=='#end_rod' else (195,185,161) if material=='#rim' else (164,161,143) if material=='#thin' else (220,217,192)
  colour=tuple(min(255,int(v*shade)) for v in rgb)
  faces.append((sum(v[0]+v[2]+v[1]*.1 for v in vertices)/4,[point(*v) for v in vertices],colour))
pixels=np.asarray(image).copy();depth_buffer=np.full((760,640),-np.inf)
def triangle(vertices,depths,colour):
 a,b,c=np.array(vertices,dtype=float)
 left=max(0,int(min(a[0],b[0],c[0])));right=min(639,int(max(a[0],b[0],c[0]))+1)
 top=max(0,int(min(a[1],b[1],c[1])));bottom=min(759,int(max(a[1],b[1],c[1]))+1)
 xx,yy=np.meshgrid(np.arange(left,right)+.5,np.arange(top,bottom)+.5)
 denominator=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
 if abs(denominator)<1e-9:return
 u=((b[1]-c[1])*(xx-c[0])+(c[0]-b[0])*(yy-c[1]))/denominator
 v=((c[1]-a[1])*(xx-c[0])+(a[0]-c[0])*(yy-c[1]))/denominator;w=1-u-v
 depth=u*depths[0]+v*depths[1]+w*depths[2]
 patch=depth_buffer[top:bottom,left:right];mask=(u>=0)&(v>=0)&(w>=0)&(depth>patch)
 pixels[top:bottom,left:right][mask]=colour;patch[mask]=depth[mask]
for e in model['elements']:
 x,y,z=e['from'];X,Y,Z=e['to']
 for face,vertices,shade in [('up',[(x,Y,z),(X,Y,z),(X,Y,Z),(x,Y,Z)],1.08),('south',[(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)],.95),('east',[(X,y,z),(X,y,Z),(X,Y,Z),(X,Y,z)],.76)]:
  material=e['faces'][face]['texture'];rgb=(22,22,25) if material=='#end_rod' else (195,185,161) if material=='#rim' else (164,161,143) if material=='#thin' else (220,217,192)
  colour=tuple(min(255,int(v*shade)) for v in rgb)
  projected=[point(*v) for v in vertices];depths=[v[1]+(v[0]+v[2])*12/33 for v in vertices]
  for indices in [(0,1,2),(0,2,3)]:triangle([projected[i] for i in indices],[depths[i] for i in indices],colour)
image=Image.fromarray(pixels)
image.save(out/'sheathed-infector-preview.png')
print('Built phantom-membrane cream rubber sleeve, rolled rim, sealed cap, black core and native model preview')
