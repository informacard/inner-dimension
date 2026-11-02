"""Recolor NASA's edge-on loop and blend its seam without reversing motion."""
from pathlib import Path
import cv2, numpy as np, json
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
DRAFT=ROOT/'texture-drafts/sky'
OUT=ROOT/'resources/assets/inner_dimension/textures/environment/inner_black_hole'
OUT.mkdir(parents=True,exist_ok=True)
cap=cv2.VideoCapture(str(DRAFT/'nasa-edge-on-loop.webm'))
fps=cap.get(cv2.CAP_PROP_FPS); frames=[]
while True:
    ok, frame=cap.read()
    if not ok: break
    frames.append(cv2.cvtColor(frame,cv2.COLOR_BGR2RGB))
assert len(frames)>=160
# Blend moving head/tail sequences over 0.6 s; smoothstep has zero slope at both ends.
overlap=18
loop=frames[overlap:-overlap]
for k in range(overlap):
    t=k/(overlap-1); w=t*t*(3-2*t)
    loop.append(np.rint(frames[-overlap+k].astype(np.float32)*(1-w)+frames[k].astype(np.float32)*w).astype(np.uint8))
previews=[]; measures=[]
for i, frame in enumerate(loop):
    hsv=cv2.cvtColor(frame,cv2.COLOR_RGB2HSV)
    hsv[:,:,0]=np.clip(137+hsv[:,:,0].astype(np.float32)*.18,0,179).astype(np.uint8)
    hsv[:,:,1]=np.maximum(hsv[:,:,1],210)
    hsv[:,:,2]=np.rint((hsv[:,:,2].astype(np.float32)/255)**1.08*200).astype(np.uint8)
    rgb=cv2.cvtColor(hsv,cv2.COLOR_HSV2RGB)
    rgb[np.max(frame,axis=2)<=2]=0
    im=Image.fromarray(rgb);im=im.resize((1024,round(im.height*1024/im.width)),Image.Resampling.LANCZOS)
    square=Image.new('RGB',(1024,1024));square.paste(im,(0,(1024-im.height)//2))
    square.save(OUT/f'frame_{i:03d}.png',optimize=True)
    small=square.resize((512,512),Image.Resampling.LANCZOS);previews.append(small)
    measures.append(np.array(small.resize((256,256)),dtype=np.float32))
previews[0].save(DRAFT/'purple-black-hole-loop.gif',save_all=True,append_images=previews[1:],duration=[30 if i%3 else 40 for i in range(len(previews))],loop=0,optimize=False,disposal=2)
previews[0].save(DRAFT/'purple-black-hole-preview.png')
a=np.array(measures);ds=np.abs(np.diff(a,axis=0)).mean(axis=(1,2,3));seam=float(np.abs(a[-1]-a[0]).mean())
report={'source_frames':len(frames),'frames':len(loop),'fps':fps,'duration_seconds':len(loop)/fps,'crossfade_frames':overlap,'mean_adjacent_difference':float(ds.mean()),'max_adjacent_difference':float(ds.max()),'wrap_difference':seam,'resolution':[1024,1024]}
assert seam<=ds.max()*1.05,report
(OUT/'animation.json').write_text(json.dumps({'frames':len(loop),'fps':fps},indent=2))
(DRAFT/'loop-validation.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))

