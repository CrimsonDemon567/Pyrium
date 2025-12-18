package io.pyrium.core;

import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {
  private static final CopyOnWriteArrayList<TickHandler> tickHandlers = new CopyOnWriteArrayList<>();

  public interface TickHandler { void onTick(Events.Tick t) throws Exception; }

  public static void init() {
    System.out.println("[Pyrium] EventBus initialized.");
    new Thread(() -> {
      try {
        long last = System.nanoTime();
        while (true) {
          long now = System.nanoTime();
          var evt = new Events.Tick(now, (now - last) / 1_000_000.0);
          last = now;
          for (var h : tickHandlers) {
            try { h.onTick(evt); } catch (Throwable ex) { ex.printStackTrace(); }
          }
          Thread.sleep(50);
        }
      } catch (InterruptedException ignored) {}
    }, "PyriumTickLoop").start();
  }

  public static void onTick(TickHandler h) { tickHandlers.add(h); }
}
