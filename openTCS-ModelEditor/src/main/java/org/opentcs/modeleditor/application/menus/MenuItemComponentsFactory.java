/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.menus;

import org.opentcs.guing.base.components.properties.type.CoordinateProperty;

/**
 * A factory for creating instances in relation to menu items.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface MenuItemComponentsFactory {

  /**
   * Creates a {@link LayoutToModelCoordinateUndoActivity} for the given coordinate property.
   *
   * @param property The property.
   * @return The {@link LayoutToModelCoordinateUndoActivity}.
   */
  LayoutToModelCoordinateUndoActivity createLayoutToModelCoordinateUndoActivity(
      CoordinateProperty property);

  /**
   * Creates a {@link ModelToLayoutCoordinateUndoActivity} for the given coordinate property.
   *
   * @param property The property.
   * @return The {@link ModelToLayoutCoordinateUndoActivity}.
   */
  ModelToLayoutCoordinateUndoActivity createModelToLayoutCoordinateUndoActivity(
      CoordinateProperty property);
}
