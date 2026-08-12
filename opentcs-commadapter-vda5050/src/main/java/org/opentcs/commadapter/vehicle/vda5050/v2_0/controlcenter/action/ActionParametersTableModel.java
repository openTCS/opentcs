// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter.action;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.ActionParameter;

/**
 * A model for a table of action parameters.
 */
public class ActionParametersTableModel
    extends
      AbstractTableModel {

  /**
   * List of entries in the table.
   */
  private final List<ActionParameter> parameters = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public ActionParametersTableModel() {
  }

  @Override
  public int getRowCount() {
    return parameters.size();
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public Object getValueAt(int row, int col) {
    if (row < 0 || row >= getRowCount()) {
      return null;
    }

    return (col == 0) ? parameters.get(row).getKey() : parameters.get(row).getValue();
  }

  @Override
  public boolean isCellEditable(int row, int col) {
    return true;
  }

  @Override
  public void setValueAt(Object value, int row, int col) {
    if (row < 0 || row >= getRowCount()) {
      return;
    }

    if (col == 0) {
      parameters.get(row).setKey((String) value);
    }
    else if (col == 1) {
      parameters.get(row).setValue((String) value);
    }
    fireTableCellUpdated(row, col);
  }

  /**
   * Adds a parameter to the table and returns its index.
   *
   * @return The index of the new parameter.
   */
  public int addParameter() {
    parameters.add(new ActionParameter("", ""));
    fireTableDataChanged();
    return parameters.size() - 1;
  }

  /**
   * Removes one paramter at the specified index.
   *
   * @param index The index of the paramter to remove.
   */
  public void removeParameter(int index) {
    if (index >= 0 && index < parameters.size()) {
      parameters.remove(index);
    }
    fireTableDataChanged();
  }

  /**
   * Set a list of paramters to replace the current parameters.
   *
   * @param parameters List of paramters.
   */
  public void setParameters(List<ActionParameter> parameters) {
    this.parameters.clear();
    if (parameters != null) {
      this.parameters.addAll(parameters);
    }
    fireTableDataChanged();
  }

  /**
   * Returns the list of action paramters currently in the table.
   *
   * @return List of action parameters.
   */
  public List<ActionParameter> getParameters() {
    return new ArrayList<>(parameters);
  }

}
