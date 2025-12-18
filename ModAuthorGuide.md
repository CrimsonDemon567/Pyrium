# Pyrium Mod Author Guide

This document explains how to write mods for **Pyrium**, the server-side Minecraft meta-loader and virtual machine.

It assumes basic Python knowledge but **no familiarity with Minecraft modding internals**.

---

## 1. Modding Model

Pyrium mods are:

- **Server-side only**
- Written in **Python**
- Compiled Ahead-of-Time into **PyBC bytecode**
- Executed inside the **Pyrium VM**
- Isolated from the JVM and from other mods

Mods interact with the game **only through Pyrium opcodes**.

---

## 2. Mod Structure

Each mod lives in its own directory:

```
my_mod/
 ├─ manifest.json
 ├─ mod.py
 └─ assets/        # optional
```

### Required Files

#### `manifest.json`

Defines metadata and compatibility:

```json
{
  "name": "my_mod",
  "entry": "mod.py",
  "version": "0.1.0",
  "mc_compat": ">=1.21.0",
  "pyrium_api": "0.2.0"
}
```

- `name` must be unique
- `entry` is the main Python file
- `mc_compat` defines supported Minecraft versions
- `pyrium_api` specifies the required API version

---

## 3. Writing Code

### Entry Point

Your mod can define one or more event handlers:

```python
def on_tick():
    log("Tick running")
```

Pyrium automatically discovers and registers event functions.

---

## 4. Event Handlers

Common event hooks include:

```python
def on_player_join(player): ...
def on_player_quit(player): ...
def on_chat(player, message): ...
def on_command(player, command): ...
def on_block_place(player, block, pos): ...
def on_block_break(player, block, pos): ...
def on_entity_spawn(entity): ...
def on_entity_death(entity): ...
```

Event handlers run **inside the VM** and must be deterministic.

---

## 5. Control Flow & Variables

Pyrium supports standard Python control flow:

```python
counter = 0

def on_tick():
    global counter
    counter += 1
    if counter % 20 == 0:
        broadcast("One second passed")
```

Supported constructs:

- `if / else`
- `for`
- `while`
- `break / continue`
- `try / except`
- `return`

---

## 6. Logging & Debugging

```python
log("Info message")
debug("Verbose output")
assert player is not None
```

- `log()` prints to server console
- `debug()` may be disabled in production
- Failed assertions do **not** crash the server

---

## 7. World Interaction

### Blocks

```python
set_block(0, 64, 0, "minecraft:gold_block")
block = get_block(0, 64, 0)
```

### Regions

```python
fill_region((0,64,0), (10,70,10), "minecraft:stone")
replace_region("minecraft:dirt", "minecraft:grass_block")
```

---

## 8. Entities

### Spawning & Querying

```python
zombie = spawn_entity("minecraft:zombie", (0,64,0))
entities = find_entities(type="minecraft:zombie", radius=10)
```

### Modifying Entities

```python
set_entity_attr(zombie, "health", 40)
add_effect(zombie, "minecraft:speed", duration=200, amplifier=1)
set_entity_flag(zombie, "no_ai", True)
```

---

## 9. Players

```python
message_player(player, "Hello!")
give_item(player, "minecraft:diamond", 3)
set_gamemode(player, "creative")
play_sound(player, "minecraft:entity.player.levelup")
```

---

## 10. Inventory & Items

```python
item = create_itemstack("minecraft:diamond_sword")
enchant_item(item, "sharpness", 5)
set_item_name(item, "Epic Sword")
give_item(player, item, 1)
```

---

## 11. Scoreboards & Data Storage

```python
scoreboard_create("kills")
set_score(player, "kills", 1)

data_store_set("global.kills", 42)
value = data_store_get("global.kills")
```

Data storage persists across restarts.

---

## 12. Assets & Resource Packs

Mods may include assets:

```
assets/
 ├─ textures/
 ├─ sounds/
 └─ lang/en_us.json
```

Assets are automatically bundled into the server resource pack.

---

## 13. Compilation

Mods are compiled automatically on server start.

Manual compilation:

```bash
pyrium-aot \
  --in my_mod/mod.py \
  --out .pyrium/runtime/<version>/mods/my_mod/
```

This produces `mod.pybc`.

---

## 14. Security & Limitations

- Mods cannot import arbitrary Python modules
- No filesystem or network access unless permitted
- No direct access to Minecraft internals
- Infinite loops are detected and halted

⚠️ Do not assume hostile-mod safety yet.

---

## 15. Best Practices

- Keep mods small and focused
- Avoid heavy work in `on_tick`
- Prefer event-driven logic
- Use data storage for persistence
- Log clearly and sparingly

---

## 16. Versioning & Compatibility

- Target a specific `pyrium_api` version
- Expect breaking changes during pre-alpha
- Watch release notes before upgrading

---

## 17. Example Mod

```python
def on_player_join(player):
    broadcast(f"Welcome {player}!")
    give_item(player, "minecraft:bread", 5)
```

---

## 18. Final Notes

Pyrium is an **experimental platform**.

If you enjoy:
- VM design
- sandboxed execution
- language tooling

…you are encouraged to contribute.

---

## License

MIT
