/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.components.layer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.guing.common.components.layer.LayerGroupManager;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.IconToolkit;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;

/**
 * A panel to display and edit layer groups.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayerGroupsPanel
    extends JPanel {

  /**
   * The path containing the icons.
   */
  private static final String ICON_PATH = "/org/opentcs/guing/res/symbols/layer/";
  /**
   * The resource bundle to use.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle(I18nPlantOverviewModeling.LAYERS_PATH);
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
  private final LayerGroupEditorModeling layerGroupEditor;
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
                          LayerGroupEditorModeling layerGroupEditor) {
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

    add(createToolBar(), BorderLayout.NORTH);
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

  private JToolBar createToolBar() {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);

    toolBar.add(createAddGroupButton());
    toolBar.add(createRemoveGroupButton());

    return toolBar;
  }

  private JButton createAddGroupButton() {
    IconToolkit iconkit = IconToolkit.instance();
    JButton button = new JButton(iconkit.getImageIconByFullPath(ICON_PATH + "create-layer-group.16.png"));
    button.addActionListener(actionEvent -> {
      layerGroupEditor.createLayerGroup();
      table.getSelectionModel().setSelectionInterval(0, 0);
    });
    button.setToolTipText(BUNDLE.getString("layerGroupsPanel.button_addGroup.tooltipText"));

    return button;
  }

  private JButton createRemoveGroupButton() {
    IconToolkit iconkit = IconToolkit.instance();
    JButton button = new JButton(iconkit.getImageIconByFullPath(ICON_PATH + "delete-layer-group.16.png"));
    button.addActionListener(new RemoveGroupListener());
    button.setToolTipText(BUNDLE.getString("layerGroupsPanel.button_removeGroup.tooltipText"));

    // Allow the remove group button to be pressed only if there's a group selected and if there's
    // more than one group in the model.
    table.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
      button.setEnabled(table.getSelectedRow() != -1 && tableModel.getRowCount() > 1);
    });
    tableModel.addTableModelListener(tableModelEvent -> {
      button.setEnabled(table.getSelectedRow() != -1 && tableModel.getRowCount() > 1);
    });

    return button;
  }

  private class RemoveGroupListener
      implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      int selectedRow = table.getSelectedRow();
      int selectedGroupId = tableModel.getDataAt(table.convertRowIndexToModel(selectedRow)).getId();

      Map<Boolean, List<Layer>> layersByGroupAssignment = modelManager.getModel()
          .getLayoutModel().getPropertyLayerWrappers().getValue().values().stream()
          .map(wrapper -> wrapper.getLayer())
          .collect(Collectors.partitioningBy(layer -> layer.getGroupId() == selectedGroupId));
      List<Layer> layersAssignedToGroupToDelete = layersByGroupAssignment.get(Boolean.TRUE);
      List<Layer> layersAssignedToOtherGroups = layersByGroupAssignment.get(Boolean.FALSE);

      if (layersAssignedToOtherGroups.isEmpty()) {
        // All layers in the model are assigned to the group the user wants to remove.
        // In this case, removing the group is not allowed as that would mean that all layers would 
        // be removed as well and there wouldn't be any layers left.
        JOptionPane.showMessageDialog(
            LayerGroupsPanel.this,
            BUNDLE.getString("layerGroupsPanel.optionPane_groupRemovalNotPossible.message"),
            BUNDLE.getString("layerGroupsPanel.optionPane_groupRemovalNotPossible.title"),
            JOptionPane.INFORMATION_MESSAGE
        );
        return;
      }

      if (!layersAssignedToGroupToDelete.isEmpty()) {
        // The user is about remove a group with layers assigned to it. Removing the group results
        // in the assigned layers and the model components they contain to be removed as well.
        int selectedOption = JOptionPane.showConfirmDialog(
            LayerGroupsPanel.this,
            BUNDLE.getString("layerGroupsPanel.optionPane_confirmGroupAndAssignedLayersRemoval.message"),
            BUNDLE.getString("layerGroupsPanel.optionPane_confirmGroupAndAssignedLayersRemoval.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (selectedOption == JOptionPane.NO_OPTION) {
          return;
        }
      }

      layerGroupEditor.deleteLayerGroup(selectedGroupId);
    }
  }
}
