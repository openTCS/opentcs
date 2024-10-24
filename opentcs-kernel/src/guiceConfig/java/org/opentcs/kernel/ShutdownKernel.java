// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Shuts down a running kernel via its administration interface.
 */
public class ShutdownKernel {

  private ShutdownKernel() {
  }

  /**
   * Java main.
   *
   * @param args command line args
   */
  public static void main(String[] args) {
    if (args.length > 2) {
      System.err.println("ShutdownKernel [<host>] [<port>]");
      return;
    }

    String hostName = args.length > 0 ? args[0] : "localhost";
    int port = args.length > 1 ? Integer.parseInt(args[1]) : 55001;

    try {
      URL url = new URI("http://" + hostName + ":" + port + "/v1/kernel").toURL();
      System.err.println("Calling to " + url + "...");
      HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
      httpCon.setRequestMethod("DELETE");
      httpCon.connect();
      httpCon.getInputStream();
    }
    catch (IOException | URISyntaxException exc) {
      System.err.println("Exception accessing admin interface:");
      exc.printStackTrace();
    }

  }

}
