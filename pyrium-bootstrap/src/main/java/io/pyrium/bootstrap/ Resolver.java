package io.pyrium.bootstrap;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.time.Instant;

public final class Resolver {
  public record Artifact(Path path, String versionTag, boolean mojang) {}

  public static Artifact resolve(VersionConfig cfg) throws Exception {
    if ("mojang".equalsIgnoreCase(cfg.source())) {
      var vtag = cfg.version().isBlank() ? latestMojangVersion() : cfg.version();
      var url = serverJarUrlForVersion(vtag);
      var path = download(url, ".pyrium/runtime/" + vtag + "/server.jar");
      return new Artifact(path, vtag, true);
    } else if ("custom".equalsIgnoreCase(cfg.source())) {
      if (cfg.artifact().isBlank()) throw new IllegalArgumentException("custom source requires 'artifact'");
      Path out;
      if (cfg.artifact().startsWith("http://") || cfg.artifact().startsWith("https://")) {
        out = download(cfg.artifact(), ".pyrium/runtime/custom-" + Instant.now().toEpochMilli() + "/server.jar");
      } else {
        Path src = Paths.get(cfg.artifact());
        String tag = "local-" + Instant.now().toEpochMilli();
        Path dest = Paths.get(".pyrium/runtime/" + tag + "/server.jar");
        Files.createDirectories(dest.getParent());
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        out = dest;
      }
      if (!cfg.verifySha256().isBlank()) Hashing.assertSha256(out, cfg.verifySha256());
      return new Artifact(out, out.getParent().getFileName().toString(), false);
    } else {
      throw new IllegalArgumentException("Unknown source: " + cfg.source());
    }
  }

  private static String latestMojangVersion() throws Exception {
    var url = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
    try (var in = url.openStream()) {
      var json = new String(in.readAllBytes());
      var marker = "\"latest\":{\"release\":\"";
      int i = json.indexOf(marker);
      if (i < 0) throw new IllegalStateException("Cannot parse latest version.");
      int start = i + marker.length();
      int end = json.indexOf("\"", start);
      return json.substring(start, end);
    }
  }

  private static String serverJarUrlForVersion(String vtag) throws Exception {
    var manifestUrl = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
    String manifest;
    try (var in = manifestUrl.openStream()) { manifest = new String(in.readAllBytes()); }
    var key = "\"id\":\"" + vtag + "\"";
    int i = manifest.indexOf(key);
    if (i < 0) throw new IllegalStateException("Version not found: " + vtag);
    int urlStart = manifest.indexOf("\"url\":\"", i) + 7;
    int urlEnd = manifest.indexOf("\"", urlStart);
    var versionMetaUrl = manifest.substring(urlStart, urlEnd);
    String vm;
    try (var in = new URL(versionMetaUrl).openStream()) { vm = new String(in.readAllBytes()); }
    int j = vm.indexOf("\"server\":{\"sha1\":\"");
    if (j < 0) throw new IllegalStateException("Server section missing for: " + vtag);
    int s = vm.indexOf("\"url\":\"", j) + 7;
    int e = vm.indexOf("\"", s);
    return vm.substring(s, e);
  }

  private static Path download(String url, String outPath) throws Exception {
    Path out = Paths.get(outPath);
    Files.createDirectories(out.getParent());
    System.out.println("[Pyrium] Downloading: " + url);
    try (var in = new URL(url).openStream()) {
      Path tmp = out.resolveSibling(out.getFileName().toString() + ".tmp");
      Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
      Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
    return out;
  }
}
