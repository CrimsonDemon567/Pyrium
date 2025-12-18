package io.pyrium.rpb;

import io.pyrium.bootstrap.RuntimeLayout;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public final class ResourcePackBuilder {

  public static void buildIfNeeded(RuntimeLayout rt) {
    // modsDir zeigt jetzt direkt auf .pyrium/runtime/<version>/mods
    Path modsDir = rt.modsDir();
    Path assetsZip = modsDir.getParent().resolve("pyrium-resource-pack.zip");
    try {
      try (var out = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(assetsZip)))) {
        // Scan mods/*/assets and include into zip (namespaced)
        if (Files.exists(modsDir)) {
          try (var mods = Files.list(modsDir).filter(Files::isDirectory)) {
            for (Path modDir : (Iterable<Path>) mods::iterator) {
              Path assets = modDir.resolve("assets");
              if (!Files.exists(assets)) continue;
              Files.walk(assets).filter(Files::isRegularFile).forEach(f -> {
                try {
                  String rel = assets.relativize(f).toString().replace('\\','/');
                  // Namespace: pyrium/<modname>/<assetpath>
                  String entryName = "pyrium/" + modDir.getFileName() + "/" + rel;
                  out.putNextEntry(new ZipEntry(entryName));
                  Files.copy(f, out);
                  out.closeEntry();
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });
            }
          }
        }
        // Add mandatory pack.mcmeta
        out.putNextEntry(new ZipEntry("pack.mcmeta"));
        String mcmeta = """
          {
            "pack": { "pack_format": 48, "description": "Pyrium Resource Pack" }
          }
          """;
        out.write(mcmeta.getBytes());
        out.closeEntry();
      }
      System.out.println("[Pyrium] Built resource pack: " + assetsZip);
      // Delivery to players is server-specific; for Bukkit/Paper, you'd call ResourcePack API per player.
    } catch (IOException e) {
      System.err.println("[Pyrium] Resource pack build failed: " + e.getMessage());
    }
  }
}
