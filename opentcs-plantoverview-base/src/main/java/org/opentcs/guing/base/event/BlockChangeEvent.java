// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.event;

import java.util.EventObject;
import org.opentcs.guing.base.model.elements.BlockModel;

/**
 * An event that informs listener about changes in a block area.
 */
public class BlockChangeEvent
    extends
      EventObject {

  /**
   * Creates a new instance of BlockElementChangeEvent.
   *
   * @param block The <code>BlockModel</code> that has changed.
   */
  public BlockChangeEvent(BlockModel block) {
    super(block);
  }
}
