/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Shuts down a running kernel via its administration interface.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ShutdownKernel {

  private ShutdownKernel() {
  }

  public static void main(String[] args) {
    if (args.length > 2) {
      System.err.println("ShutdownKernel [<host>] [<port>]");
      return;
    }

    String hostName = args.length > 0 ? args[0] : "localhost";
    int port = args.length > 1 ? Integer.parseInt(args[1]) : 55001;

    try {
      URL url = new URL("http", hostName, port, "/v1/kernel");
      System.err.println("Calling to " + url + "...");
      HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
      httpCon.setRequestMethod("DELETE");
      httpCon.connect();
      httpCon.getInputStream();
    }
    catch (IOException exc) {
      System.err.println("Exception accessing admin interface:");
      exc.printStackTrace();
    }

  }

}
