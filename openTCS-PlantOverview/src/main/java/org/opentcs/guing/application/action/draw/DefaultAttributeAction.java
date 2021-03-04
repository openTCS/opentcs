/**
 * (c): IML, JHotDraw.
 * 
 * Changed by IML to allow access to ResourceBundle.
 *
 * 
 * @(#)DefaultAttributeAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.application.action.draw;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.undo.CompositeEdit;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * DefaultAttributeAction. <p> XXX - should listen to changes in the default
 * attributes of its DrawingEditor.
 *
 * @author Werner Randelshofer
 */
public class DefaultAttributeAction
    extends AbstractSelectedAction {

  private AttributeKey[] keys;
  private Map<AttributeKey, Object> fixedAttributes;

  /**
   * Creates a new instance.
   * 
   * @param editor The drawing editor
   * @param key The attribute kez
   */
  public DefaultAttributeAction(
      DrawingEditor editor,
      AttributeKey key) {

    this(editor, key, null, null);
  }

  public DefaultAttributeAction(
      DrawingEditor editor,
      AttributeKey key,
      Map<AttributeKey, Object> fixedAttributes) {

    this(editor, new AttributeKey[] {key}, null, null, fixedAttributes);
  }

  public DefaultAttributeAction(
      DrawingEditor editor,
      AttributeKey[] keys) {

    this(editor, keys, null, null);
  }

  /**
   * Creates a new instance.
   */
  public DefaultAttributeAction(
      DrawingEditor editor,
      AttributeKey key,
      Icon icon) {

    this(editor, key, null, icon);
  }

  /**
   * Creates a new instance.
   */
  public DefaultAttributeAction(
      DrawingEditor editor,
      AttributeKey key,
      String name) {

    this(editor, key, name, null);
  }

  public DefaultAttributeAction(
      DrawingEditor editor,
      AttributeKey key,
      String name,
      Icon icon) {

    this(editor, new AttributeKey[] {key}, name, icon);
  }

  public DefaultAttributeAction(
      DrawingEditor editor,
      AttributeKey[] keys,
      String name,
      Icon icon) {

    this(editor, keys, name, icon, new HashMap<AttributeKey, Object>());
  }

  public DefaultAttributeAction(
      DrawingEditor editor,
      AttributeKey[] keys,
      String name,
      Icon icon,
      Map<AttributeKey, Object> fixedAttributes) {

    super(editor);
    this.keys = keys.clone();
    putValue(AbstractAction.NAME, name);
    putValue(AbstractAction.SMALL_ICON, icon);
    setEnabled(true);
    editor.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(DefaultAttributeAction.this.keys[0].getKey())) {
          putValue("attribute_" + DefaultAttributeAction.this.keys[0].getKey(), evt.getNewValue());
        }
      }
    });
    this.fixedAttributes = fixedAttributes;
    updateEnabledState();
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    if (getView() != null && getView().getSelectionCount() > 0) {
      ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
      CompositeEdit edit = new CompositeEdit(labels.getString("drawAttributeChange"));
      fireUndoableEditHappened(edit);
      changeAttribute();
      fireUndoableEditHappened(edit);
    }
  }

  @SuppressWarnings("unchecked")
  public void changeAttribute() {
    CompositeEdit edit = new CompositeEdit("attributes");
    fireUndoableEditHappened(edit);
    DrawingEditor editor = getEditor();

    for (Figure figure : getView().getSelectedFigures()) {
      figure.willChange();
      for (AttributeKey key : keys) {
        figure.set(key, editor.getDefaultAttribute(key));
      }

      for (Map.Entry<AttributeKey, Object> entry : fixedAttributes.entrySet()) {
        figure.set(entry.getKey(), entry.getValue());

      }

      figure.changed();
    }

    fireUndoableEditHappened(edit);
  }

  public void selectionChanged(FigureSelectionEvent evt) {
    //setEnabled(getView().getSelectionCount() > 0);
  }
}
