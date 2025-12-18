package io.pyrium.core;

import java.io.*;
import java.util.*;

public final class PyBCModule {
  public static final int MAGIC = 0x50594243; // 'PYBC'
  public static final int VERSION = 2;        // upgraded format

  // Constant pool for strings to reduce size
  public static final class ConstPool {
    public final List<String> strings;
    ConstPool(List<String> strings){ this.strings = strings; }
    String get(int idx){ return strings.get(idx); }
  }

  public static final class Function {
    public final String name;
    public final List<Op> ops;
    public Function(String name, List<Op> ops) { this.name = name; this.ops = ops; }
  }

  public enum OpCode {
    NOP(0), LOG(1), MUL_ENTITY_SPEED(2),
    BROADCAST(3), EXEC_CMD(4), GIVE_ITEM(5),
    NBT_SET(6), NBT_GET(7), FILTER_ENTITIES_REGION(8);
    public final int id; OpCode(int id){this.id=id;}
    static OpCode of(int id){
      for (var o : values()) if (o.id == id) return o;
      throw new IllegalArgumentException("Unknown opcode: " + id);
    }
  }

  public static final class Op {
    public final OpCode code;
    public final int sIdxA;      // string index A
    public final int sIdxB;      // string index B
    public final double d;       // numeric payload
    public final long i;         // integer payload
    public Op(OpCode code, int sIdxA, int sIdxB, double d, long i){
      this.code = code; this.sIdxA = sIdxA; this.sIdxB = sIdxB; this.d = d; this.i = i;
    }
  }

  public final String moduleName;
  public final ConstPool pool;
  public final Map<String, Function> functions;

  public PyBCModule(String moduleName, ConstPool pool, Map<String, Function> functions) {
    this.moduleName = moduleName; this.pool = pool; this.functions = functions;
  }

  public static PyBCModule read(Path file) throws IOException {
    try (var in = new DataInputStream(new BufferedInputStream(new FileInputStream(file.toFile())))) {
      int magic = in.readInt();
      if (magic != MAGIC) throw new IOException("Bad magic in " + file);
      int ver = in.readInt();
      if (ver != VERSION) throw new IOException("Unsupported pybc version: " + ver);

      String modName = in.readUTF();

      int poolCount = in.readInt();
      List<String> strings = new ArrayList<>(poolCount);
      for (int k=0;k<poolCount;k++){ strings.add(in.readUTF()); }
      ConstPool pool = new ConstPool(strings);

      int fnCount = in.readInt();
      Map<String, Function> fns = new HashMap<>();
      for (int i=0;i<fnCount;i++){
        String fname = in.readUTF();
        int opCount = in.readInt();
        List<Op> ops = new ArrayList<>(opCount);
        for (int j=0;j<opCount;j++){
          int opcode = in.readInt();
          int sA = in.readInt();
          int sB = in.readInt();
          double d = in.readDouble();
          long i64 = in.readLong();
          ops.add(new Op(OpCode.of(opcode), sA, sB, d, i64));
        }
        fns.put(fname, new Function(fname, ops));
      }
      return new PyBCModule(modName, pool, fns);
    }
  }
}
