/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.vehicles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.AttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.CallWrapper;
import org.opentcs.virtualvehicle.LoopbackCommunicationAdapterDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A model for displaying a list/table of vehicles in the kernel GUI.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class VehicleTableModel
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
      = ResourceBundle.getBundle("org/opentcs/kernelcontrolcenter/Bundle");
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    BUNDLE.getString("VehicleTableModel.Vehicle"),
    BUNDLE.getString("VehicleTableModel.State"),
    BUNDLE.getString("VehicleTableModel.Adapter"),
    BUNDLE.getString("VehicleTableModel.Enabled?"),
    BUNDLE.getString("VehicleTableModel.Position")
  };
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
    String.class,
    String.class,
    VehicleCommAdapterDescription.class,
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
  private final List<LocalVehicleEntry> entries = new ArrayList<>();
  /**
   * The identifier for the adapter column.
   */
  public static final String ADAPTER_COLUMN_IDENTIFIER = COLUMN_NAMES[ADAPTER_COLUMN];
  /**
   * The identifier for the position column.
   */
  public static final String POSITION_COLUMN_IDENTIFIER = COLUMN_NAMES[POSITION_COLUMN];
  /**
   * The vehicle service used for interactions.
   */
  private final VehicleService vehicleService;
  /**
   * The call wrapper to use for service calls.
   */
  private final CallWrapper callWrapper;

  /**
   * Creates a new instance.
   *
   * @param vehicleService The vehicle service used for interactions.
   * @param callWrapper The call wrapper to use for service calls.
   */
  public VehicleTableModel(VehicleService vehicleService,
                           CallWrapper callWrapper) {
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");
  }

  /**
   * Adds a new entry to this model.
   *
   * @param newEntry The new entry.
   */
  public void addData(LocalVehicleEntry newEntry) {
    entries.add(newEntry);
    fireTableRowsInserted(entries.size(), entries.size());
  }

  /**
   * Returns the vehicle entry at the given row.
   *
   * @param row The row.
   * @return The entry at the given row.
   */
  public LocalVehicleEntry getDataAt(int row) {
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
    LocalVehicleEntry entry = entries.get(rowIndex);
    switch (columnIndex) {
      case ADAPTER_COLUMN:
        break;
      case ENABLED_COLUMN:
        try {
          if ((boolean) aValue) {
            callWrapper.call(() -> vehicleService.enableCommAdapter(
                entry.getAttachmentInformation().getVehicleReference()));
          }
          else {
            callWrapper.call(() -> vehicleService.disableCommAdapter(
                entry.getAttachmentInformation().getVehicleReference()));
          }
        }
        catch (Exception ex) {
          LOG.warn("Error enabling/disabling comm adapter for {}", entry.getVehicleName(), ex);
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

    LocalVehicleEntry entry = entries.get(rowIndex);

    switch (columnIndex) {
      case VEHICLE_COLUMN:
        return entry.getVehicleName();
      case STATE_COLUMNN:
        return getVehicleState(entry);
      case ADAPTER_COLUMN:
        return entry.getAttachmentInformation().getAttachedCommAdapter();
      case ENABLED_COLUMN:
        return entry.getProcessModel().isCommAdapterEnabled();
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
        LocalVehicleEntry entry = entries.get(rowIndex);
        return entry.getAttachedCommAdapterDescription() instanceof LoopbackCommunicationAdapterDescription
            && entry.getProcessModel().isCommAdapterEnabled();
      default:
        return false;
    }
  }

  /**
   * Returns a list containing the vehicle models associated with this model.
   *
   * @return A list containing the vehicle models associated with this model.
   */
  public List<LocalVehicleEntry> getVehicleEntries() {
    return entries;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (!(evt.getSource() instanceof LocalVehicleEntry)) {
      return;
    }

    if (!isRelevantUpdate(evt)) {
      return;
    }

    LocalVehicleEntry entry = (LocalVehicleEntry) evt.getSource();
    for (int index = 0; index < entries.size(); index++) {
      if (entry == entries.get(index)) {
        int myIndex = index;
        SwingUtilities.invokeLater(() -> fireTableRowsUpdated(myIndex, myIndex));
      }
    }
  }

  private String getVehicleState(LocalVehicleEntry entry) {
    return entry.getProcessModel().getVehicleState().name();
  }

  private boolean isRelevantUpdate(PropertyChangeEvent evt) {
    if (Objects.equals(evt.getPropertyName(),
                       LocalVehicleEntry.Attribute.ATTACHMENT_INFORMATION.name())) {
      AttachmentInformation oldInfo = (AttachmentInformation) evt.getOldValue();
      AttachmentInformation newInfo = (AttachmentInformation) evt.getNewValue();
      return !oldInfo.getAttachedCommAdapter().equals(newInfo.getAttachedCommAdapter());
    }
    if (Objects.equals(evt.getPropertyName(),
                       LocalVehicleEntry.Attribute.PROCESS_MODEL.name())) {
      VehicleProcessModelTO oldTo = (VehicleProcessModelTO) evt.getOldValue();
      VehicleProcessModelTO newTo = (VehicleProcessModelTO) evt.getNewValue();
      return oldTo.isCommAdapterEnabled() != newTo.isCommAdapterEnabled()
          || oldTo.getVehicleState() != newTo.getVehicleState()
          || !Objects.equals(oldTo.getVehiclePosition(), newTo.getVehiclePosition());
    }
    return false;
  }
}
