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
import org.opentcs.guing.common.components.layer.LayerGroupEditor;
import org.opentcs.guing.common.components.layer.LayerGroupManager;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * A panel to display and edit layer groups.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayerGroupsPanel
    extends JPanel {

  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * The layer manager.
   */
  private final LayerGroupManager layerGroupManager;
  /**
   * The layer editor.
   */
  private final LayerGroupEditor layerGroupEditor;
  /**
   * The table to display available layers.
   */
  private JTable table;
  /**
   * The table model.
   */
  private LayerGroupsTableModel tableModel;

  @Inject
  public LayerGroupsPanel(ModelManager modelManager,
                          LayerGroupManager layerGroupManager,
                          LayerGroupEditor layerGroupEditor) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.layerGroupManager = requireNonNull(layerGroupManager, "layerGroupManager");
    this.layerGroupEditor = requireNonNull(layerGroupEditor, "layerGroupEditor");

    initComponents();
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    tableModel = new LayerGroupsTableModel(modelManager, layerGroupEditor);
    layerGroupManager.addLayerGroupChangeListener(tableModel);
    table = new JTable(tableModel);
    initTable();

    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  private void initTable() {
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    TableRowSorter<LayerGroupsTableModel> sorter = new TableRowSorter<>(tableModel);
    // Sort the table by the layer ordinals...
    sorter.setSortKeys(Arrays.asList(
        new RowSorter.SortKey(LayerGroupsTableModel.COLUMN_ID, SortOrder.DESCENDING)
    ));
    // ...but prevent manual sorting.
    for (int i = 0; i < table.getColumnCount(); i++) {
      sorter.setSortable(i, false);
    }
    sorter.setSortsOnUpdates(true);
    table.setRowSorter(sorter);

    // Hide the column that shows the layer group IDs.
    table.removeColumn(table.getColumnModel()
        .getColumn(table.convertColumnIndexToView(LayerGroupsTableModel.COLUMN_ID)));
  }
}
