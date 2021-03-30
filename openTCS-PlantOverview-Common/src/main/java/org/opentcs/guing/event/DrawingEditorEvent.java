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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;

/**
 * Event object that a DrawingEditor fires when a Figure object was selected,
 * added or removed.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see DrawingEditorListener
 */
public class DrawingEditorEvent
    extends EventObject {

  /**
   * The affected Figure objects.
   */
  private final List<Figure> fFigures;

  /**
   * Creates a new instance.
   *
   * @param editor The event source.
   * @param figures The affected figures.
   */
  public DrawingEditorEvent(DrawingEditor editor, List<Figure> figures) {
    super(editor);
    fFigures = Objects.requireNonNull(figures);
  }

  /**
   * Creates a new instance for a single figure.
   *
   * @param editor The event source.
   * @param figure The affected figure.
   */
  public DrawingEditorEvent(DrawingEditor editor, Figure figure) {
    super(editor);
    fFigures = new LinkedList<>();
    fFigures.add(figure);
  }

  /**
   * Checks whether this event references at least one figure.
   *
   * @return <code>true</code> if, and only if, this event references at least
   * one figure.
   */
  public boolean hasFigure() {
    return !fFigures.isEmpty();
  }

  /**
   * Returns the originating DrawingEditor.
   *
   * @return The originating DrawingEditor.
   */
  public DrawingEditor getDrawingEditor() {
    return (DrawingEditor) getSource();
  }

  /**
   * Returns the affected Figure objects.
   *
   * @return The affected Figure objects.
   */
  public List<Figure> getFigures() {
    return fFigures;
  }

  /**
   * Returns the first affected figure.
   *
   * @return The first affected figure.
   */
  public Figure getFigure() {
    if (!fFigures.isEmpty()) {
      return fFigures.get(0);
    }
    else {
      return null;
    }
  }

  /**
   * Returns the number of affected figures.
   *
   * @return The number of affected figures.
   */
  public int getCount() {
    return fFigures.size();
  }
}
