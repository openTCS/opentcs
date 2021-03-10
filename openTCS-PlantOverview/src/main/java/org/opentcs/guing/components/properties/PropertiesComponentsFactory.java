/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties;

import javax.swing.JTextField;
import org.opentcs.guing.components.properties.table.CoordinateCellEditor;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * A factory for creating instances in relation to properties.
 * 
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PropertiesComponentsFactory {

  /**
   * Creates a {@link CoordinateUndoActivity} for the given coordinate property.
   * 
   * @param property The property.
   * @return The {@link CoordinateUndoActivity}.
   */
  CoordinateUndoActivity createCoordinateUndoActivity(CoordinateProperty property);

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
