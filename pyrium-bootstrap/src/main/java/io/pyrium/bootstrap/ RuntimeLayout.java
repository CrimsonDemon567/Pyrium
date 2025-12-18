package io.pyrium.bootstrap;

import java.nio.file.*;

public record RuntimeLayout(Path versionDir, Path serverJar, Path modsDir) {
  public static RuntimeLayout prepare(Resolver.Artifact art) throws Exception {
    Path dir = Paths.get(".pyrium/runtime/" + art.versionTag());
    Files.createDirectories(dir);
    Path mods = dir.resolve("mods");
    Files.createDirectories(mods);
    return new RuntimeLayout(dir, art.path(), mods);
  }
}
