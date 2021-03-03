/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.PointFigure;
import org.opentcs.guing.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PointModel;

/**
 * An adapter for points.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class PointAdapter
    extends OpenTCSProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(PointAdapter.class.getName());

  /**
   * Creates a new instance of PointAdapter.
   */
  public PointAdapter() {
    super();
  }

  @Override
  @SuppressWarnings("unchecked")
  public TCSObjectReference<Point> getProcessObject() {
    return (TCSObjectReference<Point>) super.getProcessObject();
  }

  @Override
  public PointModel getModel() {
    return (PointModel) super.getModel();
  }

  @Override
  public void setModel(ModelComponent model) {
    if (!PointModel.class.isInstance(model)) {
      throw new IllegalArgumentException(model + " is not a PointModel");
    }
    super.setModel(model);
  }

  @Override	// AbstractProcessAdapter
  public Object createProcessObject() throws KernelRuntimeException {
    if (!hasModelingState()) {
      return null;
    }
    Point point = kernel().createPoint();
    setProcessObject(point.getReference());
    // Only adopt the name
    nameToModel(point);
    register();

    return point;
  }

  @Override	// AbstractProcessAdapter
  public void releaseProcessObject() {
    try {
      releaseLayoutElement();
      kernel().removeTCSObject(getProcessObject());
      super.releaseProcessObject(); // also delete the Adapter
    }
    catch (KernelRuntimeException e) {
      log.log(Level.WARNING, null, e);
    }
  }

  @Override	// OpenTCSProcessAdapter
  public void propertiesChanged(AttributesChangeEvent event) {
    if (hasModelingState() && event.getInitiator() != this) {
      updateProcessProperties(false);
    }
  }

  @Override	// OpenTCSProcessAdapter
  public void updateModelProperties() {
    TCSObjectReference<Point> reference = getProcessObject();

    synchronized (reference) {
      try {
        Point point = kernel().getTCSObject(Point.class, reference);

        if (point == null) {
          return;
        }
        // Name
        StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
        pName.setText(point.getName());

        // Position in model
        CoordinateProperty cpx = (CoordinateProperty) getModel().getProperty(AbstractFigureComponent.MODEL_X_POSITION);
        cpx.setValueAndUnit(point.getPosition().getX(), LengthProperty.Unit.MM);

        CoordinateProperty cpy = (CoordinateProperty) getModel().getProperty(AbstractFigureComponent.MODEL_Y_POSITION);
        cpy.setValueAndUnit(point.getPosition().getY(), LengthProperty.Unit.MM);

        AngleProperty pAngle = (AngleProperty) getModel().getProperty(PointModel.VEHICLE_ORIENTATION_ANGLE);
        pAngle.setValueAndUnit(point.getVehicleOrientationAngle(), AngleProperty.Unit.DEG);

        updateModelType(point);
        updateModelLayoutProperties();
        updateMiscModelProperties(point);
      }
      catch (CredentialsException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  private void updateModelLayoutProperties() {
    if (fLayoutElement != null) {
      Map<String, String> properties = fLayoutElement.getProperties();
      StringProperty sp = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_POS_X);
      sp.setText(properties.get(ElementPropKeys.POINT_POS_X));

      sp = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_POS_Y);
      sp.setText(properties.get(ElementPropKeys.POINT_POS_Y));

      sp = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_LABEL_OFFSET_X);
      sp.setText(properties.get(ElementPropKeys.POINT_LABEL_OFFSET_X));

      sp = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_LABEL_OFFSET_Y);
      sp.setText(properties.get(ElementPropKeys.POINT_LABEL_OFFSET_Y));

      sp = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);
      sp.setText(properties.get(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE));
    }
  }

  private void updateModelType(Point point) {
    SelectionProperty pType = (SelectionProperty) getModel().getProperty(PointModel.TYPE);
    PointModel.PointType value;

    switch (point.getType()) {
      case HALT_POSITION:
        value = PointModel.PointType.HALT;
        break;

      case PARK_POSITION:
        value = PointModel.PointType.PARK;
        break;

      case REPORT_POSITION:
        value = PointModel.PointType.REPORT;
        break;
      default:
        value = PointModel.PointType.HALT;
    }

    pType.setValue(value);
  }

  @Override	// OpenTCSProcessAdapter
  public void updateProcessProperties(boolean updateAllProperties) {
    super.updateProcessProperties(updateAllProperties);
    TCSObjectReference<Point> reference = getProcessObject();

    if (isInTransition()) {
      return;
    }

    synchronized (reference) {
      StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      String name = pName.getText();

      try {
        // Name
        if (updateAllProperties || pName.hasChanged()) {
          kernel().renameTCSObject(reference, name);
        }
        updateProcessPosition(updateAllProperties, reference);

        // Write new position into the layout element
        Set<VisualLayout> layouts = kernel().getTCSObjects(VisualLayout.class);

        for (VisualLayout layout : layouts) {
          updateLayoutElement(layout, updateAllProperties);
        }
        updateProcessAngle(updateAllProperties, reference);
        updateProcessType(updateAllProperties, reference);
        updateMiscProcessProperties(updateAllProperties);
      }
      catch (ObjectExistsException e) {
        undo(name, e);
      }
      catch (ObjectUnknownException | CredentialsException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  private void updateProcessType(boolean updateAllProperties,
                                 TCSObjectReference<Point> reference)
      throws CredentialsException, ObjectUnknownException {
    SelectionProperty pType = (SelectionProperty) getModel().getProperty(PointModel.TYPE);

    if (updateAllProperties || pType.hasChanged()) {
      PointModel.PointType type = (PointModel.PointType) pType.getValue();
      kernel().setPointType(reference, convertPointType(type));
      pType.unmarkChanged();
    }
  }

  private void updateProcessAngle(boolean updateAllProperties,
                                  TCSObjectReference<Point> reference)
      throws ObjectUnknownException, CredentialsException {
    AngleProperty pAngle = (AngleProperty) getModel().getProperty(PointModel.VEHICLE_ORIENTATION_ANGLE);

    if (updateAllProperties || pAngle.hasChanged()) {
      double angle = pAngle.getValueByUnit(AngleProperty.Unit.DEG);
      kernel().setPointVehicleOrientationAngle(reference, angle);
      pAngle.unmarkChanged();
    }
  }

  private void updateProcessPosition(boolean updateAllProperties,
                                     TCSObjectReference<Point> reference)
      throws ObjectUnknownException, CredentialsException {
    CoordinateProperty cpx = (CoordinateProperty) getModel().getProperty(PointModel.MODEL_X_POSITION);
    CoordinateProperty cpy = (CoordinateProperty) getModel().getProperty(PointModel.MODEL_Y_POSITION);

    if (updateAllProperties || cpx.hasChanged() || cpy.hasChanged()) {
      kernel().setPointPosition(reference, convertToTriple(cpx, cpy));
      cpx.unmarkChanged();
      cpy.unmarkChanged();
    }
  }

  private Point.Type convertPointType(PointModel.PointType type) {
    assert type != null;
    switch (type) {
      case PARK:
        return Point.Type.PARK_POSITION;

      case REPORT:
        return Point.Type.REPORT_POSITION;

      case HALT:
      default:
        return Point.Type.HALT_POSITION;
    }
  }

  private Triple convertToTriple(CoordinateProperty cpx, CoordinateProperty cpy) {
    Triple result = new Triple();
    result.setX((int) cpx.getValueByUnit(LengthProperty.Unit.MM));
    result.setY((int) cpy.getValueByUnit(LengthProperty.Unit.MM));

    return result;
  }

  /**
   * Refreshes the properties of the layout element and saves it in the kernel.
   *
   * @param layout The VisualLayout.
   */
  private void updateLayoutElement(VisualLayout layout, boolean updateAllProperties) {
    StringProperty spx = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_POS_X);
    StringProperty spy = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_POS_Y);
    StringProperty splox = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_LABEL_OFFSET_X);
    StringProperty sploy = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_LABEL_OFFSET_Y);
    StringProperty sploa = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);

    if (updateAllProperties || spx.hasChanged() || spy.hasChanged() || splox.hasChanged() || sploy.hasChanged() || sploa.hasChanged()) {
      LabeledPointFigure lpf = getModel().getFigure();
      PointFigure pf = (PointFigure) lpf.getPresentationFigure();
      double scaleX = layout.getScaleX();
      double scaleY = layout.getScaleY();
      int xPos = (int) (pf.getZoomPoint().getX() * scaleX);
      int yPos = (int) -(pf.getZoomPoint().getY() * scaleY);
      TCSLabelFigure label = lpf.getLabel();
      Point2D.Double offset = label.getOffset();

      if (fLayoutElement == null) {
        fLayoutElement = new ModelLayoutElement(getProcessObject());
      }

      Map<String, String> layoutProperties = fLayoutElement.getProperties();
      layoutProperties.put(ElementPropKeys.POINT_POS_X, xPos + "");
      layoutProperties.put(ElementPropKeys.POINT_POS_Y, yPos + "");
      layoutProperties.put(ElementPropKeys.POINT_LABEL_OFFSET_X, (int) offset.x + "");
      layoutProperties.put(ElementPropKeys.POINT_LABEL_OFFSET_Y, (int) offset.y + "");
      // TODO:
//		layoutProperties.put(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE, ...);
      fLayoutElement.setProperties(layoutProperties);

      Set<LayoutElement> layoutElements = layout.getLayoutElements();
      Iterator<LayoutElement> iElements = layoutElements.iterator();

      while (iElements.hasNext()) {
        ModelLayoutElement element = (ModelLayoutElement) iElements.next();
        TCSObjectReference<?> visualizedObject = element.getVisualizedObject();

        if (visualizedObject.getId() == fLayoutElement.getVisualizedObject().getId()) {
          layoutElements.remove(element);
          break;
        }
      }

      layoutElements.add(fLayoutElement);
      kernel().setVisualLayoutElements(layout.getReference(), layoutElements);
      spx.unmarkChanged();
      spy.unmarkChanged();
      sploa.unmarkChanged();
      splox.unmarkChanged();
      sploy.unmarkChanged();
    }
  }
}
