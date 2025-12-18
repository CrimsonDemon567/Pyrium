from dataclasses import dataclass, field
from typing import List

@dataclass
class Op:
    code: int
    a: str = ""
    d: float = 0.0

@dataclass
class IRFunction:
    name: str
    ops: List[Op] = field(default_factory=list)

@dataclass
class IRModule:
    name: str
    functions: List[IRFunction] = field(default_factory=list)
