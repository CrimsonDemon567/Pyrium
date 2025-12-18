Pyrium Meta‑Loader

Pyrium is a server‑side meta‑loader: it loads a base Minecraft server (default: Vanilla via Mojang) and automatically extends it with Pyrium hooks. Mods are written in Python, compiled into PyBC (optimized binary bytecode), and executed inside the server runtime. No client mods are required.

---

Quick Start

1. Install Java 17+ and Python 3.11+.
2. Optionally edit mc_version.json. By default, Pyrium resolves the latest Vanilla release.
3. Run ./build.sh to build all Java modules and set up the Python AOT compiler.
4. Start the server with:java -jar pyrium-bootstrap/target/pyrium-bootstrap.jar



---

mc_version.json

• If "source": "mojang" and "version": "", Pyrium automatically resolves the latest stable Vanilla server.
• For Paper/Fabric/Forge or custom builds, set "source": "custom" and provide "artifact" (URL or local path).
• Optional "verify_sha256" ensures integrity.


Example:

{
  "version": "",
  "base_loader": "vanilla",
  "source": "mojang",
  "artifact": "",
  "verify_sha256": "",
  "auto_update": true,
  "allow_snapshot": false,
  "resource_pack_policy": "lock"
}


---

Runtime Layout

Pyrium stores resolved server jars and caches under:

.pyrium/runtime/<version>/


Each version is isolated and includes:

• Base server jar
• Mods directory (mods/)
• Compiled .pybc files
• Resource pack (pyrium-resource-pack.zip)


---

Modules

• pyrium-bootstrap: launcher, resolver, classloader, AOT integration
• pyrium-core: event bus, PyBC runtime, mods loader
• pyrium-rpb: resource pack builder (assets integration)
• pyrium-aot: Python → IR → PyBC compiler
• pymod-examples: sample mods


---

Writing Mods

Project Structure

Each mod lives in its own folder:

pymod-examples/
 └─ my_first_mod/
    ├─ manifest.json
    ├─ mod.py
    └─ assets/        # optional: textures, sounds, language files


manifest.json

Defines metadata and compatibility:

{
  "name": "my_first_mod",
  "entry": "mod.py",
  "version": "0.1.0",
  "mc_compat": ">=1.21.0",
  "pyrium_api": "0.2.0"
}


mod.py

Write your logic in Python using Pyrium’s DSL. Supported functions include:

• log("message") — print to server console
• broadcast("message") — send chat message to all players
• exec_cmd("command") — run a server command
• mul_speed("Zombie", 1.05) — multiply entity speed
• give_item("PlayerName", "minecraft:diamond", 1) — give items
• set_block(x, y, z, "minecraft:stone") — change blocks
• Control flow: for, if/else, while


Example:

def on_tick():
    log("Hello from Pyrium!")
    broadcast("Welcome players!")
    for i in range(0, 5):
        if i % 2 == 0:
            broadcast(f"even {i}")
        else:
            broadcast(f"odd {i}")
    give_item("Vincent", "minecraft:diamond", 1)
    set_block(0, 64, 0, "minecraft:gold_block")


Assets

Optional resources (textures, sounds, language files) go into assets/.
They are automatically bundled into pyrium-resource-pack.zip.

Example:

assets/lang/en_us.json
{
  "message.pyrium.welcome": "Welcome to Pyrium!"
}


---

Compiling Mods

Mods are compiled automatically at server startup.
You can also compile manually:

pyrium-aot --in pymod-examples/my_first_mod/mod.py --out .pyrium/runtime/<version>/mods/my_first_mod


This produces mod.pybc inside the runtime mods directory.

---

Running the Server

Start Pyrium:

java -jar pyrium-bootstrap/target/pyrium-bootstrap.jar


• Base server jar is resolved and launched.
• Mods are compiled to .pybc and loaded.
• Resource pack is built and zipped.
• Event bus dispatches ticks; on_tick functions run each tick.


---

Debugging

• Logs appear in the server console ([Pyrium Mod] ...).
• Errors in mods produce stack traces but do not crash the server.
• Use log() for debugging output.
• Use assert() in mods to enforce conditions.


---

Tips for Mod Authors

• Use control flow (for, if/else, while) to build complex logic.
• Use exec_cmd() to leverage any built‑in Minecraft command.
• Use set_block() and spawn_entity() to manipulate the world.
• Add assets to provide custom items, textures, or translations.
• Keep mods modular: one folder per mod, with its own manifest and assets.


---

License

MIT

---
