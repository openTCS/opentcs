/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A table model for drive orders.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
class DriveOrderTableModel
    extends AbstractTableModel {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DriveOrderTableModel.class);
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    "Location",
    "Operation"
  };
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
    TCSObjectReference.class,
    String.class
  };
  /**
   * The actual content.
   */
  private final List<DriveOrderStructure> driveOrderDataList = new ArrayList<>();

  /**
   * Creates a new DriveOrderTableModel.
   *
   * @param list A list with the current <code>DriveOrderStructure</code>s
   */
  public DriveOrderTableModel(List<DriveOrderStructure> list) {
    super();
    Objects.requireNonNull(list);
    for (DriveOrderStructure curDOS : list) {
      driveOrderDataList.add(curDOS);
    }
    fireTableDataChanged();
  }

  @Override
  public int getRowCount() {
    return driveOrderDataList.size();
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

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
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
        throw new IllegalArgumentException("Invalid columnIndex: "
            + columnIndex);
    }
  }

  @Override
  public String getColumnName(int columnIndex) {
    try {
      return COLUMN_NAMES[columnIndex];
    }
    catch (ArrayIndexOutOfBoundsException exc) {
      LOG.warn("Invalid columnIndex", exc);
      return "FEHLER";
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return true;
      case 1:
        return true;
      default:
        throw new IllegalArgumentException("Invalid columnIndex: "
            + columnIndex);
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
        throw new IllegalArgumentException("Unhandled columnIndex: "
            + columnIndex);
    }
  }

  /**
   * Returns the list containing all <code>DriveOrderStructure</code>s.
   *
   * @return the list containing all <code>DriveOrderStructure</code>s.
   */
  public List<DriveOrderStructure> getList() {
    return driveOrderDataList;
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
   * Adds a new DriveOrderStructure.
   *
   * @param dos The new drive order structure
   */
  public void addData(DriveOrderStructure dos) {
    Objects.requireNonNull(dos);
    driveOrderDataList.add(dos);
  }
}
