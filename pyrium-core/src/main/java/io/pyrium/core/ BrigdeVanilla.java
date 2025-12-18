package io.pyrium.core;

public final class BridgeVanilla {
  public static void install() {
    // TODO: Wire into the actual Vanilla/Paper tick once server is running.
    // For now, EventBus has a placeholder loop. In production, this replaces
    // the loop with server's native tick hook and cancels the placeholder thread.
    System.out.println("[Pyrium] Vanilla bridge installed (stub).");
  }
}
