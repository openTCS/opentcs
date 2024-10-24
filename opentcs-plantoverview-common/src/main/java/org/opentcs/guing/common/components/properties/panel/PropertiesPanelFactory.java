// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.properties.panel;

import javax.swing.JPanel;

/**
 * A factory for properties-related panels.
 */
public interface PropertiesPanelFactory {

  /**
   * Creates a new <code>PropertiesTableContent</code>.
   *
   * @param dialogParent The component to be used as the parent for dialogs
   * created by the new instance.
   * @return The newly created instance.
   */
  PropertiesTableContent createPropertiesTableContent(JPanel dialogParent);
}
