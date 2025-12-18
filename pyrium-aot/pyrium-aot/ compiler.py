import ast
from .ir import IRModule, IRFunction, Op
from .ops import OpCode

def intern(pool, s):
    if s not in pool: pool.append(s)
    return pool.index(s)

def compile_python_to_ir(path:str)->IRModule:
    with open(path, "r", encoding="utf-8") as f:
        src = f.read()
    tree = ast.parse(src, filename=path)

    mod = IRModule(name=path)
    for node in tree.body:
        if isinstance(node, ast.FunctionDef) and node.name == "on_tick":
            fn = IRFunction("on_tick")
            for n in node.body:
                if isinstance(n, ast.Expr) and isinstance(n.value, ast.Call):
                    call = n.value
                    if isinstance(call.func, ast.Name):
                        fn.ops.append(dispatch_call(mod.pool, call))
            mod.functions.append(fn)
    return mod

def dispatch_call(pool, call:ast.Call)->Op:
    name = call.func.id
    if name == "log" and call.args and isinstance(call.args[0], ast.Constant):
        s = str(call.args[0].value); return Op(OpCode.LOG, a=s)
    if name == "mul_speed":
        t="Zombie"; f=1.0
        if call.args:
            a0 = call.args[0]
            if isinstance(a0, ast.Constant): t = str(a0.value)
        if len(call.args)>1:
            a1 = call.args[1]
            if isinstance(a1, ast.Constant) and isinstance(a1.value,(int,float)): f=float(a1.value)
        return Op(OpCode.MUL_ENTITY_SPEED, a=t, d=f)
    if name == "broadcast" and call.args and isinstance(call.args[0], ast.Constant):
        return Op(OpCode.BROADCAST, a=str(call.args[0].value))
    if name == "exec_cmd" and call.args and isinstance(call.args[0], ast.Constant):
        return Op(OpCode.EXEC_CMD, a=str(call.args[0].value))
    if name == "give_item" and len(call.args)>=3:
        player = str(call.args[0].value) if isinstance(call.args[0], ast.Constant) else "Player"
        item = str(call.args[1].value) if isinstance(call.args[1], ast.Constant) else "minecraft:stone"
        count = int(call.args[2].value) if isinstance(call.args[2], ast.Constant) else 1
        return Op(OpCode.GIVE_ITEM, a=player, b=item, i=count)
    if name == "nbt_set" and len(call.args)>=3:
        return Op(OpCode.NBT_SET, a=str(call.args[1].value), b=str(call.args[2].value))
    if name == "nbt_get" and len(call.args)>=2:
        return Op(OpCode.NBT_GET, a=str(call.args[1].value))
    if name == "filter_entities_region" and len(call.args)>=7:
        t = str(call.args[0].value) if isinstance(call.args[0], ast.Constant) else "Zombie"
        # pack ints (demo packing)
        nums = []
        for k in range(1,7):
            a = call.args[k]
            nums.append(int(a.value) if isinstance(a, ast.Constant) else 0)
        packed = (nums[0] & 0xFFFF) | ((nums[1] & 0xFFFF)<<16) | ((nums[2] & 0xFFFF)<<32) | ((nums[3] & 0xFFFF)<<48)
        return Op(OpCode.FILTER_ENTITIES_REGION, a=t, i=packed)
    return Op(OpCode.NOP)
