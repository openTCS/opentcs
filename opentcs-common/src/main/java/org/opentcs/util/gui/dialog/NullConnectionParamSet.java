// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.gui.dialog;

/**
 * A connection param set used for not established connections.
 */
public class NullConnectionParamSet
    extends
      ConnectionParamSet {

  public NullConnectionParamSet() {
    super("-", "", 0);
  }
}
