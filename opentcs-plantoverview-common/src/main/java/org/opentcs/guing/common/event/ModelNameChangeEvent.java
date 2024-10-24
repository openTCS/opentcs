// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.event;

import java.util.EventObject;

/**
 * An event that indicates that the model name has changed.
 */
public class ModelNameChangeEvent
    extends
      EventObject {

  private final String newName;

  /**
   * Creates a new instance of PathLockedEvent.
   *
   * @param source The <code>PathConnection</code> that has been locked.
   * @param newName The new name of the model.
   */
  public ModelNameChangeEvent(Object source, String newName) {
    super(source);
    this.newName = newName;
  }

  public String getNewName() {
    return newName;
  }

  @Override
  public String toString() {
    return "ModelNameChangeEvent{"
        + "newName=" + newName
        + ", source=" + getSource()
        + '}';
  }

}
