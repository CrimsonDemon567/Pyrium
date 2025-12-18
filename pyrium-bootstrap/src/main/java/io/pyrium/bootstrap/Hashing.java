package io.pyrium.bootstrap;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;

public final class Hashing {
  public static void assertSha256(Path file, String expectedHex) throws IOException {
    String hex = sha256(file);
    if (!hex.equalsIgnoreCase(expectedHex)) {
      throw new IllegalStateException("SHA-256 mismatch for " + file + ": computed " + hex + ", expected " + expectedHex);
    }
  }
  public static String sha256(Path file) throws IOException {
    try {
      var md = MessageDigest.getInstance("SHA-256");
      try (var in = Files.newInputStream(file)) {
        byte[] buf = new byte[1 << 20];
        int n;
        while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
      }
      var b = md.digest();
      var sb = new StringBuilder();
      for (byte x : b) sb.append(String.format("%02x", x));
      return sb.toString();
    } catch (Exception e) {
      throw new IOException(e);
    }
  }
}
