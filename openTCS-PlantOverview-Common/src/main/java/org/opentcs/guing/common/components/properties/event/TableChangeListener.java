/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.event;

/** 
 * A listener that listens for changes on a table.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface TableChangeListener
    extends java.util.EventListener {

  /**
   * Indicates that a line in the table has been selected.
   *
   * @param event The event.
   */
  void tableSelectionChanged(TableSelectionChangeEvent event);

  /**
   * Indicates that changes in the table have occured.
   */
  void tableModelChanged();
}
