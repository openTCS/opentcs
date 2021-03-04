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

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty.Unit;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.Colors;
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

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService,
                                    @Nullable ModelLayoutElement layoutElement) {
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
      model.getPropertyEnergyLevel().setValueAndUnit(vehicle.getEnergyLevel(),
                                                     PercentProperty.Unit.PERCENT);

      updateModelEnergyState(model, vehicle);

      model.getPropertyLoaded().setValue(vehicle.getLoadHandlingDevices().stream().anyMatch(lhe -> lhe.isFull()));
      model.getPropertyState().setValue(vehicle.getState());
      model.getPropertyProcState().setValue(vehicle.getProcState());
      model.getPropertyIntegrationLevel().setValue(vehicle.getIntegrationLevel());

      updateModelCurrentPoint(model, vehicle, systemModel);
      updateModelNextPoint(model, vehicle, systemModel);

      model.getPropertyPrecisePosition().setValue(vehicle.getPrecisePosition());
      model.setPrecisePosition(vehicle.getPrecisePosition());

      model.getPropertyOrientationAngle().setValueAndUnit(vehicle.getOrientationAngle(),
                                                          AngleProperty.Unit.DEG);
      model.setOrientationAngle(vehicle.getOrientationAngle());

      updateCurrentTransportName(vehicle, model);
      updateCurrentOrderSequenceName(vehicle, model);

      model.getPropertyProcessableCategories().setItems(vehicle.getProcessableCategories());
      model.setVehicle(vehicle);

      updateMiscModelProperties(model, vehicle);
      updateModelDriveOrder(objectService, vehicle, model, systemModel);

      if (layoutElement != null) {
        updateModelLayoutProperties(model, layoutElement);
      }

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
                .withMaxVelocity(getMaximumVelocity(vehicleModel))
                .withMaxReverseVelocity(getMaximumReverseVelocity(vehicleModel))
                .withProperties(getKernelProperties(vehicleModel))
        )
        .withVisualLayouts(updatedLayouts(vehicleModel, plantModel.getVisualLayouts()));
  }

  @Override
  protected VisualLayoutCreationTO updatedLayout(ModelComponent model,
                                                 VisualLayoutCreationTO layout) {
    VehicleModel vehicleModel = (VehicleModel) model;
    return layout.withModelElement(
        new ModelLayoutElementCreationTO(vehicleModel.getName())
            .withProperty(ElementPropKeys.VEHICLE_ROUTE_COLOR,
                          Colors.encodeToHexRGB(vehicleModel.getPropertyRouteColor().getColor()))
    );
  }

  private void updateModelDriveOrder(TCSObjectService objectService,
                                     Vehicle vehicle,
                                     VehicleModel vehicleModel,
                                     SystemModel systemModel)
      throws CredentialsException {
    TransportOrder transportOrder = getTransportOrder(objectService, vehicle.getTransportOrder());

    if (transportOrder != null) {
      List<FigureComponent> c = composeDriveOrderComponents(transportOrder.getCurrentDriveOrder(),
                                                            vehicle.getRouteProgressIndex(),
                                                            systemModel);
      vehicleModel.setDriveOrderComponents(c);
      vehicleModel.setDriveOrderState(transportOrder.getState());
    }
    else {
      vehicleModel.setDriveOrderComponents(null);
    }
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

  private void updateModelEnergyState(VehicleModel vehicleModel, Vehicle vehicle) {
    if (vehicle.isEnergyLevelCritical()) {
      vehicleModel.getPropertyEnergyState().setValue(VehicleModel.EnergyState.CRITICAL);
      vehicleModel.getPropertyEnergyState().setHelptext(
          ResourceBundleUtil.getBundle().getString("vehicle.energyLevelCritical.helptext"));
    }
    else if (vehicle.isEnergyLevelDegraded()) {
      vehicleModel.getPropertyEnergyState().setValue(VehicleModel.EnergyState.DEGRADED);
      vehicleModel.getPropertyEnergyState().setHelptext(
          ResourceBundleUtil.getBundle().getString("vehicle.energyLevelDegraded.helptext"));
    }
    else if (vehicle.isEnergyLevelGood()) {
      vehicleModel.getPropertyEnergyState().setValue(VehicleModel.EnergyState.GOOD);
      vehicleModel.getPropertyEnergyState().setHelptext(
          ResourceBundleUtil.getBundle().getString("vehicle.energyLevelGood.helptext"));
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

  @Nullable
  private TransportOrder getTransportOrder(TCSObjectService objectService,
                                           TCSObjectReference<TransportOrder> ref)
      throws CredentialsException {
    if (ref == null) {
      return null;
    }
    return objectService.fetchObject(TransportOrder.class, ref);
  }

  /**
   * Extracts the left over course elements from a drive order and progress.
   *
   * @param driveOrder The <code>DriveOrder</code>.
   * @param routeProgressIndex Index of the current position in the drive order.
   * @return List containing the left over course elements or <code>null</code>
   * if driveOrder is <code>null</code>.
   */
  private List<FigureComponent> composeDriveOrderComponents(@Nullable DriveOrder driveOrder,
                                                            int routeProgressIndex,
                                                            SystemModel systemModel) {
    if (driveOrder == null) {
      return null;
    }

    List<FigureComponent> result = new LinkedList<>();
    List<Route.Step> lSteps = driveOrder.getRoute().getSteps();

    ProcessAdapter adapter;
    for (int i = lSteps.size() - 1; i >= 0; i--) {
      if (i == routeProgressIndex) {
        break;
      }

      Route.Step step = lSteps.get(i);
      Path path = step.getPath();
      Point point = step.getDestinationPoint();
      PointModel pointModel = systemModel.getPointModel(point.getName());
      result.add(0, pointModel);

      if (path != null) {
        PathModel pathModel = systemModel.getPathModel(path.getName());
        result.add(0, pathModel);
      }
    }

    TCSObjectReference<?> ref = driveOrder.getDestination().getDestination();
    ModelComponent pointOrLocationModel = systemModel.getModelComponent(ref.getName());
    if (pointOrLocationModel != null) {
      result.add((FigureComponent) pointOrLocationModel);
    }

    return result;
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

  private void updateModelLayoutProperties(VehicleModel model, ModelLayoutElement layoutElement) {
    String sRouteColor = layoutElement.getProperties().get(ElementPropKeys.VEHICLE_ROUTE_COLOR);
    if (sRouteColor != null) {
      String srgb = sRouteColor.substring(1); // delete contained '#'
      model.getPropertyRouteColor().setColor(new Color(Integer.parseInt(srgb, 16)));
    }
  }
}
