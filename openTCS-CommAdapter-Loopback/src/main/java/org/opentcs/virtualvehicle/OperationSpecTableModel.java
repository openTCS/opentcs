/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A table model for operations and their operating times and devices. 
 * @author Tobias Marquardt (Fraunhofer IML)
 */
final class OperationSpecTableModel
    extends AbstractTableModel {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      LoggerFactory.getLogger(OperationSpecTableModel.class);
  /**
   * This class's resource bundle.
   */
  private static final ResourceBundle bundle =
      ResourceBundle.getBundle("org/opentcs/virtualvehicle/Bundle");
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    bundle.getString("OpSpecTable.nameColumn"),
    bundle.getString("OpSpecTable.timeColumn"),
    bundle.getString("OpSpecTable.deviceColumn")
  };
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
    String.class,
    Integer.class,
    Map.class
  };
  /**
   * The column numbers.
   */
  private static final int NAME_COLUMN = 0;
  private static final int TIME_COLUMN = 1;
  private static final int DEVICE_COLUMN = 2;
  /**
   * Operation specifications for every row.
   */
  private List<OperationSpec> opSpecList = new LinkedList<>();

  /**
   * Create a new instance.
   */
  public OperationSpecTableModel() {
    super();
  }

  /**
   * Set the list of <code>OperationSpec</code>s.
   * 
   * @param opSpecList The new list
   */
  public void setOpSpecList(List<OperationSpec> opSpecList) {
    this.opSpecList = opSpecList;
    fireTableDataChanged();
  }

  /**
   * Get the list of <code>OperationSpec</code>s.
   * @return The current list. 
   */
  public List<OperationSpec> getOpSpecList() {
    return opSpecList;
  }

  @Override
  public int getRowCount() {
    return opSpecList.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(int columnIndex) {
    try {
      return COLUMN_NAMES[columnIndex];
    }
    catch (ArrayIndexOutOfBoundsException exc) {
      log.warn("Invalid columnIndex", exc);
      return "ERROR";
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    OperationSpec opSpec = opSpecList.get(rowIndex);

    switch (columnIndex) {
      case NAME_COLUMN:
        return opSpec.getOperationName();
      case TIME_COLUMN:
        return opSpec.getOperatingTime();
      case DEVICE_COLUMN:
        List<LoadHandlingDevice> opDevices = opSpec.getLoadCondition();
        // Build string of device labels and device status
        StringBuilder devicesString = new StringBuilder("");
        String seperator = ", ";
        String prefix = "";
        for (LoadHandlingDevice device : opDevices) {
          devicesString.append(prefix);
          devicesString.append(device.getLabel());
          int isFull = device.isFull() ? 1 : 0;
          devicesString.append("(").append(isFull).append(")");
          prefix = seperator;
        }
        return devicesString.toString();
      default:
        throw new IllegalArgumentException("Unhandled columnIndex: "
            + columnIndex);
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    OperationSpec opSpec = opSpecList.get(rowIndex);
    OperationSpec newOpSpec;

    switch (columnIndex) {
      case NAME_COLUMN:
        newOpSpec = new OperationSpec(
            (String) aValue,
            opSpec.getOperatingTime(),
            opSpec.getLoadCondition());
        opSpecList.set(rowIndex, newOpSpec);
        break;
      case TIME_COLUMN:
        newOpSpec = new OperationSpec(
            opSpec.getOperationName(),
            (Integer) aValue,
            opSpec.getLoadCondition());
        opSpecList.set(rowIndex, newOpSpec);
        break;
      case DEVICE_COLUMN:
        @SuppressWarnings("unchecked") // The compiler can't check the type parameter
        List<LoadHandlingDevice> devices = (List<LoadHandlingDevice>) aValue;
        newOpSpec = new OperationSpec(
            opSpec.getOperationName(),
            opSpec.getOperatingTime(),
            devices);
        opSpecList.set(rowIndex, newOpSpec);
        break;
      default:
        throw new IllegalArgumentException("Unhandled columnIndex: "
            + columnIndex);
    }
  }
}
