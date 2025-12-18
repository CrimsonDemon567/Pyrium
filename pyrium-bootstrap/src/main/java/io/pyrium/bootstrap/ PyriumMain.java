package io.pyrium.bootstrap;

import java.nio.file.*;
import java.util.stream.*;

public final class PyriumMain {
  public static void main(String[] args) throws Exception {
    System.out.println("[Pyrium] Bootstrapping...");
    var cfg = VersionConfig.loadOrInit(Path.of("mc_version.json"));
    var art = Resolver.resolve(cfg);
    var rt = RuntimeLayout.prepare(art);

    // Compile Python mods to .pybc into runtime mods dir
    compileModsToPybc(rt);

    var loader = new PyriumClassLoader(rt);
    loader.attachBaseServer(art.path());
    loader.loadCore();
    loader.start();
  }

  static void compileModsToPybc(RuntimeLayout rt) throws Exception {
    Path examples = Paths.get("pymod-examples");
    if (Files.exists(examples)) {
      try (var dirs = Files.list(examples).filter(Files::isDirectory)) {
        for (Path modDir : (Iterable<Path>)dirs::iterator) {
          Path entry = modDir.resolve("mod.py");
          if (Files.exists(entry)) {
            Path outDir = rt.modsDir().resolve(modDir.getFileName());
            Files.createDirectories(outDir);
            var proc = new ProcessBuilder()
              .command(".venv/bin/pyrium-aot", "--in", entry.toString(), "--out", outDir.toString())
              .inheritIO()
              .start();
            int code = proc.waitFor();
            if (code != 0) throw new IllegalStateException("pyrium-aot failed for " + modDir);
          }
        }
      }
    }
  }
}
