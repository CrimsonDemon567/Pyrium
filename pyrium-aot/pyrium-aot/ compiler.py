import ast
from .ir import IRModule, IRFunction, Op
from .ops import OpCode

def compile_python_to_ir(path: str) -> IRModule:
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

def compile_stmt(stmt, fn: IRFunction):
    # Funktionsaufrufe (DSL)
    if isinstance(stmt, ast.Expr) and isinstance(stmt.value, ast.Call):
        call = stmt.value
        if isinstance(call.func, ast.Name):
            name = call.func.id
            args = call.args
            if name == "log" and args:
                fn.ops.append(Op(OpCode.LOG, a=str(_const(args[0]))))
            elif name == "broadcast" and args:
                fn.ops.append(Op(OpCode.BROADCAST, a=str(_const(args[0]))))
            elif name == "exec_cmd" and args:
                fn.ops.append(Op(OpCode.EXEC_CMD, a=str(_const(args[0]))))
            elif name == "mul_speed" and len(args) >= 2:
                fn.ops.append(Op(OpCode.MUL_ENTITY_SPEED,
                                 a=str(_const(args[0])),
                                 d=float(_const(args[1]))))
            elif name == "give_item" and len(args) >= 3:
                fn.ops.append(Op(OpCode.GIVE_ITEM,
                                 a=str(_const(args[0])),
                                 b=str(_const(args[1])),
                                 i=int(_const(args[2]))))
            elif name == "set_block" and len(args) >= 4:
                # pack x,y,z in i64 (demo)
                x,y,z = [int(_const(a)) for a in args[:3]]
                packed = (x & 0xFFFF) | ((y & 0xFFFF)<<16) | ((z & 0xFFFF)<<32)
                fn.ops.append(Op(OpCode.SET_BLOCK,
                                 a=str(_const(args[3])),
                                 i=packed))

    # For-Schleifen mit range()
    elif isinstance(stmt, ast.For) and isinstance(stmt.iter, ast.Call) and isinstance(stmt.iter.func, ast.Name) and stmt.iter.func.id == "range":
        target = stmt.target.id
        args = stmt.iter.args
        start = _const(args[0]) if len(args) >= 1 else 0
        end = _const(args[1]) if len(args) >= 2 else start
        step = _const(args[2]) if len(args) >= 3 else 1
        fn.ops.append(Op(OpCode.FOR_INIT, a=target, i=int(start)))
        fn.ops.append(Op(OpCode.FOR_ITER, a=target, i=int(end), d=float(step)))
        for s in stmt.body:
            compile_stmt(s, fn)
        fn.ops.append(Op(OpCode.FOR_END, a=target))

    # If/Else
    elif isinstance(stmt, ast.If):
        cond = _expr_to_str(stmt.test)
        fn.ops.append(Op(OpCode.IF_BEGIN, a=cond))
        for s in stmt.body: compile_stmt(s, fn)
        if stmt.orelse:
            fn.ops.append(Op(OpCode.IF_ELSE))
            for s in stmt.orelse: compile_stmt(s, fn)
        fn.ops.append(Op(OpCode.IF_END))

    # While-Schleife
    elif isinstance(stmt, ast.While):
        cond = _expr_to_str(stmt.test)
        fn.ops.append(Op(OpCode.WHILE_BEGIN, a=cond))
        fn.ops.append(Op(OpCode.WHILE_CHECK, a=cond))
        for s in stmt.body: compile_stmt(s, fn)
        fn.ops.append(Op(OpCode.WHILE_END))

def _const(node):
    if isinstance(node, ast.Constant):
        return node.value
    return 0

def _expr_to_str(node):
    # sehr einfache String-Repräsentation für Bedingungen
    if isinstance(node, ast.Compare):
        left = _expr_to_str(node.left)
        op = type(node.ops[0]).__name__
        right = _expr_to_str(node.comparators[0])
        return f"{left} {op} {right}"
    elif isinstance(node, ast.Name):
        return node.id
    elif isinstance(node, ast.Constant):
        return str(node.value)
    return ast.dump(node)
