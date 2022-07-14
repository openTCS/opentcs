/**
 * (c): IML, JHotDraw.
 *
 *
 * Extended by IML: 1. Show Blocks and Pathes as overlay 2. Switch labels on/off
 *
 * @(#)DefaultDrawingView.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.thirdparty.modeleditor.jhotdraw.components.drawing;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.application.ApplicationState;
import org.opentcs.guing.common.components.drawing.course.Origin;
import org.opentcs.guing.common.components.drawing.course.OriginChangeListener;
import org.opentcs.guing.common.components.drawing.figures.FigureConstants;
import org.opentcs.guing.common.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.common.components.drawing.figures.ModelBasedFigure;
import org.opentcs.guing.common.components.drawing.figures.OriginFigure;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.modeleditor.components.drawing.BlockChangeHandler;
import org.opentcs.modeleditor.components.drawing.DeleteEdit;
import org.opentcs.modeleditor.components.drawing.PasteEdit;
import org.opentcs.modeleditor.components.layer.ActiveLayerProvider;
import org.opentcs.modeleditor.util.FigureCloner;
import org.opentcs.thirdparty.guing.common.jhotdraw.components.drawing.AbstractOpenTCSDrawingView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DrawingView implementation for the openTCS plant overview.
 *
 */
public class OpenTCSDrawingViewModeling
    extends AbstractOpenTCSDrawingView {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OpenTCSDrawingViewModeling.class);
  /**
   * A helper for cloning figures.
   */
  private final FigureCloner figureCloner;
  /**
   * The active layer provider.
   */
  private final ActiveLayerProvider activeLayerProvider;
  /**
   * Contains figures currently in the buffer (eg when copying or cutting figures).
   */
  private List<Figure> bufferedFigures = new ArrayList<>();
  /**
   * Handles events for blocks.
   */
  private final BlockChangeHandler blockChangeHandler;

  /**
   * Creates new instance.
   *
   * @param appState Stores the application's current state.
   * @param modelManager Provides the current system model.
   * @param figureCloner A helper for cloning figures.
   * @param activeLayerProvider The active layer provider.
   * @param blockChangeHandler The handler for block changes.
   */
  @Inject
  public OpenTCSDrawingViewModeling(ApplicationState appState,
                                    ModelManager modelManager,
                                    FigureCloner figureCloner,
                                    ActiveLayerProvider activeLayerProvider,
                                    BlockChangeHandler blockChangeHandler) {
    super(appState, modelManager);
    this.figureCloner = requireNonNull(figureCloner, "figureCloner");
    this.activeLayerProvider = requireNonNull(activeLayerProvider, "activeLayerProvider");
    this.blockChangeHandler = requireNonNull(blockChangeHandler, "blockChangeHandler");
  }

  @Override
  public void cutSelectedItems() {
    deleteSelectedFigures();
  }

  @Override
  public void copySelectedItems() {
    bufferedFigures = getDrawing().sort(getSelectedFigures());
  }

  @Override
  public void pasteBufferedItems() {
    clearSelection();

    List<Figure> pastedFigures = new ArrayList<>();
    if (getDrawing().getChildren().containsAll(bufferedFigures)) {
      pastedFigures.addAll(copyPasteBufferedItems());
    }
    else if (Collections.disjoint(getDrawing().getChildren(), bufferedFigures)) {
      pastedFigures.addAll(cutPasteBufferedItems());
    }
    else {
      // The list of buffered figures contains a mix of figures contained in the drawing and 
      // figures not contained in the drawing. This should never happen.
      throw new IllegalStateException("Some figures to be pasted are already in the drawing, some "
          + "are not.");
    }

    placeFiguresOnActiveLayer(pastedFigures);
  }

  @Override
  public void delete() {
    deleteSelectedFigures();

    if (!bufferedFigures.isEmpty()) {
      getDrawing().fireUndoableEditHappened(new DeleteEdit(this, bufferedFigures));
    }
  }

  @Override
  public void duplicate() {
    copySelectedItems();
    pasteBufferedItems();
  }

  @Override
  public void displayDriveOrders(VehicleModel vehicle, boolean visible) {
    // Displaying drive orders is specific to operating mode
  }

  @Override
  public void followVehicle(@Nonnull final VehicleModel model) {
    // Follwing a vehicle is not possible in modeling mode
  }

  @Override
  public void stopFollowVehicle() {
    // Follwing a vehicle is not possible in modeling mode
  }

  @Override
  protected void drawTool(Graphics2D g2d) {
    super.drawTool(g2d);

    if (getEditor() == null || getEditor().getTool() == null || getEditor().getActiveView() != this) {
      return;
    }

    // Set focus on the selected figure
    highlightFocus(g2d);
  }

  @Override
  protected DefaultDrawingView.EventHandler createEventHandler() {
    return new ExtendedEventHandler();
  }

  @Override
  public void delete(Set<ModelComponent> components) {
    List<Figure> figuresToDelete = components.stream()
        .map(component -> getModelManager().getModel().getFigure(component))
        .collect(Collectors.toList());
    deleteFigures(figuresToDelete);
  }

  @Override
  public void setBlocks(ModelComponent blocks) {
    synchronized (this) {
      for (ModelComponent blockComp : blocks.getChildComponents()) {
        BlockModel block = (BlockModel) blockComp;
        block.addBlockChangeListener(blockChangeHandler);
      }
    }
  }

  /**
   * Message of the application that a block area was created.
   *
   * @param block The newly created block.
   */
  public void blockAdded(BlockModel block) {
    block.addBlockChangeListener(blockChangeHandler);
  }

  /**
   * Pastes the list of buffered figures by cloning them and adding the clones to the drawing.
   *
   * @return The list of pasted figures. (The cloned figures.)
   */
  private List<Figure> copyPasteBufferedItems() {
    // Create clones of all buffered figures
    List<Figure> clonedFigures = figureCloner.cloneFigures(bufferedFigures);
    addToSelection(clonedFigures);
    getDrawing().fireUndoableEditHappened(new PasteEdit(this, clonedFigures));

    return clonedFigures;
  }

  /**
   * Pastes the list of buffered figures by adding them to the drawing.
   *
   * @return The list of pasted figures. (The buffered figures.)
   */
  private List<Figure> cutPasteBufferedItems() {
    for (Figure deletedFigure : bufferedFigures) {
      getDrawing().add(deletedFigure);
    }
    getDrawing().fireUndoableEditHappened(new PasteEdit(this, bufferedFigures));

    return bufferedFigures;
  }

  private void placeFiguresOnActiveLayer(List<Figure> figures) {
    for (Figure figure : figures) {
      if (figure instanceof ModelBasedFigure) {
        ((ModelBasedFigure) figure).getModel()
            .getPropertyLayerWrapper().setValue(activeLayerProvider.getActiveLayer());
      }
      else if (figure instanceof LabeledFigure) {
        ((LabeledFigure) figure).getPresentationFigure().getModel()
            .getPropertyLayerWrapper().setValue(activeLayerProvider.getActiveLayer());
      }
    }
  }

  private void deleteSelectedFigures() {
    final List<Figure> deletedFigures = getDrawing().sort(getSelectedFigures());
    deleteFigures(deletedFigures);
  }

  private void deleteFigures(List<Figure> figures) {
    // Abort, if not all of the selected figures may be removed from the drawing
    for (Figure figure : figures) {
      if (!figure.isRemovable()) {
        LOG.warn("Figure is not removable: {}. Aborting.", figure);
        return;
      }
    }

    bufferedFigures = figures;
    clearSelection();

    for (Figure figure : figures) {
      if (figure instanceof OriginChangeListener) {
        Origin ref = figure.get(FigureConstants.ORIGIN);

        if (ref != null) {
          ref.removeListener((OriginChangeListener) figure);
          figure.set(FigureConstants.ORIGIN, null);
        }
      }

      getDrawing().remove(figure);
    }
  }

  private class ExtendedEventHandler
      extends AbstractExtendedEventHandler {

    @Override
    protected boolean shouldShowFigure(Figure figure) {
      return !(figure instanceof OriginFigure);
    }
  }
}
