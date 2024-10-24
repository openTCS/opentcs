// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.menus;

/**
 * A factory for various menus and menu items.
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

  /**
   * Creates a menu item for calculating the length of paths.
   *
   * @return The created menu item.
   */
  CalculatePathLengthMenuItem createCalculatePathLengthMenuItem();
}
