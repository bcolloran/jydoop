/* -*- Mode: Java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 2 -*- */

package org.mozilla.pydoop;
import org.python.util.PythonInterpreter;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.core.Py;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

public class PythonWrapper {
  private PythonInterpreter interp;

  public static class JSONImporter extends PyObject {
    public static final String JSON_IMPORT_PATH_ENTRY = "__jsonpath__";

    public PyObject __call__(PyObject args[], String keywords[]) {
      if (args[0].toString().endsWith(JSON_IMPORT_PATH_ENTRY)) {
        return this;
      }
      throw Py.ImportError("unable to handle");
    }

    public PyObject find_module(String name) {
      if (!name.equals("json")) {
	return Py.None;
      }

      return this;
    }

    public PyObject load_module(String name) {
      PyObject mod = org.python.core.imp.importName("com.xhaus.jyson.JysonCodec", true);
      return mod.__getattr__("xhaus").__getattr__("jyson").__getattr__("JysonCodec");
    }

    public String toString() {
      return this.getType().toString();
    }
  }

  PythonWrapper(String pathname) {
    interp = new PythonInterpreter();

    // Use an import hook so that "import json" uses Jyson instead of the very
    // slow builtin module.
    interp.getSystemState().path.insert(0, Py.newString(JSONImporter.JSON_IMPORT_PATH_ENTRY));
    interp.getSystemState().path_hooks.insert(0, new JSONImporter());

    // Get the pathname off of the classpath

    System.err.println("PythonWrapper pathname: " + pathname);

    URL rurl = this.getClass().getResource("/" + pathname);
    System.err.println("rurl: " + rurl);

    InputStream pythonstream = this.getClass().getResourceAsStream("/" + pathname);
    if (null == pythonstream) {
      throw new java.lang.NullPointerException("pythonstream");
    }
    interp.execfile(pythonstream, pathname);
  }
  public PyObject getFunction(String name) {
    return interp.get(name.intern());
  }
}
