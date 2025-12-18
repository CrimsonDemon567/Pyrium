# Pyrium meta-loader

Pyrium is a constant server-side layer that loads a base Minecraft server (default: Vanilla via Mojang).
It resolves versions from `mc_version.json`, isolates runtime per MC version, injects hooks, and runs Python mods compiled to JVM bytecode via the Pyrium AOT pipeline.

## Quick start
1. Ensure Java 17+ and Python 3.11+ are installed.
2. Edit `mc_version.json` (or leave defaults for latest Vanilla).
3. Run `./build.sh` to build all modules.
4. Start: `java -jar pyrium-bootstrap/target/pyrium-bootstrap.jar`.

## mc_version.json
- If `"source": "mojang"` and `"version": ""`, Pyrium resolves the latest stable Vanilla server automatically.
- For Paper/Fabric/Forge, set `"source": "custom"` and provide `"artifact"` URL plus optional `"verify_sha256"`.

## Runtime layout
Pyrium stores resolved server jars and caches under `.pyrium/runtime/<version>/`.
Each version is isolated and includes profiling, caches, and compiled mod classes.

## Modules
- pyrium-bootstrap: launcher, resolver, classloader
- pyrium-core: event bus, mod loader, vanilla bridge
- pyrium-rpb: resource pack builder (stub)
- pyrium-aot: Python → IR → JVM class generation (M1)
- pymod-examples: sample Pyrium mod

## License
MIT
