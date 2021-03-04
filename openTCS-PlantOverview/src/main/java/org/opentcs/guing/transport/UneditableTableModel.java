/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.transport;

import javax.swing.table.DefaultTableModel;

/**
 * A table model in which each cell is uneditable.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class UneditableTableModel
    extends DefaultTableModel {

  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }
}
