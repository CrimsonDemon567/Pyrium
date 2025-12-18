package io.pyrium.core;

public final class Pyrium {
  public static void boot(ClassLoader cl) throws Exception {
    System.out.println("[Pyrium] Core boot: installing vanilla bridge and event bus.");
    BridgeVanilla.install();
    EventBus.init();
    Mods.loadAll(); // loads Python-compiled mods and Java-side stubs
    Diagnostics.banner();
  }
}
