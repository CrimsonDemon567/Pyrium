package io.pyrium.bootstrap;

import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;

public final class PyriumClassLoader extends URLClassLoader {
  private final RuntimeLayout rt;
  private Resolver.Artifact base;

  public PyriumClassLoader(RuntimeLayout rt) throws MalformedURLException {
    super(new URL[] {}, ClassLoader.getSystemClassLoader());
    this.rt = rt;
    addURL(rt.versionDir().toUri().toURL());
  }

  public void attachBaseServer(Path serverJar) throws MalformedURLException {
    addURL(serverJar.toUri().toURL());
  }

  public void loadCore() throws MalformedURLException {
    Path coreJar = Paths.get("pyrium-core/target/pyrium-core-0.1.0.jar");
    Path rpbJar = Paths.get("pyrium-rpb/target/pyrium-rpb-0.1.0.jar");
    addURL(coreJar.toUri().toURL());
    addURL(rpbJar.toUri().toURL());
  }

  public void start(Resolver.Artifact art) throws Exception {
    this.base = art;
    // 1) Start the base server main on this classloader
    startBaseServer();

    // 2) Boot Pyrium core, then install runtime bridges
    Class<?> pyrium = loadClass("io.pyrium.core.Pyrium");
    pyrium.getMethod("boot", ClassLoader.class, RuntimeLayout.class).invoke(null, this, rt);
  }

  private void startBaseServer() throws Exception {
    System.out.println("[Pyrium] Starting base server...");
    String mainClass = detectMainClass();
    Class<?> main = loadClass(mainClass);
    Method m = main.getMethod("main", String[].class);
    String[] args = new String[] { "nogui" }; // vanilla/paper respect nogui
    new Thread(() -> {
      try { m.invoke(null, (Object) args); }
      catch (Throwable t) { t.printStackTrace(); }
    }, "BaseServerMain").start();
  }

  private String detectMainClass() throws Exception {
    // Simple heuristics for common distributions
    // Mojang Vanilla:
    try { loadClass("net.minecraft.server.Main"); return "net.minecraft.server.Main"; } catch (Throwable ignored) {}
    // Paper (paperclip):
    try { loadClass("io.papermc.paperclip.Main"); return "io.papermc.paperclip.Main"; } catch (Throwable ignored) {}
    // Spigot:
    try { loadClass("org.bukkit.craftbukkit.Main"); return "org.bukkit.craftbukkit.Main"; } catch (Throwable ignored) {}
    throw new IllegalStateException("Cannot detect server main class");
  }
}
