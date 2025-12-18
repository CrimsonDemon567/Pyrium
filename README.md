# Pyrium meta-loader (PyBC)

Pyrium is a server-side meta-loader: It loads a base Minecraft server JAR (default: Vanilla via Mojang) and automatically extends it with Pyrium hooks. Mods are written in Python, compiled to PyBC (optimized binary bytecode), and executed server-side. No client-side mods.

## Quick start

1. Install Java 17+ and Python 3.11+.

2. Optional: Adjust `mc_version.json`. The default loads the latest Vanilla version.

3. Run `./build.sh` (builds Java modules and sets up Python AOT).

4. Start: `java -jar pyrium-bootstrap/target/pyrium-bootstrap.jar`.


## mc_version.json

- `"source": "mojang"` and an empty `"version"` → the latest vanilla version will be loaded automatically.

- For Paper/Fabric/Forge or custom builds: Set `"source": "custom"` and `"artifact"` (URL or path); optionally, `verify_sha256`.

## Mods and PyBC

- Write mods in Python and compile them to `.pybc` using `pyrium-aot`.

- Pyrium loads all `.pybc` files into `.pyrium/runtime/<version>/mods/`.



``` ## Modules

- pyrium-bootstrap: Resolver, Runtime, ClassLoader, AOT Integration

- pyrium-core: EventBus, PyBC Runtime, Mods Loader

- pyrium-aot: Python → IR → PyBC Compiler

- pyrium-rpb: Resource Pack Builder (Stub)

- pymod-examples: Example Mod

## License
MIT
