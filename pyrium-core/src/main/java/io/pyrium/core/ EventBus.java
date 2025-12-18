package io.pyrium.core;

import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {
  private static final CopyOnWriteArrayList<TickHandler> tickHandlers = new CopyOnWriteArrayList<>();
  private static volatile boolean externalTickSourceAttached = false;

  public interface TickHandler { void onTick(Events.Tick t) throws Exception; }

  public static void init() {
    System.out.println("[Pyrium] EventBus initialized.");
    // Fallback tick loop only until a server bridge installs an external tick source.
    Thread t = new Thread(() -> {
      try {
        long last = System.nanoTime();
        while (!externalTickSourceAttached) {
          long now = System.nanoTime();
          var evt = new Events.Tick(now, (now - last) / 1_000_000.0);
          last = now;
          for (var h : tickHandlers) { try { h.onTick(evt); } catch (Throwable ex) { ex.printStackTrace(); } }
          Thread.sleep(50);
        }
      } catch (InterruptedException ignored) {}
    }, "PyriumFallbackTick");
    t.setDaemon(true);
    t.start();
  }

  public static void attachExternalTickSource() { externalTickSourceAttached = true; }

  public static void onTick(TickHandler h) { tickHandlers.add(h); }

  public static void dispatchTick(long nowNanos, double dtMillis) {
    var evt = new Events.Tick(nowNanos, dtMillis);
    for (var h : tickHandlers) { try { h.onTick(evt); } catch (Throwable ex) { ex.printStackTrace(); } }
  }
}
