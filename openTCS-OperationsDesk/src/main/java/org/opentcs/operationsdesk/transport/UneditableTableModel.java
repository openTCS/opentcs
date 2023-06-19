/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.transport;

import javax.swing.table.DefaultTableModel;

/**
 * A table model in which each cell is uneditable.
 */
public class UneditableTableModel
    extends DefaultTableModel {

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
