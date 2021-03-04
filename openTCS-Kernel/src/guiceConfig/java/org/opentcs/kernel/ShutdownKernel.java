/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import org.opentcs.access.Kernel;
import org.opentcs.access.rmi.KernelProxyBuilder;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class ShutdownKernel {

  private ShutdownKernel() {
  }

  public static void main(String[] args) {
    if (args.length > 2) {
      System.out.println("ShutdownKernel [<registry-hostname>] [<registry-portnummer>]");
      return;
    }
    String hostName = args.length > 0 ? args[0] : "localhost";
    int port = args.length > 1 ? Integer.parseInt(args[1]) : 1099;
    Kernel kernel = new KernelProxyBuilder().setHost(hostName).setPort(port).build();
    kernel.setState(Kernel.State.SHUTDOWN);
  }

}
