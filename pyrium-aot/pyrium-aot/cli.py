import argparse
from .compiler import compile_python_to_ir
from .emit_class import emit_ir_to_class

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--in", dest="inp", required=True, help="Path to Python mod")
    p.add_argument("--out", dest="out", required=True, help="Output directory for class files")
    args = p.parse_args()
    ir = compile_python_to_ir(args.inp)
    cls = emit_ir_to_class(ir, args.out)
    print(f"[Pyrium AOT] Emitted: {cls}")
