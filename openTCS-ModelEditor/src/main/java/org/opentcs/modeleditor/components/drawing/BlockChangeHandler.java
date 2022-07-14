/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.components.drawing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.jhotdraw.draw.AbstractFigure;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.base.event.BlockChangeEvent;
import org.opentcs.guing.base.event.BlockChangeListener;
import org.opentcs.guing.base.model.FigureDecorationDetails;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.ModelComponentUtil;

/**
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class BlockChangeHandler
    implements BlockChangeListener {

  /**
   * The members/elements of a block mapped to the block model.
   */
  private final Map<BlockModel, Set<FigureDecorationDetails>> blockElementsHistory
      = new HashMap<>();

  /**
   * The manager keeping/providing the currently loaded model.
   */
  private final ModelManager modelManager;

  /**
   * Creates a new instance.
   *
   * @param modelManager The model manager.
   */
  @Inject
  public BlockChangeHandler(ModelManager modelManager) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
  }

  @Override // BlockChangeListener
  public void courseElementsChanged(BlockChangeEvent e) {
    BlockModel block = (BlockModel) e.getSource();

    // Let the block's elements know the block they are now part of.
    Set<FigureDecorationDetails> blockElements = block.getPropertyElements().getItems().stream()
        .map(elementName -> modelManager.getModel().getModelComponent(elementName))
        .filter(modelComponent -> modelComponent instanceof FigureDecorationDetails)
        .map(modelComponent -> (FigureDecorationDetails) modelComponent)
        .collect(Collectors.toSet());
    for (FigureDecorationDetails component : blockElements) {
      component.addBlockModel(block);
    }

    // The elements that are no longer part of the block should also know this.
    Set<FigureDecorationDetails> removedBlockElements = updateBlockElementHistory(block,
                                                                                  blockElements);
    for (FigureDecorationDetails component : removedBlockElements) {
      component.removeBlockModel(block);
      // Update the figure so that it no longer appears as being part of the block.
      Figure figure = modelManager.getModel().getFigure(((ModelComponent) component));
      ((AbstractFigure) figure).fireFigureChanged();
    }

    updateBlock(block);
  }

  @Override // BlockChangeListener
  public void colorChanged(BlockChangeEvent e) {
    updateBlock((BlockModel) e.getSource());
  }

  @Override // BlockChangeListener
  public void blockRemoved(BlockChangeEvent e) {
    BlockModel block = (BlockModel) e.getSource();

    // Let the block's elements know they are no longer part of the block.
    Set<FigureDecorationDetails> removedBlockElements
        = updateBlockElementHistory(block, new HashSet<>());
    for (FigureDecorationDetails component : removedBlockElements) {
      component.removeBlockModel(block);
    }

    block.removeBlockChangeListener(this);
    updateBlock(block);
  }

  /**
   * Remembers the given set of components as the new block elements for the given
   * block model and returns the set difference of the old and the new block elements
   * (e.g. the elements that are no longer part of the block).
   *
   * @param block The block model.
   * @param newBlockElements The new block elements.
   * @return The set difference of the old and the new block elements.
   */
  private Set<FigureDecorationDetails> updateBlockElementHistory(
      BlockModel block,
      Set<FigureDecorationDetails> newBlockElements) {
    Set<FigureDecorationDetails> oldBlockElements = getBlockElements(block);
    Set<FigureDecorationDetails> removedBlockElements = new HashSet<>(oldBlockElements);

    removedBlockElements.removeAll(newBlockElements);

    oldBlockElements.clear();
    oldBlockElements.addAll(newBlockElements);

    return removedBlockElements;
  }

  private Set<FigureDecorationDetails> getBlockElements(BlockModel block) {
    if (!blockElementsHistory.containsKey(block)) {
      blockElementsHistory.put(block, new HashSet<>());
    }

    return blockElementsHistory.get(block);
  }

  private void updateBlock(BlockModel block) {
    for (Figure figure : ModelComponentUtil.getChildFigures(block, modelManager.getModel())) {
      ((AbstractFigure) figure).fireFigureChanged();
    }
  }
}
