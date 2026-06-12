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
   * Key for the X (model) coordinate.
   */
  String MODEL_X_POSITION = "modelXPosition";
  /**
   * Key for the Y (model) coordinate.
   */
  String MODEL_Y_POSITION = "modelYPosition";
  /**
   * Key for the Z (model) coordinate.
   */
  String MODEL_Z_POSITION = "modelZPosition";
}
