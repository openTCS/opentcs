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

/**
 * Interface for listener that want to be informed wenn a block area has
 * changed.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface BlockChangeListener {

  /**
   * Message that the course elements have changed.
   *
   * @param e The fire event.
   */
  void courseElementsChanged(BlockChangeEvent e);

  /**
   * Message that the color of the block area has changed.
   *
   * @param e The fire event.
   */
  void colorChanged(BlockChangeEvent e);

  /**
   * Message that a block area was removed.
   *
   * @param e The fire event.
   */
  void blockRemoved(BlockChangeEvent e);
}
