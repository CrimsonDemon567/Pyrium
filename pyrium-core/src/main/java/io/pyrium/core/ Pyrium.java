package io.pyrium.core;

import io.pyrium.bootstrap.RuntimeLayout;
import io.pyrium.rpb.ResourcePackBuilder;

public final class Pyrium {
  public static void boot(ClassLoader cl, RuntimeLayout rt) throws Exception {
    System.out.println("[Pyrium] Core boot: Event bus, PyBC runtime, mods.");
    EventBus.init();
    BridgeServer.install(); // attach synchronized tick if possible
    ResourcePackBuilder.buildIfNeeded(rt); // build assets before mods load
    Mods.loadAll(rt);
    Diagnostics.banner(rt);
  }
}
