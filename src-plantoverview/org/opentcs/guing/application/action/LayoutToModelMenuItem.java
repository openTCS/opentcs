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
import org.opentcs.guing.components.properties.CoordinateUndoActivity;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A menu item that copies the value of the layout property 
 * of a point or location to the corresponding model property.
 * 
 * @author pseifert
 */
public class LayoutToModelMenuItem
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
  public LayoutToModelMenuItem(DrawingEditor drawingEditor, boolean copyAll) {
    super(ResourceBundleUtil.getBundle().getString("propertiesTable.fromLayout"));
    setIcon(new ImageIcon(
        getClass().getClassLoader().getResource("org/opentcs/guing/res/symbols/menu/arrow-up-3.png")));
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
            updateModelX(model);
            updateModelY(model);
            model.propertiesChanged(LayoutToModelMenuItem.this);
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

  private void updateModelY(ModelComponent model) throws IllegalArgumentException {
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
    StringProperty spy;
    if (model instanceof PointModel) {
      spy = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_Y);
    }
    else {
      spy = (StringProperty) model.getProperty(ElementPropKeys.LOC_POS_Y);
    }

    if (!spy.getText().isEmpty()) {
      modelProperty.setValueAndUnit(Double.parseDouble(spy.getText()), modelProperty.getUnit());
      modelProperty.markChanged();
    }
    cua.setSaveTransform(false);
    cua.snapShotAfterModification();
  }

  private void updateModelX(ModelComponent model) throws IllegalArgumentException {
    CoordinateProperty modelProperty;
    if (model instanceof PointModel) {
      modelProperty = (CoordinateProperty) model.getProperty(PointModel.MODEL_X_POSITION);
    }
    else {
      modelProperty = (CoordinateProperty) model.getProperty(LocationModel.MODEL_X_POSITION);
    }
    CoordinateUndoActivity cua = new CoordinateUndoActivity(modelProperty);
    cua.snapShotBeforeModification();
    StringProperty spx;
    if (model instanceof PointModel) {
      spx = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_X);
    }
    else {
      spx = (StringProperty) model.getProperty(ElementPropKeys.LOC_POS_X);
    }
    if (!spx.getText().isEmpty()) {
      modelProperty.setValueAndUnit(Double.parseDouble(spx.getText()), modelProperty.getUnit());
      modelProperty.markChanged();
    }
    cua.setSaveTransform(false);
    cua.snapShotAfterModification();
    OpenTCSView.instance().getUndoRedoManager().addEdit(cua);
  }
}
