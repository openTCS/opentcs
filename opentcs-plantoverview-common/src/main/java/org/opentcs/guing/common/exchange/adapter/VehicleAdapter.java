// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.exchange.adapter;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.to.model.BoundingBoxCreationTO;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.base.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.base.components.properties.type.AngleProperty;
import org.opentcs.guing.base.components.properties.type.EnergyLevelThresholdSetModel;
import org.opentcs.guing.base.components.properties.type.KeyValueProperty;
import org.opentcs.guing.base.components.properties.type.PercentProperty;
import org.opentcs.guing.base.components.properties.type.SpeedProperty;
import org.opentcs.guing.base.components.properties.type.SpeedProperty.Unit;
import org.opentcs.guing.base.model.BoundingBoxModel;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.model.SystemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for vehicles.
 */
public class VehicleAdapter
    extends
      AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleAdapter.class);

  public VehicleAdapter() {
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(
      TCSObject<?> tcsObject,
      ModelComponent modelComponent,
      SystemModel systemModel,
      TCSObjectService objectService
  ) {
    requireNonNull(objectService, "objectService");
    Vehicle vehicle = requireNonNull((Vehicle) tcsObject, "tcsObject");
    VehicleModel model = (VehicleModel) modelComponent;

    try {
      model.getPropertyName().setText(vehicle.getName());
      model.getPropertyBoundingBox().setValue(
          new BoundingBoxModel(
              vehicle.getBoundingBox().getLength(),
              vehicle.getBoundingBox().getWidth(),
              vehicle.getBoundingBox().getHeight(),
              new Couple(
                  vehicle.getBoundingBox().getReferenceOffset().getX(),
                  vehicle.getBoundingBox().getReferenceOffset().getY()
              )
          )
      );
      model.getPropertyMaxVelocity().setValueAndUnit(vehicle.getMaxVelocity(), Unit.MM_S);
      model.getPropertyMaxReverseVelocity().setValueAndUnit(
          vehicle.getMaxReverseVelocity(),
          Unit.MM_S
      );
      model.getPropertyEnergyLevel().setValueAndUnit(
          vehicle.getEnergyLevel(),
          PercentProperty.Unit.PERCENT
      );

      model.getPropertyEnergyLevelThresholdSet().setValue(
          new EnergyLevelThresholdSetModel(
              vehicle.getEnergyLevelThresholdSet().getEnergyLevelCritical(),
              vehicle.getEnergyLevelThresholdSet().getEnergyLevelGood(),
              vehicle.getEnergyLevelThresholdSet().getEnergyLevelSufficientlyRecharged(),
              vehicle.getEnergyLevelThresholdSet().getEnergyLevelFullyRecharged()
          )
      );

      model.getPropertyLoaded().setValue(
          vehicle.getLoadHandlingDevices().stream().anyMatch(lhe -> lhe.isFull())
      );
      model.getPropertyState().setValue(vehicle.getState());
      model.getPropertyProcState().setValue(vehicle.getProcState());
      model.getPropertyIntegrationLevel().setValue(vehicle.getIntegrationLevel());
      model.getPropertyPaused().setValue(vehicle.isPaused());

      updateModelCurrentPoint(model, vehicle, systemModel);
      updateModelNextPoint(model, vehicle, systemModel);

      model.getPropertyPrecisePosition().setValue(vehicle.getPose().getPosition());
      model.setPrecisePosition(vehicle.getPose().getPosition());

      model.getPropertyOrientationAngle().setValueAndUnit(
          vehicle.getPose().getOrientationAngle(),
          AngleProperty.Unit.DEG
      );
      model.setOrientationAngle(vehicle.getPose().getOrientationAngle());

      updateCurrentTransportName(vehicle, model);
      updateCurrentOrderSequenceName(vehicle, model);

      model.getPropertyAllowedOrderTypes().setItems(vehicle.getAllowedOrderTypes());
      model.setVehicle(vehicle);

      model.getPropertyEnvelopeKey().setText(vehicle.getEnvelopeKey());

      updateMiscModelProperties(model, vehicle);
      updateModelDriveOrder(objectService, vehicle, model, systemModel);
      updateModelLayoutProperties(model, vehicle);

      model.getAllocatedResources().setItems(vehicle.getAllocatedResources());
      model.getClaimedResources().setItems(vehicle.getClaimedResources());

      model.propertiesChanged(new NullAttributesChangeListener());
    }
    catch (CredentialsException e) {
      LOG.warn("", e);
    }
  }

  @Override // OpenTCSProcessAdapter
  public PlantModelCreationTO storeToPlantModel(
      ModelComponent modelComponent,
      SystemModel systemModel,
      PlantModelCreationTO plantModel
  ) {
    VehicleModel vehicleModel = (VehicleModel) modelComponent;
    return plantModel
        .withVehicle(
            new VehicleCreationTO(vehicleModel.getName())
                .withBoundingBox(getBoundingBox(vehicleModel))
                .withEnergyLevelThresholdSet(getEnergyLevelThresholdSet(vehicleModel))
                .withMaxVelocity(getMaximumVelocity(vehicleModel))
                .withMaxReverseVelocity(getMaximumReverseVelocity(vehicleModel))
                .withEnvelopeKey(getEnvelopeKey(vehicleModel))
                .withProperties(getKernelProperties(vehicleModel))
                .withLayout(getLayout(vehicleModel))
        );
  }

  protected void updateModelDriveOrder(
      TCSObjectService objectService,
      Vehicle vehicle,
      VehicleModel vehicleModel,
      SystemModel systemModel
  )
      throws CredentialsException {
    vehicleModel.setDriveOrderDestination(null);
    vehicleModel.setCurrentDriveOrderPath(null);
  }

  private void updateModelNextPoint(
      VehicleModel vehicleModel,
      Vehicle vehicle,
      SystemModel systemModel
  ) {
    if (vehicle.getNextPosition() != null) {
      PointModel pointModel = systemModel.getPointModel(vehicle.getNextPosition().getName());
      vehicleModel.setNextPoint(pointModel);
      vehicleModel.getPropertyNextPoint().setText(vehicle.getNextPosition().getName());
    }
    else {
      vehicleModel.setNextPoint(null);
      vehicleModel.getPropertyNextPoint().setText("null");
    }
  }

  private void updateModelCurrentPoint(
      VehicleModel vehicleModel,
      Vehicle vehicle,
      SystemModel systemModel
  ) {
    if (vehicle.getCurrentPosition() != null) {
      PointModel pointModel = systemModel.getPointModel(vehicle.getCurrentPosition().getName());

      if (pointModel == null) {
        LOG.error("Error: Point " + vehicle.getCurrentPosition().getName() + "not found.");
      }
      else {
        vehicleModel.placeOnPoint(pointModel);
        vehicleModel.getPropertyPoint().setText(vehicle.getCurrentPosition().getName());
      }
    }
    else {
      vehicleModel.placeOnPoint(null);
      vehicleModel.getPropertyPoint().setText("null");
    }
  }

  private void updateCurrentTransportName(
      Vehicle vehicle,
      VehicleModel vehicleModel
  ) {
    if (vehicle.getTransportOrder() == null) {
      vehicleModel.getPropertyCurrentOrderName().setText("null");
    }
    else {
      vehicleModel.getPropertyCurrentOrderName().setText(vehicle.getTransportOrder().getName());
    }

  }

  private void updateCurrentOrderSequenceName(
      Vehicle vehicle,
      VehicleModel vehicleModel
  ) {
    if (vehicle.getOrderSequence() == null) {
      vehicleModel.getPropertyCurrentSequenceName().setText("null");
    }
    else {
      vehicleModel.getPropertyCurrentSequenceName().setText(vehicle.getOrderSequence().getName());
    }

  }

  private BoundingBoxCreationTO getBoundingBox(VehicleModel model) {
    return new BoundingBoxCreationTO(
        model.getPropertyBoundingBox().getValue().getLength(),
        model.getPropertyBoundingBox().getValue().getWidth(),
        model.getPropertyBoundingBox().getValue().getHeight()
    ).withReferenceOffset(
        new CoupleCreationTO(
            model.getPropertyBoundingBox().getValue().getReferenceOffset().getX(),
            model.getPropertyBoundingBox().getValue().getReferenceOffset().getY()
        )
    );
  }

  private int getMaximumReverseVelocity(VehicleModel model) {
    return ((Double) model.getPropertyMaxReverseVelocity().getValueByUnit(SpeedProperty.Unit.MM_S))
        .intValue();
  }

  private int getMaximumVelocity(VehicleModel model) {
    return ((Double) model.getPropertyMaxVelocity().getValueByUnit(SpeedProperty.Unit.MM_S))
        .intValue();
  }

  private VehicleCreationTO.EnergyLevelThresholdSet getEnergyLevelThresholdSet(VehicleModel model) {
    return new VehicleCreationTO.EnergyLevelThresholdSet(
        model.getPropertyEnergyLevelThresholdSet().getValue().getEnergyLevelCritical(),
        model.getPropertyEnergyLevelThresholdSet().getValue().getEnergyLevelGood(),
        model.getPropertyEnergyLevelThresholdSet().getValue().getEnergyLevelSufficientlyRecharged(),
        model.getPropertyEnergyLevelThresholdSet().getValue().getEnergyLevelFullyRecharged()
    );
  }

  private String getEnvelopeKey(VehicleModel model) {
    return model.getPropertyEnvelopeKey().getText();
  }

  private VehicleCreationTO.Layout getLayout(VehicleModel model) {
    return new VehicleCreationTO.Layout(model.getPropertyRouteColor().getColor());
  }

  @Override // OpenTCSProcessAdapter
  protected void updateMiscModelProperties(ModelComponent model, TCSObject<?> tcsObject) {
    VehicleModel vehicleModel = (VehicleModel) model;
    List<KeyValueProperty> items = new ArrayList<>();

    for (Map.Entry<String, String> curEntry : tcsObject.getProperties().entrySet()) {
      if (!curEntry.getValue().contains("Unknown")) {
        items.add(new KeyValueProperty(vehicleModel, curEntry.getKey(), curEntry.getValue()));
      }
    }

    vehicleModel.getPropertyMiscellaneous().setItems(items);
  }

  private void updateModelLayoutProperties(VehicleModel model, Vehicle vehicle) {
    model.getPropertyRouteColor().setColor(vehicle.getLayout().getRouteColor());
  }
}
