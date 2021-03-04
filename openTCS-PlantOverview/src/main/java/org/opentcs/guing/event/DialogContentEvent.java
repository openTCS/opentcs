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

/**
 * An event that is sent from dialog content to the parent dialogs. The content
 * can force its parent to repaint or close.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class DialogContentEvent
    extends EventObject {

  /**
   * Creates a new instance of DialogContentEvent.
   *
   * @param dialogContent The dialog content.
   */
  public DialogContentEvent(Object dialogContent) {
    super(dialogContent);
  }
}
