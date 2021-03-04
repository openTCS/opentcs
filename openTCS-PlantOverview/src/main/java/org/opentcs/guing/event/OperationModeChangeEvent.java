/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.event;

import java.util.EventObject;
import static java.util.Objects.requireNonNull;
import org.opentcs.guing.application.OperationMode;

/**
 * Informs listeners about a change of the application's mode of operation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OperationModeChangeEvent
    extends EventObject {

  /**
   * The old operation mode.
   */
  private final OperationMode oldMode;
  /**
   * The new/current operation mode.
   */
  private final OperationMode newMode;

  /**
   * Creates a new instance.
   *
   * @param source The source of this event.
   * @param oldMode The old operation mode.
   * @param newMode The new/current operation mode.
   */
  public OperationModeChangeEvent(Object source,
                                  OperationMode oldMode,
                                  OperationMode newMode) {
    super(source);
    this.oldMode = requireNonNull(oldMode, "oldMode");
    this.newMode = requireNonNull(newMode, "newMode");
  }

  /**
   * Returns the old operation mode.
   *
   * @return The old operation mode.
   */
  public OperationMode getOldMode() {
    return oldMode;
  }

  /**
   * Returns the new/current operation mode.
   *
   * @return The new/current operation mode.
   */
  public OperationMode getNewMode() {
    return newMode;
  }

  @Override
  public String toString() {
    return "OperationModeChangeEvent{"
        + "oldMode=" + oldMode
        + ", newMode=" + newMode
        + ", source=" + getSource()
        + '}';
  }
}
