class IRModule:
    def __init__(self, name):
        self.name = name
        self.functions = []

class IRFunction:
    def __init__(self, name, sig):
        self.name = name
        self.sig = sig
        self.ops = []

class Op:
    def __init__(self, kind, args=None):
        self.kind = kind
        self.args = args or []
