package io.pyrium.core;

public final class Events {
  public record Tick(long nowNanos) {}
}
