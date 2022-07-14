/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.layer;

import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.common.components.layer.LayerChangeListener;
import org.opentcs.guing.common.components.layer.LayerEditor;
import org.opentcs.guing.common.components.layer.LayerGroupChangeListener;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;

/**
 * A table model for layers.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
class LayersTableModel
    extends AbstractTableModel
    implements LayerChangeListener,
               LayerGroupChangeListener {

  /**
   * The resource bundle to use.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle(I18nPlantOverviewOperating.LAYERS_PATH);
  /**
   * The number of the "Ordinal" column.
   */
  public static final int COLUMN_ORDINAL = 0;
  /**
   * The number of the "Visible" column.
   */
  public static final int COLUMN_VISIBLE = 1;
  /**
   * The number of the "Name" column.
   */
  public static final int COLUMN_NAME = 2;
  /**
   * The number of the "Group" column.
   */
  public static final int COLUMN_GROUP = 3;
  /**
   * The number of the "Group visible" column.
   */
  public static final int COLUMN_GROUP_VISIBLE = 4;
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[]{
    BUNDLE.getString("layersTableModel.column_ordinal.headerText"),
    BUNDLE.getString("layersTableModel.column_visible.headerText"),
    BUNDLE.getString("layersTableModel.column_name.headerText"),
    BUNDLE.getString("layersTableModel.column_group.headerText"),
    BUNDLE.getString("layersTableModel.column_groupVisible.headerText")
  };
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[]{
    Integer.class,
    Boolean.class,
    String.class,
    LayerGroup.class,
    Boolean.class
  };
  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * The layer editor.
   */
  private final LayerEditor layerEditor;

  /**
   * Creates a new instance.
   *
   * @param modelManager The model manager.
   * @param layerEditor The layer editor.
   */
  public LayersTableModel(ModelManager modelManager,
                          LayerEditor layerEditor) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.layerEditor = requireNonNull(layerEditor, "layerEditor");
  }

  @Override
  public int getRowCount() {
    return getLayers().size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= getRowCount()) {
      return null;
    }

    Layer entry = getLayers().get(rowIndex);
    switch (columnIndex) {
      case COLUMN_ORDINAL:
        return entry.getOrdinal();
      case COLUMN_VISIBLE:
        return entry.isVisible();
      case COLUMN_NAME:
        return entry.getName();
      case COLUMN_GROUP:
        return getLayerGroups().get(entry.getGroupId());
      case COLUMN_GROUP_VISIBLE:
        return getLayerGroups().get(entry.getGroupId()).isVisible();
      default:
        throw new IllegalArgumentException("Invalid column index: " + columnIndex);
    }
  }

  @Override
  public String getColumnName(int columnIndex) {
    return COLUMN_NAMES[columnIndex];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case COLUMN_ORDINAL:
        return false;
      case COLUMN_VISIBLE:
        return true;
      case COLUMN_NAME:
        return false;
      case COLUMN_GROUP:
        return false;
      case COLUMN_GROUP_VISIBLE:
        return false;
      default:
        throw new IllegalArgumentException("Invalid column index: " + columnIndex);
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= getRowCount()) {
      return;
    }

    if (aValue == null) {
      return;
    }

    Layer entry = getLayers().get(rowIndex);
    switch (columnIndex) {
      case COLUMN_ORDINAL:
        // Do nothing.
        break;
      case COLUMN_VISIBLE:
        layerEditor.setLayerVisible(entry.getId(), (boolean) aValue);
        break;
      case COLUMN_NAME:
        // Do nothing.
        break;
      case COLUMN_GROUP:
        // Do nothing.
        break;
      case COLUMN_GROUP_VISIBLE:
        // Do nothing.
        break;
      default:
        throw new IllegalArgumentException("Invalid column index: " + columnIndex);
    }
  }

  @Override
  public void layersInitialized() {
    // Once the layers are initialized we want to redraw the entire table to avoid any 
    // display errors.
    executeOnEventDispatcherThread(() -> fireTableDataChanged());
  }

  @Override
  public void layersChanged() {
    // Update the entire table but don't use fireTableDataChanged() to preserve the current 
    // selection.
    executeOnEventDispatcherThread(() -> fireTableRowsUpdated(0, getRowCount() - 1));
  }

  @Override
  public void layerAdded() {
  }

  @Override
  public void layerRemoved() {
  }

  @Override
  public void groupsInitialized() {
  }

  @Override
  public void groupsChanged() {
    // The visibility of a group, which we display as well, may have changed. Update the table.
    executeOnEventDispatcherThread(() -> fireTableRowsUpdated(0, getRowCount() - 1));
  }

  @Override
  public void groupAdded() {
  }

  @Override
  public void groupRemoved() {
  }

  private List<Layer> getLayers() {
    return getLayerWrappers().values().stream()
        .map(wrapper -> wrapper.getLayer())
        .collect(Collectors.toList());
  }

  private Map<Integer, LayerWrapper> getLayerWrappers() {
    return modelManager.getModel().getLayoutModel().getPropertyLayerWrappers().getValue();
  }

  private Map<Integer, LayerGroup> getLayerGroups() {
    return modelManager.getModel().getLayoutModel().getPropertyLayerGroups().getValue();
  }

  /**
   * Ensures the given runnable is executed on the EDT.
   * If the runnable is already being called on the EDT, the runnable is executed immediately.
   * Otherwise it is scheduled for execution on the EDT.
   * <p>
   * Note: Deferring a runnable by scheduling it for execution on the EDT even though it would 
   * have already been executed on the EDT may lead to exceptions due to data inconsistency.
   * </p>
   *
   * @param runnable The runnable.
   */
  private void executeOnEventDispatcherThread(Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    }
    else {
      SwingUtilities.invokeLater(runnable);
    }
  }
}
