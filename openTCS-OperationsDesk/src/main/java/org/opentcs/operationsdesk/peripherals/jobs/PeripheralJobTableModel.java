/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.peripherals.jobs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import static org.opentcs.util.Assertions.checkArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A table model for peripheral jobs.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
class PeripheralJobTableModel
    extends AbstractTableModel
    implements PeripheralJobsContainerListener {

  private static final Logger LOG = LoggerFactory.getLogger(PeripheralJobTableModel.class);
  /**
   * The resource bundle to use.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle(I18nPlantOverviewOperating.PERIPHERALJOB_PATH);

  public static final int COLUMN_NAME = 0;
  public static final int COLUMN_LOCATION = 1;
  public static final int COLUMN_OPERATION = 2;
  public static final int COLUMN_RELATED_VEHICLE = 3;
  public static final int COLUMN_RELATED_ORDER = 4;
  public static final int COLUMN_STATE = 5;
  public static final int COLUMN_CREATION_TIME = 6;
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[]{
    BUNDLE.getString("peripheralJobTableModel.column_name.headerText"),
    BUNDLE.getString("peripheralJobTableModel.column_location.headerText"),
    BUNDLE.getString("peripheralJobTableModel.column_operation.headerText"),
    BUNDLE.getString("peripheralJobTableModel.column_relatedVehicle.headerText"),
    BUNDLE.getString("peripheralJobTableModel.column_relatedTransportOrder.headerText"),
    BUNDLE.getString("peripheralJobTableModel.column_state.headerText"),
    BUNDLE.getString("peripheralJobTableModel.column_creationTime.headerText")
  };
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[]{
    String.class,
    String.class,
    String.class,
    TCSObjectReference.class,
    TCSObjectReference.class,
    String.class,
    Instant.class
  };
  /**
   * The entries in the table.
   */
  private final List<PeripheralJob> entries = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public PeripheralJobTableModel() {
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
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= getRowCount()) {
      return null;
    }

    PeripheralJob entry = entries.get(rowIndex);
    switch (columnIndex) {
      case COLUMN_NAME:
        return entry.getName();
      case COLUMN_LOCATION:
        return entry.getPeripheralOperation().getLocation().getName();
      case COLUMN_OPERATION:
        return entry.getPeripheralOperation().getOperation();
      case COLUMN_RELATED_VEHICLE:
        return entry.getRelatedVehicle();
      case COLUMN_RELATED_ORDER:
        return entry.getRelatedTransportOrder();
      case COLUMN_STATE:
        return entry.getState().name();
      case COLUMN_CREATION_TIME:
        return entry.getCreationTime();
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
  public void containerInitialized(Collection<PeripheralJob> jobs) {
    requireNonNull(jobs, "jobs");

    SwingUtilities.invokeLater(() -> {
      // Notifiations of any change listeners must happen at the same time/in the same thread the 
      // data behind the model is updated. Otherwise, there is a risk that listeners work with/
      // refer to outdated data, which can lead to runtime exceptions.
      entries.clear();
      entries.addAll(jobs);
      fireTableDataChanged();
    });
  }

  @Override
  public void peripheralJobAdded(PeripheralJob job) {
    requireNonNull(job, "job");

    SwingUtilities.invokeLater(() -> {
      entries.add(job);
      fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
    });
  }

  @Override
  public void peripheralJobUpdated(PeripheralJob job) {
    requireNonNull(job, "job");

    SwingUtilities.invokeLater(() -> {
      int jobIndex = entries.indexOf(job);
      if (jobIndex == -1) {
        LOG.warn("Unknown job: {}. Ignoring job update.", job.getName());
        return;
      }

      entries.set(jobIndex, job);
      fireTableRowsUpdated(jobIndex, jobIndex);
    });
  }

  @Override
  public void peripheralJobRemoved(PeripheralJob job) {
    requireNonNull(job, "job");

    SwingUtilities.invokeLater(() -> {
      int jobIndex = entries.indexOf(job);
      if (jobIndex == -1) {
        LOG.warn("Unknown job: {}. Ignoring job removal.", job.getName());
        return;
      }

      entries.remove(jobIndex);
      fireTableRowsDeleted(jobIndex, jobIndex);
    });
  }

  public PeripheralJob getEntryAt(int index) {
    checkArgument(index >= 0 && index < entries.size(),
                  "index must be in 0..%d: %d",
                  entries.size(),
                  index);
    return entries.get(index);
  }
}
