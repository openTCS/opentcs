/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.components.layer;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.base.model.DrawnModelComponent;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.common.components.drawing.figures.FigureConstants;
import org.opentcs.guing.common.event.DrawingEditorEvent;
import org.opentcs.guing.common.event.DrawingEditorListener;

/**
 * Handles drawing editor events that the layer editor needs to know about.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayerEditorEventHandler
    implements DrawingEditorListener {

  /**
   * The layer editor.
   */
  private final LayerEditorModeling layerEditor;

  @Inject
  public LayerEditorEventHandler(LayerEditorModeling layerEditor) {
    this.layerEditor = requireNonNull(layerEditor, "layerEditor");
  }

  @Override
  public void figureAdded(DrawingEditorEvent e) {
    Figure figure = e.getFigure();
    ModelComponent model = figure.get(FigureConstants.MODEL);
    if (model instanceof DrawnModelComponent) {
      layerEditor.add((DrawnModelComponent) model);
    }
  }

  @Override
  public void figureRemoved(DrawingEditorEvent e) {
    Figure figure = e.getFigure();
    ModelComponent model = figure.get(FigureConstants.MODEL);
    if (model instanceof DrawnModelComponent) {
      layerEditor.remove((DrawnModelComponent) model);
    }
  }

  @Override
  public void figureSelected(DrawingEditorEvent e) {
  }
}
