package io.pyrium.core;

import io.pyrium.bootstrap.RuntimeLayout;

public final class Diagnostics {
  public static void banner(RuntimeLayout rt) {
    System.out.println("[Pyrium] Active. Runtime: " + rt.versionDir() + " | Mods dir: " + rt.modsDir());
  }
}
