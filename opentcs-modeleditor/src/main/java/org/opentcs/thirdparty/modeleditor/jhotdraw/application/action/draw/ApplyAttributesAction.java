// SPDX-FileCopyrightText: The original authors of JHotDraw and all its contributors
// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.thirdparty.modeleditor.jhotdraw.application.action.draw;

import static java.util.Objects.requireNonNull;
import static javax.swing.Action.LARGE_ICON_KEY;
import static javax.swing.Action.SMALL_ICON;
import static org.jhotdraw.draw.AttributeKeys.TEXT;
import static org.jhotdraw.draw.AttributeKeys.TRANSFORM;
import static org.opentcs.modeleditor.util.I18nPlantOverviewModeling.TOOLBAR_PATH;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.undo.CompositeEdit;
import org.opentcs.guing.common.util.ImageDirectory;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * ApplyAttributesAction.
 *
 * @author Werner Randelshofer
 */
public class ApplyAttributesAction
    extends
      AbstractSelectedAction {

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(TOOLBAR_PATH);

  private Set<AttributeKey<?>> excludedAttributes = new HashSet<>(
      Arrays.asList(new AttributeKey<?>[]{TRANSFORM, TEXT})
  );

  /**
   * Creates a new instance.
   *
   * @param editor The editor.
   */
  @SuppressWarnings("this-escape")
  public ApplyAttributesAction(DrawingEditor editor) {
    super(editor);

    putValue(NAME, BUNDLE.getString("applyAttributesAction.name"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("applyAttributesAction.shortDescription"));

    ImageIcon icon = ImageDirectory.getImageIcon("/toolbar/view-media-visualization.png");
    putValue(SMALL_ICON, icon);
    putValue(LARGE_ICON_KEY, icon);
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

    ResourceBundleUtil labels
        = ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.TOOLBAR_PATH);
    CompositeEdit edit
        = new CompositeEdit(labels.getString("applyAttributesAction.undo.presentationName"));
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
