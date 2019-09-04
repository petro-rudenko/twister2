package edu.iu.dsc.tws.python.processors;

import jep.Jep;
import jep.JepException;

public final class JepInstance {

  private static volatile Jep jep;

  private JepInstance() {

  }

  public static Jep get() throws JepException {
    if (jep == null) {
      jep = new Jep();
      jep.eval("import cloudpickle as cp");
      jep.eval("import base64");
    }
    return jep;
  }
}