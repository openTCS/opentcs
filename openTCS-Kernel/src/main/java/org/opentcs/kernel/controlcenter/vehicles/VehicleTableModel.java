/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter.vehicles;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import org.opentcs.drivers.CommunicationAdapterFactory;
import org.opentcs.drivers.VehicleModel;
import org.opentcs.virtualvehicle.LoopbackCommunicationAdapterFactory;

/**
 * A model for displaying a list/table of vehicles in the kernel GUI.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
final class VehicleTableModel
    extends AbstractTableModel
    implements Observer {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(VehicleTableModel.class.getName());
  /**
   * This class's resource bundle.
   */
  private static final ResourceBundle bundle =
      ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/vehicles/Bundle");
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    bundle.getString("Vehicle"),
    bundle.getString("State"),
    "Adapter",
    bundle.getString("Enabled?"),
    "Position"
  };
  /**
   * The column classes.
   */
  private static final Class[] COLUMN_CLASSES = new Class[] {
    String.class,
    String.class,
    CommunicationAdapterFactory.class,
    Boolean.class,
    String.class
  };
  /**
   * The index of the column showing the vehicle name.
   */
  private static final int VEHICLE_COLUMN = 0;
  /**
   * The index of the column showing the vehicle state.
   */
  private static final int STATE_COLUMNN = 1;
  /**
   * The index of the column showing the associated adapter.
   */
  private static final int ADAPTER_COLUMN = 2;
  /**
   * The index of the column showing the adapter's enabled state.
   */
  private static final int ENABLED_COLUMN = 3;
  /**
   * The index of the column showing the vehicle's current position.
   */
  private static final int POSITION_COLUMN = 4;
  /**
   * The vehicles we're controlling.
   */
  private final List<VehicleModel> vehicleList = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  VehicleTableModel() {
    // Do nada.
  }

  /**
   * Adds a new <code>VehicleModel</code> to this model.
   * 
   * @param newVehicle The new <code>VehicleModel</code>
   */
  public void addData(VehicleModel newVehicle) {
    vehicleList.add(newVehicle);
    fireTableRowsInserted(vehicleList.size(), vehicleList.size());
  }

  /**
   * Returns the vehicle model at the given row.
   * 
   * @param row The row.
   * @return The VehicleModel at this row.
   */
  public VehicleModel getDataAt(int row) {
    if (row >= 0) {
      return vehicleList.get(row);
    }
    else {
      return null;
    }
  }

  @Override
  public int getRowCount() {
    return vehicleList.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    VehicleModel vehicle = vehicleList.get(rowIndex);
    switch (columnIndex) {
      case ADAPTER_COLUMN:
        break;
      case ENABLED_COLUMN:
        if (vehicle.getCommunicationAdapter() != null) {
          boolean enabled = (boolean) aValue;
          if (enabled) {
            vehicle.getCommunicationAdapter().enable();
          }
          else {
            vehicle.getCommunicationAdapter().disable();
          }
        }
        break;
      case POSITION_COLUMN:
        break;
      default:
        throw new IllegalArgumentException("Unhandled columnIndex: "
            + columnIndex);
    }
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex >= vehicleList.size()) {
      return null;
    }
    VehicleModel vehicle = vehicleList.get(rowIndex);

    switch (columnIndex) {
      case VEHICLE_COLUMN:
        return vehicle.getName();
      case STATE_COLUMNN:
        return vehicle.getVehicle().getState().name();
      case ADAPTER_COLUMN:
        try {
          return vehicle.getCommunicationFactory().getAdapterDescription();
        }
        catch (NullPointerException e) {
          return null;
        }
      case ENABLED_COLUMN:
        return vehicle.isCommunicationAdapterEnabled();
      case POSITION_COLUMN:
        return vehicle.getPosition();
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
      log.log(Level.WARNING, "Invalid columnIndex", exc);
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
      case ADAPTER_COLUMN:
        return true;
      case ENABLED_COLUMN:
        return true;
      case POSITION_COLUMN:
        return vehicleList.get(rowIndex).isCommunicationAdapterEnabled()
            && vehicleList.get(rowIndex).getCommunicationFactory() instanceof LoopbackCommunicationAdapterFactory;
      default:
        return false;
    }
  }

  /**
   * Returns a list containing the vehicle models associated with this model.
   * 
   * @return A list containing the vehicle models associated with this model.
   */
  public List<VehicleModel> getVehicleModels() {
    return vehicleList;
  }

  @Override
  public void update(Observable o, Object arg) {
    VehicleModel model = (VehicleModel) o;
    int i = 0;

    while (!model.equals(vehicleList.get(i))) {
      i++;
    }
    vehicleList.set(i, model);
    fireTableRowsUpdated(i, i);
  }
}
