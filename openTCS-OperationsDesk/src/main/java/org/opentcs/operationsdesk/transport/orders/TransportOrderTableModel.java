/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.transport.orders;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A table model for transport orders.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class TransportOrderTableModel
    extends AbstractTableModel
    implements TransportOrderContainerListener {

  private static final Logger LOG = LoggerFactory.getLogger(TransportOrderTableModel.class);
  /**
   * The resource bundle to use.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle(I18nPlantOverviewOperating.TRANSPORTORDER_PATH);

  public static final int COLUMN_NAME = 0;
  public static final int COLUMN_SOURCE = 1;
  public static final int COLUMN_DESTINATION = 2;
  public static final int COLUMN_INTENDED_VEHICLE = 3;
  public static final int COLUMN_EXECUTING_VEHICLE = 4;
  public static final int COLUMN_STATUS = 5;
  public static final int COLUMN_ORDER_SEQUENCE = 6;
  public static final int COLUMN_CREATION_TIME = 7;

  private static final String[] COLUMN_NAMES = new String[]{
    "Name",
    BUNDLE.getString("transportOrderTableModel.column_source.headerText"),
    BUNDLE.getString("transportOrderTableModel.column_destination.headerText"),
    BUNDLE.getString("transportOrderTableModel.column_intendedVehicle.headerText"),
    BUNDLE.getString("transportOrderTableModel.column_executingVehicle.headerText"),
    "Status",
    BUNDLE.getString("transportOrderTableModel.column_orderSequence.headerText"),
    BUNDLE.getString("transportOrderTableModel.column_creationTime.headerText")
  };

  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[]{
    String.class,
    String.class,
    String.class,
    String.class,
    String.class,
    String.class,
    String.class,
    Instant.class
  };

  private final List<TransportOrder> entries = new LinkedList<>();

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

    TransportOrder entry = entries.get(rowIndex);
    Vector<DriveOrder> driveOrders = new Vector<>(entry.getAllDriveOrders());
    switch (columnIndex) {
      case COLUMN_NAME:
        return entry.getName();
      case COLUMN_SOURCE:
        if (driveOrders.size() == 1) {
          return "";
        }
        else {
          return driveOrders.firstElement().getDestination().getDestination().getName();
        }
      case COLUMN_DESTINATION:
        return driveOrders.lastElement().getDestination().getDestination().getName();
      case COLUMN_INTENDED_VEHICLE:
        if (entry.getIntendedVehicle() != null) {
          return entry.getIntendedVehicle().getName();
        }
        else {
          return BUNDLE.getString("transportOrderTableModel.column_intendedVehicle.determinedAutomatic.text");
        }
      case COLUMN_EXECUTING_VEHICLE:
        if (entry.getProcessingVehicle() != null) {
          return entry.getProcessingVehicle().getName();
        }
        else {
          return "?";
        }
      case COLUMN_STATUS:
        return entry.getState().toString();
      case COLUMN_ORDER_SEQUENCE:
        if (entry.getWrappingSequence() != null) {
          return entry.getWrappingSequence().getName();
        }
        else {
          return "-";
        }
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
  public void containerInitialized(Collection<TransportOrder> orders) {
    requireNonNull(orders, "orders");

    SwingUtilities.invokeLater(() -> {
      // Notifiations of any change listeners must happen at the same time/in the same thread the 
      // data behind the model is updated. Otherwise, there is a risk that listeners work with/
      // refer to outdated data, which can lead to runtime exceptions.
      entries.clear();
      entries.addAll(orders);
      fireTableDataChanged();
    });
  }

  @Override
  public void transportOrderAdded(TransportOrder order) {
    requireNonNull(order, "order");

    SwingUtilities.invokeLater(() -> {
      entries.add(order);
      fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
    });
  }

  @Override
  public void transportOrderUpdated(TransportOrder order) {
    requireNonNull(order, "order");

    SwingUtilities.invokeLater(() -> {
      int orderIndex = entries.indexOf(order);
      if (orderIndex == -1) {
        LOG.warn("Unknown transport order: {}. Ignoring order update.", order.getName());
        return;
      }
      entries.set(orderIndex, order);
      fireTableRowsUpdated(orderIndex, orderIndex);
    });
  }

  @Override
  public void transportOrderRemoved(TransportOrder order) {
    requireNonNull(order, "order");

    SwingUtilities.invokeLater(() -> {
      int orderIndex = entries.indexOf(order);
      if (orderIndex == -1) {
        LOG.warn("Unknown transport order: {}. Ignoring order removal.", order.getName());
        return;
      }
      entries.remove(orderIndex);
      fireTableRowsDeleted(orderIndex, orderIndex);
    });

  }

  /**
   * Returns the transport order at the specified index.
   *
   * @param index the index to return.
   * @return the transport order at that index.
   */
  public TransportOrder getEntryAt(int index) {
    if (index < 0 || index >= entries.size()) {
      return null;
    }

    return entries.get(index);
  }
}
