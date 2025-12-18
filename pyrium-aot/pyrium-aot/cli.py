import argparse, os
from .compiler import compile_python_to_ir
from .emit_pybc import emit_ir_to_pybc

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--in", dest="inp", required=True, help="Path to Python mod")
    p.add_argument("--out", dest="out", required=True, help="Directory to place .pybc")
    args = p.parse_args()

    ir = compile_python_to_ir(args.inp)
    os.makedirs(args.out, exist_ok=True)
    out_file = os.path.join(args.out, "mod.pybc")
    emit_ir_to_pybc(ir, out_file)
    print(f"[Pyrium AOT] Emitted: {out_file}")
