// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.exchange.adapter;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.BoundingBoxCreationTO;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.EnvelopeCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.PoseCreationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Point;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.components.properties.type.AngleProperty;
import org.opentcs.guing.base.components.properties.type.CoordinateProperty;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.model.BoundingBoxModel;
import org.opentcs.guing.base.model.EnvelopeModel;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.common.model.SystemModel;

/**
 * An adapter for points.
 */
public class PointAdapter
    extends
      AbstractProcessAdapter {

  /**
   * Creates a new instance.
   */
  public PointAdapter() {
  }

  @Override  // OpenTCSProcessAdapter
  public void updateModelProperties(
      TCSObject<?> tcsObject,
      ModelComponent modelComponent,
      SystemModel systemModel,
      TCSObjectService objectService
  ) {
    Point point = requireNonNull((Point) tcsObject, "tcsObject");
    PointModel model = (PointModel) modelComponent;

    // Name
    model.getPropertyName().setText(point.getName());

    // Position in model
    model.getPropertyModelPositionX().setValueAndUnit(
        point.getPose().getPosition().getX(),
        LengthProperty.Unit.MM
    );
    model.getPropertyModelPositionY().setValueAndUnit(
        point.getPose().getPosition().getY(),
        LengthProperty.Unit.MM
    );
    model.getPropertyVehicleOrientationAngle()
        .setValueAndUnit(point.getPose().getOrientationAngle(), AngleProperty.Unit.DEG);

    updateModelType(model, point);

    for (Map.Entry<String, Envelope> entry : point.getVehicleEnvelopes().entrySet()) {
      model.getPropertyVehicleEnvelopes().getValue().add(
          new EnvelopeModel(entry.getKey(), entry.getValue().getVertices())
      );
    }

    model.getPropertyMaxVehicleBoundingBox()
        .setValue(
            new BoundingBoxModel(
                point.getMaxVehicleBoundingBox().getLength(),
                point.getMaxVehicleBoundingBox().getWidth(),
                point.getMaxVehicleBoundingBox().getHeight(),
                new Couple(
                    point.getMaxVehicleBoundingBox().getReferenceOffset().getX(),
                    point.getMaxVehicleBoundingBox().getReferenceOffset().getY()
                )
            )
        );

    updateMiscModelProperties(model, point);
    updateModelLayoutProperties(model, point, systemModel.getLayoutModel());
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(
      ModelComponent modelComponent,
      SystemModel systemModel,
      PlantModelCreationTO plantModel
  ) {
    PlantModelCreationTO result = plantModel
        .withPoint(
            new PointCreationTO(modelComponent.getName())
                .withPose(
                    new PoseCreationTO(
                        getKernelCoordinates((PointModel) modelComponent),
                        getKernelVehicleAngle((PointModel) modelComponent)
                    )
                )
                .withType(getKernelPointType((PointModel) modelComponent))
                .withVehicleEnvelopes(getKernelVehicleEnvelopes((PointModel) modelComponent))
                .withMaxVehicleBoundingBox(
                    getKernelMaxVehicleBoundingBox((PointModel) modelComponent)
                )
                .withProperties(getKernelProperties(modelComponent))
                .withLayout(getLayout((PointModel) modelComponent))
        );

    unmarkAllPropertiesChanged(modelComponent);

    return result;
  }

  private void updateModelLayoutProperties(PointModel model, Point point, LayoutModel layoutModel) {
    model.getPropertyPointLabelOffsetX()
        .setText(String.valueOf(point.getLayout().getLabelOffset().getX()));
    model.getPropertyPointLabelOffsetY()
        .setText(String.valueOf(point.getLayout().getLabelOffset().getY()));
    LayerWrapper layerWrapper = layoutModel.getPropertyLayerWrappers()
        .getValue().get(point.getLayout().getLayerId());
    model.getPropertyLayerWrapper().setValue(layerWrapper);
  }

  private void updateModelType(PointModel model, Point point) {
    PointModel.Type value;

    switch (point.getType()) {
      case PARK_POSITION:
        value = PointModel.Type.PARK;
        break;
      case HALT_POSITION:
      default:
        value = PointModel.Type.HALT;
    }

    model.getPropertyType().setValue(value);
  }

  private PointCreationTO.Type getKernelPointType(PointModel model) {
    return convertPointType((PointModel.Type) model.getPropertyType().getValue());
  }

  private TripleCreationTO getKernelCoordinates(PointModel model) {
    return convertToTripleCreationTO(
        model.getPropertyModelPositionX(),
        model.getPropertyModelPositionY()
    );
  }

  private double getKernelVehicleAngle(PointModel model) {
    return model.getPropertyVehicleOrientationAngle().getValueByUnit(AngleProperty.Unit.DEG);
  }

  private Map<String, EnvelopeCreationTO> getKernelVehicleEnvelopes(PointModel model) {
    return model.getPropertyVehicleEnvelopes().getValue().stream()
        .collect(
            Collectors.toMap(
                EnvelopeModel::getKey,
                envelopeModel -> new EnvelopeCreationTO(
                    toCoupleCreationTOs(envelopeModel.getVertices())
                )
            )
        );
  }

  private List<CoupleCreationTO> toCoupleCreationTOs(List<Couple> couples) {
    return couples.stream()
        .map(
            couple -> new CoupleCreationTO(
                couple.getX(),
                couple.getY()
            )
        )
        .toList();
  }

  private BoundingBoxCreationTO getKernelMaxVehicleBoundingBox(PointModel model) {
    return new BoundingBoxCreationTO(
        model.getPropertyMaxVehicleBoundingBox().getValue().getLength(),
        model.getPropertyMaxVehicleBoundingBox().getValue().getWidth(),
        model.getPropertyMaxVehicleBoundingBox().getValue().getHeight()
    )
        .withReferenceOffset(
            new CoupleCreationTO(
                model.getPropertyMaxVehicleBoundingBox().getValue().getReferenceOffset().getX(),
                model.getPropertyMaxVehicleBoundingBox().getValue().getReferenceOffset().getY()
            )
        );
  }

  private PointCreationTO.Layout getLayout(PointModel model) {
    return new PointCreationTO.Layout(
        new CoupleCreationTO(
            Long.parseLong(model.getPropertyPointLabelOffsetX().getText()),
            Long.parseLong(model.getPropertyPointLabelOffsetY().getText())
        ),
        model.getPropertyLayerWrapper().getValue().getLayer().getId()
    );
  }

  private PointCreationTO.Type convertPointType(PointModel.Type type) {
    requireNonNull(type, "type");
    return switch (type) {
      case PARK -> PointCreationTO.Type.PARK_POSITION;
      case HALT -> PointCreationTO.Type.HALT_POSITION;
    };
  }

  private TripleCreationTO convertToTripleCreationTO(
      CoordinateProperty cpx, CoordinateProperty cpy
  ) {
    TripleCreationTO result = new TripleCreationTO(
        (int) cpx.getValueByUnit(LengthProperty.Unit.MM),
        (int) cpy.getValueByUnit(LengthProperty.Unit.MM),
        0
    );

    return result;
  }
}
