/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui.dialog;

/**
 * A connection param set used for not established connections.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class NullConnectionParamSet
    extends ConnectionParamSet {

  public NullConnectionParamSet() {
    super("-", "", 0);
  }
}
