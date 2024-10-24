// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.properties;

import javax.swing.JTextField;
import org.opentcs.guing.common.components.properties.table.CoordinateCellEditor;
import org.opentcs.guing.common.util.UserMessageHelper;

/**
 * A factory for creating instances in relation to properties.
 */
public interface PropertiesComponentsFactory {

  /**
   * Creates a {@link CoordinateCellEditor}.
   *
   * @param textField The text field for the cell editor.
   * @param userMessageHelper The user message helper.
   * @return The {@link CoordinateCellEditor}.
   */
  CoordinateCellEditor createCoordinateCellEditor(
      JTextField textField,
      UserMessageHelper userMessageHelper
  );
}
