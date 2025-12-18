package io.pyrium.core;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBus {
  private static final CopyOnWriteArrayList<Consumer<Events.Tick>> tickHandlers = new CopyOnWriteArrayList<>();

  public static void init() {
    System.out.println("[Pyrium] EventBus initialized.");
    // Minimal scheduler: simulate ticks until vanilla hook is wired by BridgeVanilla
    new Thread(() -> {
      try {
        while (true) {
          dispatch(new Events.Tick(System.nanoTime()));
          Thread.sleep(50); // placeholder until hooked to server tick
        }
      } catch (InterruptedException ignored) {}
    }, "PyriumTickLoop").start();
  }

  public static void onTick(Consumer<Events.Tick> h) { tickHandlers.add(h); }

  public static void dispatch(Events.Tick t) {
    for (var h : tickHandlers) {
      try { h.accept(t); } catch (Throwable ex) { ex.printStackTrace(); }
    }
  }
}
