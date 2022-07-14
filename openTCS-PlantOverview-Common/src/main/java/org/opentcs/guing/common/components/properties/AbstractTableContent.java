/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.inject.Provider;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.opentcs.guing.base.components.properties.type.Property;
import org.opentcs.guing.common.components.properties.event.TableChangeListener;
import org.opentcs.guing.common.components.properties.event.TableSelectionChangeEvent;
import org.opentcs.guing.common.components.properties.table.AttributesTable;

/**
 * Base implementation for content that displays model properties in a table.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractTableContent
    extends AbstractAttributesContent
    implements TableChangeListener {

  /**
   * The table that shows the properties.
   */
  protected AttributesTable fTable;
  /**
   * The cell editors.
   */
  protected List<TableCellEditor> fCellEditors = new ArrayList<>();
  /**
   * Indicates that changes to the tabel model should also update the model component.
   */
  protected boolean fEvaluateTableChanges = true;
  /**
   * Provides attribute tables.
   */
  private final Provider<AttributesTable> tableProvider;

  /**
   * Creates a new instance.
   *
   * @param tableProvider Provides attribute tables.
   */
  public AbstractTableContent(Provider<AttributesTable> tableProvider) {
    this.tableProvider = requireNonNull(tableProvider, "tableProvider");
  }

  @Override // AbstractAttributesContent
  protected JComponent createComponent() {
    JPanel component = new JPanel();

    initTable();
    JScrollPane scrollPane = new JScrollPane(fTable);

    component.setLayout(new BorderLayout());
    component.add(scrollPane, BorderLayout.CENTER);

    return component;
  }

  @Override // TableChangeListener
  public void tableSelectionChanged(TableSelectionChangeEvent e) {
  }

  @Override // TableChangeListener
  public void tableModelChanged() {
  }

  /**
   * Initialises the table.
   */
  protected void initTable() {
    fTable = tableProvider.get();
    setTableCellRenderers();
    setTableCellEditors();
    fTable.addTableChangeListener(this);
  }

  /**
   * Set the table cell renderers.
   */
  protected void setTableCellRenderers() {
  }

  /**
   * Set the table cell editors.
   */
  protected void setTableCellEditors() {
  }

  /**
   * Set new table content.
   *
   * @param content A map from property name to property.
   */
  protected void setTableContent(Map<String, Property> content) {
    fEvaluateTableChanges = false;

    TableColumnModel columnModel = fTable.getColumnModel();
    int[] widths = new int[columnModel.getColumnCount()];

    for (int i = 0; i < widths.length; i++) {
      widths[i] = columnModel.getColumn(i).getWidth();
    }

    fTable.setModel(createTableModel(content));

    for (int i = 0; i < widths.length; i++) {
      columnModel.getColumn(i).setPreferredWidth(widths[i]);
    }

    fEvaluateTableChanges = true;
  }

  /**
   * Creates a new table model from the content.
   *
   * @param content Map from property name to property.
   * @return A table model that represents the content.
   */
  protected abstract TableModel createTableModel(Map<String, Property> content);

}
