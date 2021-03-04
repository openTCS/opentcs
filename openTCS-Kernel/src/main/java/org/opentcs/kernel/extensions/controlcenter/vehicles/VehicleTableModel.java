/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.controlcenter.vehicles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.SimVehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A model for displaying a list/table of vehicles in the kernel GUI.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
final class VehicleTableModel
    extends AbstractTableModel
    implements PropertyChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleTableModel.class);
  /**
   * This class's resource bundle.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/vehicles/Bundle");
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    BUNDLE.getString("Vehicle"),
    BUNDLE.getString("State"),
    BUNDLE.getString("Adapter"),
    BUNDLE.getString("Enabled?"),
    BUNDLE.getString("Position")
  };
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
    String.class,
    String.class,
    VehicleCommAdapterFactory.class,
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
  private final List<VehicleEntry> entries = new ArrayList<>();
  /**
   * The identifier for the adapter column.
   */
  public static final String ADAPTER_COLUMN_IDENTIFIER = COLUMN_NAMES[ADAPTER_COLUMN];
  /**
   * The identifier for the position column.
   */
  public static final String POSITION_COLUMN_IDENTIFIER = COLUMN_NAMES[POSITION_COLUMN];  

  /**
   * Creates a new instance.
   */
  VehicleTableModel() {
  }

  /**
   * Adds a new entry to this model.
   *
   * @param newEntry The new entry.
   */
  public void addData(VehicleEntry newEntry) {
    entries.add(newEntry);
    fireTableRowsInserted(entries.size(), entries.size());
  }

  /**
   * Returns the vehicle entry at the given row.
   *
   * @param row The row.
   * @return The entry at the given row.
   */
  public VehicleEntry getDataAt(int row) {
    if (row >= 0) {
      return entries.get(row);
    }
    else {
      return null;
    }
  }

  @Override
  public int getRowCount() {
    return entries.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    VehicleEntry entry = entries.get(rowIndex);
    switch (columnIndex) {
      case ADAPTER_COLUMN:
        break;
      case ENABLED_COLUMN:
        VehicleCommAdapter commAdapter = entry.getCommAdapter();
        if (commAdapter != null) {
          if ((boolean) aValue) {
            commAdapter.enable();
          }
          else {
            commAdapter.disable();
          }
        }
        break;
      case POSITION_COLUMN:
        break;
      default:
        LOG.warn("Unhandled column index: {}", columnIndex);
    }
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex >= entries.size()) {
      return null;
    }

    VehicleEntry entry = entries.get(rowIndex);

    switch (columnIndex) {
      case VEHICLE_COLUMN:
        return entry.getVehicle().getName();
      case STATE_COLUMNN:
        return getVehicleState(entry);
      case ADAPTER_COLUMN:
        return entry.getCommAdapterFactory();
      case ENABLED_COLUMN:
        VehicleCommAdapter commAdapter = entry.getCommAdapter();
        return commAdapter != null && commAdapter.isEnabled();
      case POSITION_COLUMN:
        return entry.getProcessModel().getVehiclePosition();
      default:
        LOG.warn("Unhandled column index: {}", columnIndex);
        return "Invalid column index " + columnIndex;
    }
  }

  @Override
  public String getColumnName(int columnIndex) {
    try {
      return COLUMN_NAMES[columnIndex];
    }
    catch (ArrayIndexOutOfBoundsException exc) {
      LOG.warn("Invalid columnIndex", exc);
      return "Invalid column index " + columnIndex;
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
        VehicleCommAdapter commAdapter = entries.get(rowIndex).getCommAdapter();
        return commAdapter instanceof SimVehicleCommAdapter && commAdapter.isEnabled();
      default:
        return false;
    }
  }

  /**
   * Returns a list containing the vehicle models associated with this model.
   *
   * @return A list containing the vehicle models associated with this model.
   */
  public List<VehicleEntry> getVehicleEntries() {
    return entries;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (!(evt.getSource() instanceof VehicleEntry)) {
      return;
    }

    if (!isRelevantUpdate(evt.getPropertyName())) {
      return;
    }

    VehicleEntry entry = (VehicleEntry) evt.getSource();
    for (int index = 0; index < entries.size(); index++) {
      if (entry == entries.get(index)) {
        int myIndex = index;
        SwingUtilities.invokeLater(() -> fireTableRowsUpdated(myIndex, myIndex));
      }
    }
  }

  private String getVehicleState(VehicleEntry entry) {
    VehicleCommAdapter commAdapter = entry.getCommAdapter();
    if (commAdapter == null) {
      return Vehicle.State.UNKNOWN.name();
    }
    else {
      return commAdapter.getProcessModel().getVehicleState().name();
    }
  }

  private boolean isRelevantUpdate(String propertyName) {
    return Objects.equals(propertyName, VehicleEntry.Attribute.COMM_ADAPTER_FACTORY.name())
        || Objects.equals(propertyName, VehicleEntry.Attribute.COMM_ADAPTER.name())
        || Objects.equals(propertyName, VehicleProcessModel.Attribute.STATE.name())
        || Objects.equals(propertyName, VehicleProcessModel.Attribute.COMM_ADAPTER_ENABLED.name())
        || Objects.equals(propertyName, VehicleProcessModel.Attribute.POSITION.name());
  }
}
