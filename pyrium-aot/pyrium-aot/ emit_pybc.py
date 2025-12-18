import struct

MAGIC = 0x50594243  # 'PYBC'
VERSION = 2

def emit_ir_to_pybc(ir, out_path:str):
    pool = []
    for fn in ir.functions:
        for op in fn.ops:
            for s in (op.a, op.b):
                if s and s not in pool: pool.append(s)

    with open(out_path, "wb") as out:
        out.write(struct.pack(">II", MAGIC, VERSION))
        out.write(_utf(ir.name))
        out.write(struct.pack(">I", len(pool)))
        for s in pool: out.write(_utf(s))
        out.write(struct.pack(">I", len(ir.functions)))
        for fn in ir.functions:
            out.write(_utf(fn.name))
            out.write(struct.pack(">I", len(fn.ops)))
            for op in fn.ops:
                out.write(struct.pack(">I", int(op.code)))
                sA = pool.index(op.a) if op.a in pool else -1
                sB = pool.index(op.b) if op.b in pool else -1
                out.write(struct.pack(">ii", sA, sB))
                out.write(struct.pack(">d", float(op.d)))
                out.write(struct.pack(">q", int(op.i)))
    return out_path

def _utf(s:str):
    b = s.encode("utf-8")
    return struct.pack(">H", len(b)) + b
