import ast
from .ir import IRModule, IRFunction, Op
from .ops import OpCode


def compile_python_to_ir(path: str) -> IRModule:
    """
    Compile a Python mod file to a simple IR (intermediate representation)
    consisting of functions and Op instructions.
    """
    with open(path, "r", encoding="utf-8") as f:
        src = f.read()

    tree = ast.parse(src, filename=path)
    mod = IRModule(name=path)

    for node in tree.body:
        if isinstance(node, ast.FunctionDef):
            fn = IRFunction(node.name)
            for stmt in node.body:
                compile_stmt(stmt, fn)
            mod.functions.append(fn)

    return mod


def compile_stmt(stmt, fn: IRFunction):
    """
    Compile a single Python AST statement into one or more IR ops.
    """
    # --- Expression statements (typically function calls like log(), broadcast(), etc.) ---
    if isinstance(stmt, ast.Expr) and isinstance(stmt.value, ast.Call):
        call = stmt.value
        if isinstance(call.func, ast.Name):
            name = call.func.id
            args = call.args

            # -------------------------
            # Core DSL examples
            # -------------------------
            if name == "log" and args:
                fn.ops.append(Op(OpCode.LOG, a=str(_const(args[0]))))

            elif name == "broadcast" and args:
                fn.ops.append(Op(OpCode.BROADCAST, a=str(_const(args[0]))))

            elif name == "exec_cmd" and args:
                fn.ops.append(Op(OpCode.EXEC_CMD, a=str(_const(args[0]))))

            elif name == "set_block" and len(args) >= 4:
                # set_block(x, y, z, block_id)
                x, y, z = [int(_const(a)) for a in args[:3]]
                packed = (x & 0xFFFF) | ((y & 0xFFFF) << 16) | ((z & 0xFFFF) << 32)
                fn.ops.append(Op(
                    OpCode.SET_BLOCK,
                    a=str(_const(args[3])),
                    i=packed
                ))

            elif name == "give_item" and len(args) >= 3:
                # give_item(player, item_id, count)
                fn.ops.append(Op(
                    OpCode.GIVE_ITEM,
                    a=str(_const(args[0])),
                    b=str(_const(args[1])),
                    i=int(_const(args[2]))
                ))

            elif name == "mul_speed" and len(args) >= 2:
                # mul_speed(entity_type, factor)
                fn.ops.append(Op(
                    OpCode.MUL_ENTITY_SPEED,
                    a=str(_const(args[0])),
                    d=float(_const(args[1]))
                ))

            # ------------------------------------------------
            # Custom Mobs (600–699 reserved in OpCode)
            # ------------------------------------------------
            elif name == "register_custom_mob" and len(args) >= 2:
                # register_custom_mob(mob_id, config_key_or_name)
                fn.ops.append(Op(
                    OpCode.REGISTER_CUSTOM_MOB,
                    a=str(_const(args[0])),   # mob_id
                    b=str(_const(args[1]))    # config key / display name / identifier
                ))

            elif name == "set_custom_mob_model" and len(args) >= 2:
                # set_custom_mob_model(mob_id, model_filename)
                fn.ops.append(Op(
                    OpCode.SET_CUSTOM_MOB_MODEL,
                    a=str(_const(args[0])),
                    b=str(_const(args[1]))    # model file name in assets/
                ))

            elif name == "set_custom_mob_texture" and len(args) >= 2:
                # set_custom_mob_texture(mob_id, texture_filename)
                fn.ops.append(Op(
                    OpCode.SET_CUSTOM_MOB_TEXTURE,
                    a=str(_const(args[0])),
                    b=str(_const(args[1]))    # texture file name in assets/
                ))

            elif name == "set_custom_mob_size" and len(args) >= 2:
                # set_custom_mob_size(mob_id, scale)
                fn.ops.append(Op(
                    OpCode.SET_CUSTOM_MOB_SIZE,
                    a=str(_const(args[0])),
                    d=float(_const(args[1]))  # scale factor
                ))

            elif name == "set_custom_mob_attr" and len(args) >= 3:
                # set_custom_mob_attr(mob_id, attr_name, value)
                fn.ops.append(Op(
                    OpCode.SET_CUSTOM_MOB_ATTR,
                    a=str(_const(args[0])),
                    b=str(_const(args[1])),
                    d=float(_const(args[2]))
                ))

            elif name == "set_custom_mob_loot_table" and len(args) >= 2:
                # set_custom_mob_loot_table(mob_id, loot_table_id)
                fn.ops.append(Op(
                    OpCode.SET_CUSTOM_MOB_LOOT_TABLE,
                    a=str(_const(args[0])),
                    b=str(_const(args[1]))
                ))

            elif name == "set_custom_mob_equip" and len(args) >= 3:
                # set_custom_mob_equip(mob_id, slot, item_id)
                # Wir benutzen: a = mob_id, b = "slot|item_id" (einfaches Encoding im String)
                mob_id = str(_const(args[0]))
                slot = str(_const(args[1]))
                item_id = str(_const(args[2]))
                encoded = f"{slot}|{item_id}"
                fn.ops.append(Op(
                    OpCode.SET_CUSTOM_MOB_EQUIP,
                    a=mob_id,
                    b=encoded
                ))

            elif name == "set_custom_mob_ai" and len(args) >= 2:
                # set_custom_mob_ai(mob_id, ai_profile_id)
                fn.ops.append(Op(
                    OpCode.SET_CUSTOM_MOB_AI,
                    a=str(_const(args[0])),
                    b=str(_const(args[1]))
                ))

            elif name == "spawn_custom_mob" and len(args) >= 4:
                # spawn_custom_mob(mob_id, x, y, z)
                x, y, z = [int(_const(a)) for a in args[1:4]]
                packed = (x & 0xFFFF) | ((y & 0xFFFF) << 16) | ((z & 0xFFFF) << 32)
                fn.ops.append(Op(
                    OpCode.SPAWN_CUSTOM_MOB,
                    a=str(_const(args[0])),
                    i=packed
                ))

            elif name == "remove_custom_mobs" and len(args) >= 1:
                # remove_custom_mobs(mob_id)
                fn.ops.append(Op(
                    OpCode.REMOVE_CUSTOM_MOBS,
                    a=str(_const(args[0]))
                ))

        return  # done with this Expr/Call

    # --- If statements ---
    if isinstance(stmt, ast.If):
        cond = _expr_to_str(stmt.test)
        fn.ops.append(Op(OpCode.IF_BEGIN, a=cond))
        for s in stmt.body:
            compile_stmt(s, fn)
        if stmt.orelse:
            fn.ops.append(Op(OpCode.IF_ELSE))
            for s in stmt.orelse:
                compile_stmt(s, fn)
        fn.ops.append(Op(OpCode.IF_END))
        return

    # --- While loops ---
    if isinstance(stmt, ast.While):
        cond = _expr_to_str(stmt.test)
        fn.ops.append(Op(OpCode.WHILE_BEGIN, a=cond))
        fn.ops.append(Op(OpCode.WHILE_CHECK, a=cond))
        for s in stmt.body:
            compile_stmt(s, fn)
        fn.ops.append(Op(OpCode.WHILE_END))
        return

    # --- For loops with range(...) ---
    if (
        isinstance(stmt, ast.For)
        and isinstance(stmt.iter, ast.Call)
        and isinstance(stmt.iter.func, ast.Name)
        and stmt.iter.func.id == "range"
        and isinstance(stmt.target, ast.Name)
    ):
        target = stmt.target.id
        args = stmt.iter.args

        # range(start, end, step) Formen
        if len(args) == 1:
            start = 0
            end = _const(args[0])
            step = 1
        elif len(args) == 2:
            start = _const(args[0])
            end = _const(args[1])
            step = 1
        else:
            start = _const(args[0])
            end = _const(args[1])
            step = _const(args[2])

        fn.ops.append(Op(OpCode.FOR_INIT, a=target, i=int(start)))
        fn.ops.append(Op(OpCode.FOR_ITER, a=target, i=int(end), d=float(step)))
        for s in stmt.body:
            compile_stmt(s, fn)
        fn.ops.append(Op(OpCode.FOR_END, a=target))
        return

    # Other statements (pass, etc.) are ignored for now


def _const(node):
    """
    Extract a constant value from an AST node, if possible.
    """
    if isinstance(node, ast.Constant):
        return node.value
    return 0


def _expr_to_str(node):
    """
    Turn a boolean/arith expression into a simple string
    that the runtime can evaluate (very basic).
    """
    if isinstance(node, ast.Compare):
        left = _expr_to_str(node.left)
        op = type(node.ops[0]).__name__
        right = _expr_to_str(node.comparators[0])
        return f"{left} {op} {right}"
    elif isinstance(node, ast.BinOp):
        return f"{_expr_to_str(node.left)} {type(node.op).__name__} {_expr_to_str(node.right)}"
    elif isinstance(node, ast.Name):
        return node.id
    elif isinstance(node, ast.Constant):
        return str(node.value)
    return ast.dump(node)
