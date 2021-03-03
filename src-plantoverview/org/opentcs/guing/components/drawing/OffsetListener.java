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
  private final OpenTCSDrawingView drawingView;

  /**
   * Creates a new instance.
   *
   * @param drawingView The drawing view to call for (re-)initialization of its
   * offset figures.
   */
  OffsetListener(final OpenTCSDrawingView drawingView) {
    this.drawingView = drawingView;
  }

  @Override
  public void componentResized(ComponentEvent e) {
    drawingView.initializeOffsetFigures();
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
