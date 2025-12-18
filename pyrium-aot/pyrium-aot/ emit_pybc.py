import struct

MAGIC = 0x50594243  # 'PYBC'
VERSION = 1

def emit_ir_to_pybc(ir, out_path:str):
    with open(out_path, "wb") as out:
        out.write(struct.pack(">II", MAGIC, VERSION))
        name = ir.name.encode("utf-8")
        out.write(struct.pack(">H", len(name)))
        out.write(name)
        out.write(struct.pack(">I", len(ir.functions)))
        for fn in ir.functions:
            fname = fn.name.encode("utf-8")
            out.write(struct.pack(">H", len(fname)))
            out.write(fname)
            out.write(struct.pack(">I", len(fn.ops)))
            for op in fn.ops:
                out.write(struct.pack(">I", int(op.code)))
                astr = (op.a or "").encode("utf-8")
                out.write(struct.pack(">H", len(astr)))
                out.write(astr)
                out.write(struct.pack(">d", float(op.d)))
    return out_path
