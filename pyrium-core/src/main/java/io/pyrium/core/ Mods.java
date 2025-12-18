package io.pyrium.core;

import io.pyrium.bootstrap.RuntimeLayout;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class Mods {

  public static void loadAll(RuntimeLayout rt) throws IOException {
    System.out.println("[Pyrium] Loading .pybc mods from " + rt.modsDir());

    var world = new DemoWorld(); // Replace with actual server bridge
    try (var stream = Files.walk(rt.modsDir())) {
      stream.filter(p -> p.toString().endsWith(".pybc")).forEach(p -> {
        try {
          var mod = PyBCModule.read(p);
          var vm = new PyBCRuntime(world, mod.pool);
          var tickFn = mod.functions.get("on_tick");
          if (tickFn != null) {
            EventBus.onTick(evt -> vm.executeTick(tickFn, evt));
            System.out.println("[Pyrium] Registered tick from mod " + mod.moduleName + " (" + p.getFileName() + ")");
          }
        } catch (Exception e) { e.printStackTrace(); }
      });
    }
  }

  // Demo world extended to satisfy new facade methods
  static final class DemoWorld implements PyBCRuntime.WorldFacade {
    static final class Ent implements PyBCRuntime.EntityFacade {
      double speed = 0.2; String name = "Demo";
      public double getSpeed(){ return speed; }
      public void setSpeed(double v){ speed = v; }
      public String getName(){ return name; }
    }
    public java.util.List<PyBCRuntime.EntityFacade> entities(String type) {
      return java.util.List.of(new Ent(), new Ent(), new Ent());
    }
    public java.util.List<PyBCRuntime.EntityFacade> entitiesInRegion(String type, int x1, int y1, int z1, int x2, int y2, int z2) {
      return entities(type);
    }
    public void broadcast(String msg) { System.out.println("[Broadcast] " + msg); }
    public void execCommand(String cmd) { System.out.println("[Command] " + cmd); }
    public void giveItem(String playerName, String itemId, int count) { System.out.println("[Give] " + playerName + " <- " + itemId + " x" + count); }
    public void nbtSet(PyBCRuntime.EntityFacade e, String path, String value) { System.out.println("[NBT SET] " + e.getName() + " " + path + "=" + value); }
    public String nbtGet(PyBCRuntime.EntityFacade e, String path) { String v = "value"; System.out.println("[NBT GET] " + e.getName() + " " + path + " -> " + v); return v; }
  }
}
