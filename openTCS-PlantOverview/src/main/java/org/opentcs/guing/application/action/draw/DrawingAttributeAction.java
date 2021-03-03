/**
 * (c): IML, JHotDraw.
 *
 * Changed by IML to allow access to ResourceBundle.
 *
 *
 * @(#)DrawingAttributeAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.application.action.draw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import org.jhotdraw.app.action.ActionUtil;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractDrawingViewAction;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * AttributeAction.
 *
 * @author Werner Randelshofer
 */
public class DrawingAttributeAction
    extends AbstractDrawingViewAction {

  protected Map<AttributeKey, Object> attributes;

  /**
   * Creates a new instance.
   */
  /**
   * Creates a new instance.
   */
  public <T> DrawingAttributeAction(
      DrawingEditor editor,
      AttributeKey<T> key,
      T value) {

    this(editor, key, value, null, null);
  }

  /**
   * Creates a new instance.
   */
  public <T> DrawingAttributeAction(
      DrawingEditor editor,
      AttributeKey<T> key,
      T value,
      Icon icon) {

    this(editor, key, value, null, icon);
  }

  /**
   * Creates a new instance.
   */
  public <T> DrawingAttributeAction(
      DrawingEditor editor,
      AttributeKey<T> key,
      T value,
      String name) {

    this(editor, key, value, name, null);
  }

  public <T> DrawingAttributeAction(
      DrawingEditor editor,
      AttributeKey<T> key,
      T value,
      String name,
      Icon icon) {

    this(editor, key, value, name, icon, null);
  }

  public <T> DrawingAttributeAction(
      DrawingEditor editor,
      AttributeKey<T> key,
      T value,
      String name,
      Icon icon,
      Action compatibleTextAction) {

    super(editor);
    this.attributes = new HashMap<>();
    attributes.put(key, value);

    putValue(AbstractAction.NAME, name);
    putValue(AbstractAction.SMALL_ICON, icon);
    setEnabled(true);
  }

  public DrawingAttributeAction(
      DrawingEditor editor,
      Map<AttributeKey, Object> attributes,
      String name,
      Icon icon) {

    super(editor);
    this.attributes = attributes;

    putValue(AbstractAction.NAME, name);
    putValue(AbstractAction.SMALL_ICON, icon);
    updateEnabledState();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void actionPerformed(java.awt.event.ActionEvent evt) {
    final ArrayList<Object> restoreData = new ArrayList<>();
    final Figure drawing = getView().getDrawing();
    restoreData.add(drawing.getAttributesRestoreData());
    drawing.willChange();

    for (Map.Entry<AttributeKey, Object> entry : attributes.entrySet()) {
      drawing.set(entry.getKey(), entry.getValue());
    }

    drawing.changed();

    UndoableEdit edit = new AbstractUndoableEdit() {

      @Override
      public String getPresentationName() {
        String name = (String) getValue(ActionUtil.UNDO_PRESENTATION_NAME_KEY);

        if (name == null) {
          name = (String) getValue(AbstractAction.NAME);
        }

        if (name == null) {
          ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
          name = labels.getString("attribute.text");
        }

        return name;
      }

      @Override
      public void undo() {
        super.undo();
        Iterator<Object> iRestore = restoreData.iterator();

        drawing.willChange();
        drawing.restoreAttributesTo(iRestore.next());
        drawing.changed();
      }

      @Override
      @SuppressWarnings("unchecked")
      public void redo() {
        super.redo();
        //restoreData.add(drawing.getAttributesRestoreData());
        drawing.willChange();

        for (Map.Entry<AttributeKey, Object> entry : attributes.entrySet()) {
          drawing.set(entry.getKey(), entry.getValue());
        }

        drawing.changed();
      }
    };

    fireUndoableEditHappened(edit);
  }
}
