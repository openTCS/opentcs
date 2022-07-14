/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.layer;

import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.I18nPlantOverview;

/**
 * A table model for layer groups.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public abstract class AbstractLayerGroupsTableModel
    extends AbstractTableModel
    implements LayerGroupChangeListener {

  /**
   * The resource bundle to use.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle(I18nPlantOverview.LAYERS_PATH);
  /**
   * The number of the "ID" column.
   */
  public static final int COLUMN_ID = 0;
  /**
   * The number of the "Name" column.
   */
  public static final int COLUMN_NAME = 1;
  /**
   * The number of the "Visible" column.
   */
  public static final int COLUMN_VISIBLE = 2;
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[]{
    BUNDLE.getString("abstractLayerGroupsTableModel.column_id.headerText"),
    BUNDLE.getString("abstractLayerGroupsTableModel.column_name.headerText"),
    BUNDLE.getString("abstractLayerGroupsTableModel.column_visible.headerText")
  };
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[]{
    Integer.class,
    String.class,
    Boolean.class
  };
  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * The layer group editor.
   */
  private final LayerGroupEditor layerGroupEditor;

  /**
   * Creates a new instance.
   *
   * @param modelManager The model manager.
   * @param layerGroupEditor The layer group editor.
   */
  public AbstractLayerGroupsTableModel(ModelManager modelManager,
                                       LayerGroupEditor layerGroupEditor) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.layerGroupEditor = requireNonNull(layerGroupEditor, "layerGroupEditor");
  }

  @Override
  public int getRowCount() {
    return layerGroups().size();
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

    LayerGroup entry = layerGroups().get(rowIndex);
    switch (columnIndex) {
      case COLUMN_ID:
        return entry.getId();
      case COLUMN_NAME:
        return entry.getName();
      case COLUMN_VISIBLE:
        return entry.isVisible();
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
      case COLUMN_ID:
        return false;
      case COLUMN_NAME:
        return isNameColumnEditable();
      case COLUMN_VISIBLE:
        return isVisibleColumnEditable();
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

    LayerGroup entry = layerGroups().get(rowIndex);
    switch (columnIndex) {
      case COLUMN_ID:
        // Do nothing.
        break;
      case COLUMN_NAME:
        layerGroupEditor.setGroupName(entry.getId(), aValue.toString());
        break;
      case COLUMN_VISIBLE:
        layerGroupEditor.setGroupVisible(entry.getId(), (boolean) aValue);
        break;
      default:
        throw new IllegalArgumentException("Invalid column index: " + columnIndex);
    }
  }

  public LayerGroup getDataAt(int index) {
    return layerGroups().get(index);
  }

  protected abstract boolean isNameColumnEditable();

  protected abstract boolean isVisibleColumnEditable();

  private List<LayerGroup> layerGroups() {
    return getLayerGroups().values().stream().collect(Collectors.toList());
  }

  private Map<Integer, LayerGroup> getLayerGroups() {
    return modelManager.getModel().getLayoutModel().getPropertyLayerGroups().getValue();
  }
}
