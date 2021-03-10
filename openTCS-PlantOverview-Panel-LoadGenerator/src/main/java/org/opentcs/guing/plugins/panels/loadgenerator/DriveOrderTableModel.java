/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import static org.opentcs.guing.plugins.panels.loadgenerator.I18nPlantOverviewPanelLoadGenerator.BUNDLE_PATH;

/**
 * A table model for drive orders.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
class DriveOrderTableModel
    extends AbstractTableModel {

  /**
   * This classe's bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * The column names.
   */
  private final String[] columnNames = new String[] {
    bundle.getString("driveOrderTableModel.column_location.headerText"),
    bundle.getString("driveOrderTableModel.column_operation.headerText")
  };
  /**
   * The column classes.
   */
  private final Class<?>[] columnClasses = new Class<?>[] {
    TCSObjectReference.class,
    String.class
  };
  /**
   * The actual content.
   */
  private final List<DriveOrderStructure> driveOrderDataList = new ArrayList<>();

  /**
   * Creates a new instance.
   *
   * @param driveOrders The actual list of drive orders.
   */
  public DriveOrderTableModel(List<DriveOrderStructure> driveOrders) {
    requireNonNull(driveOrders, "driveOrders");

    for (DriveOrderStructure curDOS : driveOrders) {
      driveOrderDataList.add(curDOS);
    }
  }

  /**
   * Creates a new instance.
   */
  public DriveOrderTableModel() {
  }

  @Override
  public int getRowCount() {
    return driveOrderDataList.size();
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= driveOrderDataList.size()) {
      return null;
    }
    DriveOrderStructure data = driveOrderDataList.get(rowIndex);

    if (data == null) {
      return null;
    }
    switch (columnIndex) {
      case 0:
        if (data.getDriveOrderLocation() == null) {
          return null;
        }
        else {
          return data.getDriveOrderLocation().getName();
        }

      case 1:
        return data.getDriveOrderVehicleOperation();
      default:
        throw new IllegalArgumentException("Invalid columnIndex: " + columnIndex);
    }
  }

  @Override
  public String getColumnName(int columnIndex) {
    return columnNames[columnIndex];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnClasses[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return true;
      case 1:
        return true;
      default:
        throw new IllegalArgumentException("Invalid columnIndex: " + columnIndex);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= driveOrderDataList.size()) {
      return;
    }
    DriveOrderStructure data = driveOrderDataList.get(rowIndex);
    if (aValue == null) {
      return;
    }
    switch (columnIndex) {
      case 0:
        data.setDriveOrderLocation((TCSObjectReference<Location>) aValue);
        break;
      case 1:
        data.setDriveOrderVehicleOperation((String) aValue);
        break;
      default:
        throw new IllegalArgumentException("Unhandled columnIndex: " + columnIndex);
    }
  }

  /**
   * Returns this model's complete content.
   *
   * @return This model's complete content. The result list is unmodifiable.
   */
  public List<DriveOrderStructure> getContent() {
    return Collections.unmodifiableList(driveOrderDataList);
  }

  /**
   * Returns the DriveOrderStructure at the given index.
   *
   * @param row Index which DriveOrderStructure shall be returned
   * @return The DriveOrderStructure
   */
  public DriveOrderStructure getDataAt(int row) {
    if (row < 0 || row >= driveOrderDataList.size()) {
      return null;
    }
    return driveOrderDataList.get(row);
  }

  /**
   * Adds drive order data to the end of the model/list.
   *
   * @param driveOrder The new drive order data
   */
  public void addData(DriveOrderStructure driveOrder) {
    requireNonNull(driveOrder, "driveOrder");

    driveOrderDataList.add(driveOrder);
    fireTableDataChanged();
  }

  /**
   * Removes the DriveOrderStructure at the given index.
   * Does nothing if <code>row</code> is not in scope.
   *
   * @param row Index which DriveOrderStructure shall be removed
   */
  public void removeData(int row) {
    if (row < 0 || row >= driveOrderDataList.size()) {
      return;
    }
    driveOrderDataList.remove(row);
    fireTableDataChanged();
  }
}
