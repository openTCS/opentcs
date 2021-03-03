/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dockable;

import bibliothek.gui.dock.common.event.CFocusListener;
import bibliothek.gui.dock.common.intern.CDockable;
import java.awt.event.FocusEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.draw.DrawingEditor;
import org.opentcs.guing.application.ViewManager;
import org.opentcs.guing.components.drawing.DrawingViewScrollPane;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;

/**
 * Handles focussing of dockable drawing views.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DrawingViewFocusHandler
    implements CFocusListener {

  /**
   * Manages the application's views.
   */
  private final ViewManager viewManager;
  /**
   * The drawing editor.
   */
  private final DrawingEditor drawingEditor;

  /**
   * Creates a new instance.
   *
   * @param viewManager Manages the application's views.
   * @param drawingEditor The drawing editor.
   */
  @Inject
  public DrawingViewFocusHandler(ViewManager viewManager,
                              DrawingEditor drawingEditor) {
    this.viewManager = requireNonNull(viewManager, "viewManager");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
  }

  @Override
  public void focusGained(CDockable dockable) {
    DrawingViewScrollPane scrollPane = viewManager.getDrawingViewMap().get(dockable);
    if (scrollPane == null) {
      return;
    }
    OpenTCSDrawingView drawView = scrollPane.getDrawingView();
    drawingEditor.setActiveView(drawView);
    // XXX Looks suspicious: Why are the same values set again here?
    drawView.setConstrainerVisible(drawView.isConstrainerVisible());
    drawView.setLabelsVisible(drawView.isLabelsVisible());
    scrollPane.setRulersVisible(scrollPane.isRulersVisible());
    drawView.setStaticRoutesVisible(drawView.isStaticRoutesVisible());
    drawView.setBlocksVisible(drawView.isBlocksVisible());
    drawView.dispatchEvent(new FocusEvent(scrollPane, FocusEvent.FOCUS_GAINED));
  }

  @Override
  public void focusLost(CDockable dockable) {
  }
}
