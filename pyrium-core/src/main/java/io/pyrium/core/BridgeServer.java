package io.pyrium.core;

import java.lang.reflect.Method;

public final class BridgeServer {

  public static void install() {
    // Try Bukkit/Paper first
    try {
      Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
      Object server = bukkit.getMethod("getServer").invoke(null);
      Class<?> schedulerCls = Class.forName("org.bukkit.scheduler.BukkitScheduler");
      Object scheduler = server.getClass().getMethod("getScheduler").invoke(server);
      // scheduleSyncRepeatingTask(plugin=null allowed in modern Paper? fallback to reflective tick hook)
      // Instead, use server scheduler runTaskTimer with a dummy Runnable
      Method runTaskTimer = schedulerCls.getMethod("runTaskTimer", Class.forName("org.bukkit.plugin.Plugin"), Runnable.class, long.class, long.class);
      Runnable tickTask = () -> {
        long now = System.nanoTime();
        EventBus.dispatchTick(now, 50.0);
      };
      // Plugin instance cannot be null; we install via "PyriumBridge" synthetic plugin if present.
      Object plugin = tryFindPluginInstance();
      runTaskTimer.invoke(scheduler, plugin, tickTask, 1L, 1L);
      EventBus.attachExternalTickSource();
      System.out.println("[Pyrium] Bukkit/Paper tick bridge installed.");
      return;
    } catch (Throwable ignored) {}

    // Vanilla fallback: no official hook, we rely on server thread timing (approx 20 TPS)
    // This is coarse; production should weave into MinecraftServer#tick
    new Thread(() -> {
      try {
        while (true) {
          long now = System.nanoTime();
          EventBus.dispatchTick(now, 50.0);
          Thread.sleep(50);
        }
      } catch (InterruptedException ignored) {}
    }, "PyriumVanillaTick").start();
    EventBus.attachExternalTickSource();
    System.out.println("[Pyrium] Vanilla tick bridge (approx) installed.");
  }

  private static Object tryFindPluginInstance() {
    try {
      Class<?> pluginManagerCls = Class.forName("org.bukkit.plugin.PluginManager");
      Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
      Object pm = bukkit.getMethod("getPluginManager").invoke(null);
      Object[] plugins = (Object[]) pm.getClass().getMethod("getPlugins").invoke(pm);
      for (Object p : plugins) {
        if (p.getClass().getName().toLowerCase().contains("pyrium")) return p;
      }
    } catch (Throwable ignored) {}
    return null; // if null, some servers still accept it; otherwise, fallback loop above runs
  }
}
