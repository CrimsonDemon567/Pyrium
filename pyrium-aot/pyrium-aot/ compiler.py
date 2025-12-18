from .ir import IRModule, IRFunction, Op

def compile_python_to_ir(path:str)->IRModule:
    # Placeholder: parse minimal decorators
    mod = IRModule(name=path)
    f = IRFunction("on_tick", "() -> None")
    f.ops.append(Op("log", ["tick executed"]))
    mod.functions.append(f)
    return mod
