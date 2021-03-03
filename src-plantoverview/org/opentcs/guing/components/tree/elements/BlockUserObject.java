/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree.elements;

import com.google.common.collect.Lists;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.opentcs.data.model.Block;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.util.IconToolkit;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A Block object in the tree view.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 * @see Block
 */
public class BlockUserObject
    extends AbstractUserObject {

  /**
   * The affected models.
   */
  private List<ModelComponent> fAffectedModels;

  /**
   * Creates a new instance.
   *
   * @param dataObject The corresponding model component.
   */
  public BlockUserObject(ModelComponent dataObject) {
    super(dataObject);
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

  @Override
  public JPopupMenu getPopupMenu() {
    JPopupMenu menu = new JPopupMenu();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    JMenuItem item = new JMenuItem(labels.getString("blockUserObject.addSelection"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent event) {
        execute();
        addToBlock();
      }
    });

    item.setEnabled(OpenTCSView.instance().hasOperationMode(
        GuiManager.OperationMode.MODELLING));
    menu.add(item);

    item = new JMenuItem(labels.getString("blockUserObject.removeSelection"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent event) {
        execute();
        removeFromBlock();
      }
    });

    item.setEnabled(OpenTCSView.instance().hasOperationMode(
        GuiManager.OperationMode.MODELLING));
    menu.add(item);

    menu.addSeparator();

    item = new JMenuItem(labels.getString("blockUserObject.selectAll"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent event) {
        OpenTCSView.instance().blockSelected(getModelComponent());
      }
    });

    menu.add(item);

    return menu;
  }

  @Override
  public BlockModel getModelComponent() {
    return (BlockModel) super.getModelComponent();
  }

  @Override
  public void doubleClicked() {
    OpenTCSView.instance().blockSelected(getModelComponent());
  }

  @Override
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/block.18x18.png");
  }

  private void execute() {
    fAffectedModels = new ArrayList<>();
    DrawingView view = OpenTCSView.instance().getEditor().getActiveView();

    for (Figure figure : view.getSelectedFigures()) {
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
  private void addToBlock() {
    for (ModelComponent model : fAffectedModels) {
      if (!getModelComponent().contains(model) && !(model instanceof LinkModel)) {
        getModelComponent().addCourseElement(model);
      }
    }

    getModelComponent().courseElementsChanged();
  }

  /**
   * Removes all affected models from the block.
   */
  private void removeFromBlock() {
    for (ModelComponent cmp : new ArrayList<>(Lists.reverse(fAffectedModels))) {
      getModelComponent().removeCourseElement(cmp);
    }

    getModelComponent().courseElementsChanged();
  }
}
