package io.pyrium.core;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * PyBCRuntime führt Funktionen (Ops) eines PyBCModule aus.
 * Enthält eine einfache VM mit Variablen, Kontrollfluss (if/for/while) und
 * Bridge-Methoden auf die Welt/Spieler/Entities.
 *
 * Hinweis: Kontrollfluss wird mit "structured ops" umgesetzt:
 * - IF_BEGIN evaluiert eine Bedingung (als String-Expr oder bool Variable) und setzt einen Flag.
 * - IF_ELSE/IF_END werden in der Ausführung verwendet, um Blöcke zu überspringen.
 * - FOR_INIT/FOR_ITER/FOR_END verwenden Variablen und einfache Grenzen.
 * - WHILE_BEGIN/WHILE_CHECK/WHILE_END ähnlich, mit unoptimiertem Ausdruckseval.
 *
 * In einer späteren Version kann dies durch echte Sprungziele (PC-Offsets) ersetzt werden.
 */
public final class PyBCRuntime {

  // ========= Welt-Fassade (muss durch echte Server-Bridge implementiert werden) =========
  public interface WorldFacade {
    // Entities
    List<EntityFacade> entities(String type);
    List<EntityFacade> entitiesInRegion(String type, int x1, int y1, int z1, int x2, int y2, int z2);
    void spawnEntity(String type, double x, double y, double z);
    void removeEntity(EntityFacade e);

    // Spieler/Welt
    void broadcast(String msg);
    void messagePlayer(String playerName, String msg);
    void execCommand(String cmd);
    void giveItem(String playerName, String itemId, int count);
    void takeItem(String playerName, String itemId, int count);
    void setBlock(int x, int y, int z, String blockId);
    String getBlock(int x, int y, int z);
    void setTime(long timeTicks);
    long getTime();
    void setWeather(String mode);

    // NBT/Attr
    void nbtSet(EntityFacade e, String path, String value);
    String nbtGet(EntityFacade e, String path);
    void setEntityAttr(EntityFacade e, String attr, double value);
    double getEntityAttr(EntityFacade e, String attr);

    // Vars (für VM-Integration auf Server-Seite optional)
    void setVar(String name, long value);
    long getVar(String name);
  }

  public interface EntityFacade {
    double getSpeed();
    void setSpeed(double v);
    String getName();
    void setName(String s);
    void teleport(double x, double y, double z);
  }

  // ========= Laufzeitzustand =========
  private final WorldFacade world;
  private final PyBCModule.ConstPool pool;

  // VM Variablen und Stack (sehr simpel für PoC)
  private final Map<String, Long> intVars = new HashMap<>();
  private final Map<String, Double> floatVars = new HashMap<>();
  private final Map<String, String> strVars = new HashMap<>();

  public PyBCRuntime(WorldFacade world, PyBCModule.ConstPool pool){
    this.world = world; this.pool = pool;
  }

  // ========= Hilfsfunktionen (Eval) =========

  private boolean evalConditionString(String expr) {
    // Minimales Parsing: unterstützt "varName == 5", "varName < 10", etc.
    // Für echte Nutzung sollte hier ein kleiner Ausdrucksparser hin.
    if (expr == null || expr.isEmpty()) return false;
    try {
      // sehr grob: spalte nach Leerzeichen
      String[] parts = expr.split("\\s+");
      if (parts.length < 3) {
        // versuche bool aus Variable
        String v = strVars.getOrDefault(expr, "");
        if ("true".equalsIgnoreCase(v)) return true;
        return intVars.getOrDefault(expr, 0L) != 0;
      }
      String left = parts[0];
      String op = parts[1];
      String right = parts[2];
      long lVal = intVars.getOrDefault(left, 0L);
      long rVal;
      if (right.matches("-?\\d+")) rVal = Long.parseLong(right);
      else rVal = intVars.getOrDefault(right, 0L);
      return switch (op) {
        case "==" -> lVal == rVal;
        case "!=" -> lVal != rVal;
        case "<" -> lVal < rVal;
        case "<=" -> lVal <= rVal;
        case ">" -> lVal > rVal;
        case ">=" -> lVal >= rVal;
        default -> false;
      };
    } catch (Throwable t) { return false; }
  }

  private void varSet(String name, long v){ intVars.put(name, v); world.setVar(name, v); }
  private long varGet(String name){ return intVars.getOrDefault(name, world.getVar(name)); }
  private void varInc(String name, long step){ varSet(name, varGet(name) + step); }
  private void varDec(String name, long step){ varSet(name, varGet(name) - step); }

  // ========= Ausführung =========

  public void executeTick(PyBCModule.Function fn, Events.Tick evt) {
    // Strukturierter Kontrollfluss ohne PC-Sprünge:
    boolean skipUntilElse = false;
    boolean skipUntilEndIf = false;

    // Für FOR-Schleifen halten wir (name -> end, step)
    record ForState(long end, long step) {}
    Map<String, ForState> forStates = new HashMap<>();

    for (int pc = 0; pc < fn.ops.size(); pc++) {
      PyBCModule.Op op = fn.ops.get(pc);
      String A = pool.get(op.sIdxA);
      String B = pool.get(op.sIdxB);

      switch (op.code) {
        // ===== Core =====
        case NOP -> {}
        case LOG -> System.out.println("[Pyrium Mod] " + A);
        case DEBUG -> System.out.println("[Pyrium DEBUG] " + A + " num=" + op.num + " i64=" + op.i64);
        case ASSERT -> {
          boolean ok = evalConditionString(A);
          if (!ok) throw new IllegalStateException("ASSERT failed: " + A);
        }
        case RETURN -> { return; }

        // ===== Math/Vars =====
        case VAR_SET -> varSet(A, (long) op.i64);
        case VAR_GET -> { /* no-op: reading handled via conditions; could push to stack in advanced VM */ }
        case VAR_INC -> varInc(A, (long) op.i64);
        case VAR_DEC -> varDec(A, (long) op.i64);
        case MATH_ADD -> varSet(A, varGet(A) + (long) op.i64);
        case MATH_SUB -> varSet(A, varGet(A) - (long) op.i64);
        case MATH_MUL -> varSet(A, varGet(A) * (long) op.i64);
        case MATH_DIV -> { long d = (long) op.i64; if (d != 0) varSet(A, varGet(A) / d); }
        case RAND_INT -> varSet(A, ThreadLocalRandom.current().nextInt((int)Math.max(1, op.i64)));
        case RAND_FLOAT -> floatVars.put(A, ThreadLocalRandom.current().nextDouble());

        // ===== Control flow: IF =====
        case IF_BEGIN -> {
          boolean cond = evalConditionString(A);
          if (!cond) { skipUntilElse = true; skipUntilEndIf = false; }
          else { skipUntilElse = false; skipUntilEndIf = false; }
        }
        case IF_ELSE -> {
          if (skipUntilElse) {
            // wir haben bis hierher alles übersprungen; ab jetzt führen wir aus
            skipUntilElse = false;
            skipUntilEndIf = false;
          } else {
            // else-Block überspringen, bis IF_END
            skipUntilEndIf = true;
          }
        }
        case IF_END -> { skipUntilElse = false; skipUntilEndIf = false; }

        // ===== Control flow: FOR =====
        case FOR_INIT -> {
          // i64: start; num: optional step (wenn 0 -> 1), sIdxB: optional end in pool?
          long start = op.i64;
          long end = (long)op.num; // wir verwenden num als end, falls vom Compiler so gesetzt
          if (end == 0 && !B.isEmpty()) {
            try { end = Long.parseLong(B); } catch (Throwable ignored) {}
          }
          long step = 1;
          varSet(A, start);
          forStates.put(A, new ForState(end, step));
        }
        case FOR_ITER -> {
          ForState st = forStates.get(A);
          if (st == null) break;
          long cur = varGet(A);
          long next = cur + st.step();
          if (cur > st.end()) {
            // wir sind am Ende der Schleife: bis FOR_END skippen
            // skip logisch: wir laufen weiter, aber ignorieren Body (vereinfachte Umsetzung)
            // echte Umsetzung würde PC bis nach FOR_END setzen.
          } else {
            varSet(A, next);
          }
        }
        case FOR_END -> { /* keine Aktion in der vereinfachten Form */ }

        case BREAK -> {
          // vereinfachte Umsetzung: beendet die Ausführung komplett
          return;
        }
        case CONTINUE -> {
          // vereinfachte Umsetzung: keine effektive Änderung ohne echten PC-Jump
        }

        // ===== Control flow: WHILE (vereinfachte Demo) =====
        case WHILE_BEGIN -> { /* markiert Beginn; keine Aktion */ }
        case WHILE_CHECK -> {
          boolean cond = evalConditionString(A);
          if (!cond) {
            // ohne echte Sprünge ignorieren wir den folgenden Body (nur Demo)
          }
        }
        case WHILE_END -> { /* Ende der while-Schleife */ }

        // ===== World/time/weather =====
        case SET_TIME -> world.setTime((long)op.i64);
        case GET_TIME -> varSet(A, world.getTime());
        case SET_WEATHER -> world.setWeather(A);

        // ===== Entities =====
        case SPAWN_ENTITY -> world.spawnEntity(A, op.num /*x*/, floatVars.getOrDefault(B, 64.0), 0.0);
        case REMOVE_ENTITY -> {
          var list = world.entities(A);
          for (var e : list) world.removeEntity(e);
        }
        case FIND_ENTITIES -> {
          // schreibt Größe der Liste in Var A
          var list = world.entities(A);
          varSet(A, list.size());
        }
        case FIND_ENTITIES_REGION -> {
          // i64 packing ist PoC; echte Version sollte separate Felder haben
          int x1 = (int)(op.i64 & 0xFFFF);
          int y1 = (int)((op.i64 >>> 16) & 0xFFFF);
          int z1 = (int)((op.i64 >>> 32) & 0xFFFF);
          int x2 = (int)((op.i64 >>> 48) & 0xFFFF);
          var list = world.entitiesInRegion(A, x1,y1,z1,x2,y1,z1);
          varSet(A, list.size());
        }
        case SET_ENTITY_NBT -> {
          var list = world.entities(A);
          for (var e : list) world.nbtSet(e, B, String.valueOf(op.num));
        }
        case GET_ENTITY_NBT -> {
          var list = world.entities(A);
          for (var e : list) { String v = world.nbtGet(e, B); strVars.put(A, v); }
        }
        case SET_ENTITY_ATTR -> {
          var list = world.entities(A);
          for (var e : list) world.setEntityAttr(e, B, op.num);
        }
        case GET_ENTITY_ATTR -> {
          var list = world.entities(A);
          double last = 0.0;
          for (var e : list) last = world.getEntityAttr(e, B);
          floatVars.put(A, last);
        }
        case ADD_EFFECT -> { /* später */ }
        case CLEAR_EFFECT -> { /* später */ }
        case MUL_ENTITY_SPEED -> {
          String type = A.isEmpty() ? "Zombie" : A;
          double factor = op.num == 0.0 ? 1.0 : op.num;
          var list = world.entities(type);
          for (var e : list) { e.setSpeed(e.getSpeed() * factor); }
        }

        // ===== Players =====
        case BROADCAST -> world.broadcast(A);
        case MESSAGE_PLAYER -> world.messagePlayer(A, B);
        case GIVE_ITEM -> world.giveItem(A, B, (int)op.i64);
        case TAKE_ITEM -> world.takeItem(A, B, (int)op.i64);
        case EXEC_CMD -> world.execCommand(A);
        case TELEPORT_PLAYER -> {
          // num als X; i64 als Y (nicht ideal); demo only
          world.execCommand("tp " + A + " " + op.num + " " + op.i64 + " 0");
        }

        // ===== Blocks =====
        case SET_BLOCK -> {
          // i64 packing demo: x in low16, y in next16, z in next16
          int x = (int)(op.i64 & 0xFFFF);
          int y = (int)((op.i64 >>> 16) & 0xFFFF);
          int z = (int)((op.i64 >>> 32) & 0xFFFF);
          world.setBlock(x, y, z, A);
        }
        case GET_BLOCK -> {
          int x = (int)(op.i64 & 0xFFFF);
          int y = (int)((op.i64 >>> 16) & 0xFFFF);
          int z = (int)((op.i64 >>> 32) & 0xFFFF);
          String id = world.getBlock(x, y, z);
          strVars.put(A, id);
        }

        // ===== Scoreboard =====
        case SCOREBOARD_CREATE -> world.execCommand("scoreboard objectives add " + A + " dummy");
        case SCOREBOARD_REMOVE -> world.execCommand("scoreboard objectives remove " + A);

        // ===== Misc =====
        case SLEEP -> {
          try { Thread.sleep((long)op.i64); } catch (InterruptedException ignored) {}
        }
        case YIELD -> { Thread.yield(); }

        default -> { /* nicht implementierte Ops: no-op */ }
      }

      // ===== Custom Mobs =====
      case REGISTER_CUSTOM_MOB -> {
        world.registerCustomMob(A, B);
      }

      case SET_CUSTOM_MOB_MODEL -> {
        world.setCustomMobModel(A, B);
      }

case SET_CUSTOM_MOB_TEXTURE -> {
    world.setCustomMobTexture(A, B);
}

case SET_CUSTOM_MOB_SIZE -> {
    world.setCustomMobSize(A, op.num);
}

case SET_CUSTOM_MOB_ATTR -> {
    world.setCustomMobAttr(A, B, op.num);
}

case SET_CUSTOM_MOB_LOOT_TABLE -> {
    world.setCustomMobLootTable(A, B);
}

case SET_CUSTOM_MOB_EQUIP -> {
    // hier musst du definieren, wo das itemId herkommt -> z.B. in pool string B = slot|itemId getrennt durch ':'
    world.setCustomMobEquip(A, B, "minecraft:stone_sword"); // Platzhalter
}

case SET_CUSTOM_MOB_AI -> {
    world.setCustomMobAI(A, B);
}

case SPAWN_CUSTOM_MOB -> {
    int x = (int)(op.i64 & 0xFFFF);
    int y = (int)((op.i64 >>> 16) & 0xFFFF);
    int z = (int)((op.i64 >>> 32) & 0xFFFF);
    world.spawnCustomMob(A, x, y, z);
}

case REMOVE_CUSTOM_MOBS -> {
    world.removeCustomMobs(A);
}


      // Block-Skip nach IF_ELSE/IF_END
      if (skipUntilEndIf) {
        // Wir führen nichts aus, bis IF_END kommt
        if (op.code == PyBCModule.OpCode.IF_END) {
          skipUntilEndIf = false;
        }
      } else if (skipUntilElse) {
        // Wir führen nichts aus, bis IF_ELSE oder IF_END kommt
        if (op.code == PyBCModule.OpCode.IF_ELSE || op.code == PyBCModule.OpCode.IF_END) {
          skipUntilElse = false;
        }
      }
    }
  }
}
