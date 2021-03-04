/**
 * (c): IML, JHotDraw.
 *
 * Changed by IML to allow access to ResourceBundle.
 *
 *
 * @(#)BringToFrontAction.java
 *
 * Copyright (c) 2003-2008 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.application.action.draw;

import java.util.Collection;
import java.util.LinkedList;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * ToFrontAction.
 *
 * @author Werner Randelshofer
 */
public class BringToFrontAction
    extends AbstractSelectedAction {

  public final static String ID = "edit.bringToFront";

  /**
   * Creates a new instance.
   *
   * @param editor The drawing editor
   */
  public BringToFrontAction(DrawingEditor editor) {
    super(editor);
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureAction(this, ID, false);
    updateEnabledState();
  }

  @Override
  public void actionPerformed(java.awt.event.ActionEvent e) {
    final DrawingView view = getView();
    final LinkedList<Figure> figures = new LinkedList<>(view.getSelectedFigures());
    bringToFront(view, figures);
    fireUndoableEditHappened(new AbstractUndoableEdit() {
      @Override
      public String getPresentationName() {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
        return labels.getTextProperty(ID);
      }

      @Override
      public void redo()
          throws CannotRedoException {
        super.redo();
        BringToFrontAction.bringToFront(view, figures);
      }

      @Override
      public void undo()
          throws CannotUndoException {
        super.undo();
        SendToBackAction.sendToBack(view, figures);
      }
    });
  }

  public static void bringToFront(DrawingView view, Collection<Figure> figures) {
    Drawing drawing = view.getDrawing();

    for (Figure figure : drawing.sort(figures)) {
      drawing.bringToFront(figure);
    }
  }
}
