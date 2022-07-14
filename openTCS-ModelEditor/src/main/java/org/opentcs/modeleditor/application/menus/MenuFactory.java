/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.menus;

/**
 * A factory for various menus and menu items.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface MenuFactory {

  /**
   * Creates a menu item for copying the value of the layout properties of
   * selected elements to the corresponding model properties.
   *
   * @param copyAll Indicates whether the values of ALL points and locations
   * shall be copied when the menu item is clicked. If false only the selected
   * figures will be considered.
   * @return The created menu item.
   */
  LayoutToModelMenuItem createLayoutToModelMenuItem(boolean copyAll);

  /**
   *
   * @param copyAll Indicates whether the values of ALL points and locations
   * shall be copied when the menu item is clicked. If false only the selected
   * figures will be considered.
   * @return The created menu item.
   */
  ModelToLayoutMenuItem createModelToLayoutMenuItem(boolean copyAll);
}
