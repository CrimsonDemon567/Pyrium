# Pyrium — Server-Side Meta-Loader & VM for Minecraft

> **Pyrium is an experimental server-side meta-loader and virtual machine for Minecraft.**  
> It launches a base server (Vanilla, Paper, Fabric, Forge, or custom builds) and extends it with a **sandboxed, opcode-based runtime** for Python-authored mods.  
> No client mods required.

---

## What Pyrium Is (and Is Not)

### ✅ Pyrium *is*:
- A **server-side meta-loader**, not a plugin API
- A **custom VM** with an explicit opcode instruction set
- A **Python-fronted, AOT-compiled runtime** (Python → IR → PyBC)
- Designed for **deterministic, isolated mod execution**
- Client-agnostic (resource packs generated automatically)

### ❌ Pyrium is *not*:
- A Forge / Fabric replacement (yet)
- A scripting layer that executes raw Python
- A production-ready platform (currently **pre-alpha**)

---

## Architecture Overview

```
Python Mod
   ↓
AOT Compiler (pyrium-aot)
   ↓
PyBC (binary bytecode)
   ↓
Pyrium VM (pyrium-core)
   ↓
Server API Bridge
   ↓
Base Minecraft Server
```

Each mod runs inside the Pyrium VM and interacts with the server **only through defined opcodes**.

---

## Instruction Set (OpCodes)

Pyrium exposes a **large but controlled opcode set**, grouped by domain:

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

> The README documents only stable, high-level ops.  
> Experimental or unsafe ops are intentionally undocumented.

---

## Runtime Isolation

Each Minecraft version runs in its own isolated runtime:

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
- Errors are sandboxed and logged
- Future versions will support permission-scoped ops

---

## Quick Start

```bash
# Requirements
Java 17+
Python 3.11+

# Build everything
./build.sh

# Start the server
java -jar pyrium-bootstrap/target/pyrium-bootstrap.jar
```

Pyrium resolves the base server automatically (default: latest Vanilla).

---

## Writing a Mod

```python
def on_player_join(player):
    broadcast(f"Welcome {player}!")
    give_item(player, "minecraft:diamond", 1)
```

Mods are compiled at startup into PyBC and loaded by the VM.

---

## Security Model (Important)

Pyrium executes **bytecode, not Python source**.

- No dynamic imports
- No reflection
- No JVM access
- IO and networking ops are permission-gated
- Unsafe ops may be disabled by server config

⚠️ Pyrium is **not yet hardened for hostile mods**.

---

## Project Status

- 🧪 **Stage:** Pre-Alpha
- 🧠 **Focus:** Architecture, VM correctness, opcode design
- 🚧 **Missing:** API freeze, docs, stability guarantees

Breaking changes **will happen**.

---

## Why This Exists

Java-based modding is powerful but complex.  
Pyrium explores whether a **VM-driven, language-agnostic approach** can offer:

- safer mods
- simpler authoring
- deterministic execution
- better isolation

This project is an experiment.

---

## Contributing

If you are interested in:
- VM design
- compiler pipelines
- Minecraft server internals

…you’re welcome here.

---

## License

MIT
