/**
 * (c): IML, JHotDraw.
 *
 * Changed by IML to allow access to ResourceBundle.
 *
 *
 * @(#)EditorColorChooserAction.java
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
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.action.EditorColorIcon;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * EditorColorChooserAction.
 * <p>
 * The behavior for choosing the initial color of
 * the JColorChooser matches with
 * {@link EditorColorIcon }.
 *
 * @author Werner Randelshofer
 */
public class EditorColorChooserAction
    extends AttributeAction {

  protected AttributeKey<Color> key;

  /**
   * Creates a new instance.
   *
   * @param editor The drawing editor
   * @param key The attribute key
   * @param name The name
   * @param icon The icon
   * @param fixedAttributes The fixed attributes
   */
  public EditorColorChooserAction(DrawingEditor editor,
                                  AttributeKey<Color> key,
                                  String name,
                                  Icon icon,
                                  Map<AttributeKey, Object> fixedAttributes) {

    super(editor, fixedAttributes, name, icon);
    this.key = key;
    putValue(AbstractAction.NAME, name);
    putValue(AbstractAction.SMALL_ICON, icon);
    updateEnabledState();
  }

  @Override
  public void actionPerformed(java.awt.event.ActionEvent e) {
    Color initialColor = getInitialColor();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    Color chosenColor = JColorChooser.showDialog((Component) e.getSource(),
                                                 labels.getString("attribute.color.text"),
                                                 initialColor);

    if (chosenColor != null) {
      HashMap<AttributeKey, Object> attr = new HashMap<>(attributes);
      attr.put(key, chosenColor);
      applyAttributesTo(attr, getView().getSelectedFigures());
    }
  }

  public void selectionChanged(FigureSelectionEvent evt) {
    //setEnabled(getView().getSelectionCount() > 0);
  }

  protected Color getInitialColor() {
    Color initialColor = getEditor().getDefaultAttribute(key);

    if (initialColor == null) {
      initialColor = Color.red;
    }

    return initialColor;
  }
}
