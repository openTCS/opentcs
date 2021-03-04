/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import com.google.inject.assistedinject.Assisted;
import java.awt.geom.Point2D;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.PointFigure;
import org.opentcs.guing.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.components.properties.type.AbstractProperty;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PointModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for points.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PointAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PointAdapter.class);

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model component.
   * @param eventDispatcher The event dispatcher.
   */
  @Inject
  public PointAdapter(@Assisted PointModel model,
                      @Assisted EventDispatcher eventDispatcher) {
    super(model, eventDispatcher);
  }

  @Override
  public PointModel getModel() {
    return (PointModel) super.getModel();
  }

  @Override  // OpenTCSProcessAdapter
  public void updateModelProperties(Kernel kernel,
                                    TCSObject<?> tcsObject,
                                    @Nullable ModelLayoutElement layoutElement) {
    Point point = requireNonNull((Point) tcsObject, "tcsObject");
    // Name
    StringProperty pName
        = (StringProperty) getModel().getProperty(ModelComponent.NAME);
    pName.setText(point.getName());

    // Position in model
    CoordinateProperty cpx = (CoordinateProperty) getModel().getProperty(
        AbstractFigureComponent.MODEL_X_POSITION);
    cpx.setValueAndUnit(point.getPosition().getX(), LengthProperty.Unit.MM);

    CoordinateProperty cpy = (CoordinateProperty) getModel().getProperty(
        AbstractFigureComponent.MODEL_Y_POSITION);
    cpy.setValueAndUnit(point.getPosition().getY(), LengthProperty.Unit.MM);

    AngleProperty pAngle = (AngleProperty) getModel().getProperty(
        PointModel.VEHICLE_ORIENTATION_ANGLE);
    pAngle.setValueAndUnit(point.getVehicleOrientationAngle(),
                           AngleProperty.Unit.DEG);

    updateModelType(point);
    if (layoutElement != null) {
      updateModelLayoutProperties(layoutElement);
    }
    updateMiscModelProperties(point);
  }

  @Override  // OpenTCSProcessAdapter
  public void storeToPlantModel(PlantModelCreationTO plantModel) {
    plantModel.getPoints().add(
        new PointCreationTO(getModel().getName())
            .setPosition(getKernelCoordinates())
            .setVehicleOrientationAngle(getKernelVehicleAngle())
            .setType(getKernelPointType())
            .setProperties(getKernelProperties()));

    // Write new position into the layout element
    for (VisualLayoutCreationTO layout : plantModel.getVisualLayouts()) {
      updateLayoutElement(layout);
    }

    unmarkAllPropertiesChanged();
  }

  private void updateModelLayoutProperties(ModelLayoutElement layoutElement) {
    Map<String, String> properties = layoutElement.getProperties();

    StringProperty sp = (StringProperty) getModel().getProperty(
        ElementPropKeys.POINT_POS_X);
    sp.setText(properties.get(ElementPropKeys.POINT_POS_X));

    sp = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_POS_Y);
    sp.setText(properties.get(ElementPropKeys.POINT_POS_Y));

    sp = (StringProperty) getModel().getProperty(
        ElementPropKeys.POINT_LABEL_OFFSET_X);
    sp.setText(properties.get(ElementPropKeys.POINT_LABEL_OFFSET_X));

    sp = (StringProperty) getModel().getProperty(
        ElementPropKeys.POINT_LABEL_OFFSET_Y);
    sp.setText(properties.get(ElementPropKeys.POINT_LABEL_OFFSET_Y));

    sp = (StringProperty) getModel().getProperty(
        ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);
    sp.setText(properties.get(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE));
  }

  private void updateModelType(Point point) {
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

    ((SelectionProperty) getModel().getProperty(PointModel.TYPE))
        .setValue(value);
  }

  private Point.Type getKernelPointType() {
    AbstractProperty pType = (AbstractProperty) getModel().getProperty(PointModel.TYPE);
    return convertPointType((PointModel.PointType) pType.getValue());
  }

  private Triple getKernelCoordinates() {
    return convertToTriple(
        (CoordinateProperty) getModel().getProperty(PointModel.MODEL_X_POSITION),
        (CoordinateProperty) getModel().getProperty(PointModel.MODEL_Y_POSITION));
  }

  private double getKernelVehicleAngle() {
    return ((AngleProperty) getModel().getProperty(PointModel.VEHICLE_ORIENTATION_ANGLE))
        .getValueByUnit(AngleProperty.Unit.DEG);
  }

  private Point.Type convertPointType(PointModel.PointType type) {
    requireNonNull(type, "type");
    switch (type) {
      case PARK:
        return Point.Type.PARK_POSITION;
      case REPORT:
        return Point.Type.REPORT_POSITION;
      case HALT:
        return Point.Type.HALT_POSITION;
      default:
        throw new IllegalArgumentException("Unhandled type: " + type);
    }
  }

  private Triple convertToTriple(CoordinateProperty cpx, CoordinateProperty cpy) {
    Triple result = new Triple((int) cpx.getValueByUnit(LengthProperty.Unit.MM),
                               (int) cpy.getValueByUnit(LengthProperty.Unit.MM),
                               0);

    return result;
  }

  /**
   * Refreshes the properties of the layout element and saves it in the kernel.
   *
   * @param layout The VisualLayout.
   */
  private void updateLayoutElement(VisualLayoutCreationTO layout) {
    StringProperty spx = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_POS_X);
    StringProperty spy = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_POS_Y);
    StringProperty splox
        = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_LABEL_OFFSET_X);
    StringProperty sploy
        = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_LABEL_OFFSET_Y);
    StringProperty sploa
        = (StringProperty) getModel().getProperty(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);

    LabeledPointFigure lpf = getModel().getFigure();
    PointFigure pf = lpf.getPresentationFigure();
    double scaleX = layout.getScaleX();
    double scaleY = layout.getScaleY();
    int xPos = (int) (pf.getZoomPoint().getX() * scaleX);
    int yPos = (int) -(pf.getZoomPoint().getY() * scaleY);
    TCSLabelFigure label = lpf.getLabel();
    Point2D.Double offset = label.getOffset();

    layout.getModelElements().add(new ModelLayoutElementCreationTO(getModel().getName())
        .setProperty(ElementPropKeys.POINT_POS_X, xPos + "")
        .setProperty(ElementPropKeys.POINT_POS_Y, yPos + "")
        .setProperty(ElementPropKeys.POINT_LABEL_OFFSET_X, (int) offset.x + "")
        .setProperty(ElementPropKeys.POINT_LABEL_OFFSET_Y, (int) offset.y + "")
    );

    spx.unmarkChanged();
    spy.unmarkChanged();
    sploa.unmarkChanged();
    splox.unmarkChanged();
    sploy.unmarkChanged();
  }
}
