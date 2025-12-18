package io.pyrium.bootstrap;

import java.nio.file.Path;

public final class PyriumMain {
  public static void main(String[] args) throws Exception {
    System.out.println("[Pyrium] Bootstrapping...");
    var cfg = VersionConfig.loadOrInit(Path.of("mc_version.json"));
    var art = Resolver.resolve(cfg);
    var rt = RuntimeLayout.prepare(art);

    var loader = new PyriumClassLoader(rt);
    loader.attachBaseServer(art.path());
    loader.loadCore(); // pyrium-core.jar
    loader.start();    // hooks, event bus, mods
  }
}
