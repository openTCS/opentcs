// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.application.menus.menubar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JCheckBoxMenuItem;
import org.opentcs.guing.common.components.dockable.AbstractDockingManager;

/**
 * Handles changes of a plugin panel's properties.
 */
class PluginPanelPropertyHandler
    implements
      PropertyChangeListener {

  /**
   * The menu item corresponding to the plugin panel.
   */
  private final JCheckBoxMenuItem utilMenuItem;

  /**
   * Creates a new instance.
   *
   * @param utilMenuItem The menu item corresponding to the plugin panel.
   */
  PluginPanelPropertyHandler(JCheckBoxMenuItem utilMenuItem) {
    this.utilMenuItem = Objects.requireNonNull(utilMenuItem, "utilMenuItem is null");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(AbstractDockingManager.DOCKABLE_CLOSED)) {
      utilMenuItem.setSelected(false);
    }
  }
}
