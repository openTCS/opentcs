// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.toolbar;

import static java.util.Objects.requireNonNull;
import static org.opentcs.modeleditor.util.I18nPlantOverviewModeling.TOOLBAR_PATH;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.tool.CreationTool;
import org.opentcs.guing.common.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.common.components.drawing.figures.ModelBasedFigure;
import org.opentcs.modeleditor.components.layer.ActiveLayerProvider;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * A custom tool used to create {@code PointFigure}s and {@code LocationFigure}s.
 */
public class OpenTCSCreationTool
    extends
      CreationTool {

  /**
   * The resource bundle to use.
   */
  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(TOOLBAR_PATH);
  /**
   * Provides the currently active layer.
   */
  private final ActiveLayerProvider activeLayerProvider;

  /**
   * Creates a new instance.
   *
   * @param activeLayerProvider Provides the currently active layer.
   * @param prototype The figure to be used as a prototype.
   */
  @Inject
  public OpenTCSCreationTool(
      ActiveLayerProvider activeLayerProvider,
      @Assisted
      Figure prototype
  ) {
    super(prototype);
    this.activeLayerProvider = requireNonNull(activeLayerProvider, "activeLayerProvider");
  }

  @Override
  public void mousePressed(MouseEvent evt) {
    if (!activeLayerProvider.getActiveLayer().getLayer().isVisible()
        || !activeLayerProvider.getActiveLayer().getLayerGroup().isVisible()) {
      JOptionPane.showMessageDialog(
          evt.getComponent(),
          BUNDLE.getString("openTcsCreationTool.optionPane_activeLayerNotVisible.message"),
          BUNDLE.getString("openTcsCreationTool.optionPane_activeLayerNotVisible.title"),
          JOptionPane.INFORMATION_MESSAGE
      );
      return;
    }

    super.mousePressed(evt);
  }

  @Override
  protected Figure createFigure() {
    Figure figure = super.createFigure();

    if (figure instanceof ModelBasedFigure) {
      ((ModelBasedFigure) figure).getModel()
          .getPropertyLayerWrapper().setValue(activeLayerProvider.getActiveLayer());
    }
    else if (figure instanceof LabeledFigure) {
      ((LabeledFigure) figure).getPresentationFigure().getModel()
          .getPropertyLayerWrapper().setValue(activeLayerProvider.getActiveLayer());
    }

    return figure;
  }
}
