/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.table;

import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.application.GuiManager.OperationMode;
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
   * The drawing view.
   */
  private final GuiManager fOpenTCSView;

  /**
   * Creates a new instance of AttributesTableModel
   *
   * @param openTCSView
   */
  public AttributesTableModel(GuiManager openTCSView) {
    super();
    this.fOpenTCSView = openTCSView;
  }

  /**
   *
   * @return
   */
  public GuiManager getView() {
    return fOpenTCSView;
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
    else {	// col == 1
      ModelAttribute attribute = (ModelAttribute) getValueAt(row, col);
      OperationMode operationMode = fOpenTCSView.getOperationMode();

      if (operationMode == OperationMode.MODELLING) {
        return attribute.isModellingEditable();
      }

      return attribute.isOperatingEditable();
    }
  }

  @Override	// AbstractTableModel
  public void fireTableDataChanged() {
    super.fireTableDataChanged();
  }
}
