/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.toolbar;

import com.google.inject.assistedinject.Assisted;
import java.awt.event.MouseEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.tool.CreationTool;
import org.opentcs.guing.common.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.common.components.drawing.figures.ModelBasedFigure;
import org.opentcs.modeleditor.components.layer.ActiveLayerProvider;
import static org.opentcs.modeleditor.util.I18nPlantOverviewModeling.TOOLBAR_PATH;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * A custom tool used to create {@code PointFigure}s and {@code LocationFigure}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class OpenTCSCreationTool
    extends CreationTool {

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
  public OpenTCSCreationTool(ActiveLayerProvider activeLayerProvider,
                             @Assisted Figure prototype) {
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
