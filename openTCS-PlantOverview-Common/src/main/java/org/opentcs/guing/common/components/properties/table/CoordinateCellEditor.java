/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.table;

import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.opentcs.guing.common.util.UserMessageHelper;

/**
 * 
 * A cell editor for a coordinate property.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class CoordinateCellEditor
    extends QuantityCellEditor {

  /**
   * Creates a new instance.
   *
   * @param textField
   */
  @Inject
  public CoordinateCellEditor(@Assisted JTextField textField, @Assisted UserMessageHelper umh) {
    super(textField, umh);
  }

  /**
   * The table cell contains (from left to right):
   * 1. An editable textfield
   * 2. A button with "arrow down" symbol to copy values from model to layout
   * 3. A button with "arrow up" symbol to copy values from layout to model
   * 4. A button with "..." symbol to open a dialog with extended editor
   * functionality.
   *
   * @return
   */
  @Override  // AbstractPropertyCellEditor
  protected JComponent createComponent() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(getComponent());
    JComponent button = createButtonDetailsDialog();
    panel.add(button);

    return panel;
  }
}
