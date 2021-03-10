/*
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import java.awt.geom.Rectangle2D;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.persistence.ModelManager;

/**
 * A helper for selecting blocks/block elements.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class BlockSelector {

  /**
   * The application's model manager.
   */
  private final ModelManager modelManager;
  /**
   * The application's drawing editor.
   */
  private final OpenTCSDrawingEditor drawingEditor;

  /**
   * Creates a new instance.
   *
   * @param modelManager The application's model manager.
   * @param drawingEditor The application's drawing editor.
   */
  @Inject
  public BlockSelector(ModelManager modelManager, OpenTCSDrawingEditor drawingEditor) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
  }

  /**
   * Called when a block was selected, for instance in the tree view.
   * Should select all figures in the drawing view belonging to the block.
   *
   * @param block The selected block.
   */
  public void blockSelected(BlockModel block) {
    requireNonNull(block, "block");

    Rectangle2D r = null;

    List<Figure> blockElementFigures
        = ModelComponentUtil.getChildFigures(block, modelManager.getModel());

    for (Figure figure : blockElementFigures) {
      Rectangle2D displayBox = figure.getDrawingArea();

      if (r == null) {
        r = displayBox;
      }
      else {
        r.add(displayBox);
      }
    }

    if (r != null) {
      OpenTCSDrawingView drawingView = drawingEditor.getActiveView();

      drawingView.clearSelection();

      for (Figure figure : blockElementFigures) {
        drawingView.addToSelection(figure);
      }

      drawingView.updateBlock(block);
    }
  }
}
