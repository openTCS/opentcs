/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.components.layer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.guing.common.components.layer.DisabledCheckBoxCellRenderer;
import org.opentcs.guing.common.components.layer.LayerGroupCellRenderer;
import org.opentcs.guing.common.components.layer.LayerGroupManager;
import org.opentcs.guing.common.components.layer.LayerManager;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.IconToolkit;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;
import org.opentcs.util.gui.StringListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel to display and edit layers.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayersPanel
    extends JPanel {

  private static final Logger LOG = LoggerFactory.getLogger(LayersPanel.class);
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
  private final LayerManager layerManager;
  /**
   * The layer group manager.
   */
  private final LayerGroupManager layerGroupManager;
  /**
   * The layer editor.
   */
  private final LayerEditorModeling layerEditor;
  /**
   * Provides the currently active layer.
   */
  private final ActiveLayerProvider activeLayerProvider;
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
                     LayerEditorModeling layerEditor,
                     ActiveLayerProvider activeLayerProvider) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.layerManager = requireNonNull(layerManager, "layerManager");
    this.layerGroupManager = requireNonNull(layerGroupManager, "layerGroupManager");
    this.layerEditor = requireNonNull(layerEditor, "layerEditor");
    this.activeLayerProvider = requireNonNull(activeLayerProvider, "activeLayerProvider");

    initComponents();
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    tableModel = new LayersTableModel(modelManager, activeLayerProvider, layerEditor);
    layerManager.setLayerChangeListener(tableModel);
    layerGroupManager.addLayerGroupChangeListener(tableModel);
    table = new JTable(tableModel);
    initTable();
    
    add(createToolBar(), BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
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
        .getColumn(table.convertColumnIndexToView(LayersTableModel.COLUMN_GROUP_VISIBLE))
        .setCellRenderer(new DisabledCheckBoxCellRenderer());

    initActiveColumn();
    initGroupColumn();
  }

  private JToolBar createToolBar() {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);

    toolBar.add(createAddLayerButton());
    toolBar.add(createRemoveLayerButton());
    toolBar.add(createMoveLayerUpButton());
    toolBar.add(createMoveLayerDownButton());

    return toolBar;
  }

  private void initActiveColumn() {
    // Since our concept of layers allows only one active layer at a time, a representation of that
    // state as radio buttons seems to have the potential to be more intuitive for users.
    // Radio buttons usually come with/in a button group which ensures that only one of the radio
    // buttons in that group can be selected at a time. In a table, though, such a button group
    // doesn't seem to work very well if it's used to group radio buttons across multiple rows.
    // For that reason the behavior of radio buttons in a button group is "emulated" in/trough
    // the LayerManager implementation.
    TableColumn columnActive = table.getColumnModel()
        .getColumn(table.convertColumnIndexToView(LayersTableModel.COLUMN_ACTIVE));
    columnActive.setCellEditor(new RadioButtonCellEditor());
    columnActive.setCellRenderer(new RadioButtonCellRenderer());
  }

  private void initGroupColumn() {
    TableColumn columnGroup = table.getColumnModel()
        .getColumn(table.convertColumnIndexToView(LayersTableModel.COLUMN_GROUP));
    columnGroup.setCellRenderer(new LayerGroupCellRenderer());
    columnGroup.setCellEditor(new GroupCellEditor());
  }

  private JButton createAddLayerButton() {
    IconToolkit iconkit = IconToolkit.instance();
    JButton button = new JButton(iconkit.getImageIconByFullPath(ICON_PATH + "create-layer.16.png"));
    button.addActionListener(actionEvent -> {
      layerEditor.createLayer();
      table.getSelectionModel().setSelectionInterval(0, 0);
    });
    button.setToolTipText(BUNDLE.getString("layersPanel.button_addLayer.tooltipText"));

    return button;
  }

  private JButton createRemoveLayerButton() {
    IconToolkit iconkit = IconToolkit.instance();
    JButton button = new JButton(iconkit.getImageIconByFullPath(ICON_PATH + "delete-layer.16.png"));
    button.addActionListener(new RemoveLayerListener());
    button.setToolTipText(BUNDLE.getString("layersPanel.button_removeLayer.tooltipText"));

    // Allow the remove layer button to be pressed only if there's a layer selected and if there's
    // more than one layer in the model.
    table.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
      button.setEnabled(table.getSelectedRow() != -1 && tableModel.getRowCount() > 1);
    });
    tableModel.addTableModelListener(tableModelEvent -> {
      button.setEnabled(table.getSelectedRow() != -1 && tableModel.getRowCount() > 1);
    });

    return button;
  }

  private JButton createMoveLayerUpButton() {
    IconToolkit iconkit = IconToolkit.instance();
    JButton button = new JButton(iconkit.getImageIconByFullPath(ICON_PATH + "move-layer-up.16.png"));
    button.setEnabled(false);
    button.addActionListener(actionEvent -> {
      int selectedRow = table.getSelectedRow();
      int selectedLayerId = tableModel.getDataAt(table.convertRowIndexToModel(selectedRow)).getId();
      layerEditor.moveLayerUp(selectedLayerId);
    });
    button.setToolTipText(BUNDLE.getString("layersPanel.button_moveLayerUp.tooltipText"));

    // Allow the button to be pressed only if there's a layer selected.
    table.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
      button.setEnabled(table.getSelectedRow() != -1);
    });

    return button;
  }

  private JButton createMoveLayerDownButton() {
    IconToolkit iconkit = IconToolkit.instance();
    JButton button = new JButton(iconkit.getImageIconByFullPath(ICON_PATH + "move-layer-down.16.png"));
    button.setEnabled(false);
    button.addActionListener(actionEvent -> {
      int selectedRow = table.getSelectedRow();
      int selectedLayerId = tableModel.getDataAt(table.convertRowIndexToModel(selectedRow)).getId();
      layerEditor.moveLayerDown(selectedLayerId);
    });
    button.setToolTipText(BUNDLE.getString("layersPanel.button_moveLayerDown.tooltipText"));

    // Allow the button to be pressed only if there's a layer selected.
    table.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
      button.setEnabled(table.getSelectedRow() != -1);
    });

    return button;
  }

  private class RemoveLayerListener
      implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      int selectedRow = table.getSelectedRow();
      int selectedLayerId = tableModel.getDataAt(table.convertRowIndexToModel(selectedRow)).getId();

      if (layerManager.containsComponents(selectedLayerId)) {
        int selectedOption = JOptionPane.showConfirmDialog(
            LayersPanel.this,
            BUNDLE.getString("layersPanel.optionPane_confirmLayerWithComponentsRemoval.message"),
            BUNDLE.getString("layersPanel.optionPane_confirmLayerWithComponentsRemoval.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (selectedOption == JOptionPane.NO_OPTION) {
          return;
        }
      }

      layerEditor.deleteLayer(selectedLayerId);
    }
  }

  private class RadioButtonCellRenderer
      implements TableCellRenderer {

    private final Border unfocusedCellBorder = BorderFactory.createEmptyBorder();
    private final Border focusedCellBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
    private final JRadioButton radioButton;

    public RadioButtonCellRenderer() {
      radioButton = new JRadioButton();
      radioButton.setHorizontalAlignment(JRadioButton.CENTER);
      radioButton.setBorderPainted(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int col) {
      radioButton.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
      radioButton.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
      radioButton.setBorder(hasFocus ? focusedCellBorder : unfocusedCellBorder);
      radioButton.setSelected(Boolean.TRUE.equals(value));

      return radioButton;
    }
  }

  private class RadioButtonCellEditor
      extends AbstractCellEditor
      implements TableCellEditor {

    private final JRadioButton radioButton;

    public RadioButtonCellEditor() {
      radioButton = new JRadioButton();
      radioButton.setHorizontalAlignment(JRadioButton.CENTER);
      radioButton.addActionListener(actionEvent -> stopCellEditing());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int col) {
      radioButton.setSelected(Boolean.TRUE.equals(value));
      return radioButton;
    }

    @Override
    public Object getCellEditorValue() {
      return radioButton.isSelected();
    }
  }

  private class GroupCellEditor
      extends DefaultCellEditor {

    private final DefaultComboBoxModel<LayerGroup> model;

    public GroupCellEditor() {
      super(new JComboBox<LayerGroup>());
      @SuppressWarnings("unchecked")
      JComboBox<LayerGroup> combobox = (JComboBox<LayerGroup>) getComponent();
      combobox.setRenderer(new StringListCellRenderer<>(group -> group.getName()));
      this.model = (DefaultComboBoxModel<LayerGroup>) combobox.getModel();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
      model.removeAllElements();
      List<LayerGroup> groups = modelManager.getModel().getLayoutModel().getPropertyLayerGroups()
          .getValue().values().stream()
          .sorted((o1, o2) -> Integer.compare(o1.getId(), o2.getId()))
          .collect(Collectors.toList());
      model.addAll(groups);
      model.setSelectedItem(table.getModel().getValueAt(row, column));

      return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
  }
}
