/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree;

import com.google.common.collect.Lists;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.tree.elements.UserObject;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class BlockMouseListener
    extends TreeMouseAdapter {

  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  private final DrawingEditor drawingEditor;
  private List<ModelComponent> fAffectedModels;

  /**
   * Creates a new instance.
   *
   * @param appState The application state
   * @param drawingEditor The drawing editor
   * @param treeView The tree view
   */
  @Inject
  public BlockMouseListener(ApplicationState appState,
                            DrawingEditor drawingEditor,
                            TreeView treeView) {
    super(treeView);
    this.appState = requireNonNull(appState, "appState");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
  }

  @Override
  protected void evaluateRightClick(MouseEvent e,
                                    UserObject userObject,
                                    Set<UserObject> oldSelection) {
    JPopupMenu menu = new JPopupMenu();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    final OpenTCSView openTCSView = OpenTCSView.instance();

    ModelComponent modelComponent = userObject.getModelComponent();
    if (modelComponent instanceof BlockModel) {
      final BlockModel blockModel = (BlockModel) modelComponent;
      JMenuItem item = new JMenuItem(labels.getString("blockUserObject.addSelection"));
      item.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent event) {
          execute();
          addToBlock(blockModel);
        }
      });

      item.setEnabled(appState.hasOperationMode(OperationMode.MODELLING));
      menu.add(item);

      item = new JMenuItem(labels.getString("blockUserObject.removeSelection"));
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

      item = new JMenuItem(labels.getString("blockUserObject.selectAll"));
      item.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent event) {
          openTCSView.blockSelected((blockModel));
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
