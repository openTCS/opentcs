// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.menus;

import org.opentcs.guing.base.components.properties.type.CoordinateProperty;

/**
 * A factory for creating instances in relation to menu items.
 */
public interface MenuItemComponentsFactory {

  /**
   * Creates a {@link LayoutToModelCoordinateUndoActivity} for the given coordinate property.
   *
   * @param property The property.
   * @return The {@link LayoutToModelCoordinateUndoActivity}.
   */
  LayoutToModelCoordinateUndoActivity createLayoutToModelCoordinateUndoActivity(
      CoordinateProperty property
  );

  /**
   * Creates a {@link ModelToLayoutCoordinateUndoActivity} for the given coordinate property.
   *
   * @param property The property.
   * @return The {@link ModelToLayoutCoordinateUndoActivity}.
   */
  ModelToLayoutCoordinateUndoActivity createModelToLayoutCoordinateUndoActivity(
      CoordinateProperty property
  );
}
