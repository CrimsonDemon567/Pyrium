from enum import IntEnum

class OpCode(IntEnum):
    # Core and control flow (0–49)
    NOP = 0
    LOG = 1
    DEBUG = 2
    ASSERT = 3

    IF_BEGIN = 10      # conditional start
    IF_ELSE = 11       # else branch
    IF_END = 12        # end if
    WHILE_BEGIN = 13   # loop start
    WHILE_CHECK = 14   # loop condition check
    WHILE_END = 15     # loop end
    FOR_INIT = 16      # init: var=start
    FOR_ITER = 17      # iter: var+=step, check <=end
    FOR_END = 18       # end for
    BREAK = 19
    CONTINUE = 20

    TRY_BEGIN = 21
    CATCH = 22
    TRY_END = 23

    RETURN = 24
    CALL_FUNC = 25     # call another function within module
    CALL_API = 26      # call external API bridge (server/proxy)

    # World, time, weather, dimension (50–79)
    SET_TIME = 50
    GET_TIME = 51
    SET_WEATHER = 52   # clear | rain | thunder
    GET_WEATHER = 53
    SET_GAMERULE = 54
    TELEPORT = 55      # entity or player to x,y,z,dim
    CHANGE_DIMENSION = 56
    RAYCAST = 57
    PATHFIND_TO = 58
    SET_DIFFICULTY = 59

    # Entities (80–119)
    SPAWN_ENTITY = 80
    REMOVE_ENTITY = 81
    FIND_ENTITIES = 82              # by type/tag/nbt
    FIND_ENTITIES_REGION = 83       # axis-aligned region
    SET_ENTITY_NBT = 84
    GET_ENTITY_NBT = 85
    SET_ENTITY_ATTR = 86            # attributes (health, speed, etc.)
    GET_ENTITY_ATTR = 87
    ADD_EFFECT = 88                 # potion/status
    CLEAR_EFFECT = 89
    SET_ENTITY_FLAG = 90            # AI, gravity, invulnerable
    PLAY_ENTITY_ANIMATION = 91
    EQUIP_ITEM = 92
    DROP_ITEM = 93
    MOUNT = 94
    DISMOUNT = 95
    SET_ENTITY_ROTATION = 96
    SET_ENTITY_POSITION = 97
    DAMAGE_ENTITY = 98
    HEAL_ENTITY = 99
    SET_ENTITY_NAME = 100
    SET_ENTITY_TAG = 101
    CLEAR_ENTITY_TAG = 102
    MUL_ENTITY_SPEED = 103          # convenience op

    # Players (120–159)
    BROADCAST = 120
    MESSAGE_PLAYER = 121
    TITLE_PLAYER = 122
    ACTIONBAR_PLAYER = 123
    GIVE_ITEM = 124
    TAKE_ITEM = 125
    OPEN_GUI = 126
    CLOSE_GUI = 127
    SET_PERMISSIONS = 128
    EXEC_CMD = 129
    TELEPORT_PLAYER = 130
    SET_FOOD = 131
    SET_XP = 132
    ADD_XP = 133
    SET_GAMEMODE = 134
    PLAY_SOUND = 135
    SHOW_PARTICLES = 136
    SET_PLAYER_NBT = 137
    GET_PLAYER_NBT = 138
    SET_SCORE = 139
    GET_SCORE = 140
    ADVANCEMENT_GRANT = 141
    ADVANCEMENT_REVOKE = 142

    # Inventory and items (160–199)
    CREATE_ITEMSTACK = 160
    SET_ITEM_NBT = 161
    GET_ITEM_NBT = 162
    ENCHANT_ITEM = 163
    SET_ITEM_NAME = 164
    SET_ITEM_LORE = 165
    ADD_ITEM_ATTRIBUTE = 166
    CLEAR_ITEM_ENCHANTS = 167
    REGISTER_RECIPE = 168
    UNREGISTER_RECIPE = 169
    CRAFT_ITEM = 170

    # Blocks and world edits (200–239)
    SET_BLOCK = 200
    GET_BLOCK = 201
    SET_BLOCK_NBT = 202
    GET_BLOCK_NBT = 203
    FILL_REGION = 204
    REPLACE_REGION = 205
    PASTE_SCHEMATIC = 206
    SAVE_STRUCTURE = 207
    LOAD_STRUCTURE = 208
    UPDATE_BLOCK = 209
    POWER_BLOCK = 210
    TICK_BLOCK = 211
    SET_BIOME = 212
    GET_BIOME = 213
    PLACE_FLUID = 214
    REMOVE_FLUID = 215

    # Scoreboard, tags, data storage (240–269)
    SCOREBOARD_CREATE = 240
    SCOREBOARD_REMOVE = 241
    SCOREBOARD_SET_DISPLAY = 242
    TAG_ADD = 243
    TAG_REMOVE = 244
    TAG_HAS = 245
    DATA_STORE_SET = 246
    DATA_STORE_GET = 247
    DATA_STORE_DELETE = 248

    # Loot, recipes, advancements (270–299)
    LOOT_TABLE_ROLL = 270
    LOOT_DROP_AT = 271
    ADVANCEMENT_REGISTER = 272
    ADVANCEMENT_UNREGISTER = 273
    RECIPE_REGISTER = 274
    RECIPE_UNREGISTER = 275

    # Networking and IO (300–329)
    SEND_PACKET = 300
    LISTEN_PACKET = 301
    HTTP_GET = 302
    HTTP_POST = 303
    FILE_READ = 304
    FILE_WRITE = 305

    # Events (register hooks) (330–359)
    ON_TICK = 330
    ON_ENTITY_SPAWN = 331
    ON_ENTITY_DEATH = 332
    ON_PLAYER_JOIN = 333
    ON_PLAYER_QUIT = 334
    ON_CHAT = 335
    ON_COMMAND = 336
    ON_BLOCK_PLACE = 337
    ON_BLOCK_BREAK = 338
    ON_INTERACT = 339
    ON_CRAFT = 340

    # Math and variables (utility for DSL runtime) (360–399)
    VAR_SET = 360
    VAR_GET = 361
    VAR_INC = 362
    VAR_DEC = 363
    MATH_ADD = 364
    MATH_SUB = 365
    MATH_MUL = 366
    MATH_DIV = 367
    COMP_EQ = 368
    COMP_NE = 369
    COMP_LT = 370
    COMP_LE = 371
    COMP_GT = 372
    COMP_GE = 373
    RAND_INT = 374
    RAND_FLOAT = 375

    # Particles, sounds, visuals (400–429)
    PARTICLE_SPAWN = 400
    SOUND_PLAY = 401
    BOSSBAR_CREATE = 402
    BOSSBAR_UPDATE = 403
    BOSSBAR_REMOVE = 404

    # Attributes and effects (430–459)
    ATTR_SET = 430
    ATTR_GET = 431
    EFFECT_ADD = 432
    EFFECT_REMOVE = 433

    # Tags, registries (460–489)
    TAG_REGISTER = 460
    TAG_UNREGISTER = 461
    REGISTRY_LOOKUP = 462

    # Misc (490–499)
    SLEEP = 490
    YIELD = 491
    
    #-------------------------------
    # Custom Mobs (600-699 reserved)
    #-------------------------------
    # Mob definition
    REGISTER_CUSTOM_MOB = 600
    SET_CUSTOM_MOB_MODEL = 601
    SET_CUSTOM_MOB_TEXTURE = 602    # a = mob_id, b = texture_path ("textures/entity/crimson_demon.png")
    SET_CUSTOM_MOB_SIZE = 603       # a = mob_id, d = scale (1.0 = normal)

    # Behavior / Attribute
    SET_CUSTOM_MOB_ATTR = 610       # a = mob_id, b = attr, d = value (z.B. "health", 200)
    SET_CUSTOM_MOB_LOOT_TABLE = 611 # a = mob_id, b = loot_table_id
    SET_CUSTOM_MOB_EQUIP = 612      # a = mob_id, b = slot ("mainhand"/"head"/...), i = item_id_index?? => wir lassen b=item_id
    SET_CUSTOM_MOB_AI = 613         # a = mob_id, b = ai_profile_id (serverseitig definiert)

    # Instances
    SPAWN_CUSTOM_MOB = 620          # a = mob_id, i = packed(x,y,z)
    REMOVE_CUSTOM_MOBS = 621 
