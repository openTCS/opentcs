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
 * Interface for dialogs that want to react on messages of its content.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface DialogContentListener {

  /**
   * Message of the content that it needs repainting.
   *
   * @param evt The fired event.
   */
  void requestLayoutUpdate(DialogContentEvent evt);

  /**
   * Message of the content that it is ready to be closed.
   *
   * @param evt The fired event.
   */
  void requestClose(DialogContentEvent evt);
}
