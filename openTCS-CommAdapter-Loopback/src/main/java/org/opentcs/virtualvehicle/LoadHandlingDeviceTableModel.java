/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.swing.table.AbstractTableModel;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A table model for LoadHandlingDevices.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
final class LoadHandlingDeviceTableModel
    extends AbstractTableModel {

  /**
   * This class's Logger.
   */
  private static final Logger LOG
      = LoggerFactory.getLogger(LoadHandlingDeviceTableModel.class);
  /**
   * This class's resource bundle.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle("org/opentcs/virtualvehicle/Bundle");
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    "Name",
    BUNDLE.getString("Full?")};
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
    String.class,
    Boolean.class};
  /**
   * The index of the column displaying the name.
   */
  private static final int NAME_COLUMN = 0;
  /**
   * The index of the column displaying the 'full' property.
   */
  private static final int FULL_COLUMN = 1;
  /**
   * The devices in our table.
   */
  private List<LoadHandlingDevice> devices = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public LoadHandlingDeviceTableModel() {
    // Do nada.
  }

  /**
   * Updates the list of devices.
   *
   * @param newDevices The new list of devices
   */
  public void updateLoadHandlingDevices(@Nonnull List<LoadHandlingDevice> newDevices) {
    requireNonNull(newDevices, "newDevices");
    if (!Objects.equals(this.devices, newDevices)) {
      this.devices = newDevices;
      fireTableDataChanged();
    }
  }

  @Override
  public int getRowCount() {
    return devices.size();
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
      LOG.warn("Invalid columnIndex", exc);
      return "ERROR";
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    LoadHandlingDevice device = devices.get(rowIndex);

    switch (columnIndex) {
      case NAME_COLUMN:
        return device.getLabel();
      case FULL_COLUMN:
        return device.isFull();
      default:
        throw new IllegalArgumentException("Unhandled columnIndex: "
            + columnIndex);
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    LoadHandlingDevice device = devices.get(rowIndex);

    switch (columnIndex) {
      case NAME_COLUMN:
        devices.set(rowIndex,
                    new LoadHandlingDevice((String) aValue, device.isFull()));
        break;
      case FULL_COLUMN:
        devices.set(rowIndex,
                    new LoadHandlingDevice(device.getLabel(), (boolean) aValue));
        break;
      default:
        throw new IllegalArgumentException("Unhandled columnIndex: "
            + columnIndex);
    }
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return true;
  }

  /**
   * Returns the list containing the LoadHandlingDevices associated with his model.
   *
   * @return The list containing the LoadHandlingDevices associated with his model.
   */
  public List<LoadHandlingDevice> getLoadHandlingDevices() {
    return devices;
  }
}
