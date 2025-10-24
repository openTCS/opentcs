// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.persistence.unified;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.EnvelopeCreationTO;
import org.opentcs.access.to.model.LayerCreationTO;
import org.opentcs.access.to.model.LayerGroupCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.components.properties.type.AngleProperty;
import org.opentcs.guing.base.components.properties.type.KeyValueProperty;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.components.properties.type.SpeedProperty;
import org.opentcs.guing.base.model.BoundingBoxModel;
import org.opentcs.guing.base.model.EnergyLevelThresholdSetModel;
import org.opentcs.guing.base.model.EnvelopeModel;
import org.opentcs.guing.base.model.PeripheralOperationModel;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.model.SystemModel;

/**
 */
public class PlantModelElementConverter {

  public PlantModelElementConverter() {
  }

  public PointModel importPoint(PointCreationTO pointTO, SystemModel systemModel) {
    requireNonNull(pointTO, "pointTO");
    requireNonNull(systemModel, "systemModel");

    PointModel model = new PointModel();

    model.setName(pointTO.getName());

    model.getPropertyModelPositionX().setValueAndUnit(
        pointTO.getPose().getPosition().getX(),
        LengthProperty.Unit.MM
    );
    model.getPropertyModelPositionY().setValueAndUnit(
        pointTO.getPose().getPosition().getY(),
        LengthProperty.Unit.MM
    );
    model.getPropertyVehicleOrientationAngle().setValueAndUnit(
        pointTO.getPose().getOrientationAngle(),
        AngleProperty.Unit.DEG
    );
    model.getPropertyType().setValue(mapPointType(pointTO.getType()));

    for (Map.Entry<String, EnvelopeCreationTO> entry : pointTO.getVehicleEnvelopes().entrySet()) {
      model.getPropertyVehicleEnvelopes().getValue().add(
          new EnvelopeModel(entry.getKey(), mapCouples(entry.getValue().getVertices()))
      );
    }

    model.getPropertyMaxVehicleBoundingBox().setValue(
        new BoundingBoxModel(
            pointTO.getMaxVehicleBoundingBox().getLength(),
            pointTO.getMaxVehicleBoundingBox().getWidth(),
            pointTO.getMaxVehicleBoundingBox().getHeight(),
            new Couple(
                pointTO.getMaxVehicleBoundingBox().getReferenceOffset().getX(),
                pointTO.getMaxVehicleBoundingBox().getReferenceOffset().getY()
            )
        )
    );

    for (Map.Entry<String, String> property : pointTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(
          new KeyValueProperty(
              model,
              property.getKey(),
              property.getValue()
          )
      );
    }

    // Gather information contained in the layout
    model.getPropertyPointLabelOffsetX().setText(
        String.valueOf(pointTO.getLayout().getLabelOffset().getX())
    );
    model.getPropertyPointLabelOffsetY().setText(
        String.valueOf(pointTO.getLayout().getLabelOffset().getY())
    );
    model.getPropertyPointLabelOrientationAngle().setText("");
    LayerWrapper layerWrapper = systemModel.getLayoutModel().getPropertyLayerWrappers()
        .getValue().get(pointTO.getLayout().getLayerId());
    model.getPropertyLayerWrapper().setValue(layerWrapper);

    return model;
  }

  public PathModel importPath(PathCreationTO pathTO, SystemModel systemModel) {
    PathModel model = new PathModel();

    model.setName(pathTO.getName());
    model.getPropertyLength().setValueAndUnit(pathTO.getLength(), LengthProperty.Unit.MM);
    model.getPropertyMaxVelocity().setValueAndUnit(
        pathTO.getMaxVelocity(),
        SpeedProperty.Unit.MM_S
    );
    model.getPropertyMaxReverseVelocity().setValueAndUnit(
        pathTO.getMaxReverseVelocity(),
        SpeedProperty.Unit.MM_S
    );
    model.getPropertyStartComponent().setText(pathTO.getSrcPointName());
    model.getPropertyEndComponent().setText(pathTO.getDestPointName());
    model.getPropertyLocked().setValue(pathTO.isLocked());

    for (Map.Entry<String, EnvelopeCreationTO> entry : pathTO.getVehicleEnvelopes().entrySet()) {
      model.getPropertyVehicleEnvelopes().getValue().add(
          new EnvelopeModel(entry.getKey(), mapCouples(entry.getValue().getVertices()))
      );
    }

    for (Map.Entry<String, String> property : pathTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(
          new KeyValueProperty(
              model,
              property.getKey(),
              property.getValue()
          )
      );
    }

    for (PeripheralOperationCreationTO operationTO : pathTO.getPeripheralOperations()) {
      model.getPropertyPeripheralOperations().getValue().add(
          new PeripheralOperationModel(
              operationTO.getLocationName(),
              operationTO.getOperation(),
              mapExecutionTrigger(operationTO.getExecutionTrigger()),
              operationTO.isCompletionRequired()
          )
      );
    }

    // Gather information contained in the layout
    model.getPropertyPathConnType()
        .setValue(PathModel.Type.valueOf(pathTO.getLayout().getConnectionType().name()));
    model.getPropertyPathControlPoints().setText(
        pathTO.getLayout().getControlPoints().stream()
            .map(controlPoint -> String.format("%d,%d", controlPoint.getX(), controlPoint.getY()))
            .collect(Collectors.joining(";"))
    );
    LayerWrapper layerWrapper = systemModel.getLayoutModel().getPropertyLayerWrappers()
        .getValue().get(pathTO.getLayout().getLayerId());
    model.getPropertyLayerWrapper().setValue(layerWrapper);

    return model;
  }

  public VehicleModel importVehicle(VehicleCreationTO vehicleTO) {
    VehicleModel model = new VehicleModel();

    model.setName(vehicleTO.getName());
    model.getPropertyBoundingBox().setValue(
        new BoundingBoxModel(
            vehicleTO.getBoundingBox().getLength(),
            vehicleTO.getBoundingBox().getWidth(),
            vehicleTO.getBoundingBox().getHeight(),
            new Couple(
                vehicleTO.getBoundingBox().getReferenceOffset().getX(),
                vehicleTO.getBoundingBox().getReferenceOffset().getY()
            )
        )
    );
    model.getPropertyMaxVelocity().setValueAndUnit(
        ((double) vehicleTO.getMaxVelocity()),
        SpeedProperty.Unit.MM_S
    );
    model.getPropertyMaxReverseVelocity().setValueAndUnit(
        ((double) vehicleTO.getMaxReverseVelocity()), SpeedProperty.Unit.MM_S
    );

    model.getPropertyEnergyLevelThresholdSet().setValue(
        new EnergyLevelThresholdSetModel(
            vehicleTO.getEnergyLevelThresholdSet().getEnergyLevelCritical(),
            vehicleTO.getEnergyLevelThresholdSet().getEnergyLevelGood(),
            vehicleTO.getEnergyLevelThresholdSet().getEnergyLevelSufficientlyRecharged(),
            vehicleTO.getEnergyLevelThresholdSet().getEnergyLevelFullyRecharged()
        )
    );

    model.getPropertyEnvelopeKey().setText(vehicleTO.getEnvelopeKey());

    for (Map.Entry<String, String> property : vehicleTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(
          new KeyValueProperty(
              model,
              property.getKey(),
              property.getValue()
          )
      );
    }

    // Gather information contained in the layout
    model.getPropertyRouteColor().setColor(vehicleTO.getLayout().getRouteColor());

    return model;
  }

  public LocationTypeModel importLocationType(LocationTypeCreationTO locTypeTO) {
    LocationTypeModel model = new LocationTypeModel();

    model.setName(locTypeTO.getName());
    for (String allowedOperation : locTypeTO.getAllowedOperations()) {
      model.getPropertyAllowedOperations().addItem(allowedOperation);
    }

    for (String allowedPeripheralOperations : locTypeTO.getAllowedPeripheralOperations()) {
      model.getPropertyAllowedPeripheralOperations().addItem(allowedPeripheralOperations);
    }

    for (Map.Entry<String, String> property : locTypeTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(
          new KeyValueProperty(
              model,
              property.getKey(),
              property.getValue()
          )
      );
    }

    // Gather information contained in the layout
    model.getPropertyDefaultRepresentation().setLocationRepresentation(
        LocationRepresentation.valueOf(locTypeTO.getLayout().getLocationRepresentation().name())
    );

    return model;
  }

  public LocationModel importLocation(
      LocationCreationTO locationTO,
      Collection<LocationTypeCreationTO> locTypes,
      SystemModel systemModel
  ) {
    LocationModel model = new LocationModel();

    model.setName(locationTO.getName());
    model.getPropertyModelPositionX().setValueAndUnit(
        locationTO.getPosition().getX(),
        LengthProperty.Unit.MM
    );
    model.getPropertyModelPositionY().setValueAndUnit(
        locationTO.getPosition().getY(),
        LengthProperty.Unit.MM
    );

    List<String> possibleLocationTypes = new ArrayList<>();
    for (LocationTypeCreationTO locType : locTypes) {
      if (!possibleLocationTypes.contains(locType.getName())) {
        possibleLocationTypes.add(locType.getName());
      }
    }
    model.getPropertyType().setPossibleValues(possibleLocationTypes);
    model.getPropertyType().setValue(locationTO.getTypeName());
    model.getPropertyLocked().setValue(locationTO.isLocked());

    for (Map.Entry<String, String> property : locationTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(
          new KeyValueProperty(
              model,
              property.getKey(),
              property.getValue()
          )
      );
    }

    // Gather information contained in the layout
    model.getPropertyLabelOffsetX().setText(
        String.valueOf(locationTO.getLayout().getLabelOffset().getX())
    );
    model.getPropertyLabelOffsetY().setText(
        String.valueOf(locationTO.getLayout().getLabelOffset().getY())
    );
    model.getPropertyDefaultRepresentation().setLocationRepresentation(
        LocationRepresentation.valueOf(locationTO.getLayout().getLocationRepresentation().name())
    );
    model.getPropertyLabelOrientationAngle().setText("");
    LayerWrapper layerWrapper = systemModel.getLayoutModel().getPropertyLayerWrappers()
        .getValue().get(locationTO.getLayout().getLayerId());
    model.getPropertyLayerWrapper().setValue(layerWrapper);

    return model;
  }

  public LinkModel importLocationLink(
      LocationCreationTO locationTO,
      String pointName,
      Set<String> operations,
      SystemModel systemModel
  ) {
    LinkModel model = new LinkModel();

    model.setName(String.format("%s --- %s", pointName, locationTO.getName()));

    for (String operation : operations) {
      model.getPropertyAllowedOperations().addItem(operation);
    }

    model.getPropertyStartComponent().setText(pointName);
    model.getPropertyEndComponent().setText(locationTO.getName());
    LayerWrapper layerWrapper = systemModel.getLayoutModel().getPropertyLayerWrappers()
        .getValue().get(locationTO.getLayout().getLayerId());
    model.getPropertyLayerWrapper().setValue(layerWrapper);

    return model;
  }

  public BlockModel importBlock(BlockCreationTO blockTO) {
    BlockModel model = new BlockModel();

    model.setName(blockTO.getName());

    model.getPropertyType().setValue(mapBlockType(blockTO.getType()));

    for (String member : blockTO.getMemberNames()) {
      model.getPropertyElements().addItem(member);
    }

    for (Map.Entry<String, String> property : blockTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(
          new KeyValueProperty(
              model,
              property.getKey(),
              property.getValue()
          )
      );
    }

    // Gather information contained in the layout
    model.getPropertyColor().setColor(blockTO.getLayout().getColor());

    return model;
  }

  public LayoutModel importLayout(VisualLayoutCreationTO layoutTO) {
    LayoutModel model = new LayoutModel();

    model.setName(layoutTO.getName());
    model.getPropertyScaleX().setValueAndUnit(layoutTO.getScaleX(), LengthProperty.Unit.MM);
    model.getPropertyScaleY().setValueAndUnit(layoutTO.getScaleY(), LengthProperty.Unit.MM);
    initLayerGroups(model, layoutTO.getLayerGroups());
    initLayers(model, layoutTO.getLayers());

    for (Map.Entry<String, String> property : layoutTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(
          new KeyValueProperty(
              model,
              property.getKey(),
              property.getValue()
          )
      );
    }

    return model;
  }

  private PointModel.Type mapPointType(PointCreationTO.Type type) {
    return switch (type) {
      case HALT_POSITION -> PointModel.Type.HALT;
      case PARK_POSITION -> PointModel.Type.PARK;
    };
  }

  private BlockModel.Type mapBlockType(BlockCreationTO.Type type) {
    return switch (type) {
      case SINGLE_VEHICLE_ONLY -> BlockModel.Type.SINGLE_VEHICLE_ONLY;
      case SAME_DIRECTION_ONLY -> BlockModel.Type.SAME_DIRECTION_ONLY;
    };
  }

  private List<Couple> mapCouples(List<CoupleCreationTO> couples) {
    return couples.stream()
        .map(
            couple -> new Couple(
                couple.getX(),
                couple.getY()
            )
        )
        .toList();
  }

  private PeripheralOperation.ExecutionTrigger mapExecutionTrigger(
      PeripheralOperationCreationTO.ExecutionTrigger executionTrigger
  ) {
    return switch (executionTrigger) {
      case IMMEDIATE -> PeripheralOperation.ExecutionTrigger.IMMEDIATE;
      case AFTER_ALLOCATION -> PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION;
      case AFTER_MOVEMENT -> PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT;
    };
  }

  private void initLayerGroups(LayoutModel model, Collection<LayerGroupCreationTO> groups) {
    Map<Integer, LayerGroup> layerGroups = model.getPropertyLayerGroups().getValue();
    layerGroups.clear();
    for (LayerGroupCreationTO groupCreationTO : groups) {
      layerGroups.put(
          groupCreationTO.getId(),
          new LayerGroup(
              groupCreationTO.getId(),
              groupCreationTO.getName(),
              groupCreationTO.isVisible()
          )
      );
    }
  }

  private void initLayers(LayoutModel model, Collection<LayerCreationTO> layers) {
    Map<Integer, LayerWrapper> layerWrappers = model.getPropertyLayerWrappers().getValue();
    layerWrappers.clear();

    Map<Integer, LayerGroup> layerGroups = model.getPropertyLayerGroups().getValue();
    for (LayerCreationTO layerCreationTO : layers) {
      layerWrappers.put(
          layerCreationTO.getId(),
          new LayerWrapper(
              new Layer(
                  layerCreationTO.getId(),
                  layerCreationTO.getOrdinal(),
                  layerCreationTO.isVisible(),
                  layerCreationTO.getName(),
                  layerCreationTO.getGroupId()
              ),
              layerGroups.get(layerCreationTO.getGroupId())
          )
      );
    }
  }
}
