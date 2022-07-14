/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.transport.sequences;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class OrderSequenceTableModel
    extends AbstractTableModel
    implements OrderSequenceContainerListener {

  private static final Logger LOG = LoggerFactory.getLogger(OrderSequenceTableModel.class);

  /**
   * The resource bundle to use.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle(I18nPlantOverviewOperating.TO_SEQUENCE_PATH);

  private static final int COLUMN_NAME = 0;
  private static final int COLUMN_INTENDED_VEHICLE = 1;
  private static final int COLUMN_EXECUTING_VEHICLE = 2;
  private static final int COLUMN_INDEX = 3;
  private static final int COLUMN_COMPLETED = 4;
  private static final int COLUMN_FINISHED = 5;
  private static final int COLUMN_FAILURE = 6;

  private static final String[] COLUMN_NAMES = {
    "Name",
    BUNDLE.getString("orderSequenceTableModel.column_intendedVehicle.headerText"),
    BUNDLE.getString("orderSequenceTableModel.column_executingVehicle.headerText"),
    "Index",
    BUNDLE.getString("orderSequenceTableModel.column_complete.headerText"),
    BUNDLE.getString("orderSequenceTableModel.column_finished.headerText"),
    BUNDLE.getString("orderSequenceTableModel.column_failureFatal.headerText")
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
    String.class
  };

  private final List<OrderSequence> entries = new LinkedList<>();

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
    if (rowIndex < 0 || rowIndex >= entries.size()) {
      return null;
    }

    OrderSequence entry = entries.get(rowIndex);
    switch (columnIndex) {
      case COLUMN_NAME:
        return entry.getName();

      case COLUMN_INTENDED_VEHICLE:
        if (entry.getIntendedVehicle() != null) {
          return entry.getIntendedVehicle().getName();
        }
        else {
          return BUNDLE.getString("orderSequenceTableModel.column_intendedVehicle.determinedAutomatic.text");
        }
      case COLUMN_EXECUTING_VEHICLE:

        if (entry.getProcessingVehicle() != null) {
          return entry.getProcessingVehicle().getName();
        }
        else {
          return BUNDLE.getString("orderSequenceTableModel.column_intendedVehicle.determinedAutomatic.text");
        }
      case COLUMN_INDEX:
        return entry.getFinishedIndex();
      case COLUMN_COMPLETED:
        return entry.isComplete();
      case COLUMN_FINISHED:
        return entry.isFinished();
      case COLUMN_FAILURE:
        return entry.isFailureFatal();
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
  public void containerInitialized(Collection<OrderSequence> sequences) {
    requireNonNull(sequences, "sequences");

    SwingUtilities.invokeLater(() -> {
      // Notifiations of any change listeners must happen at the same time/in the same thread the 
      // data behind the model is updated. Otherwise, there is a risk that listeners work with/
      // refer to outdated data, which can lead to runtime exceptions.
      entries.clear();
      entries.addAll(sequences);
      fireTableDataChanged();
    });
  }

  @Override
  public void orderSequenceAdded(OrderSequence sequence) {
    requireNonNull(sequence, "sequence");

    SwingUtilities.invokeLater(() -> {
      entries.add(sequence);
      fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
    });
  }

  @Override
  public void orderSequenceUpdated(OrderSequence sequence) {
    requireNonNull(sequence, "sequence");

    SwingUtilities.invokeLater(() -> {
      int sequenceIndex = entries.indexOf(sequence);
      if (sequenceIndex == -1) {
        LOG.warn("Unknown order sequence: {}. Ignoring order sequence update.", sequence.getName());
        return;
      }
      entries.set(sequenceIndex, sequence);
      fireTableRowsUpdated(sequenceIndex, sequenceIndex);
    });
  }

  @Override
  public void orderSequenceRemoved(OrderSequence sequence) {
    requireNonNull(sequence, "sequence");
    SwingUtilities.invokeLater(() -> {
      int sequenceIndex = entries.indexOf(sequence);
      if (sequenceIndex == -1) {
        LOG.warn("Unknown order sequence: {}. Ignoring order sequence removal.", sequence.getName());
        return;
      }
      entries.remove(sequenceIndex);
      fireTableRowsDeleted(sequenceIndex, sequenceIndex);
    });
  }

  /**
   * Returns the order sequence at the specified index.
   *
   * @param index the index to return.
   * @return the order sequence at that index.
   */
  public OrderSequence getEntryAt(int index) {
    if (index < 0 || index >= entries.size()) {
      return null;
    }

    return entries.get(index);
  }
}
