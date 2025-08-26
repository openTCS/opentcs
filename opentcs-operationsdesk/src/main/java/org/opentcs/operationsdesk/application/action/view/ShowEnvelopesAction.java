// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.application.action.view;

import static java.util.Objects.requireNonNull;
import static org.opentcs.operationsdesk.util.I18nPlantOverviewOperating.MENU_PATH;

import jakarta.annotation.Nonnull;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.common.components.drawing.DrawingOptions;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * An action for showing envelopes at allocated and claimed resources.
 */
public class ShowEnvelopesAction
    extends
      AbstractAction {

  /**
   * This action's ID.
   */
  public static final String ID = "view.showEnvelopes";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);

  private final OpenTCSDrawingEditor drawingEditor;

  private final DrawingOptions drawingOptions;

  /**
   * Creates a new instance.
   *
   * @param drawingEditor The drawing editor.
   * @param drawingOptions The drawing options.
   */
  @SuppressWarnings("this-escape")
  public ShowEnvelopesAction(
      @Nonnull
      OpenTCSDrawingEditor drawingEditor,
      @Nonnull
      DrawingOptions drawingOptions
  ) {
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.drawingOptions = requireNonNull(drawingOptions, "drawingOptions");

    putValue(NAME, BUNDLE.getString("showEnvelopesAction.name"));
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    drawingOptions.setEnvelopesVisible(!drawingOptions.isEnvelopesVisible());
    drawingEditor.getActiveView().drawingOptionsChanged();
  }
}
