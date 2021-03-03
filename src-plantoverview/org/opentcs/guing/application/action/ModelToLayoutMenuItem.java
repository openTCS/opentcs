/**
 * (c): IML, 2014.
 */
package org.opentcs.guing.application.action;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.components.properties.CoordinateUndoActivity;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.ModelAttribute;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A menu item that copies the value of the model property 
 * of a point or location to the corresponding layout property.
 * 
 * @author pseifert
 */
public class ModelToLayoutMenuItem
    extends JMenuItem
    implements AttributesChangeListener {

  /**
   * The <code>DrawingEditor</code> instance.
   */
  private final DrawingEditor drawingEditor;
  /**
   * A flag if the values of ALL points and location shall be copied when
   * the menu item is clicked. If false only the selected figures will be
   * considered.
   */
  private final boolean copyAll;

  /**
   * Creates a new instance. 
   * 
   * @param drawingEditor A <code>DrawingEditor</code> instance.
   * @param copyAll A flag if the values of ALL points and location shall be 
   * copied when the menu item is clicked. If false only the selected figures 
   * will be considered.
   */
  public ModelToLayoutMenuItem(DrawingEditor drawingEditor, boolean copyAll) {
    super(ResourceBundleUtil.getBundle().getString("propertiesTable.toLayout"));
    setIcon(new ImageIcon(
        getClass().getClassLoader().getResource("org/opentcs/guing/res/symbols/menu/arrow-down-3.png")));
    this.drawingEditor = Objects.requireNonNull(drawingEditor, "drawingEditor is null");
    this.copyAll = copyAll;
    setMargin(new Insets(0, 2, 0, 2));
    addActionListener();
  }

  private void addActionListener() {
    addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        for (Figure figure : copyAll
            ? getView().getDrawing().getFiguresFrontToBack()
            : getView().getSelectedFigures()) {
          ModelComponent model = figure.get(FigureConstants.MODEL);
          if (model instanceof PointModel || model instanceof LocationModel) {
            updateLayoutX(model);
            updateLayoutY(model);
            // ... and move the figure
            final LabeledFigure labeledFigure = (LabeledFigure) ((AbstractFigureComponent) model).getFigure();
            labeledFigure.propertiesChanged(new AttributesChangeEvent(ModelToLayoutMenuItem.this, model));

            model.propertiesChanged(ModelToLayoutMenuItem.this);
          }
        }
      }
    });
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    //is this neccessary?
    if (e.getInitiator() == this) {
      OpenTCSView.instance().resetSelectionTool();
    }
  }

  private DrawingView getView() {
    return drawingEditor.getActiveView();
  }

  private void updateLayoutY(ModelComponent model) {
    CoordinateProperty modelProperty;
    CoordinateUndoActivity cua;
    if (model instanceof PointModel) {
      modelProperty = (CoordinateProperty) model.getProperty(PointModel.MODEL_Y_POSITION);
    }
    else {
      modelProperty = (CoordinateProperty) model.getProperty(LocationModel.MODEL_Y_POSITION);
    }
    cua = new CoordinateUndoActivity(modelProperty);
    cua.snapShotBeforeModification();
    cua.setSaveTransform(true);
    modelProperty.setChangeState(ModelAttribute.ChangeState.DETAIL_CHANGED);
    StringProperty spy;
    if (model instanceof PointModel) {
      spy = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_Y);
    }
    else {
      spy = (StringProperty) model.getProperty(ElementPropKeys.LOC_POS_Y);
    }
    spy.setText(String.valueOf(modelProperty.getValue()));
    spy.markChanged();
    cua.snapShotAfterModification();
    OpenTCSView.instance().getUndoRedoManager().addEdit(cua);
  }

  private void updateLayoutX(ModelComponent model) {
    CoordinateProperty modelProperty;
    if (model instanceof PointModel) {
      modelProperty = (CoordinateProperty) model.getProperty(PointModel.MODEL_X_POSITION);
    }
    else {
      modelProperty = (CoordinateProperty) model.getProperty(LocationModel.MODEL_X_POSITION);
    }
    CoordinateUndoActivity cua = new CoordinateUndoActivity(modelProperty);
    cua.snapShotBeforeModification();
    cua.setSaveTransform(true);
    modelProperty.setChangeState(ModelAttribute.ChangeState.DETAIL_CHANGED);
    // Copy the model coordinates to the layout coordinates...
    StringProperty spx;
    if (model instanceof PointModel) {
      spx = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_X);
    }
    else {
      spx = (StringProperty) model.getProperty(ElementPropKeys.LOC_POS_X);
    }
    spx.setText(String.valueOf(modelProperty.getValue()));
    spx.markChanged();
    cua.snapShotAfterModification();
    OpenTCSView.instance().getUndoRedoManager().addEdit(cua);
  }
}
