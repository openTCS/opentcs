/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.LoadHandlingDevice;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.CoursePointProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.TripleProperty;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An adapter for vehicles.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class VehicleAdapter
    extends OpenTCSProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(VehicleAdapter.class.getName());

  /**
   * Creates a new instance.
   */
  public VehicleAdapter() {
    super();
  }

  @Override
  @SuppressWarnings("unchecked")
  public TCSObjectReference<Vehicle> getProcessObject() {
    return (TCSObjectReference<Vehicle>) super.getProcessObject();
  }

  @Override
  public VehicleModel getModel() {
    return (VehicleModel) super.getModel();
  }

  @Override
  public void setModel(ModelComponent model) {
    if (!VehicleModel.class.isInstance(model)) {
      throw new IllegalArgumentException(model + " is not a VehicleModel");
    }
    super.setModel(model);
  }

  /**
   * Returns if the vehicle is currently available.
   *
   * @return True if it is available, false otherwise.
   */
  @SuppressWarnings("unchecked")
  public boolean isVehicleAvailable() {
    Vehicle vehicle = kernel().getTCSObject(Vehicle.class, getProcessObject());
    return vehicle.getProcState() != Vehicle.ProcState.UNAVAILABLE;
  }

  @Override // AbstractProcessAdapter
  public void releaseProcessObject() {
    try {
      kernel().removeTCSObject(getProcessObject());
      super.releaseProcessObject(); // also delete the Adapter
    }
    catch (KernelRuntimeException e) {
      log.log(Level.WARNING, null, e);
    }
  }

  @Override // AbstractProcessAdapter
  public Vehicle createProcessObject() throws KernelRuntimeException {
    if (!hasModelingState()) {
      return null;
    }
    Vehicle vehicle = kernel().createVehicle();
    setProcessObject(vehicle.getReference());

    StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
    LengthProperty pLength = (LengthProperty) getModel().getProperty(VehicleModel.LENGTH);
    // At creation name and length are empty
    if (pName.getText().isEmpty() && pLength.getValueByUnit(LengthProperty.Unit.MM) == 0.0) {
      updateModelProperties();
    }
    else {
        // if an "old" object was restored by undo() save the properties
      // in the kernel
      pName.setText(vehicle.getName());
      updateProcessProperties(true);
    }
    nameToModel(vehicle);
    getModel().setReference(vehicle.getReference());

    register();

    return vehicle;
  }

  @Override // OpenTCSProcessAdapter
  public void propertiesChanged(AttributesChangeEvent event) {
    if (hasModelingState() && event.getInitiator() != this) {
      updateProcessProperties(false);
    }
  }

  @Override // OpenTCSProcessAdapter
  @SuppressWarnings("unchecked")
  public void updateModelProperties() {
    TCSObjectReference<Vehicle> reference = getProcessObject();

    synchronized (reference) {
      try {
        Vehicle vehicle = kernel().getTCSObject(Vehicle.class, reference);
        VehicleModel vehicleModel = getModel();
        updateModelName(vehicleModel, vehicle);
        updateModelLength(vehicle, vehicleModel);
        updateModelEnergy(vehicle, vehicleModel);
        updateModelEnergyState(vehicleModel, vehicle);
        updateModelLoadedState(vehicleModel, vehicle);
        updateModelState(vehicle, vehicleModel);
        updateModelCurrentPoint(vehicleModel, vehicle);
        updateModelNextPoint(vehicleModel, vehicle);
        updateModelPrecisePosition(vehicle, vehicleModel);
        updateModelOrientationAngle(vehicle, vehicleModel);

        vehicleModel.setReference(reference);

        updateMiscModelProperties(vehicle);
        updateModelDriveOrder(vehicle, vehicleModel);

        vehicleModel.propertiesChanged(this);
      }
      catch (CredentialsException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  private void updateModelDriveOrder(Vehicle vehicle, VehicleModel vehicleModel) throws CredentialsException {
    TCSObjectReference<TransportOrder> rTransportOrder = vehicle.getTransportOrder();

    if (rTransportOrder != null) {
      TransportOrder transportOrder = kernel().getTCSObject(TransportOrder.class, rTransportOrder);
      DriveOrder driveOrder = transportOrder.getCurrentDriveOrder();
      List<FigureComponent> c
          = composeDriveOrderComponents(driveOrder, vehicle.getRouteProgressIndex());
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

  private void updateModelPrecisePosition(Vehicle vehicle, VehicleModel vehicleModel) {
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

  private void updateModelCurrentPoint(VehicleModel vehicleModel, Vehicle vehicle) {
    StringProperty pPoint = (StringProperty) vehicleModel.getProperty(VehicleModel.POINT);
    TCSObjectReference<Point> rCurrentPosition = vehicle.getCurrentPosition();

    if (rCurrentPosition != null) {
      ProcessAdapter pointAdapter = getEventDispatcher().findProcessAdapter(rCurrentPosition);

      if (pointAdapter == null) {
        log.severe("Error: Point " + rCurrentPosition.getName() + "not found.");
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
    SelectionProperty pState = (SelectionProperty) vehicleModel.getProperty(VehicleModel.STATE);
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
    SelectionProperty pEnergyState = (SelectionProperty) vehicleModel.getProperty(VehicleModel.ENERGY_STATE);

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

  private void updateModelLength(Vehicle vehicle, VehicleModel vehicleModel) {
    int length = vehicle.getLength();
    LengthProperty pLength = (LengthProperty) vehicleModel.getProperty(VehicleModel.LENGTH);
    pLength.setValueAndUnit(length, LengthProperty.Unit.MM);
  }

  @Override // OpenTCSProcessAdapter
  public void updateProcessProperties(boolean updateAllProperties) {
    super.updateProcessProperties(updateAllProperties);
    TCSObjectReference<Vehicle> reference = getProcessObject();

    if (isInTransition()) {
      return;
    }

    synchronized (reference) {
      StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      String name = pName.getText();

      try {
        updateDefaultProcessProperties(updateAllProperties, pName, reference, name);
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

  @SuppressWarnings("unchecked")
  private void updateDefaultProcessProperties(boolean updateAllProperties,
                                              StringProperty pName,
                                              TCSObjectReference<?> reference,
                                              String name)
      throws CredentialsException, ObjectExistsException, ObjectUnknownException {
    if (updateAllProperties || pName.hasChanged()) {
      kernel().renameTCSObject(reference, name);
    }

    LengthProperty pLength = (LengthProperty) getModel().getProperty(VehicleModel.LENGTH);

    if (updateAllProperties || pLength.hasChanged()) {
      kernel().setVehicleLength((TCSObjectReference<Vehicle>) reference,
                                ((Double) pLength.getValueByUnit(LengthProperty.Unit.MM)).intValue());
    }

    PercentProperty pEnergy = (PercentProperty) getModel().getProperty(VehicleModel.ENERGY_LEVEL_CRITICAL);

    if (updateAllProperties || pEnergy.hasChanged()) {
      kernel().setVehicleEnergyLevelCritical((TCSObjectReference<Vehicle>) reference,
                                             (Integer) pEnergy.getValue());
    }

    pEnergy = (PercentProperty) getModel().getProperty(VehicleModel.ENERGY_LEVEL_GOOD);

    if (updateAllProperties || pEnergy.hasChanged()) {
      kernel().setVehicleEnergyLevelGood((TCSObjectReference<Vehicle>) reference,
                                         (Integer) pEnergy.getValue());
    }
  }

  /**
   * Extracts the left over course elements from a drive order and progress.
   *
   * @param driveOrder The <code>DriveOrder</code>.
   * @param routeProgressIndex Index of the current position in the drive order.
   * @return List containing the left over course elements or <code>null</code>
   * if driveOrder is <code>null</code>.
   */
  private List<FigureComponent> composeDriveOrderComponents(
      DriveOrder driveOrder, int routeProgressIndex) {
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

    TCSObjectReference<Location> ref = driveOrder.getDestination().getLocation();
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

    String initialPointName = misc.get(ObjectPropConstants.VEHICLE_INITIAL_POSITION);

    if (initialPointName != null) {
      CoursePointProperty property = (CoursePointProperty) getModel().getProperty(VehicleModel.INITIAL_POSITION);
      property.setPointName(initialPointName);
    }
  }

  @Override // OpenTCSProcessAdapter
  protected void updateMiscProcessProperties(boolean updateAllProperties)
      throws ObjectUnknownException, CredentialsException {

    kernel().clearTCSObjectProperties(getProcessObject());
    KeyValueSetProperty pMisc = (KeyValueSetProperty) getModel().getProperty(ModelComponent.MISCELLANEOUS);

    if (pMisc != null) {
      for (KeyValueProperty kvp : pMisc.getItems()) {
        kernel().setTCSObjectProperty(getProcessObject(), kvp.getKey(), kvp.getValue());
      }
    }

    CoursePointProperty property = (CoursePointProperty) getModel().getProperty(VehicleModel.INITIAL_POSITION);
    kernel().setTCSObjectProperty(getProcessObject(),
                                  ObjectPropConstants.VEHICLE_INITIAL_POSITION,
                                  property.getPointName());
  }
}
