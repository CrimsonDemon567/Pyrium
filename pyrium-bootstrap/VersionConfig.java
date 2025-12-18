package io.pyrium.bootstrap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.*;
import java.nio.file.*;
import java.util.Objects;

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
      Files.writeString(path, G.toJson(def));
      System.out.println("[Pyrium] Created default mc_version.json (vanilla, latest).");
    }
    try (var r = Files.newBufferedReader(path)) {
      var obj = G.fromJson(r, JsonObject.class);
      String version = opt(obj, "version", "");
      String baseLoader = opt(obj, "base_loader", "vanilla");
      String source = opt(obj, "source", "mojang");
      String artifact = opt(obj, "artifact", "");
      String verifySha256 = opt(obj, "verify_sha256", "");
      boolean autoUpdate = optBool(obj, "auto_update", true);
      boolean allowSnapshot = optBool(obj, "allow_snapshot", false);
      String rpPolicy = opt(obj, "resource_pack_policy", "lock");
      return new VersionConfig(version, baseLoader, source, artifact, verifySha256, autoUpdate, allowSnapshot, rpPolicy);
    }
  }

  private static String opt(JsonObject o, String k, String d) {
    return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : d;
  }
  private static boolean optBool(JsonObject o, String k, boolean d) {
    return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsBoolean() : d;
  }
}
