/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JCheckBoxMenuItem;
import org.opentcs.guing.components.dockable.DockingManager;

/**
 * Handles changes of a plugin panel's properties.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
class PluginPanelPropertyHandler
    implements PropertyChangeListener {

  /**
   * The menu item corresponding to the plugin panel.
   */
  private final JCheckBoxMenuItem utilMenuItem;

  /**
   * Creates a new instance.
   *
   * @param utilMenuItem The menu item corresponding to the plugin panel.
   */
  public PluginPanelPropertyHandler(JCheckBoxMenuItem utilMenuItem) {
    this.utilMenuItem = Objects.requireNonNull(utilMenuItem, "utilMenuItem is null");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(DockingManager.DOCKABLE_CLOSED)) {
      utilMenuItem.setSelected(false);
    }
  }
}
