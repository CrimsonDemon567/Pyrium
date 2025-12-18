package io.pyrium.bootstrap;

import java.net.*;
import java.nio.file.*;

public final class PyriumClassLoader extends URLClassLoader {
  private final RuntimeLayout rt;

  public PyriumClassLoader(RuntimeLayout rt) throws MalformedURLException {
    super(new URL[] {}, ClassLoader.getSystemClassLoader());
    this.rt = rt;
    addURL(rt.versionDir().toUri().toURL()); // runtime dir (for future artifacts)
  }

  public void attachBaseServer(Path serverJar) throws MalformedURLException {
    addURL(serverJar.toUri().toURL());
  }

  public void loadCore() throws MalformedURLException {
    Path coreJar = Paths.get("pyrium-core/target/pyrium-core-0.1.0.jar");
    addURL(coreJar.toUri().toURL());
  }

  public RuntimeLayout runtime() { return rt; }

  public void start() throws Exception {
    Class<?> pyrium = loadClass("io.pyrium.core.Pyrium");
    pyrium.getMethod("boot", ClassLoader.class, RuntimeLayout.class).invoke(null, this, rt);
  }
}
