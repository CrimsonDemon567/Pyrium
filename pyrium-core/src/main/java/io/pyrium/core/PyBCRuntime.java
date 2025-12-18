package io.pyrium.core;

import java.util.List;

public final class PyBCRuntime {

  public interface WorldFacade {
    List<EntityFacade> entities(String type);
  }
  public interface EntityFacade {
    double getSpeed();
    void setSpeed(double v);
  }

  private final WorldFacade world;

  public PyBCRuntime(WorldFacade world){
    this.world = world;
  }

  public void executeTick(PyBCModule.Function fn, Events.Tick evt) {
    for (var op : fn.ops) {
      switch (op.code) {
        case NOP -> {}
        case LOG -> System.out.println("[Pyrium Mod] " + (op.a == null ? "" : op.a));
        case MUL_ENTITY_SPEED -> {
          String type = op.a == null ? "Zombie" : op.a;
          double factor = op.d == 0.0 ? 1.0 : op.d;
          var list = world.entities(type);
          for (var e : list) {
            e.setSpeed(e.getSpeed() * factor);
          }
        }
      }
    }
  }
}
