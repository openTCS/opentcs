// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.model;

/**
 * Defines constants for {@link ModelComponent}s that have model coordinates.
 */
public interface PositionableModelComponent
    extends
      ModelComponent {

  /**
   * Key for the X (model) cordinate.
   */
  String MODEL_X_POSITION = "modelXPosition";
  /**
   * Key for the Y (model) cordinate.
   */
  String MODEL_Y_POSITION = "modelYPosition";
}
