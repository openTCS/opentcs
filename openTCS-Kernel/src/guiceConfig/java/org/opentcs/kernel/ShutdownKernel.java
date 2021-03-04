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
import org.opentcs.access.rmi.factories.AnonSslSocketFactoryProvider;
import org.opentcs.access.rmi.factories.NullSocketFactoryProvider;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.rmi.factories.SslSocketFactoryProvider;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class ShutdownKernel {

  private ShutdownKernel() {
  }

  public static void main(String[] args) {
    if (args.length < 1 || args.length > 3) {
      System.out.println("ShutdownKernel "
          + "[<registry-hostname>] [<registry-portnummer>] <connection-encryption>");
      return;
    }
    String hostName = args.length > 0 ? args[0] : "localhost";
    int port = args.length > 1 ? Integer.parseInt(args[1]) : 1099;

    SocketFactoryProvider socketFactoryProvider;
    switch (args[2]) {
      case "NONE":
        socketFactoryProvider = new NullSocketFactoryProvider();
        break;
      case "SSL_UNTRUSTED":
        socketFactoryProvider = new AnonSslSocketFactoryProvider();
        break;
      case "SSL":
        socketFactoryProvider = new SslSocketFactoryProvider();
        break;
      default:
        System.out.println("Unknown connection encryption '" + args[2] + "'. Supported values: "
            + "NONE, SSL_UNTRUSTED, SSL.");
        return;
    }

    Kernel kernel = new KernelProxyBuilder()
        .setSocketFactoryProvider(socketFactoryProvider)
        .setHost(hostName)
        .setPort(port)
        .build();
    kernel.setState(Kernel.State.SHUTDOWN);
  }

}
