/**
 * (c): IML, JHotDraw.
 *
 * Changed by IML to allow access to ResourceBundle.
 *
 *
 * @(#)ApplyAttributesAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.application.action.draw;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import org.jhotdraw.draw.AttributeKey;
import static org.jhotdraw.draw.AttributeKeys.TEXT;
import static org.jhotdraw.draw.AttributeKeys.TRANSFORM;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.undo.CompositeEdit;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * ApplyAttributesAction.
 *
 * @author Werner Randelshofer
 */
public class ApplyAttributesAction
    extends AbstractSelectedAction {

  private Set<AttributeKey<?>> excludedAttributes = new HashSet<>(
      Arrays.asList(new AttributeKey<?>[] {TRANSFORM, TEXT}));

  /**
   * Creates a new instance.
   *
   * @param editor The editor.
   */
  public ApplyAttributesAction(DrawingEditor editor) {
    super(editor);
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureAction(this, "edit.applyAttributes");
    updateEnabledState();
  }

  /**
   * Set of attributes that is excluded when applying default attributes.
   *
   * @param excludedAttributes The set of attributes to be excluded.
   */
  public void setExcludedAttributes(Set<AttributeKey<?>> excludedAttributes) {
    this.excludedAttributes = requireNonNull(excludedAttributes, "excludedAttributes");
  }

  @Override
  public void actionPerformed(java.awt.event.ActionEvent e) {
    applyAttributes();
  }

  @SuppressWarnings("unchecked")
  public void applyAttributes() {
    DrawingEditor editor = getEditor();

    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    CompositeEdit edit = new CompositeEdit(labels.getString("edit.applyAttributes.text"));
    DrawingView view = getView();
    view.getDrawing().fireUndoableEditHappened(edit);

    for (Figure figure : view.getSelectedFigures()) {
      figure.willChange();

      for (Map.Entry<AttributeKey, Object> entry : editor.getDefaultAttributes().entrySet()) {
        if (!excludedAttributes.contains(entry.getKey())) {
          figure.set(entry.getKey(), entry.getValue());
        }
      }

      figure.changed();
    }

    view.getDrawing().fireUndoableEditHappened(edit);
  }

  public void selectionChanged(FigureSelectionEvent evt) {
    setEnabled(getView().getSelectionCount() == 1);
  }
}
