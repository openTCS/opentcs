/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.layer;

import java.awt.BorderLayout;
import java.util.Arrays;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import org.opentcs.guing.common.components.layer.DisabledCheckBoxCellRenderer;
import org.opentcs.guing.common.components.layer.LayerEditor;
import org.opentcs.guing.common.components.layer.LayerGroupCellRenderer;
import org.opentcs.guing.common.components.layer.LayerGroupManager;
import org.opentcs.guing.common.components.layer.LayerManager;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * A panel to display and edit layers.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayersPanel
    extends JPanel {

  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * The layer manager.
   */
  private final LayerManager layerManager;
  /**
   * The layer group manager.
   */
  private final LayerGroupManager layerGroupManager;
  /**
   * The layer editor.
   */
  private final LayerEditor layerEditor;
  /**
   * The table to display available layers.
   */
  private JTable table;
  /**
   * The table model.
   */
  private LayersTableModel tableModel;

  @Inject
  public LayersPanel(ModelManager modelManager,
                     LayerManager layerManager,
                     LayerGroupManager layerGroupManager,
                     LayerEditor layerEditor) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.layerManager = requireNonNull(layerManager, "layerManager");
    this.layerGroupManager = requireNonNull(layerGroupManager, "layerGroupManager");
    this.layerEditor = requireNonNull(layerEditor, "layerEditor");

    initComponents();
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    tableModel = new LayersTableModel(modelManager, layerEditor);
    layerManager.setLayerChangeListener(tableModel);
    layerGroupManager.addLayerGroupChangeListener(tableModel);
    table = new JTable(tableModel);
    initTable();

    JScrollPane scrollPane = new JScrollPane(table);
    add(scrollPane, BorderLayout.CENTER);
  }

  private void initTable() {
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    TableRowSorter<LayersTableModel> sorter = new TableRowSorter<>(tableModel);
    // Sort the table by the layer ordinals...
    sorter.setSortKeys(Arrays.asList(
        new RowSorter.SortKey(LayersTableModel.COLUMN_ORDINAL, SortOrder.DESCENDING)
    ));
    // ...but prevent manual sorting.
    for (int i = 0; i < table.getColumnCount(); i++) {
      sorter.setSortable(i, false);
    }
    sorter.setSortsOnUpdates(true);
    table.setRowSorter(sorter);

    // Hide the column that shows the layer ordinals.
    table.removeColumn(table.getColumnModel()
        .getColumn(table.convertColumnIndexToView(LayersTableModel.COLUMN_ORDINAL)));

    table.getColumnModel()
        .getColumn(table.convertColumnIndexToView(LayersTableModel.COLUMN_GROUP))
        .setCellRenderer(new LayerGroupCellRenderer());
    table.getColumnModel()
        .getColumn(table.convertColumnIndexToView(LayersTableModel.COLUMN_GROUP_VISIBLE))
        .setCellRenderer(new DisabledCheckBoxCellRenderer());
  }
}
