/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.layer;

import javax.swing.table.DefaultTableCellRenderer;
import org.opentcs.data.model.visualization.LayerGroup;

/**
 * A table cell renderer for {@link LayerGroup}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayerGroupCellRenderer
    extends DefaultTableCellRenderer {

  @Override
  protected void setValue(Object value) {
    setText(((LayerGroup) value).getName());
  }
}
