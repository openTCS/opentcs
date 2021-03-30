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
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.components.properties.event.TableChangeListener;
import org.opentcs.guing.components.properties.event.TableSelectionChangeEvent;
import org.opentcs.guing.components.properties.type.ModelAttribute;

/**
 * Eine Tabelle, in der Attribute dargestellt und verändert werden können. Sie
 * besteht aus zwei Spalten: die erste enthält die Namen der Attribute, die
 * zweite die Werte der Attribute.
 * <p>
 * Die Tabelle ist Teil der {
 *
 * @see PropertiesComponent}. Diese besitzt unterhalb der Tabelle einen Bereich
 * für attributspezifische Hilfetexte. PropertiesComponent muss deshalb wissen,
 * welche Tabellenzeile der Benutzer gerade selektiert, um den entsprechenden
 * Hilfetext anzeigen zu können. Daher registriert sich PropertiesComponent als
 * {
 * @see TableSelectionChangeListener} bei der Tabelle und wird dann über jede
 * Veränderung informiert.
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
   * Eine Liste von Objekten, die daran interessiert sind, welche Tabellenzeile
   * gerade selektiert ist.
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
   * Konfiguriert das Erscheinungsbild der Tabelle.
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
   * Fügt einen TableSelectionChangeListener hinzu.
   *
   * @param l
   */
  public void addTableChangeListener(TableChangeListener l) {
    fTableChangeListeners.add(l);
  }

  /**
   * Entfernt einen TableSelectionChangeListener.
   *
   * @param l
   */
  public void removeTableChangeListener(TableChangeListener l) {
    fTableChangeListeners.remove(l);
  }

  /**
   * Benachrichtigt alle registrierten TableChangeListener, dass der Benutzer
   * eine Tabellenzeile (und damit ein Attribut) selektiert hat.
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
   * Zeigt an, ob die übergebene Zeile editierbar ist. Dies ist dann der Fall,
   * wenn das Attribut in seiner
   * <code>isEditable()
   * </code> Methode
   * <code>true</code> liefert. Diese Methode wird von CellRenderern benutzt, um
   * nicht veränderbare Zeilen anders darzustellen.
   *
   * @param row
   * @return
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
