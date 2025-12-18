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
            for stmt in node.body:
                compile_stmt(stmt, fn)
            mod.functions.append(fn)
    return mod

def compile_stmt(stmt, fn):
    if isinstance(stmt, ast.Expr) and isinstance(stmt.value, ast.Call):
        call = stmt.value
        if isinstance(call.func, ast.Name):
            name = call.func.id
            if name == "log" and call.args:
                fn.ops.append(Op(OpCode.LOG, a=str(call.args[0].value)))
            elif name == "broadcast" and call.args:
                fn.ops.append(Op(OpCode.BROADCAST, a=str(call.args[0].value)))
            elif name == "exec_cmd" and call.args:
                fn.ops.append(Op(OpCode.EXEC_CMD, a=str(call.args[0].value)))
            elif name == "mul_speed" and len(call.args)>=2:
                fn.ops.append(Op(OpCode.MUL_ENTITY_SPEED, a=str(call.args[0].value), d=float(call.args[1].value)))
            elif name == "give_item" and len(call.args)>=3:
                fn.ops.append(Op(OpCode.GIVE_ITEM, a=str(call.args[0].value), b=str(call.args[1].value), i=int(call.args[2].value)))
    elif isinstance(stmt, ast.For):
        fn.ops.append(Op(OpCode.FOR_INIT, a=stmt.target.id, i=int(stmt.iter.args[0].value)))
        fn.ops.append(Op(OpCode.FOR_ITER, a=stmt.target.id, i=int(stmt.iter.args[1].value)))
        for s in stmt.body: compile_stmt(s, fn)
        fn.ops.append(Op(OpCode.FOR_END))
    elif isinstance(stmt, ast.If):
        fn.ops.append(Op(OpCode.IF_BEGIN, a=str(stmt.test)))
        for s in stmt.body: compile_stmt(s, fn)
        if stmt.orelse:
            fn.ops.append(Op(OpCode.IF_ELSE))
            for s in stmt.orelse: compile_stmt(s, fn)
        fn.ops.append(Op(OpCode.IF_END))
