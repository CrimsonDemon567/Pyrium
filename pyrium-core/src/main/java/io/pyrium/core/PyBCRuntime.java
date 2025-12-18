package io.pyrium.core;

import java.util.List;

public final class PyBCRuntime {

  public interface WorldFacade {
    List<EntityFacade> entities(String type);
    List<EntityFacade> entitiesInRegion(String type, int x1, int y1, int z1, int x2, int y2, int z2);
    void broadcast(String msg);
    void execCommand(String cmd);
    void giveItem(String playerName, String itemId, int count);
    void nbtSet(EntityFacade e, String path, String value);
    String nbtGet(EntityFacade e, String path);
  }

  public interface EntityFacade {
    double getSpeed();
    void setSpeed(double v);
    String getName();
  }

  private final WorldFacade world;
  private final PyBCModule.ConstPool pool;

  public PyBCRuntime(WorldFacade world, PyBCModule.ConstPool pool){
    this.world = world; this.pool = pool;
  }

  public void executeTick(PyBCModule.Function fn, Events.Tick evt) {
    for (var op : fn.ops) {
      switch (op.code) {
        case NOP -> {}
        case LOG -> System.out.println("[Pyrium Mod] " + pool.get(op.sIdxA));
        case BROADCAST -> world.broadcast(pool.get(op.sIdxA));
        case EXEC_CMD -> world.execCommand(pool.get(op.sIdxA));
        case GIVE_ITEM -> world.giveItem(pool.get(op.sIdxA), pool.get(op.sIdxB), (int)op.i);
        case MUL_ENTITY_SPEED -> {
          String type = pool.get(op.sIdxA);
          double factor = op.d == 0.0 ? 1.0 : op.d;
          var list = world.entities(type);
          for (var e : list) { e.setSpeed(e.getSpeed() * factor); }
        }
        case FILTER_ENTITIES_REGION -> {
          String type = pool.get(op.sIdxA);
          int x1 = (int)(op.i & 0xFFFF), y1 = (int)((op.i >>> 16) & 0xFFFF), z1 = (int)((op.i >>> 32) & 0xFFFF);
          int x2 = (int)((op.i >>> 48) & 0xFFFF); // simplified pack; production would use wider fields
          var list = world.entitiesInRegion(type, x1,y1,z1,x2,y1,z1); // demo; improve packing
          // no-op: could set a selection context for following ops
        }
        case NBT_SET -> {
          // sIdxA: path, sIdxB: value; target selection simplified for demo
          for (var e : world.entities(pool.get(0))) { world.nbtSet(e, pool.get(op.sIdxA), pool.get(op.sIdxB)); }
        }
        case NBT_GET -> {
          for (var e : world.entities(pool.get(0))) { world.nbtGet(e, pool.get(op.sIdxA)); }
        }
      }
    }
  }
}
