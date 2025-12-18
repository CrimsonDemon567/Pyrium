from enum import IntEnum

class OpCode(IntEnum):
    NOP = 0
    LOG = 1
    MUL_ENTITY_SPEED = 2
    BROADCAST = 3
    EXEC_CMD = 4
    GIVE_ITEM = 5
    NBT_SET = 6
    NBT_GET = 7
    FILTER_ENTITIES_REGION = 8
