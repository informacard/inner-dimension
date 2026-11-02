from pathlib import Path
from PIL import Image,ImageSequence
import numpy as np,json,math
ROOT=Path(__file__).resolve().parents[1];DRAFT=ROOT/'texture-drafts/sky'
OUT=ROOT/'resources/assets/inner_dimension/textures/gui/enlightenment';OUT.mkdir(parents=True,exist_ok=True)
reports={}
for variant,name in [('dance','Dance Art GIF by Psyklon.gif')]:
    src=Image.open(DRAFT/name);frames=[];dur=[]
    for f in ImageSequence.Iterator(src):frames.append(f.convert('RGB'));dur.append(f.info.get('duration',70))
    arrays=[np.array(f.resize((240,135)),dtype=np.float32) for f in frames]
    differences=[float(np.abs(a-b).mean()) for a,b in zip(arrays,arrays[1:])]
    original_seam=float(np.abs(arrays[-1]-arrays[0]).mean())
    overlap=0
    if variant!='eye' and original_seam<.01:
        frames.pop();dur.pop()
    elif variant!='eye' and original_seam>max(differences)*1.1:
        overlap=16
        old=frames;oldDur=dur
        frames=old[overlap:-overlap];dur=oldDur[overlap:-overlap]
        for k in range(overlap):
            t=k/(overlap-1);w=t*t*(3-2*t)
            frames.append(Image.blend(old[-overlap+k],old[k],w));dur.append(oldDur[k])
    width,height=frames[0].size;cw,ch=width+4,height+4;columns=8;capacity=64
    for sheet in range(math.ceil(len(frames)/capacity)):
        batch=frames[sheet*capacity:(sheet+1)*capacity];rows=math.ceil(len(batch)/columns)
        atlas=Image.new('RGB',(columns*cw,rows*ch))
        for k,f in enumerate(batch):
            x=(k%columns)*cw+2;y=(k//columns)*ch+2;atlas.paste(f,(x,y))
            atlas.paste(f.crop((0,0,width,1)).resize((width,2)),(x,y-2));atlas.paste(f.crop((0,height-1,width,height)).resize((width,2)),(x,y+height))
            atlas.paste(f.crop((0,0,1,height)).resize((2,height)),(x-2,y));atlas.paste(f.crop((width-1,0,width,height)).resize((2,height)),(x+width,y))
        indexed=atlas.quantize(colors=256,method=Image.Quantize.MEDIANCUT,dither=Image.Dither.NONE)
        if indexed.convert('RGB').tobytes()==atlas.tobytes():atlas=indexed
        atlas.save(OUT/f'{variant}_{sheet}.png',optimize=True)
    frames[0].save(DRAFT/f'{variant}-preview.png')
    frames[0].save(DRAFT/f'{variant}-loop.gif',save_all=True,append_images=frames[1:],duration=dur,loop=0,optimize=False,disposal=2)
    arrays=[np.array(f.resize((240,135)),dtype=np.float32) for f in frames];d=[float(np.abs(a-b).mean()) for a,b in zip(arrays,arrays[1:])];seam=float(np.abs(arrays[-1]-arrays[0]).mean())
    if variant!='eye':assert seam<=max(d)*1.1,(variant,seam,max(d))
    data={'width':width,'height':height,'columns':columns,'capacity':capacity,'padding':2,'durations':dur,'frames':len(frames)}
    (OUT/f'{variant}.json').write_text(json.dumps(data))
    reports[variant]={'frames':len(frames),'duration_ms':sum(dur),'crossfade_frames':overlap,'original_wrap_difference':original_seam,'wrap_difference':seam,'max_step_difference':max(d)}
(DRAFT/'overlay-loop-validation.json').write_text(json.dumps(reports,indent=2));print(json.dumps(reports,indent=2))
