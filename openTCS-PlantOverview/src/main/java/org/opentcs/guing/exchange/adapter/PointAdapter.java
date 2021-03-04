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

import java.awt.geom.Point2D;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.PointFigure;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.PointModel;

/**
 * An adapter for points.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PointAdapter
    extends AbstractProcessAdapter {

  @Override  // OpenTCSProcessAdapter
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService,
                                    @Nullable ModelLayoutElement layoutElement) {
    Point point = requireNonNull((Point) tcsObject, "tcsObject");
    PointModel model = (PointModel) modelComponent;

    // Name
    model.getPropertyName().setText(point.getName());

    // Position in model
    model.getPropertyModelPositionX().setValueAndUnit(point.getPosition().getX(),
                                                      LengthProperty.Unit.MM);
    model.getPropertyModelPositionY().setValueAndUnit(point.getPosition().getY(),
                                                      LengthProperty.Unit.MM);
    model.getPropertyVehicleOrientationAngle()
        .setValueAndUnit(point.getVehicleOrientationAngle(), AngleProperty.Unit.DEG);

    updateModelType(model, point);
    if (layoutElement != null) {
      updateModelLayoutProperties(model, layoutElement);
    }
    updateMiscModelProperties(model, point);
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    PlantModelCreationTO result = plantModel
        .withPoint(
            new PointCreationTO(modelComponent.getName())
                .withPosition(getKernelCoordinates((PointModel) modelComponent))
                .withVehicleOrientationAngle(getKernelVehicleAngle((PointModel) modelComponent))
                .withType(getKernelPointType((PointModel) modelComponent))
                .withProperties(getKernelProperties(modelComponent))
        )
        .withVisualLayouts(updatedLayouts(modelComponent, plantModel.getVisualLayouts()));

    unmarkAllPropertiesChanged(modelComponent);

    return result;
  }

  private void updateModelLayoutProperties(PointModel model, ModelLayoutElement layoutElement) {
    Map<String, String> properties = layoutElement.getProperties();

    model.getPropertyLayoutPosX().setText(properties.get(ElementPropKeys.POINT_POS_X));
    model.getPropertyLayoutPosY().setText(properties.get(ElementPropKeys.POINT_POS_Y));
    model.getPropertyPointLabelOffsetX()
        .setText(properties.get(ElementPropKeys.POINT_LABEL_OFFSET_X));
    model.getPropertyPointLabelOffsetY()
        .setText(properties.get(ElementPropKeys.POINT_LABEL_OFFSET_Y));
    model.getPropertyPointLabelOrientationAngle()
        .setText(properties.get(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE));
  }

  private void updateModelType(PointModel model, Point point) {
    PointModel.PointType value;

    switch (point.getType()) {
      case PARK_POSITION:
        value = PointModel.PointType.PARK;
        break;
      case REPORT_POSITION:
        value = PointModel.PointType.REPORT;
        break;
      case HALT_POSITION:
      default:
        value = PointModel.PointType.HALT;
    }

    model.getPropertyType().setValue(value);
  }

  private Point.Type getKernelPointType(PointModel model) {
    return convertPointType((PointModel.PointType) model.getPropertyType().getValue());
  }

  private Triple getKernelCoordinates(PointModel model) {
    return convertToTriple(model.getPropertyModelPositionX(),
                           model.getPropertyModelPositionY());
  }

  private double getKernelVehicleAngle(PointModel model) {
    return model.getPropertyVehicleOrientationAngle().getValueByUnit(AngleProperty.Unit.DEG);
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

  @Override
  protected VisualLayoutCreationTO updatedLayout(ModelComponent model,
                                                 VisualLayoutCreationTO layout) {
    PointModel pointModel = (PointModel) model;
    LabeledPointFigure lpf = pointModel.getFigure();
    PointFigure pf = lpf.getPresentationFigure();
    int xPos = (int) (pf.getZoomPoint().getX() * layout.getScaleX());
    int yPos = (int) -(pf.getZoomPoint().getY() * layout.getScaleY());
    Point2D.Double offset = lpf.getLabel().getOffset();

    VisualLayoutCreationTO result
        = layout.withModelElement(new ModelLayoutElementCreationTO(pointModel.getName())
            .withProperty(ElementPropKeys.POINT_POS_X, xPos + "")
            .withProperty(ElementPropKeys.POINT_POS_Y, yPos + "")
            .withProperty(ElementPropKeys.POINT_LABEL_OFFSET_X, (int) offset.x + "")
            .withProperty(ElementPropKeys.POINT_LABEL_OFFSET_Y, (int) offset.y + "")
        );

    pointModel.getPropertyLayoutPosX().unmarkChanged();
    pointModel.getPropertyLayoutPosY().unmarkChanged();
    pointModel.getPropertyPointLabelOrientationAngle().unmarkChanged();
    pointModel.getPropertyPointLabelOffsetX().unmarkChanged();
    pointModel.getPropertyPointLabelOffsetY().unmarkChanged();

    return result;
  }
}
