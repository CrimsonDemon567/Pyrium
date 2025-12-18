package io.pyrium.bootstrap;

import java.nio.file.*;

public record RuntimeLayout(Path versionDir, Path serverJar) {
  public static RuntimeLayout prepare(Resolver.Artifact art) throws Exception {
    Path dir = Paths.get(".pyrium/runtime/" + art.versionTag());
    Files.createDirectories(dir);
    return new RuntimeLayout(dir, art.path());
  }
}
