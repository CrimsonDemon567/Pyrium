# Pyrium — Server-Side Meta-Loader & VM for Minecraft

> Pyrium is a server-side meta-loader and virtual machine for Minecraft.  
> It launches a base server (Vanilla, Paper, Fabric, Forge, or custom builds) and extends it with a **sandboxed, opcode-based runtime** for Python-authored mods. No client mods required.

---

## What Pyrium Is (and Is Not)

### ✅ Pyrium *is*:
- A **server-side meta-loader**, not a plugin API
- A **custom VM** executing PyBC bytecode
- A **Python-fronted, AOT-compiled runtime** (Python → IR → PyBC)
- Designed for **deterministic, isolated mod execution**
- Client-agnostic (resource packs generated automatically)
- Capable of running **basic and intermediate mods** from `pymod-examples`

### ❌ Pyrium is *not*:
- A Forge / Fabric replacement
- A production-ready platform

---

## Architecture Overview

```
Python Mod
   ↓
compiler.py + ir.py
   ↓
PyBC bytecode (.pybc)
   ↓
PyBCRuntime.java
   ↓
PyBCModule.java (loaded mod)
   ↓
Event Dispatcher
   ↓
Base Minecraft Server
```

- Mods are compiled into PyBC bytecode by `compiler.py` + `ir.py`
- `PyBCRuntime` executes bytecode inside the server JVM
- `PyBCModule` represents each mod, its manifest, events, and assets

---

## Instruction Set (OpCodes)

Pyrium exposes ~170 OpCodes, grouped by domain:

- Core & control flow (if/while/for/try)
- Events (player, entity, block, chat, command)
- World & blocks (set, fill, biome, structure)
- Entities & players (NBT, attributes, effects)
- Inventory, recipes, loot tables
- Scoreboards, tags, data storage
- Networking & IO (permission-gated)
- Math, variables, randomness
- Visuals (particles, sounds, bossbars)

📄 **Full opcode list:** `pyrium-core/runtime/ops.py`  

> Only stable, high-level Ops are documented; experimental or unsafe Ops may not be fully implemented.

---

## Runtime Isolation

Each Minecraft version has an isolated runtime:

```
.pyrium/runtime/<mc-version>/
 ├─ server.jar
 ├─ mods/
 │   └─ my_mod/
 │       └─ mod.pybc
 ├─ cache/
 └─ pyrium-resource-pack.zip
```

- Mods cannot crash the server
- Errors are sandboxed
- Event hooks dispatch in the VM

---

## Quick Start

```bash
# Requirements
Java 17+
Python 3.11+

# Build Java modules and AOT compiler
./build.sh

# Start the server
java -jar pyrium-bootstrap/target/pyrium-bootstrap.jar
```

Pyrium automatically resolves the base server (latest Vanilla by default).

---

## Writing a Mod

Example structure:

```
my_mod/
 ├─ manifest.json
 ├─ mod.py
 └─ assets/
```

### manifest.json

```json
{
  "name": "my_mod",
  "entry": "mod.py",
  "version": "0.1.0",
  "mc_compat": ">=1.21.0",
  "pyrium_api": "0.2.0"
}
```

### mod.py

```python
def on_player_join(player):
    broadcast(f"Welcome {player}!")
    give_item(player, "minecraft:diamond", 1)
```

- Event functions are automatically registered
- Use `log()` and `debug()` for debugging
- Control flow, variables, math, loops, try/except are supported

---

## Assets & Resource Packs

```
assets/
 ├─ textures/
 ├─ sounds/
 └─ lang/en_us.json
```

- Bundled automatically into `pyrium-resource-pack.zip`

---

## Compilation

- Mods are compiled automatically at startup
- Manual compilation:

```bash
pyrium-aot \
  --in my_mod/mod.py \
  --out .pyrium/runtime/<version>/mods/my_mod/
```

- Output: `mod.pybc` loaded by the runtime

---

## Security & Limitations

- Bytecode execution only (no raw Python execution)
- No dynamic imports or JVM reflection
- IO and networking ops are permission-gated
- Not fully hardened for hostile mods

---

## Supported Features (Working)

- Base server launch (Vanilla)
- Simple mods: logging, broadcast, give_item, set_block
- Tick and basic player events (`on_tick`, `on_player_join`)
- PyBC compilation and execution
- Asset/resource pack bundling

---

## Experimental / Partial Features

- Advanced entity ops (NBT, attributes, AI)
- Complex event hooks (craft, interact, command)
- IO, networking, HTTP
- Pathfinding, animations

---

## Project Status

- 🧪 Alpha, experimental
- ⚠️ API not stable
- 🔧 Still under active development
- Breaking changes likely

---

## Why Pyrium Exists

- Provide a Python-friendly, VM-driven modding platform
- Enable deterministic, isolated mods
- Experiment with alternative server-side architectures

---

## Contributing

Interested in:

- VM design
- Compiler pipelines
- Minecraft server internals

…you’re welcome to contribute.

---

## License

MIT
