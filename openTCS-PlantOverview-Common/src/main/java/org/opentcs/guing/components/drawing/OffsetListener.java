/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Triggers a (re-)initialization of the view's offset figures when notified
 * about resize events.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class OffsetListener
    implements ComponentListener {

  /**
   * The drawing view we're working with.
   */
  private final OpenTCSDrawingEditor drawingEditor;
  /**
   * Initiales the offset figures once the view is resized (normally
   * done when the window becomes visible). But the listener shouldn't
   * listen to further resizing events. 
   * XXX get rid of this listener?
   */
  private boolean initialized = false;

  /**
   * Creates a new instance.
   *
   * @param drawingEditor The drawing editor to call for (re-)initialization of its
   * offset figures.
   */
  OffsetListener(OpenTCSDrawingEditor drawingEditor) {
    this.drawingEditor = drawingEditor;
  }

  @Override
  public void componentResized(ComponentEvent e) {
    if (!initialized) {
      drawingEditor.initializeViewport();
      initialized = true;
    }
  }

  @Override
  public void componentMoved(ComponentEvent e) {
  }

  @Override
  public void componentShown(ComponentEvent e) {
  }

  @Override
  public void componentHidden(ComponentEvent e) {
  }
}
