import ast
from .ir import IRModule, IRFunction, Op
from .ops import OpCode

def compile_python_to_ir(path:str)->IRModule:
    with open(path, "r", encoding="utf-8") as f:
        src = f.read()
    tree = ast.parse(src, filename=path)

    mod = IRModule(name=path)
    for node in tree.body:
        if isinstance(node, ast.FunctionDef) and node.name == "on_tick":
          fn = IRFunction("on_tick")
          # Very minimal DSL: look for calls like log("...") and mul_speed("Zombie", 1.05)
          for n in node.body:
            if isinstance(n, ast.Expr) and isinstance(n.value, ast.Call):
              call = n.value
              if isinstance(call.func, ast.Name) and call.func.id == "log" and call.args:
                arg = call.args[0]
                if isinstance(arg, ast.Constant) and isinstance(arg.value, str):
                  fn.ops.append(Op(OpCode.LOG, arg.value, 0.0))
              elif isinstance(call.func, ast.Name) and call.func.id == "mul_speed":
                t = "Zombie"; f = 1.0
                if call.args:
                  a0 = call.args[0]
                  if isinstance(a0, ast.Constant):
                    if isinstance(a0.value, str): t = a0.value
                if len(call.args) > 1:
                  a1 = call.args[1]
                  if isinstance(a1, ast.Constant) and isinstance(a1.value, (int,float)):
                    f = float(a1.value)
                fn.ops.append(Op(OpCode.MUL_ENTITY_SPEED, t, f))
          mod.functions.append(fn)
    return mod
