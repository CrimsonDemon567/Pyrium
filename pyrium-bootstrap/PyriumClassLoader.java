package io.pyrium.bootstrap;

import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public final class PyriumClassLoader extends URLClassLoader {
  public PyriumClassLoader(RuntimeLayout rt) throws MalformedURLException {
    super(new URL[] {}, ClassLoader.getSystemClassLoader());
    // Add runtime dir for compiled mods later if needed
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

  public void start() throws Exception {
    // Load Pyrium core entry point
    Class<?> pyrium = loadClass("io.pyrium.core.Pyrium");
    pyrium.getMethod("boot", ClassLoader.class).invoke(null, this);
  }
}
