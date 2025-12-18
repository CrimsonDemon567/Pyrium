package io.pyrium.core;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.*;

public final class PyBCModule {
  public static final int MAGIC = 0x50594243; // 'PYBC'
  public static final int VERSION = 1;

  public static final class Function {
    public final String name;
    public final List<Op> ops;
    public Function(String name, List<Op> ops) { this.name = name; this.ops = ops; }
  }

  public enum OpCode {
    NOP(0), LOG(1), MUL_ENTITY_SPEED(2);
    public final int id; OpCode(int id){this.id=id;}
    static OpCode of(int id){
      for (var o : values()) if (o.id == id) return o;
      throw new IllegalArgumentException("Unknown opcode: " + id);
    }
  }

  public static final class Op {
    public final OpCode code; public final String a; public final double d;
    public Op(OpCode code, String a, double d){ this.code = code; this.a = a; this.d = d; }
  }

  public final String moduleName;
  public final Map<String, Function> functions;

  public PyBCModule(String moduleName, Map<String, Function> functions) {
    this.moduleName = moduleName; this.functions = functions;
  }

  public static PyBCModule read(Path file) throws IOException {
    try (var in = new DataInputStream(new BufferedInputStream(new FileInputStream(file.toFile())))) {
      int magic = in.readInt();
      if (magic != MAGIC) throw new IOException("Bad magic in " + file);
      int ver = in.readInt();
      if (ver != VERSION) throw new IOException("Unsupported pybc version: " + ver);
      String modName = in.readUTF();
      int fnCount = in.readInt();
      Map<String, Function> fns = new HashMap<>();
      for (int i=0;i<fnCount;i++){
        String fname = in.readUTF();
        int opCount = in.readInt();
        List<Op> ops = new ArrayList<>(opCount);
        for (int j=0;j<opCount;j++){
          int opcode = in.readInt();
          String a = in.readUTF();
          double d = in.readDouble();
          ops.add(new Op(OpCode.of(opcode), a, d));
        }
        fns.put(fname, new Function(fname, ops));
      }
      return new PyBCModule(modName, fns);
    }
  }
}
