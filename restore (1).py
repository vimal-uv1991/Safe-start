#!/usr/bin/env python3
"""Rebuild the original APK, byte-for-byte, from base.apk.xz.
Usage:  python3 restore.py base.apk.xz base.apk
Needs only Python 3 (standard library)."""
import lzma, zlib, struct, json, hashlib, sys

src, dst = sys.argv[1], sys.argv[2]
blob = lzma.open(src, 'rb').read()
assert blob[:6] == b'APKX1\n', "not an APKX1 archive"
hlen = struct.unpack('<I', blob[6:10])[0]
hdr = json.loads(blob[10:10 + hlen])
p = 10 + hlen
skeleton = blob[p:p + hdr["skeleton_len"]]
payload = memoryview(blob)[p + hdr["skeleton_len"]:]

out, spos, ppos = [], 0, 0          # spos walks the skeleton, ppos the payload
opos = 0                            # position in the original file
for start, clen, rlen, level in hdr["streams"]:
    n = start - opos                # literal bytes before this stream
    out.append(skeleton[spos:spos + n]); spos += n
    c = zlib.compressobj(level, zlib.DEFLATED, -15, 8, zlib.Z_DEFAULT_STRATEGY)
    comp = c.compress(bytes(payload[ppos:ppos + rlen])) + c.flush()
    assert len(comp) == clen, "deflate mismatch"
    out.append(comp); ppos += rlen; opos = start + clen
out.append(skeleton[spos:])
result = b''.join(out)

ok = hashlib.sha256(result).hexdigest() == hdr["sha256"] and len(result) == hdr["size"]
assert ok, "checksum mismatch - archive is corrupt"
open(dst, 'wb').write(result)
print("restored %s (%d bytes), SHA-256 verified: %s" % (dst, len(result), hdr["sha256"]))
