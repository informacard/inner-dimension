from pathlib import Path
import sys,json,wave
import numpy as np
from scipy.signal import butter,sosfilt
root=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(root/'tmp/audio-tools'))
import soundfile as sf
sr=44100;rng=np.random.default_rng(230017)
out=root/'texture-drafts/audio/void-embodiment';out.mkdir(parents=True,exist_ok=True)
assets=root/'resources/assets/inner_dimension'
report={}
def noise(n,lo,hi):
 return sosfilt(butter(3,[lo,hi],btype='bandpass',fs=sr,output='sos'),rng.normal(size=n))
def save(name,x,seconds):
 t=np.arange(len(x))/sr;x*=np.minimum(1,t/.025)*np.minimum(1,(seconds-t)/.035)
 x=np.tanh(x);x*=.72/max(abs(x).max(),1e-9)
 sf.write(assets/'sounds'/f'{name}.ogg',x,sr,format='OGG',subtype='VORBIS')
 with wave.open(str(out/f'{name}.wav'),'wb') as w:
  w.setnchannels(1);w.setsampwidth(2);w.setframerate(sr);w.writeframes((x*32767).astype('<i2').tobytes())
 decoded,rate=sf.read(assets/'sounds'/f'{name}.ogg');assert rate==sr and np.isfinite(decoded).all()
 report[name]={'seconds':seconds,'peak':float(abs(decoded).max())}
t=np.arange(sr*4)/sr
x=.17*np.sin(2*np.pi*47*t+1.2*np.sin(2*np.pi*1.5*t))+.09*np.sin(2*np.pi*71*t)+.4*noise(len(t),40,680)
x=np.tanh(x*3)*(.65+.2*np.sin(2*np.pi*.75*t))+.04*noise(len(t),900,2400)
save('pillar_drone',x,4)
for name,seconds,low,high in [('pillar_leak_top',2,220,2200),('pillar_leak_bottom',2,90,1400)]:
 t=np.arange(int(sr*seconds))/sr
 # Continuous pressure escaping through a narrow opening, without tonal droplets
 # or rhythmic amplitude pulses that could read as bubbling.
 drift=sosfilt(butter(2,1.2,fs=sr,output='sos'),rng.normal(size=len(t)))
 drift/=max(np.max(np.abs(drift)),1e-9)
 env=.65+.07*drift
 x=.55*noise(len(t),low,high)*env+.12*noise(len(t),55,250)
 save(name,x,seconds)
t=np.arange(sr*5)/sr
env=(t/5)**1.25
phase=2*np.pi*np.cumsum(65+95*(t/5)**2)/sr
x=(.13*np.sin(phase)+.045*np.sin(phase*1.498)+.65*noise(len(t),65,650))*env
for at in [1.0,1.9,2.7,3.35,3.85,4.2,4.45,4.65]:
 d=at-t;gate=(d>=0)*np.exp(-np.maximum(d,0)*14)
 # Broad reversed air swells replace the former high pitched chirps.
 x+=.24*noise(len(t),80,720)*gate
x+=.12*np.sin(2*np.pi*58*t)*np.exp(-np.maximum(t-4.65,0)*9)*(t>4.65)
x=sosfilt(butter(4,850,fs=sr,output='sos'),x)
save('void_rewind',x,5)
path=assets/'sounds.json';data=json.loads(path.read_text(encoding='utf-8'))
for name in report:data[name]={'subtitle':'sound.inner_dimension.'+name,'sounds':[{'name':'inner_dimension:'+name,'stream':False}]}
path.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
for lang in ['zh_cn','en_us']:
 path=assets/'lang'/f'{lang}.json';data=json.loads(path.read_text(encoding='utf-8'))
 for name,zh,en in [('pillar_drone','侵染器：失真嗡鸣','Infection pillar drones'),('pillar_leak_top','侵染器：上端泄露','Upper vent leaks'),('pillar_leak_bottom','侵染器：下端泄露','Lower vent leaks'),('void_rewind','虚空：回溯','Void rewinds')]:data['sound.inner_dimension.'+name]=zh if lang=='zh_cn' else en
 path.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
(out/'report.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
