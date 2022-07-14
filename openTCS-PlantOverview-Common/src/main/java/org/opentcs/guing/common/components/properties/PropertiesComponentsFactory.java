/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties;

import javax.swing.JTextField;
import org.opentcs.guing.common.components.properties.table.CoordinateCellEditor;
import org.opentcs.guing.common.util.UserMessageHelper;

/**
 * A factory for creating instances in relation to properties.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PropertiesComponentsFactory {

  /**
   * Creates a {@link CoordinateCellEditor}.
   *
   * @param textField The text field for the cell editor.
   * @param userMessageHelper The user message helper.
   * @return The {@link CoordinateCellEditor}.
   */
  CoordinateCellEditor createCoordinateCellEditor(JTextField textField,
                                                  UserMessageHelper userMessageHelper);
}
