/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.event;

import java.util.EventObject;
import org.opentcs.guing.model.elements.BlockModel;

/**
 * An event that informs listener about changes in a block area.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class BlockChangeEvent
    extends EventObject {

  /**
   * Creates a new instance of BlockElementChangeEvent.
   *
   * @param block The <code>BlockModel</code> that has changed.
   */
  public BlockChangeEvent(BlockModel block) {
    super(block);
  }
}
