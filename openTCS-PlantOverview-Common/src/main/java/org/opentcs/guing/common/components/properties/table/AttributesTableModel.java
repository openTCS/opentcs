/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.table;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.guing.base.components.properties.type.ModelAttribute;
import org.opentcs.guing.common.application.ApplicationState;
import org.opentcs.guing.common.application.OperationMode;

/**
 * A table model for the PropertiesTable.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class AttributesTableModel
    extends javax.swing.table.DefaultTableModel {

  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;

  /**
   * Creates a new instance of AttributesTableModel
   *
   * @param appState Stores the application's current state.
   */
  @Inject
  public AttributesTableModel(ApplicationState appState) {
    this.appState = requireNonNull(appState, "appState");
  }

  /**
   * Indicates whether a cell is editable.
   *
   * @param row The row of the cell to test.
   * @param col The column of the cell to test.
   * @return True if the specified cell is editable.
   */
  @Override // AbstractTableModel
  public boolean isCellEditable(int row, int col) {
    if (col == 0) {
      return false;
    }
    else { // col == 1
      ModelAttribute attribute = (ModelAttribute) getValueAt(row, col);

      if (appState.hasOperationMode(OperationMode.MODELLING)) {
        return attribute.isModellingEditable();
      }

      return attribute.isOperatingEditable();
    }
  }

  @Override  // AbstractTableModel
  public void fireTableDataChanged() {
    super.fireTableDataChanged();
  }
}
