"""Try lossless PNG encodings; verify decoded RGBA pixels before accepting."""
from pathlib import Path
from PIL import Image
import numpy as np
import json
import struct, zlib

def filtered_png(im, target):
    a = np.asarray(im).astype(np.int16)
    left = np.zeros_like(a); left[:, 1:] = a[:, :-1]
    up = np.zeros_like(a); up[1:] = a[:-1]
    ul = np.zeros_like(a); ul[1:, 1:] = a[:-1, :-1]
    p = left + up - ul
    dl, du, dc = abs(p-left), abs(p-up), abs(p-ul)
    paeth = np.where((dl <= du) & (dl <= dc), left, np.where(du <= dc, up, ul))
    choices = [a, a-left, a-up, a-(left+up)//2, a-paeth]
    scores = np.stack([abs(((v+128)%256)-128).sum(axis=1) for v in choices])
    selected = scores.argmin(axis=0)
    raw = b''.join(bytes([int(f)]) + (choices[f][y] % 256).astype(np.uint8).tobytes()
                   for y, f in enumerate(selected))
    def chunk(kind, data):
        return struct.pack('>I',len(data))+kind+data+struct.pack('>I',zlib.crc32(kind+data))
    target.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',im.width,im.height,8,3,0,0,0))
                      +chunk(b'PLTE',bytes(im.getpalette()))+chunk(b'IDAT',zlib.compress(raw,9))+chunk(b'IEND',b''))

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'scratch/lossless-animation'
OUT.mkdir(parents=True, exist_ok=True)
reports = []
for source in sorted((ROOT / 'resources/assets/inner_dimension/textures/gui/enlightenment').glob('dance_*.png')):
    im = Image.open(source)
    rgba = im.convert('RGBA').tobytes()
    candidates = []
    variants = [('original', im), ('rgb', im.convert('RGB'))]
    palette = np.array(im.getpalette(), dtype=np.uint8).reshape(-1, 3)
    used = np.unique(np.array(im))
    counts = np.bincount(np.array(im).ravel(), minlength=256)
    for label, order in [('rgb-order', sorted(used, key=lambda i: tuple(palette[i]))),
                         ('luma-order', sorted(used, key=lambda i: float(palette[i] @ np.array([.299,.587,.114])))),
                         ('frequency-order', sorted(used, key=lambda i: -int(counts[i])))]:
        order = list(map(int, order))
        order += [i for i in range(len(palette)) if i not in order]
        variants.append((label, im.remap_palette(order)))
    for label, variant in variants:
        target = OUT / f'{source.stem}-{label}.png'
        variant.save(target, optimize=True, compress_level=9)
        with Image.open(target) as check:
            assert check.size == im.size and check.convert('RGBA').tobytes() == rgba, target
        candidates.append({'encoding': label, 'bytes': target.stat().st_size, 'path': str(target)})
        print(source.name, label, target.stat().st_size, flush=True)
        if variant.mode == 'P':
            target = OUT / f'{source.stem}-{label}-filtered.png'
            filtered_png(variant, target)
            with Image.open(target) as check:
                assert check.convert('RGBA').tobytes() == rgba, target
            candidates.append({'encoding': label+'-filtered', 'bytes': target.stat().st_size, 'path': str(target)})
            print(source.name, label+'-filtered', target.stat().st_size, flush=True)
    reports.append({'source': str(source), 'original_bytes': source.stat().st_size,
                    'candidates': candidates, 'best': min(candidates, key=lambda c:c['bytes'])})
(OUT / 'report.json').write_text(json.dumps(reports, indent=2), encoding='utf-8')
