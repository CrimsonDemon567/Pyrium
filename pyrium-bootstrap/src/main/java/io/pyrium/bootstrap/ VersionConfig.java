package io.pyrium.bootstrap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.*;

public record VersionConfig(String version, String baseLoader, String source,
                            String artifact, String verifySha256,
                            boolean autoUpdate, boolean allowSnapshot,
                            String resourcePackPolicy) {

  static final Gson G = new Gson();

  public static VersionConfig loadOrInit(Path path) throws IOException {
    if (Files.notExists(path)) {
      var def = new JsonObject();
      def.addProperty("version", "");
      def.addProperty("base_loader", "vanilla");
      def.addProperty("source", "mojang");
      def.addProperty("artifact", "");
      def.addProperty("verify_sha256", "");
      def.addProperty("auto_update", true);
      def.addProperty("allow_snapshot", false);
      def.addProperty("resource_pack_policy", "lock");
      Files.createDirectories(path.getParent() == null ? Path.of(".") : path.getParent());
      Files.writeString(path, G.toJson(def));
      System.out.println("[Pyrium] Created default mc_version.json (vanilla, latest).");
    }
    try (var r = Files.newBufferedReader(path)) {
      var obj = G.fromJson(r, JsonObject.class);
      return new VersionConfig(
        str(obj, "version", ""),
        str(obj, "base_loader", "vanilla"),
        str(obj, "source", "mojang"),
        str(obj, "artifact", ""),
        str(obj, "verify_sha256", ""),
        bool(obj, "auto_update", true),
        bool(obj, "allow_snapshot", false),
        str(obj, "resource_pack_policy", "lock")
      );
    }
  }

  private static String str(JsonObject o, String k, String d) {
    return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : d;
  }
  private static boolean bool(JsonObject o, String k, boolean d) {
    return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsBoolean() : d;
  }
}
