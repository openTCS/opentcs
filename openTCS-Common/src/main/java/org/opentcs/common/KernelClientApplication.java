/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.common;

import org.opentcs.components.Lifecycle;

/**
 * Provides methods used in a kernel client application's context.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface KernelClientApplication
    extends Lifecycle {

  /**
   * Tells the application to switch its state to {@link State#ONLINE}.
   *
   * @param autoConnect Whether to connect automatically to the kernel or to show a connect dialog 
   * when going online.
   */
  public void online(boolean autoConnect);

  /**
   * Tells the application to switch its state to {@link State#OFFLINE}.
   */
  public void offline();

  /**
   * Checks whether the application's state is {@link State#ONLINE}.
   *
   * @return Whether the application's state is {@link State#ONLINE}.
   */
  public boolean isOnline();

}
