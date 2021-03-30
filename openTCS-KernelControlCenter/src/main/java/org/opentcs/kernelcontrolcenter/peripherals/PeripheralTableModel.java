/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.peripherals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;
import static org.opentcs.kernelcontrolcenter.I18nKernelControlCenter.BUNDLE_PATH;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.CallWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A model for displaying a table of peripherals.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralTableModel
    extends AbstractTableModel
    implements PropertyChangeListener {

  /**
   * This class's resource bundle.
   */
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * The index of the column showing the peripheral device's name.
   */
  public static final int COLUMN_LOCATION = 0;
  /**
   * The index of the column showing the associated adapter.
   */
  public static final int COLUMN_ADAPTER = 1;
  /**
   * The index of the column showing the adapter's enabled state.
   */
  public static final int COLUMN_ENABLED = 2;
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralTableModel.class);
  /**
   * The collumn names for the table header.
   */
  private static final String[] COLUMN_NAMES = new String[]{
    BUNDLE.getString("peripheralTableModel.column_location.headerText"),
    BUNDLE.getString("peripheralTableModel.column_adapter.headerText"),
    BUNDLE.getString("peripheralTableModel.column_enabled.headerText")
  };
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[]{
    String.class,
    PeripheralCommAdapterDescription.class,
    Boolean.class
  };
  /**
   * The service portal to use for kernel interaction.
   */
  private final KernelServicePortal servicePortal;
  /**
   * The call wrapper to use.
   */
  private final CallWrapper callWrapper;
  /**
   * The entries in the table.
   */
  private final List<LocalPeripheralEntry> entries = new ArrayList<>();

  public PeripheralTableModel(KernelServicePortal servicePortal,
                              CallWrapper callWrapper) {
    this.servicePortal = requireNonNull(servicePortal, "servicePortal");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");
  }

  public void addData(LocalPeripheralEntry entry) {
    entries.add(entry);
    fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
  }

  public LocalPeripheralEntry getDataAt(int index) {
    checkArgument(index >= 0 && index < entries.size(),
                  "index must be in 0..%d: %d",
                  entries.size(),
                  index);
    return entries.get(index);
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
    if (rowIndex >= entries.size()) {
      return null;
    }

    LocalPeripheralEntry entry = entries.get(rowIndex);

    switch (columnIndex) {
      case COLUMN_LOCATION:
        return entry.getProcessModel().getLocation().getName();
      case COLUMN_ADAPTER:
        return entry.getAttachedCommAdapter();
      case COLUMN_ENABLED:
        return entry.getProcessModel().isCommAdapterEnabled();
      default:
        throw new IllegalArgumentException("Invalid column index: " + columnIndex);
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    requireNonNull(aValue, "aValue");
    LocalPeripheralEntry entry = entries.get(rowIndex);
    switch (columnIndex) {
      case COLUMN_LOCATION:
        break;
      case COLUMN_ADAPTER:
        break;
      case COLUMN_ENABLED:
        try {
        if ((boolean) aValue) {
          callWrapper.call(
              () -> servicePortal.getPeripheralService().enableCommAdapter(entry.getLocation())
          );
        }
        else {
          callWrapper.call(
              () -> servicePortal.getPeripheralService().disableCommAdapter(entry.getLocation())
          );
        }
      }
      catch (Exception ex) {
        LOG.warn("Error enabling/disabling peripheral comm adapter for {}",
                 entry.getLocation().getName(),
                 ex);
      }
      break;
      default:
        throw new IllegalArgumentException("Invalid column index: " + columnIndex);
    }
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case COLUMN_LOCATION:
        return false;
      case COLUMN_ADAPTER:
        return false;
      case COLUMN_ENABLED:
        return true;
      default:
        return false;
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
  public void propertyChange(PropertyChangeEvent evt) {
    if (!isRelevantUpdate(evt)) {
      return;
    }

    LocalPeripheralEntry entry = (LocalPeripheralEntry) evt.getSource();
    int index = entries.indexOf(entry);
    SwingUtilities.invokeLater(() -> fireTableRowsUpdated(index, index));

  }

  private boolean isRelevantUpdate(PropertyChangeEvent evt) {
    if (!(evt.getSource() instanceof LocalPeripheralEntry)) {
      return false;
    }

    if (Objects.equals(evt.getPropertyName(),
                       LocalPeripheralEntry.Attribute.ATTACHED_COMM_ADAPTER.name())) {
      PeripheralAttachmentInformation oldInfo = (PeripheralAttachmentInformation) evt.getOldValue();
      PeripheralAttachmentInformation newInfo = (PeripheralAttachmentInformation) evt.getNewValue();
      return !oldInfo.getAttachedCommAdapter().equals(newInfo.getAttachedCommAdapter());
    }
    if (Objects.equals(evt.getPropertyName(),
                       LocalPeripheralEntry.Attribute.PROCESS_MODEL.name())) {
      PeripheralProcessModel oldTo = (PeripheralProcessModel) evt.getOldValue();
      PeripheralProcessModel newTo = (PeripheralProcessModel) evt.getNewValue();
      return oldTo.isCommAdapterEnabled() != newTo.isCommAdapterEnabled();
    }

    return false;
  }
}
