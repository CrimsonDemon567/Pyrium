package io.pyrium.core;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * PyBCModule repräsentiert ein kompiliertes .pybc Modul.
 * Format v2 mit String-Konstantenpool und erweiterten Feldern.
 *
 * Binary layout (big-endian):
 * - u32 MAGIC = 'PYBC' (0x50594243)
 * - u32 VERSION = 2
 * - UTF moduleName
 * - u32 poolCount
 *   - poolCount x UTF string
 * - u32 fnCount
 *   - fnCount x {
 *       UTF fnName
 *       u32 opCount
 *       opCount x {
 *         u32 opcode
 *         s32 strAIndex
 *         s32 strBIndex
 *         f64 numberPayload
 *         s64 intPayload
 *       }
 *     }
 */
public final class PyBCModule {
  public static final int MAGIC = 0x50594243; // 'PYBC'
  public static final int VERSION = 2;

  // ========= Const pool =========
  public static final class ConstPool {
    public final List<String> strings;
    public ConstPool(List<String> strings){ this.strings = strings; }
    public String get(int idx){
      if (idx < 0 || idx >= strings.size()) return "";
      return strings.get(idx);
    }
    public int size(){ return strings.size(); }
  }

  // ========= Function =========
  public static final class Function {
    public final String name;
    public final List<Op> ops;
    public Function(String name, List<Op> ops) { this.name = name; this.ops = ops; }
    public int size(){ return ops.size(); }
  }

  // ========= OpCode (subset; erweitere bei Bedarf 1:1 zu ops.py) =========
  public enum OpCode {
    // Core and control flow
    NOP(0), LOG(1), DEBUG(2), ASSERT(3),
    IF_BEGIN(10), IF_ELSE(11), IF_END(12),
    WHILE_BEGIN(13), WHILE_CHECK(14), WHILE_END(15),
    FOR_INIT(16), FOR_ITER(17), FOR_END(18),
    BREAK(19), CONTINUE(20),
    TRY_BEGIN(21), CATCH(22), TRY_END(23),
    RETURN(24), CALL_FUNC(25), CALL_API(26),

    // World/time/weather
    SET_TIME(50), GET_TIME(51), SET_WEATHER(52), GET_WEATHER(53),
    SET_GAMERULE(54), TELEPORT(55), CHANGE_DIMENSION(56),

    // Entities
    SPAWN_ENTITY(80), REMOVE_ENTITY(81),
    FIND_ENTITIES(82), FIND_ENTITIES_REGION(83),
    SET_ENTITY_NBT(84), GET_ENTITY_NBT(85),
    SET_ENTITY_ATTR(86), GET_ENTITY_ATTR(87),
    ADD_EFFECT(88), CLEAR_EFFECT(89),
    MUL_ENTITY_SPEED(103),

    // Players
    BROADCAST(120), MESSAGE_PLAYER(121),
    GIVE_ITEM(124), TAKE_ITEM(125),
    EXEC_CMD(129), TELEPORT_PLAYER(130),

    // Blocks
    SET_BLOCK(200), GET_BLOCK(201),

    // Scoreboard (minimal)
    SCOREBOARD_CREATE(240), SCOREBOARD_REMOVE(241),

    // Events (registration no-op at runtime tick)
    ON_TICK(330), ON_ENTITY_SPAWN(331), ON_PLAYER_JOIN(333),

    // Math/vars
    VAR_SET(360), VAR_GET(361), VAR_INC(362), VAR_DEC(363),
    MATH_ADD(364), MATH_SUB(365), MATH_MUL(366), MATH_DIV(367),
    COMP_EQ(368), COMP_NE(369), COMP_LT(370), COMP_LE(371), COMP_GT(372), COMP_GE(373),
    RAND_INT(374), RAND_FLOAT(375),

    // Misc
    SLEEP(490), YIELD(491);

    // Custom Mobs
    REGISTER_CUSTOM_MOB(600), SET_CUSTOM_MOD_MODEL(601), SET_CUSTOM_MOB_TEXTURE(602), SET_CUSTOM_MOB_SIZE(603);
    SET_CUSTOM_MOB_ATTR(610), SET_CUSTOM_MOB_LOOT_TABLE(611), SET_CUSTOM_MOB_EQUIP(612), SET_CUSTOM_MOB_AI(613);
    SPAWN_CUSTOM_MOB(620), REMOVE_CUSTOM_MOBS(621);
    
    public final int id; OpCode(int id){ this.id = id; }
    public static OpCode of(int id){
      for (var o : values()) if (o.id == id) return o;
      throw new IllegalArgumentException("Unknown opcode: " + id);
    }
  }

  // ========= Op =========
  public static final class Op {
    public final OpCode code;
    public final int sIdxA;
    public final int sIdxB;
    public final double num;
    public final long i64;

    public Op(OpCode code, int sIdxA, int sIdxB, double num, long i64){
      this.code = code; this.sIdxA = sIdxA; this.sIdxB = sIdxB; this.num = num; this.i64 = i64;
    }

    @Override public String toString() {
      return "Op{" + code + ", sA=" + sIdxA + ", sB=" + sIdxB + ", num=" + num + ", i64=" + i64 + "}";
    }
  }

  // ========= Module fields =========
  public final String moduleName;
  public final ConstPool pool;
  public final Map<String, Function> functions;

  public PyBCModule(String moduleName, ConstPool pool, Map<String, Function> functions) {
    this.moduleName = moduleName;
    this.pool = pool;
    this.functions = functions;
  }

  // ========= Reader =========
  public static PyBCModule read(Path file) throws IOException {
    try (var in = new DataInputStream(new BufferedInputStream(new FileInputStream(file.toFile())))) {
      int magic = in.readInt();
      if (magic != MAGIC) throw new IOException("Bad magic in " + file);
      int ver = in.readInt();
      if (ver != VERSION) throw new IOException("Unsupported pybc version: " + ver);

      String modName = in.readUTF();

      int poolCount = in.readInt();
      List<String> strings = new ArrayList<>(Math.max(poolCount, 8));
      for (int k = 0; k < poolCount; k++) {
        strings.add(in.readUTF());
      }
      ConstPool pool = new ConstPool(strings);

      int fnCount = in.readInt();
      Map<String, Function> fns = new HashMap<>(Math.max(fnCount, 4));
      for (int i = 0; i < fnCount; i++) {
        String fname = in.readUTF();
        int opCount = in.readInt();
        List<Op> ops = new ArrayList<>(Math.max(opCount, 8));
        for (int j = 0; j < opCount; j++) {
          int opcode = in.readInt();
          int sA = in.readInt();
          int sB = in.readInt();
          double num = in.readDouble();
          long i64 = in.readLong();
          ops.add(new Op(OpCode.of(opcode), sA, sB, num, i64));
        }
        fns.put(fname, new Function(fname, ops));
      }

      return new PyBCModule(modName, pool, fns);
    }
  }
}
