/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.table;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.components.properties.type.ModelAttribute;

/**
 * Ein Tabellenmodell (TableModel) für die PropertiesTable.
 * Es sorgt dafür, dass die Namen der Attribute (die erste Spalte) sowie
 * die Attributwerte, die in ihrer Methode isEditable() false zurückliefern,
 * nicht durch den Benutzer verändert werden können.
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
   * Gibt zurück, dass die Zellen der ersten Spalte nicht editierbar sind. Auch
   * die Attributwerte (zweite Spalte) sind nur dann veränderbar, wenn
   * <code>isEditable() true</code> liefert.
   *
   * @param row
   * @param col
   * @return
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
