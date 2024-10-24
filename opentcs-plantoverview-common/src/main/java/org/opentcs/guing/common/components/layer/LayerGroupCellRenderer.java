// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.layer;

import javax.swing.table.DefaultTableCellRenderer;
import org.opentcs.data.model.visualization.LayerGroup;

/**
 * A table cell renderer for {@link LayerGroup}s.
 */
public class LayerGroupCellRenderer
    extends
      DefaultTableCellRenderer {

  /**
   * Creates a new instance.
   */
  public LayerGroupCellRenderer() {
  }

  @Override
  protected void setValue(Object value) {
    setText(((LayerGroup) value).getName());
  }
}
