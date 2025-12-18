package io.pyrium.core;

import io.pyrium.bootstrap.RuntimeLayout;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class Mods {

  public static void loadAll(RuntimeLayout rt) throws IOException {
    System.out.println("[Pyrium] Loading .pybc mods from " + rt.modsDir());
    var runtimeWorld = new DemoWorld(); // Replace with actual server bridge
    var vm = new PyBCRuntime(runtimeWorld);

    try (var stream = Files.walk(rt.modsDir())) {
      stream.filter(p -> p.toString().endsWith(".pybc")).forEach(p -> {
        try {
          var mod = PyBCModule.read(p);
          var tickFn = mod.functions.get("on_tick");
          if (tickFn != null) {
            EventBus.onTick(evt -> vm.executeTick(tickFn, evt));
            System.out.println("[Pyrium] Registered tick from mod " + mod.moduleName + " (" + p.getFileName() + ")");
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }
  }

  // Temporary demo world — replace with server integration
  static final class DemoWorld implements PyBCRuntime.WorldFacade {
    static final class Ent implements PyBCRuntime.EntityFacade {
      double speed = 0.2;
      public double getSpeed(){ return speed; }
      public void setSpeed(double v){ speed = v; }
    }
    public java.util.List<PyBCRuntime.EntityFacade> entities(String type) {
      return java.util.List.of(new Ent(), new Ent(), new Ent());
    }
  }
}
