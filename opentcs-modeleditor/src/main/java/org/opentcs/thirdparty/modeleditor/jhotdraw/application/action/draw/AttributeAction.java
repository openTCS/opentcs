// SPDX-FileCopyrightText: The original authors of JHotDraw and all its contributors
// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.thirdparty.modeleditor.jhotdraw.application.action.draw;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import org.jhotdraw.app.action.ActionUtil;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractSelectedAction;

/**
 * Applies attribute values on the selected figures of the current {@link DrawingView} of a
 * {@link DrawingEditor}.
 *
 * @author Werner Randelshofer
 */
public class AttributeAction
    extends
      AbstractSelectedAction {

  protected Map<AttributeKey, Object> attributes;

  @SuppressWarnings("this-escape")
  public AttributeAction(
      DrawingEditor editor,
      AttributeKey key,
      Object value,
      String name,
      Icon icon
  ) {
    super(editor);
    this.attributes = new HashMap<>();
    attributes.put(key, value);

    putValue(AbstractAction.NAME, name);
    putValue(AbstractAction.SMALL_ICON, icon);
    putValue(ActionUtil.UNDO_PRESENTATION_NAME_KEY, key.getPresentationName());
    updateEnabledState();
  }

  @SuppressWarnings("this-escape")
  public AttributeAction(
      DrawingEditor editor,
      Map<AttributeKey, Object> attributes,
      String name,
      Icon icon
  ) {
    super(editor);
    this.attributes = (attributes == null) ? new HashMap<>() : attributes;

    putValue(AbstractAction.NAME, name);
    putValue(AbstractAction.SMALL_ICON, icon);
    updateEnabledState();
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    applyAttributesTo(attributes, getView().getSelectedFigures());
  }

  /**
   * Applies the specified attributes to the currently selected figures of the
   * drawing.
   *
   * @param a The attributes.
   * @param figures The figures to which the attributes are applied.
   */
  @SuppressWarnings("unchecked")
  public void applyAttributesTo(final Map<AttributeKey, Object> a, Set<Figure> figures) {
    for (Map.Entry<AttributeKey, Object> entry : a.entrySet()) {
      getEditor().setDefaultAttribute(entry.getKey(), entry.getValue());
    }

    final ArrayList<Figure> selectedFigures = new ArrayList<>(figures);
    final ArrayList<Object> restoreData = new ArrayList<>(selectedFigures.size());

    for (Figure figure : selectedFigures) {
      restoreData.add(figure.getAttributesRestoreData());
      figure.willChange();

      for (Map.Entry<AttributeKey, Object> entry : a.entrySet()) {
        figure.set(entry.getKey(), entry.getValue());
      }

      figure.changed();
    }

    UndoableEdit edit = new AbstractUndoableEdit() {
      @Override
      public String getPresentationName() {
        String name = (String) getValue(ActionUtil.UNDO_PRESENTATION_NAME_KEY);

        if (name == null) {
          name = (String) getValue(AbstractAction.NAME);
        }

        return name;
      }

      @Override
      public void undo() {
        super.undo();
        Iterator<Object> iRestore = restoreData.iterator();

        for (Figure figure : selectedFigures) {
          figure.willChange();
          figure.restoreAttributesTo(iRestore.next());
          figure.changed();
        }
      }

      @Override
      public void redo() {
        super.redo();

        for (Figure figure : selectedFigures) {
          //restoreData.add(figure.getAttributesRestoreData());
          figure.willChange();

          for (Map.Entry<AttributeKey, Object> entry : a.entrySet()) {
            figure.set(entry.getKey(), entry.getValue());
          }

          figure.changed();
        }
      }
    };

    getDrawing().fireUndoableEditHappened(edit);
  }

  @Override
  protected void updateEnabledState() {
    if (getEditor() != null) {
      setEnabled(getEditor().isEnabled());
    }
  }
}
