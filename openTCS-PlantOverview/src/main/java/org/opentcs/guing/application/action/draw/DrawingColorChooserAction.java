/**
 * (c): IML, JHotDraw.
 *
 * Changed by IML to allow access to ResourceBundle.
 *
 *
 * @(#)DrawingColorChooserAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.application.action.draw;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.DrawingColorIcon;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * The DrawingColorChooserAction changes a color attribute of the Drawing object
 * in the current view of the DrawingEditor.
 * <p>
 * The behavior for choosing the
 * initial color of the JColorChooser matches with
 * {@link DrawingColorIcon }.
 *
 * @author Werner Randelshofer
 */
public class DrawingColorChooserAction
    extends EditorColorChooserAction {

  /**
   * Creates a new instance.
   */
  public DrawingColorChooserAction(
      DrawingEditor editor,
      AttributeKey<Color> key) {

    this(editor, key, null, null);
  }

  /**
   * Creates a new instance.
   */
  public DrawingColorChooserAction(
      DrawingEditor editor,
      AttributeKey<Color> key,
      Icon icon) {

    this(editor, key, null, icon);
  }

  /**
   * Creates a new instance.
   */
  public DrawingColorChooserAction(
      DrawingEditor editor,
      AttributeKey<Color> key,
      String name) {

    this(editor, key, name, null);
  }

  public DrawingColorChooserAction(
      DrawingEditor editor,
      final AttributeKey<Color> key,
      String name,
      Icon icon) {

    this(editor, key, name, icon, new HashMap<AttributeKey, Object>());
  }

  public DrawingColorChooserAction(
      DrawingEditor editor,
      final AttributeKey<Color> key,
      String name,
      Icon icon,
      Map<AttributeKey, Object> fixedAttributes) {

    super(editor, key, name, icon, fixedAttributes);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Color initialColor = getInitialColor();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    Color chosenColor = JColorChooser.showDialog((Component) e.getSource(), labels.getString("attribute.color.text"), initialColor);

    if (chosenColor != null) {
      HashMap<AttributeKey, Object> attr = new HashMap<>(attributes);
      attr.put(key, chosenColor);
      HashSet<Figure> figures = new HashSet<>();
      figures.add(getView().getDrawing());
      applyAttributesTo(attr, figures);
    }
  }

  @Override
  protected Color getInitialColor() {
    Color initialColor = null;
    DrawingView v = getEditor().getActiveView();

    if (v != null) {
      Figure f = v.getDrawing();
      initialColor = f.get(key);
    }

    if (initialColor == null) {
      initialColor = super.getInitialColor();
    }

    return initialColor;
  }

  @Override
  protected void updateEnabledState() {
    if (getView() != null) {
      setEnabled(getView().isEnabled());
    }
    else {
      setEnabled(false);
    }
  }
}
