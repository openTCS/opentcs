/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.event;

import org.opentcs.guing.common.components.drawing.OpenTCSDrawingEditor;

/**
 * An interface that has to be implemented by the application to receive
 * events from the <code>DrawingEditor</code>. Events are: figure added,
 * figure removed and figure selected.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see DrawingEditorEvent
 */
public interface DrawingEditorListener {

  /**
   * Gets called when a figure is added in the {@link OpenTCSDrawingEditor}
   * by an action of the user.
   *
   * @param e Event for when a figure is added to the OpenTCSDrawingEditor.
   */
  void figureAdded(DrawingEditorEvent e);

  /**
   * Gets called when a figure is removed by the user in the
   * <code>OpenTCSDrawingEditor</code>.
   *
   * @param e The fired event.
   */
  void figureRemoved(DrawingEditorEvent e);

  /**
   * Gets called when a figure was selected in the {@link OpenTCSDrawingEditor}.
   *
   * @param e The fired event.
   */
  void figureSelected(DrawingEditorEvent e);
}
