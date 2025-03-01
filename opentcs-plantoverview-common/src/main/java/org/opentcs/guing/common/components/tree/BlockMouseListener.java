// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.common.application.ApplicationState;
import org.opentcs.guing.common.application.OperationMode;
import org.opentcs.guing.common.components.drawing.figures.FigureConstants;
import org.opentcs.guing.common.components.tree.elements.UserObject;
import org.opentcs.guing.common.util.BlockSelector;
import org.opentcs.guing.common.util.I18nPlantOverview;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 */
public class BlockMouseListener
    extends
      TreeMouseAdapter {

  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  /**
   * The application's drawing editor.
   */
  private final DrawingEditor drawingEditor;
  /**
   * A helper for selecting blocks/block elements.
   */
  private final BlockSelector blockSelector;
  /**
   * The affected models.
   */
  private List<ModelComponent> fAffectedModels;

  /**
   * Creates a new instance.
   *
   * @param appState The application state
   * @param drawingEditor The drawing editor
   * @param treeView The tree view
   * @param blockSelector A helper for selecting blocks/block elements.
   */
  @Inject
  public BlockMouseListener(
      ApplicationState appState,
      DrawingEditor drawingEditor,
      TreeView treeView,
      BlockSelector blockSelector
  ) {
    super(treeView);
    this.appState = requireNonNull(appState, "appState");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.blockSelector = requireNonNull(blockSelector, "blockSelector");
  }

  @Override
  protected void evaluateRightClick(
      MouseEvent e,
      UserObject userObject,
      Set<UserObject> oldSelection
  ) {
    JPopupMenu menu = new JPopupMenu();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverview.TREEVIEW_PATH);

    ModelComponent modelComponent = userObject.getModelComponent();
    if (modelComponent instanceof BlockModel) {
      final BlockModel blockModel = (BlockModel) modelComponent;
      JMenuItem item = new JMenuItem(
          labels.getString("blockMouseListener.popupMenuItem_addToBlock.text")
      );
      item.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent event) {
          execute();
          addToBlock(blockModel);
        }
      });

      item.setEnabled(appState.hasOperationMode(OperationMode.MODELLING));
      menu.add(item);

      item = new JMenuItem(
          labels.getString("blockMouseListener.popupMenuItem_removeFromBlock.text")
      );
      item.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent event) {
          execute();
          removeFromBlock(blockModel);
        }
      });

      item.setEnabled(appState.hasOperationMode(OperationMode.MODELLING));
      menu.add(item);

      menu.addSeparator();

      item = new JMenuItem(
          labels.getString("blockMouseListener.popupMenuItem_selectAllElements.text")
      );
      item.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent event) {
          blockSelector.blockSelected(blockModel);
        }
      });

      menu.add(item);

      menu.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  private void execute() {
    fAffectedModels = new ArrayList<>();

    for (Figure figure : drawingEditor.getActiveView().getSelectedFigures()) {
      fAffectedModels.add(figure.get(FigureConstants.MODEL));
    }

    List<ModelComponent> suitableModels = new ArrayList<>();
    for (ModelComponent model : fAffectedModels) {
      if (isModelOk(model)) {
        suitableModels.add(model);
      }
    }
    fAffectedModels = suitableModels;
  }

  /**
   * Adds all affected models to the block.
   */
  private void addToBlock(BlockModel modelComponent) {
    for (ModelComponent model : fAffectedModels) {
      if (!modelComponent.contains(model) && !(model instanceof LinkModel)) {
        modelComponent.addCourseElement(model);
      }
    }

    modelComponent.courseElementsChanged();
  }

  /**
   * Removes all affected models from the block.
   */
  private void removeFromBlock(BlockModel modelComponent) {
    for (ModelComponent cmp : new ArrayList<>(Lists.reverse(fAffectedModels))) {
      modelComponent.removeCourseElement(cmp);
    }

    modelComponent.courseElementsChanged();
  }

  /**
   * Checks whether the given model can be added to a block.
   *
   * @param model The model to be checked.
   * @return <code>true</code> if, and only if, the given model can be added to
   * a block.
   */
  private static boolean isModelOk(ModelComponent model) {
    return (model != null
        && (model instanceof PointModel
            || model instanceof LocationModel
            || model instanceof PathModel
            || model instanceof LinkModel));
  }
}
