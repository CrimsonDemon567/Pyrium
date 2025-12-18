from dataclasses import dataclass, field
from typing import List

@dataclass
class Op:
    code: int
    a: str = ""
    b: str = ""
    d: float = 0.0
    i: int = 0

@dataclass
class IRFunction:
    name: str
    ops: List[Op] = field(default_factory=list)

@dataclass
class IRModule:
    name: str
    pool: List[str] = field(default_factory=list)
    functions: List[IRFunction] = field(default_factory=list)
