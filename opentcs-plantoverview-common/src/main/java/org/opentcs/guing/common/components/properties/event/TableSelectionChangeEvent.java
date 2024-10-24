// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.properties.event;

import java.util.EventObject;
import org.opentcs.guing.common.components.properties.table.AttributesTable;

/**
 * An Event emitted when a line in a table is selected.
 */
public class TableSelectionChangeEvent
    extends
      EventObject {

  /**
   * The attribute.
   */
  protected Object fSelectedValue;

  /**
   * Creates a new instance of TableSelectionChangeEvent
   */
  public TableSelectionChangeEvent(AttributesTable table, Object selectedValue) {
    super(table);
    fSelectedValue = selectedValue;
  }

  /**
   * Returns the attribute contained in the selected line.
   */
  public Object getSelectedValue() {
    return fSelectedValue;
  }
}
