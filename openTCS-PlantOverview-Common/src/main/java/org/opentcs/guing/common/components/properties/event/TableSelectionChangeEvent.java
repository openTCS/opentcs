/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.event;

import java.util.EventObject;
import org.opentcs.guing.common.components.properties.table.AttributesTable;

/**
 * An Event emitted when a line in a table is selected.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class TableSelectionChangeEvent
    extends EventObject {

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
