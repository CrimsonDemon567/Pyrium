package io.pyrium.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class BridgeVanilla {

  private static BukkitTask tickTask;

  public static void install(Plugin plugin) {
    // Falls bereits installiert → alten Task stoppen
    if (tickTask != null && !tickTask.isCancelled()) {
      tickTask.cancel();
    }

    // Native Paper/Vanilla Tick-Hook: läuft exakt einmal pro Server-Tick
    tickTask = Bukkit.getScheduler().runTaskTimer(
        plugin,
        () -> {
          // Hier wird der EventBus-Tick ausgelöst
          EventBus.dispatchTick();
        },
        1L, // Start nach 1 Tick
        1L  // Wiederholung: 1 Tick
    );

    // Placeholder-Thread im EventBus deaktivieren
    EventBus.disablePlaceholderLoop();

    System.out.println("[Pyrium] Vanilla bridge installed (native tick active).");
  }
}
