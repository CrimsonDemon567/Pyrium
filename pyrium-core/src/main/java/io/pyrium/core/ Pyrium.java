package io.pyrium.core;

import io.pyrium.bootstrap.RuntimeLayout;

public final class Pyrium {
  public static void boot(ClassLoader cl, RuntimeLayout rt) throws Exception {
    System.out.println("[Pyrium] Core boot: Event bus, PyBC runtime, mods.");
    EventBus.init();
    Mods.loadAll(rt);
    Diagnostics.banner(rt);
  }
}
