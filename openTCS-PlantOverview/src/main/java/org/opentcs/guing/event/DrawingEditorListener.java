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

import org.opentcs.guing.model.ModelComponent;

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
   * Listener-Methode, die aufgerufen wird, wenn ein Figure-Objekt im
   * OpenTCSDrawingEditor durch eine Aktion des Benutzers hinzugefügt wurde.
   * Gets called when a figure is added in the <code>OpenTCSDrawingEditor</code>
   * by an action of the user.
   *
   * TODO: Kommentar aktualisieren! HH 2014-02-27
   * Hier können verschiedene Konstellationen auftreten:
   * 1. das hinzugefügte Figure hat bereits einen ModelSwitcher
   * (das ist bei einem Paste-Befehl der Fall)
   * 2. das hinzugefügte Figure hat nur ein Model-Prototyp (Normalfall)
   * 3. das hinzugefügte Figure hat gar nichts (z.B. TextFigure, OriginFigure)
   *
   * @param e
   * @return
   */
  ModelComponent figureAdded(DrawingEditorEvent e);

  /**
   * Gets called when a figure is removed by the user in the
   * <code>OpenTCSDrawingEditor</code>.
   *
   * @param e The fired event.
   * @return The affected <code>ModelComponent</code>.
   */
  ModelComponent figureRemoved(DrawingEditorEvent e);

  /**
   * Nachricht, dass im DrawingEditor ein Figure-Objekt selektiert wurde.
   * Gets called when a figure was selected in the
   * <code>OpenTCSDrawingEditor</code>.
   *
   * @param e The fired event.
   */
  void figureSelected(DrawingEditorEvent e);
}
