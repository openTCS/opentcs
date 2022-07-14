/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.table;

import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.opentcs.guing.base.components.properties.type.ModelAttribute;
import org.opentcs.guing.common.application.ApplicationState;
import org.opentcs.guing.common.application.OperationMode;
import org.opentcs.guing.common.components.properties.event.TableChangeListener;
import org.opentcs.guing.common.components.properties.event.TableSelectionChangeEvent;

/**
 * A table in which properties are displayed and can be edited.
 * The table has two columns, the first with the name of the property and the second with the 
 * value of the property.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AttributesTable
    extends JTable {

  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  /**
   * List of table change listeners.
   */
  private final List<TableChangeListener> fTableChangeListeners
      = new LinkedList<>();

  /**
   * Creates a new instance.
   * 
   * @param appState Stores the application's current state.
   */
  @Inject
  public AttributesTable(ApplicationState appState) {
    this.appState = requireNonNull(appState, "appState");
    setStyle();
    putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
  }

  /**
   * Initialises the style of the table.
   */
  protected final void setStyle() {
    setRowHeight(20);
    setCellSelectionEnabled(false);
    getTableHeader().setReorderingAllowed(false);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    ListSelectionModel model = getSelectionModel();
    model.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
          return;
        }

        ListSelectionModel l = (ListSelectionModel) e.getSource();

        if (l.isSelectionEmpty()) {
          fireSelectionChanged(null);
        }
        else {
          int selectedRow = l.getMinSelectionIndex();
          fireSelectionChanged(getModel().getValueAt(selectedRow, 1));
        }
      }
    });
  }

  /**
   * Adds a TableSelectionChangeListener.
   *
   * @param l The listener to add.
   */
  public void addTableChangeListener(TableChangeListener l) {
    fTableChangeListeners.add(l);
  }

  /**
   * Removes a TableSelectionChangeListener.
   *
   * @param l the listener to remove.
   */
  public void removeTableChangeListener(TableChangeListener l) {
    fTableChangeListeners.remove(l);
  }

  /**
   * Notify all registered listeners that a table row has been selected.
   *
   * @param selectedValue
   */
  protected void fireSelectionChanged(Object selectedValue) {
    for (TableChangeListener l : fTableChangeListeners) {
      l.tableSelectionChanged(new TableSelectionChangeEvent(this, selectedValue));
    }
  }

  @Override
  public TableCellEditor getCellEditor(int row, int column) {
    TableModel tableModel = getModel();
    Object value = tableModel.getValueAt(row, column);
    TableCellEditor editor = getDefaultEditor(value.getClass());

    return editor;
  }

  @Override
  public TableCellRenderer getCellRenderer(int row, int column) {
    TableModel tableModel = getModel();
    Object value = tableModel.getValueAt(row, column);
    TableCellRenderer renderer = getDefaultRenderer(value.getClass());

    return renderer;
  }

  /**
   * Indicates whether the specified row is editable.
   *
   * @param row The index of the row to be checked.
   * @return True if the row is editable.
   */
  public boolean isEditable(int row) { 
    AttributesTableModel tableModel = (AttributesTableModel) getModel();
    ModelAttribute attribute = (ModelAttribute) tableModel.getValueAt(row, 1);

    if (appState.hasOperationMode(OperationMode.MODELLING)) {
      return attribute.isModellingEditable();
    }

    return attribute.isOperatingEditable();
  }

  @Override
  public void tableChanged(TableModelEvent event) {
    super.tableChanged(event);

    if (fTableChangeListeners != null) {
      for (TableChangeListener listener : fTableChangeListeners) {
        listener.tableModelChanged();
      }
    }
  }
}
