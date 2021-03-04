/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle.inputcomponents;

import java.util.EventListener;

/**
 * A listener interface for {@link ValidationEvent ValidationEvents}.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public interface ValidationListener
    extends EventListener {

  /**
   * Should be called when the state of validation changed.
   *
   * @param e The ValidationEvent containing validation information.
   */
  void validityChanged(ValidationEvent e);
}
