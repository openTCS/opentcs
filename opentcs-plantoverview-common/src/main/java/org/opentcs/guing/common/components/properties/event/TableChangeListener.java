// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.properties.event;

/**
 * A listener that listens for changes on a table.
 */
public interface TableChangeListener
    extends
      java.util.EventListener {

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
