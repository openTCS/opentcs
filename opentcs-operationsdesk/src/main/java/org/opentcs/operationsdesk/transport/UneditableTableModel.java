// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.transport;

import javax.swing.table.DefaultTableModel;

/**
 * A table model in which each cell is uneditable.
 */
public class UneditableTableModel
    extends
      DefaultTableModel {

  /**
   * Creates a new instance.
   */
  public UneditableTableModel() {
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }
}
