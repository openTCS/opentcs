/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.base.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.base.components.properties.type.AngleProperty;
import org.opentcs.guing.base.components.properties.type.KeyValueProperty;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.components.properties.type.PercentProperty;
import org.opentcs.guing.base.components.properties.type.SpeedProperty;
import org.opentcs.guing.base.components.properties.type.SpeedProperty.Unit;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.model.SystemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for vehicles.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleAdapter.class);

  public VehicleAdapter() {
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService) {
    requireNonNull(objectService, "objectService");
    Vehicle vehicle = requireNonNull((Vehicle) tcsObject, "tcsObject");
    VehicleModel model = (VehicleModel) modelComponent;

    try {
      model.getPropertyName().setText(vehicle.getName());
      model.getPropertyLength().setValueAndUnit(vehicle.getLength(), LengthProperty.Unit.MM);
      model.getPropertyMaxVelocity().setValueAndUnit(vehicle.getMaxVelocity(), Unit.MM_S);
      model.getPropertyMaxReverseVelocity().setValueAndUnit(vehicle.getMaxReverseVelocity(),
                                                            Unit.MM_S);
      model.getPropertyEnergyLevelCritical().setValueAndUnit(vehicle.getEnergyLevelCritical(),
                                                             PercentProperty.Unit.PERCENT);
      model.getPropertyEnergyLevelGood().setValueAndUnit(vehicle.getEnergyLevelGood(),
                                                         PercentProperty.Unit.PERCENT);
      model.getPropertyEnergyLevelFullyRecharged()
          .setValueAndUnit(vehicle.getEnergyLevelFullyRecharged(),
                           PercentProperty.Unit.PERCENT);
      model.getPropertyEnergyLevelSufficientlyRecharged()
          .setValueAndUnit(vehicle.getEnergyLevelSufficientlyRecharged(),
                           PercentProperty.Unit.PERCENT);
      model.getPropertyEnergyLevel().setValueAndUnit(vehicle.getEnergyLevel(),
                                                     PercentProperty.Unit.PERCENT);

      model.getPropertyLoaded().setValue(vehicle.getLoadHandlingDevices().stream().anyMatch(lhe -> lhe.isFull()));
      model.getPropertyState().setValue(vehicle.getState());
      model.getPropertyProcState().setValue(vehicle.getProcState());
      model.getPropertyIntegrationLevel().setValue(vehicle.getIntegrationLevel());
      model.getPropertyPaused().setValue(vehicle.isPaused());

      updateModelCurrentPoint(model, vehicle, systemModel);
      updateModelNextPoint(model, vehicle, systemModel);

      model.getPropertyPrecisePosition().setValue(vehicle.getPrecisePosition());
      model.setPrecisePosition(vehicle.getPrecisePosition());

      model.getPropertyOrientationAngle().setValueAndUnit(vehicle.getOrientationAngle(),
                                                          AngleProperty.Unit.DEG);
      model.setOrientationAngle(vehicle.getOrientationAngle());

      updateCurrentTransportName(vehicle, model);
      updateCurrentOrderSequenceName(vehicle, model);

      model.getPropertyAllowedOrderTypes().setItems(vehicle.getAllowedOrderTypes());
      model.setVehicle(vehicle);

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
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    VehicleModel vehicleModel = (VehicleModel) modelComponent;
    return plantModel
        .withVehicle(
            new VehicleCreationTO(vehicleModel.getName())
                .withLength(getLength(vehicleModel))
                .withEnergyLevelCritical(getEnergyLevelCritical(vehicleModel))
                .withEnergyLevelGood(getEnergyLevelGood(vehicleModel))
                .withEnergyLevelFullyRecharged(getEnergyLevelFullyRecharged(vehicleModel))
                .withEnergyLevelSufficientlyRecharged(getEnergyLevelSufficientlyRecharged(vehicleModel))
                .withMaxVelocity(getMaximumVelocity(vehicleModel))
                .withMaxReverseVelocity(getMaximumReverseVelocity(vehicleModel))
                .withProperties(getKernelProperties(vehicleModel))
                .withLayout(getLayout(vehicleModel))
        );
  }

  protected void updateModelDriveOrder(TCSObjectService objectService,
                                       Vehicle vehicle,
                                       VehicleModel vehicleModel,
                                       SystemModel systemModel)
      throws CredentialsException {
    vehicleModel.setDriveOrderDestination(null);
    vehicleModel.setCurrentDriveOrderPath(null);
  }

  private void updateModelNextPoint(VehicleModel vehicleModel,
                                    Vehicle vehicle,
                                    SystemModel systemModel) {
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

  private void updateModelCurrentPoint(VehicleModel vehicleModel,
                                       Vehicle vehicle,
                                       SystemModel systemModel) {
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

  private void updateCurrentTransportName(Vehicle vehicle,
                                          VehicleModel vehicleModel) {
    if (vehicle.getTransportOrder() == null) {
      vehicleModel.getPropertyCurrentOrderName().setText("null");
    }
    else {
      vehicleModel.getPropertyCurrentOrderName().setText(vehicle.getTransportOrder().getName());
    }

  }

  private void updateCurrentOrderSequenceName(Vehicle vehicle,
                                              VehicleModel vehicleModel) {
    if (vehicle.getOrderSequence() == null) {
      vehicleModel.getPropertyCurrentSequenceName().setText("null");
    }
    else {
      vehicleModel.getPropertyCurrentSequenceName().setText(vehicle.getOrderSequence().getName());
    }

  }

  private int getLength(VehicleModel model) {
    return ((Double) model.getPropertyLength().getValueByUnit(LengthProperty.Unit.MM))
        .intValue();
  }

  private int getMaximumReverseVelocity(VehicleModel model) {
    return ((Double) model.getPropertyMaxReverseVelocity().getValueByUnit(SpeedProperty.Unit.MM_S)).intValue();
  }

  private int getMaximumVelocity(VehicleModel model) {
    return ((Double) model.getPropertyMaxVelocity().getValueByUnit(SpeedProperty.Unit.MM_S))
        .intValue();
  }

  private int getEnergyLevelCritical(VehicleModel model) {
    return (Integer) model.getPropertyEnergyLevelCritical().getValue();
  }

  private int getEnergyLevelGood(VehicleModel model) {
    return (Integer) model.getPropertyEnergyLevelGood().getValue();
  }

  private int getEnergyLevelFullyRecharged(VehicleModel model) {
    return (Integer) model.getPropertyEnergyLevelFullyRecharged().getValue();
  }

  private int getEnergyLevelSufficientlyRecharged(VehicleModel model) {
    return (Integer) model.getPropertyEnergyLevelSufficientlyRecharged().getValue();
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
