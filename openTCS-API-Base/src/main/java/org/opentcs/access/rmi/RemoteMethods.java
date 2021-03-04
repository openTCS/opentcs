/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Provides helper methods for working with the RemoteKernel interface.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
final class RemoteMethods {

  /**
   * Prevents undesired instantiation.
   */
  private RemoteMethods() {
    // Do nada.
  }

  /**
   * Maps a method of the Kernel interface to a method of the RemoteKernel
   * interface.
   *
   * @param method The method of the Kernel interface.
   * @return The corresponding method of the RemoteKernel interface.
   * @throws NoSuchMethodException If <code>RemoteKernel</code> does not have a
   * corresponding method.
   */
  public static Method getRemoteKernelMethod(Method method)
      throws NoSuchMethodException {
    Objects.requireNonNull(method, "method is null");

    Class<?>[] paramTypes = method.getParameterTypes();
    Class<?>[] extParamTypes = new Class<?>[paramTypes.length + 1];
    // We're looking for a method with the same parameter types as the called
    // one, but with an additional client ID as the first parameter.
    extParamTypes[0] = ClientID.class;
    System.arraycopy(paramTypes, 0, extParamTypes, 1, paramTypes.length);
    return RemoteKernel.class.getMethod(method.getName(), extParamTypes);
  }
}
