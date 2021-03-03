/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter.vehicles;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.table.AbstractTableModel;
import org.opentcs.drivers.Message;

/**
 * A <code>DefaultTableModel</code> extended for holding {@link
 * org.opentcs.drivers.Message Message} instances in rows.
 *
 * @author Iryna Felko (Fraunhofer IML)
 */
final class LogTableModel
    extends AbstractTableModel {

  /**
   * The instance to filter errors.
   */
  public static final ErrorsFilter errorsFilter = new ErrorsFilter();
  /**
   * The instance to filter errors and warnings.
   */
  public static final ErrorsAndWarningsFilter errorsAndWarningsFilter =
      new ErrorsAndWarningsFilter();
  /**
   * The instance to filter all types of messages.
   */
  public static final NoFilter noFilter = new NoFilter();
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/vehicles/Bundle").
    getString("TimeStamp"),
    ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/vehicles/Bundle").
    getString("Message")
  };
  /**
   * The column classes.
   */
  private static final Class[] COLUMN_CLASSES = new Class[] {
    String.class,
    String.class
  };
  /**
   * The buffer for holding the model data.
   */
  private final Set<Message> values =
      new TreeSet<>(Message.youngestToEldestComparator);
  /**
   * The actual data displayed in the table.
   */
  private List<Message> actualValues = new ArrayList<>();
  /**
   * A <code>DateFormat</code> instance for formatting message's time stamps.
   */
  private final DateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

  /**
   * Creates a new instance of LogTableModel.
   */
  public LogTableModel() {
    super();
  }

  @Override
  public Object getValueAt(int row, int column) {
    if (row < 0 || row >= actualValues.size()) {
      return null;
    }
    switch (column) {
      case 0:
        return dateFormat.format(new Date(actualValues.get(row).getTimestamp()));
      case 1:
        return actualValues.get(row).getMessage();
      default:
        return new IllegalArgumentException("Column out of bounds.");
    }
  }

  /**
   * Adds a row to the model, containing the given {@link
   * org.opentcs.drivers.Message Message} in every cell.
   *
   * @param message The message to be added to every cell in a new row.
   */
  public void addRow(Message message) {
    if (message == null) {
      throw new NullPointerException("Message is null!");
    }
    values.add(message);
    actualValues.add(message);
    fireTableDataChanged();
  }

  /**
   * Removes the given message from the internal data set
   * and from the current data model.
   * @param message The message to be removed.
   */
  public void removeRow(Message message) {
    if (values.contains(message)) {
      values.remove(message);
      actualValues.remove(message);
      fireTableDataChanged();
    }
  }
  
  /**
   * Removes all messages from the model.
   */
  public void clear() {
    if (!values.isEmpty()) {
      values.clear();
      actualValues.clear();
      fireTableDataChanged();
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }
  
  /**
   * Returns the message object representing the indexed row.
   *
   * @param row The row for which to fetch the message object.
   * @return The message object representing the indexed row.
   */
  public Message getRow(int row) {
    return actualValues.get(row);
  }

  /**
   * Filters the messages and shows only errors and warnings.
   * @param filter The <code>MessageFilter</code>filter, which rules
   * must be used performing the message filtering.
   */
  public void filterMessages(MessageFilter filter) {
    Set<Message> filteredSet = new TreeSet<>(Message.youngestToEldestComparator);
    for (Message i : values) {
      if (filter.accept(i)) {
        filteredSet.add(i);
      }
    }
    actualValues = new ArrayList<>(filteredSet);
    fireTableDataChanged();
  }

  /**
   * Sorts the table data by its date.
   */
  public void sortByDate() {
    Set<Message> sortedSet = new TreeSet<>(Message.youngestToEldestComparator);
    for (Message i : values) {
      sortedSet.add(i);
    }
    actualValues = new ArrayList<>(sortedSet);
    fireTableDataChanged();
  }

  /**
   * Sorts the table data by its type.
   */
  public void sortByType() {
    Set<Message> sortedSet = new TreeSet<>(Message.typeComparator);
    for (Message i : values) {
      sortedSet.add(i);
    }
    actualValues = new ArrayList<>(sortedSet);
    fireTableDataChanged();
  }

  @Override
  public int getRowCount() {
    return actualValues.size();
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
      return "ERROR";
    }
  }

  /**
   * The filter class for error typed messages.
   */
  private static final class ErrorsFilter
      implements MessageFilter {
    
    /**
     * Creates a new instance.
     */
    private ErrorsFilter() {
      // Do nada.
    }

    @Override
    public boolean accept(Message message) {
      if (message == null) {
        throw new IllegalArgumentException("The message is null!");
      }
      return message.getType().equals(Message.Type.ERROR);
    }
  }

  /**
   * The filter class for error- and warning-messages.
   */
  private static final class ErrorsAndWarningsFilter
      implements MessageFilter {
    
    /**
     * Creates a new instance.
     */
    private ErrorsAndWarningsFilter() {
      // Do nada.
    }

    @Override
    public boolean accept(Message message) {
      if (message == null) {
        throw new IllegalArgumentException("The message is null!");
      }
      return message.getType().equals(Message.Type.ERROR)
          || message.getType().equals(Message.Type.WARNING);
    }
  }

  /**
   * The filter class for all message types.
   */
  private static final class NoFilter
      implements MessageFilter {
    
    /**
     * Creates a new instance.
     */
    private NoFilter() {
      // Do nada.
    }

    @Override
    public boolean accept(Message message) {
      if (message == null) {
        throw new IllegalArgumentException("The message is null!");
      }
      return message.getType().equals(Message.Type.ERROR)
          || message.getType().equals(Message.Type.WARNING)
          || message.getType().equals(Message.Type.INFO);
    }
  }
}
