// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.menus;

/**
 * A factory for various menus and menu items.
 */
public interface MenuFactory {
  /**
   * Creates a menu item for calculating the length of paths.
   *
   * @return The created menu item.
   */
  CalculatePathLengthMenuItem createCalculatePathLengthMenuItem();
}
