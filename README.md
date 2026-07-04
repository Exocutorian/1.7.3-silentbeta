# Silent Beta 0.0.1

A quiet fork of Minecraft Beta 1.7.3: obsidian main menu, its own splash
texts, copper (ore, worldgen, ingot, block, full tool tier) and obsidian
bricks — all built on **BetaEngine**, a modding framework layer (registry
with automatic IDs, event bus, resource loader with texture pack support,
mod interface) so content is added through an API instead of scattered
vanilla edits. See [docs/betaengine.md](docs/betaengine.md).

Workspace set up with [RetroMCP-Java](https://github.com/MCPHackers/RetroMCP-Java) 1.2.

## Layout

- `minecraft/src/betaengine/` — BetaEngine framework (mirrored to the server tree)
- `minecraft/src/silentbeta/` — Silent Beta content mod (mirrored too)
- `minecraft/src/` — decompiled, remapped client sources (edit these)
- `minecraft_server/src/` — decompiled, remapped server sources
- `conf/` — RetroMCP mappings and patches for b1.7.3
- `options.cfg` — RetroMCP workspace settings
- `jars/`, `libraries/`, `*/bin`, `*/md5`, `*/src_original` — downloaded/generated, not committed (see `.gitignore`)

## Reproducing the workspace

1. Install a JDK and download the RetroMCP-Java CLI jar from the
   [releases page](https://github.com/MCPHackers/RetroMCP-Java/releases).
2. From the repository root run:

   ```sh
   java -jar RetroMCP-Java-CLI.jar setup b1.7.3
   java -jar RetroMCP-Java-CLI.jar decompile
   ```

## Common tasks

| Command | What it does |
| --- | --- |
| `recompile` | Compile the sources in `*/src` |
| `start` | Launch the client/server from compiled classes |
| `reobfuscate` | Map your changes back to obfuscated names |
| `build` | Produce a jar/zip with the modified classes |
| `createpatch` / `applypatch` | Diff-based patches against the vanilla sources |

Run them the same way: `java -jar RetroMCP-Java-CLI.jar <command>`.
