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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.guing.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.components.properties.type.AbstractProperty;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.TripleProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import static org.opentcs.guing.model.elements.VehicleModel.ENERGY_LEVEL_CRITICAL;
import static org.opentcs.guing.model.elements.VehicleModel.ENERGY_LEVEL_GOOD;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Objects.requireNonNull;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.components.properties.type.OrderCategoriesProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty.Unit;

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
  /**
   * Handles vehicle processable categories updates.
   */
  private final VehicleProcessableCategoriesAdapter vehicleProcessableCategoriesAdapter;

  /**
   * Creates a new instance.
   *
   * @param kernelProvider A kernel provider.
   * @param applicationState The plant overviews mode.
   * @param model The corresponding model component.
   * @param eventDispatcher The event dispatcher.
   */
  @Inject
  public VehicleAdapter(SharedKernelProvider kernelProvider,
                        ApplicationState applicationState,
                        @Assisted VehicleModel model,
                        @Assisted EventDispatcher eventDispatcher) {
    super(model, eventDispatcher);
    this.vehicleProcessableCategoriesAdapter
        = new VehicleProcessableCategoriesAdapter(kernelProvider, applicationState, model);
  }

  @Override
  public VehicleModel getModel() {
    return (VehicleModel) super.getModel();
  }

  @Override
  public void register() {
    super.register();
    getModel().addAttributesChangeListener(vehicleProcessableCategoriesAdapter);
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(Kernel kernel,
                                    TCSObject<?> tcsObject,
                                    @Nullable ModelLayoutElement layoutElement) {
    requireNonNull(kernel, "kernel");
    Vehicle vehicle = requireNonNull((Vehicle) tcsObject, "tcsObject");

    try {
      VehicleModel vehicleModel = getModel();
      updateModelName(vehicleModel, vehicle);
      updateModelLength(vehicle, vehicleModel);
      updateModelMaxVelocity(vehicle, vehicleModel);
      updateModelMaxReverseVelocity(vehicle, vehicleModel);
      updateModelEnergy(vehicle, vehicleModel);
      updateModelEnergyState(vehicleModel, vehicle);
      updateModelLoadedState(vehicleModel, vehicle);
      updateModelState(vehicle, vehicleModel);
      updateModelCurrentPoint(vehicleModel, vehicle);
      updateModelNextPoint(vehicleModel, vehicle);
      updateModelPrecisePosition(vehicle, vehicleModel);
      updateModelOrientationAngle(vehicle, vehicleModel);
      updateCurrentTransportName(vehicle, vehicleModel);
      updateCurrentOrderSequenceName(vehicle, vehicleModel);
      updateProcessableCategories(vehicle, vehicleModel);
      vehicleModel.setVehicle(vehicle);

      updateMiscModelProperties(vehicle);
      updateModelDriveOrder(kernel, vehicle, vehicleModel);

      vehicleModel.propertiesChanged(new NullAttributesChangeListener());
    }
    catch (CredentialsException e) {
      LOG.warn("", e);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void storeToPlantModel(PlantModelCreationTO plantModel) {
    plantModel.getVehicles().add(
        new VehicleCreationTO(getModel().getName())
            .setLength(getLength())
            .setEnergyLevelCritical(getEnergyLevelCritical())
            .setEnergyLevelGood(getEnergyLevelGood())
            .setMaxVelocity(getMaximumVelocity())
            .setMaxReverseVelocity(getMaximumReverseVelocity())
            .setProperties(getKernelProperties())
    );
    for (VisualLayoutCreationTO layout : plantModel.getVisualLayouts()) {
      updateLayoutElement(layout);
    }
  }

  private void updateLayoutElement(VisualLayoutCreationTO layout) {
    ColorProperty pColor
        = (ColorProperty) getModel().getProperty(ElementPropKeys.VEHICLE_ROUTE_COLOR);
    int rgb = pColor.getColor().getRGB() & 0x00FFFFFF;  // mask alpha bits

    layout.getModelElements().add(
        new ModelLayoutElementCreationTO(getModel().getName())
            .setProperty(ElementPropKeys.VEHICLE_ROUTE_COLOR, String.format("#%06X", rgb))
    );
  }

  private void updateModelDriveOrder(Kernel kernel,
                                     Vehicle vehicle,
                                     VehicleModel vehicleModel)
      throws CredentialsException {
    TransportOrder transportOrder = getTransportOrder(kernel, vehicle.getTransportOrder());

    if (transportOrder != null) {
      List<FigureComponent> c = composeDriveOrderComponents(transportOrder.getCurrentDriveOrder(),
                                                            vehicle.getRouteProgressIndex());
      vehicleModel.setDriveOrderComponents(c);
      vehicleModel.setDriveOrderState(transportOrder.getState());
    }
    else {
      vehicleModel.setDriveOrderComponents(null);
    }
  }

  private void updateModelOrientationAngle(Vehicle vehicle,
                                           VehicleModel vehicleModel) {
    double orientationAngle = vehicle.getOrientationAngle();
    AngleProperty pAngle = (AngleProperty) vehicleModel.getProperty(VehicleModel.ORIENTATION_ANGLE);
    pAngle.setValueAndUnit(orientationAngle, AngleProperty.Unit.DEG);
    vehicleModel.setOrientationAngle(orientationAngle);
  }

  private void updateModelPrecisePosition(Vehicle vehicle,
                                          VehicleModel vehicleModel) {
    Triple precisePosition = vehicle.getPrecisePosition();
    TripleProperty pPosition = (TripleProperty) vehicleModel.getProperty(VehicleModel.PRECISE_POSITION);
    pPosition.setValue(precisePosition);

    vehicleModel.setPrecisePosition(precisePosition);
  }

  private void updateModelNextPoint(VehicleModel vehicleModel, Vehicle vehicle) {
    StringProperty pNextPoint = (StringProperty) vehicleModel.getProperty(VehicleModel.NEXT_POINT);
    TCSObjectReference<Point> rNextPosition = vehicle.getNextPosition();

    if (rNextPosition != null) {
      ProcessAdapter pointAdapter = getEventDispatcher().findProcessAdapter(rNextPosition);
      PointModel pointModel = (PointModel) pointAdapter.getModel();
      vehicleModel.setNextPoint(pointModel);
      pNextPoint.setText(rNextPosition.getName());
    }
    else {
      vehicleModel.setNextPoint(null);
      pNextPoint.setText("null");
    }
  }

  private void updateModelCurrentPoint(VehicleModel vehicleModel,
                                       Vehicle vehicle) {
    StringProperty pPoint = (StringProperty) vehicleModel.getProperty(VehicleModel.POINT);
    TCSObjectReference<Point> rCurrentPosition = vehicle.getCurrentPosition();

    if (rCurrentPosition != null) {
      ProcessAdapter pointAdapter = getEventDispatcher().findProcessAdapter(rCurrentPosition);

      if (pointAdapter == null) {
        LOG.error("Error: Point " + rCurrentPosition.getName() + "not found.");
      }
      else {
        PointModel pointModel = (PointModel) pointAdapter.getModel();
        vehicleModel.placeOnPoint(pointModel);
        pPoint.setText(rCurrentPosition.getName());
      }
    }
    else {
      vehicleModel.placeOnPoint(null);
      pPoint.setText("null");
    }
  }

  private void updateModelState(Vehicle vehicle, VehicleModel vehicleModel) {
    Vehicle.State state = vehicle.getState();
    AbstractProperty pState = (AbstractProperty) vehicleModel.getProperty(VehicleModel.STATE);
    pState.setValue(state);

    Vehicle.ProcState procState = vehicle.getProcState();
    pState = (SelectionProperty) vehicleModel.getProperty(VehicleModel.PROC_STATE);
    pState.setValue(procState);
  }

  private void updateModelLoadedState(VehicleModel vehicleModel, Vehicle vehicle) {
    BooleanProperty modelLoaded = (BooleanProperty) vehicleModel.getProperty(VehicleModel.LOADED);
    modelLoaded.setValue(false);

    for (LoadHandlingDevice device : vehicle.getLoadHandlingDevices()) {
      if (device.isFull()) {
        modelLoaded.setValue(true);
        break;
      }
    }
  }

  private void updateModelEnergyState(VehicleModel vehicleModel, Vehicle vehicle) {
    AbstractProperty pEnergyState = (AbstractProperty) vehicleModel.getProperty(VehicleModel.ENERGY_STATE);

    if (vehicle.isEnergyLevelCritical()) {
      pEnergyState.setValue(VehicleModel.EnergyState.CRITICAL);
      pEnergyState.setHelptext(ResourceBundleUtil.getBundle().getString("vehicle.energyLevelCritical.helptext"));
    }
    else if (vehicle.isEnergyLevelDegraded()) {
      pEnergyState.setValue(VehicleModel.EnergyState.DEGRADED);
      pEnergyState.setHelptext(ResourceBundleUtil.getBundle().getString("vehicle.energyLevelDegraded.helptext"));
    }
    else if (vehicle.isEnergyLevelGood()) {
      pEnergyState.setValue(VehicleModel.EnergyState.GOOD);
      pEnergyState.setHelptext(ResourceBundleUtil.getBundle().getString("vehicle.energyLevelGood.helptext"));
    }
  }

  private void updateModelEnergy(Vehicle vehicle, VehicleModel vehicleModel) {
    int energyLevel = vehicle.getEnergyLevelCritical();

    PercentProperty pEnergy = (PercentProperty) vehicleModel.getProperty(VehicleModel.ENERGY_LEVEL_CRITICAL);
    pEnergy.setValueAndUnit(energyLevel, PercentProperty.Unit.PERCENT);

    energyLevel = vehicle.getEnergyLevelGood();
    pEnergy = (PercentProperty) vehicleModel.getProperty(VehicleModel.ENERGY_LEVEL_GOOD);
    pEnergy.setValueAndUnit(energyLevel, PercentProperty.Unit.PERCENT);

    energyLevel = vehicle.getEnergyLevel();
    pEnergy = (PercentProperty) vehicleModel.getProperty(VehicleModel.ENERGY_LEVEL);
    pEnergy.setValueAndUnit(energyLevel, PercentProperty.Unit.PERCENT);
  }

  private void updateModelName(VehicleModel vehicleModel, Vehicle vehicle) {
    StringProperty pName = (StringProperty) vehicleModel.getProperty(ModelComponent.NAME);
    pName.setText(vehicle.getName());
  }

  private void updateModelMaxVelocity(Vehicle vehicle, VehicleModel vehicleModel) {
    SpeedProperty maxVelProperty = (SpeedProperty) vehicleModel.getProperty(VehicleModel.MAXIMUM_VELOCITY);
    maxVelProperty.setValueAndUnit(vehicle.getMaxVelocity(), Unit.MM_S);

  }

  private void updateModelMaxReverseVelocity(Vehicle vehicle, VehicleModel vehicleModel) {
    SpeedProperty maxRevVelProperty = (SpeedProperty) vehicleModel.getProperty(VehicleModel.MAXIMUM_REVERSE_VELOCITY);
    maxRevVelProperty.setValueAndUnit(vehicle.getMaxReverseVelocity(), Unit.MM_S);
  }

  private void updateModelLength(Vehicle vehicle, VehicleModel vehicleModel) {
    int length = vehicle.getLength();
    LengthProperty pLength = (LengthProperty) vehicleModel.getProperty(VehicleModel.LENGTH);
    pLength.setValueAndUnit(length, LengthProperty.Unit.MM);
  }

  private void updateCurrentTransportName(Vehicle vehicle,
                                          VehicleModel vehicleModel) {
    StringProperty ordName = (StringProperty) vehicleModel.getProperty(VehicleModel.CURRENT_TRANSPORT_ORDER_NAME);
    if (vehicle.getTransportOrder() == null) {
      ordName.setText("null");
    }
    else {
      ordName.setText(vehicle.getTransportOrder().getName());
    }

  }

  private void updateCurrentOrderSequenceName(Vehicle vehicle,
                                              VehicleModel vehicleModel) {
    StringProperty seqName = (StringProperty) vehicleModel.getProperty(VehicleModel.CURRENT_SEQUENCE_NAME);
    if (vehicle.getOrderSequence() == null) {
      seqName.setText("null");
    }
    else {
      seqName.setText(vehicle.getOrderSequence().getName());
    }

  }

  private void updateProcessableCategories(Vehicle vehicle, VehicleModel vehicleModel) {
    OrderCategoriesProperty categories
        = (OrderCategoriesProperty) vehicleModel.getProperty(VehicleModel.PROCESSABLE_CATEGORIES);
    categories.setItems(vehicle.getProcessableCategories());
  }

  private int getLength() {
    LengthProperty pLength = (LengthProperty) getModel().getProperty(VehicleModel.LENGTH);
    return ((Double) pLength.getValueByUnit(LengthProperty.Unit.MM)).intValue();
  }

  private int getMaximumReverseVelocity() {
    SpeedProperty pMaxRevVel = (SpeedProperty) getModel().getProperty(VehicleModel.MAXIMUM_REVERSE_VELOCITY);
    return ((Double) pMaxRevVel.getValueByUnit(SpeedProperty.Unit.MM_S)).intValue();
  }

  private int getMaximumVelocity() {
    SpeedProperty pMaxVel = (SpeedProperty) getModel().getProperty(VehicleModel.MAXIMUM_VELOCITY);
    return ((Double) pMaxVel.getValueByUnit(SpeedProperty.Unit.MM_S)).intValue();
  }

  private int getEnergyLevelCritical() {
    PercentProperty pEnergy = (PercentProperty) getModel().getProperty(ENERGY_LEVEL_CRITICAL);
    return (Integer) pEnergy.getValue();
  }

  private int getEnergyLevelGood() {
    PercentProperty pEnergy = (PercentProperty) getModel().getProperty(ENERGY_LEVEL_GOOD);
    return (Integer) pEnergy.getValue();
  }

  @Nullable
  private TransportOrder getTransportOrder(Kernel kernel, TCSObjectReference<TransportOrder> ref)
      throws CredentialsException {
    if (ref == null) {
      return null;
    }
    return kernel.getTCSObject(TransportOrder.class, ref);
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
                                                            int routeProgressIndex) {
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
      adapter = getEventDispatcher().findProcessAdapter(point.getReference());
      result.add(0, (FigureComponent) adapter.getModel());

      if (path != null) {
        adapter = getEventDispatcher().findProcessAdapter(path.getReference());
        result.add(0, (FigureComponent) adapter.getModel());
      }
    }

    TCSObjectReference<?> ref = driveOrder.getDestination().getDestination();
    adapter = getEventDispatcher().findProcessAdapter(ref);
    if (adapter != null) {
      result.add((FigureComponent) adapter.getModel());
    }

    return result;
  }

  @Override // OpenTCSProcessAdapter
  protected void updateMiscModelProperties(TCSObject<?> tcsObject) {
    List<KeyValueProperty> items = new ArrayList<>();
    Map<String, String> misc = tcsObject.getProperties();

    for (Map.Entry<String, String> curEntry : misc.entrySet()) {
      if (!curEntry.getValue().contains("Unknown")) {
        items.add(new KeyValueProperty(getModel(), curEntry.getKey(), curEntry.getValue()));
      }
    }

    KeyValueSetProperty miscellaneous = (KeyValueSetProperty) getModel().getProperty(ModelComponent.MISCELLANEOUS);
    miscellaneous.setItems(items);
  }
}
