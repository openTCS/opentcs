// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.model.elements;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.base.I18nPlantOverviewBase.BUNDLE_PATH;

import jakarta.annotation.Nonnull;
import java.awt.Color;
import java.util.Arrays;
import java.util.ResourceBundle;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.base.components.properties.type.AngleProperty;
import org.opentcs.guing.base.components.properties.type.BooleanProperty;
import org.opentcs.guing.base.components.properties.type.BoundingBoxProperty;
import org.opentcs.guing.base.components.properties.type.ColorProperty;
import org.opentcs.guing.base.components.properties.type.EnergyLevelThresholdSetModel;
import org.opentcs.guing.base.components.properties.type.EnergyLevelThresholdSetProperty;
import org.opentcs.guing.base.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.base.components.properties.type.OrderTypesProperty;
import org.opentcs.guing.base.components.properties.type.PercentProperty;
import org.opentcs.guing.base.components.properties.type.ResourceProperty;
import org.opentcs.guing.base.components.properties.type.SelectionProperty;
import org.opentcs.guing.base.components.properties.type.SpeedProperty;
import org.opentcs.guing.base.components.properties.type.StringProperty;
import org.opentcs.guing.base.components.properties.type.TripleProperty;
import org.opentcs.guing.base.model.AbstractModelComponent;
import org.opentcs.guing.base.model.BoundingBoxModel;
import org.opentcs.guing.base.model.DrawnModelComponent;

/**
 * Basic implementation of a vehicle. A vehicle has an unique number.
 */
public class VehicleModel
    extends
      AbstractModelComponent
    implements
      DrawnModelComponent {

  /**
   * The name/key of the 'bounding box' property.
   */
  public static final String BOUNDING_BOX = "BoundingBox";
  /**
   * The name/key of the 'energyLevelThresholdSet' property.
   */
  public static final String ENERGY_LEVEL_THRESHOLD_SET = "EnergyLevelThresholdSet";
  /**
   * The name/key of the 'loaded' property.
   */
  public static final String LOADED = "Loaded";
  /**
   * The name/key of the 'state' property.
   */
  public static final String STATE = "State";
  /**
   * The name/key of the 'proc state' property.
   */
  public static final String PROC_STATE = "ProcState";
  /**
   * The name/key of the 'integration level' property.
   */
  public static final String INTEGRATION_LEVEL = "IntegrationLevel";
  /**
   * The name/key of the 'paused' property.
   */
  public static final String PAUSED = "Paused";
  /**
   * The name/key of the 'point' property.
   */
  public static final String POINT = "Point";
  /**
   * The name/key of the 'next point' property.
   */
  public static final String NEXT_POINT = "NextPoint";
  /**
   * The name/key of the 'precise position' property.
   */
  public static final String PRECISE_POSITION = "PrecisePosition";
  /**
   * The name/key of the 'orientation angle' property.
   */
  public static final String ORIENTATION_ANGLE = "OrientationAngle";
  /**
   * The name/key of the 'energy level' property.
   */
  public static final String ENERGY_LEVEL = "EnergyLevel";
  /**
   * The name/key of the 'current transport order name' property.
   */
  public static final String CURRENT_TRANSPORT_ORDER_NAME = "currentTransportOrderName";
  /**
   * The name/key of the 'current order sequence name' property.
   */
  public static final String CURRENT_SEQUENCE_NAME = "currentOrderSequenceName";
  /**
   * The name/key of the 'maximum velocity' property.
   */
  public static final String MAXIMUM_VELOCITY = "MaximumVelocity";
  /**
   * The name/key of the 'maximum reverse velocity' property.
   */
  public static final String MAXIMUM_REVERSE_VELOCITY = "MaximumReverseVelocity";
  /**
   * The name/key of the 'acceptable order types' property.
   */
  public static final String ACCEPTABLE_ORDER_TYPES = "AcceptableOrderTypes";
  /**
   * The name/key of the 'allocated resources' property.
   */
  public static final String ALLOCATED_RESOURCES = "AllocatedResources";
  /**
   * The name/key of the 'claimed resources' property.
   */
  public static final String CLAIMED_RESOURCES = "ClaimedResources";
  /**
   * The name/key of the 'envelope key' property.
   */
  public static final String ENVELOPE_KEY = "EnvelopeKey";
  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);

  /**
   * The point the vehicle currently remains on.
   */
  private PointModel fPoint;
  /**
   * The point the vehicle will drive to next.
   */
  private PointModel fNextPoint;
  /**
   * The current position (x,y,z) the vehicle driver reported.
   */
  private Triple fPrecisePosition;
  /**
   * The current vehicle orientation.
   */
  private double fOrientationAngle;
  /**
   * The state of the drive order.
   */
  private TransportOrder.State fDriveOrderState;
  /**
   * Flag whether the drive order will be displayed.
   */
  private boolean fDisplayDriveOrders;
  /**
   * Flag whether the view follows this vehicle as it drives.
   */
  private boolean fViewFollows;
  /**
   * A reference to the vehicle.
   */
  private Vehicle vehicle = new Vehicle("Dummy");
  /**
   * The current or next path in a drive order.
   */
  private PathModel currentDriveOrderPath;
  /**
   * The destination for the current drive order.
   */
  private PointModel driveOrderDestination;

  /**
   * Creates a new instance.
   */
  @SuppressWarnings("this-escape")
  public VehicleModel() {
    createProperties();
  }

  /**
   * Sets the point the vehicle currently remains on.
   *
   * @param point The point.
   */
  public void placeOnPoint(PointModel point) {
    fPoint = point;
  }

  /**
   * Returns the point the vehicle currently remains on.
   *
   * @return The current point.
   */
  public PointModel getPoint() {
    return fPoint;
  }

  /**
   * Returns the point the vehicle will drive to next.
   *
   * @return The next point.
   */
  public PointModel getNextPoint() {
    return fNextPoint;
  }

  /**
   * Sets the point the vehicle will drive to next.
   *
   * @param point The next point.
   */
  public void setNextPoint(PointModel point) {
    fNextPoint = point;
  }

  /**
   * Returns the current position.
   *
   * @return The position (x,y,z).
   */
  public Triple getPrecisePosition() {
    return fPrecisePosition;
  }

  /**
   * Sets the current position.
   *
   * @param position A triple containing the position.
   */
  public void setPrecisePosition(Triple position) {
    fPrecisePosition = position;
  }

  /**
   * Returns the current orientation angle.
   *
   * @return The orientation angle.
   */
  public double getOrientationAngle() {
    return fOrientationAngle;
  }

  /**
   * Sets the orientation angle.
   *
   * @param angle The new angle.
   */
  public void setOrientationAngle(double angle) {
    fOrientationAngle = angle;
  }

  /**
   * Returns the color the drive order is painted in.
   *
   * @return The color.
   */
  public Color getDriveOrderColor() {
    return getPropertyRouteColor().getColor();
  }

  /**
   * Returns the state of the drive order.
   *
   * @return The state.
   */
  public TransportOrder.State getDriveOrderState() {
    return fDriveOrderState;
  }

  /**
   * Sets the drive order state.
   *
   * @param driveOrderState The new state.
   */
  public void setDriveOrderState(TransportOrder.State driveOrderState) {
    fDriveOrderState = driveOrderState;
  }

  /**
   * Sets whether the drive order shall be displayed or not.
   *
   * @param state <code>true</code> to display the drive order.
   */
  public void setDisplayDriveOrders(boolean state) {
    fDisplayDriveOrders = state;
  }

  /**
   * Returns whether the drive order is displayed.
   *
   * @return <code>true</code>, if it displayed.
   */
  public boolean getDisplayDriveOrders() {
    return fDisplayDriveOrders;
  }

  /**
   * Returns whether the view follows this vehicle as it drives.
   *
   * @return <code>true</code> if it follows.
   */
  public boolean isViewFollows() {
    return fViewFollows;
  }

  /**
   * Sets whether the view follows this vehicle as it drives.
   *
   * @param viewFollows <code>true</code> if it follows.
   */
  public void setViewFollows(boolean viewFollows) {
    this.fViewFollows = viewFollows;
  }

  /**
   * Returns the kernel object.
   *
   * @return The kernel object.
   */
  @Nonnull
  public Vehicle getVehicle() {
    return vehicle;
  }

  /**
   * Sets the kernel object.
   *
   * @param vehicle The kernel object.
   */
  public void setVehicle(
      @Nonnull
      Vehicle vehicle
  ) {
    this.vehicle = requireNonNull(vehicle, "vehicle");
  }

  /**
   * Returns the current path for the drive order.
   *
   * @return Path for the drive order.
   */
  public PathModel getCurrentDriveOrderPath() {
    return currentDriveOrderPath;
  }

  /**
   * Sets the current drive order path.
   *
   * @param path the current drive order path.
   */
  public void setCurrentDriveOrderPath(PathModel path) {
    currentDriveOrderPath = path;
  }

  /**
   * Returns the destination for the current drive order.
   *
   * @return the destination for the current drive order.
   */
  public PointModel getDriveOrderDestination() {
    return driveOrderDestination;
  }

  /**
   * Sets the destination for the current drive order.
   *
   * @param driveOrderDestination destination for the current drive order.
   */
  public void setDriveOrderDestination(PointModel driveOrderDestination) {
    this.driveOrderDestination = driveOrderDestination;
  }

  /**
   * Checks whether the last reported processing state of the vehicle would allow it to be assigned
   * an order.
   *
   * @return {@code true} if, and only if, the vehicle's integration level is TO_BE_UTILIZED.
   */
  public boolean isAvailableForOrder() {
    return vehicle != null
        && vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED;
  }

  @Override // AbstractModelComponent
  public String getTreeViewName() {
    String treeViewName = getDescription() + " " + getName();

    return treeViewName;
  }

  @Override // AbstractModelComponent
  public String getDescription() {
    return bundle.getString("vehicleModel.description");
  }

  public BoundingBoxProperty getPropertyBoundingBox() {
    return (BoundingBoxProperty) getProperty(BOUNDING_BOX);
  }

  public ColorProperty getPropertyRouteColor() {
    return (ColorProperty) getProperty(ElementPropKeys.VEHICLE_ROUTE_COLOR);
  }

  public SpeedProperty getPropertyMaxVelocity() {
    return (SpeedProperty) getProperty(MAXIMUM_VELOCITY);
  }

  public SpeedProperty getPropertyMaxReverseVelocity() {
    return (SpeedProperty) getProperty(MAXIMUM_REVERSE_VELOCITY);
  }

  public EnergyLevelThresholdSetProperty getPropertyEnergyLevelThresholdSet() {
    return (EnergyLevelThresholdSetProperty) getProperty(ENERGY_LEVEL_THRESHOLD_SET);
  }

  public PercentProperty getPropertyEnergyLevel() {
    return (PercentProperty) getProperty(ENERGY_LEVEL);
  }

  @SuppressWarnings("unchecked")
  public SelectionProperty<Vehicle.State> getPropertyState() {
    return (SelectionProperty<Vehicle.State>) getProperty(STATE);
  }

  @SuppressWarnings("unchecked")
  public SelectionProperty<Vehicle.ProcState> getPropertyProcState() {
    return (SelectionProperty<Vehicle.ProcState>) getProperty(PROC_STATE);
  }

  @SuppressWarnings("unchecked")
  public SelectionProperty<Vehicle.IntegrationLevel> getPropertyIntegrationLevel() {
    return (SelectionProperty<Vehicle.IntegrationLevel>) getProperty(INTEGRATION_LEVEL);
  }

  public BooleanProperty getPropertyPaused() {
    return (BooleanProperty) getProperty(PAUSED);
  }

  public AngleProperty getPropertyOrientationAngle() {
    return (AngleProperty) getProperty(ORIENTATION_ANGLE);
  }

  public TripleProperty getPropertyPrecisePosition() {
    return (TripleProperty) getProperty(PRECISE_POSITION);
  }

  public StringProperty getPropertyPoint() {
    return (StringProperty) getProperty(POINT);
  }

  public StringProperty getPropertyNextPoint() {
    return (StringProperty) getProperty(NEXT_POINT);
  }

  public BooleanProperty getPropertyLoaded() {
    return (BooleanProperty) getProperty(LOADED);
  }

  public StringProperty getPropertyCurrentOrderName() {
    return (StringProperty) getProperty(CURRENT_TRANSPORT_ORDER_NAME);
  }

  public StringProperty getPropertyCurrentSequenceName() {
    return (StringProperty) getProperty(CURRENT_SEQUENCE_NAME);
  }

  public OrderTypesProperty getPropertyAcceptableOrderTypes() {
    return (OrderTypesProperty) getProperty(ACCEPTABLE_ORDER_TYPES);
  }

  public StringProperty getPropertyEnvelopeKey() {
    return (StringProperty) getProperty(ENVELOPE_KEY);
  }

  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }

  public ResourceProperty getAllocatedResources() {
    return (ResourceProperty) getProperty(ALLOCATED_RESOURCES);
  }

  public ResourceProperty getClaimedResources() {
    return (ResourceProperty) getProperty(CLAIMED_RESOURCES);
  }

  private void createProperties() {
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("vehicleModel.property_name.description"));
    pName.setHelptext(bundle.getString("vehicleModel.property_name.helptext"));
    setProperty(NAME, pName);

    BoundingBoxProperty pBoundingBox = new BoundingBoxProperty(
        this,
        new BoundingBoxModel(1000, 1000, 1000, new Couple(0, 0))
    );
    pBoundingBox.setDescription(bundle.getString("vehicleModel.property_boundingBox.description"));
    pBoundingBox.setHelptext(bundle.getString("vehicleModel.property_boundingBox.helptext"));
    setProperty(BOUNDING_BOX, pBoundingBox);

    ColorProperty pColor = new ColorProperty(this, Color.red);
    pColor.setDescription(bundle.getString("vehicleModel.property_routeColor.description"));
    pColor.setHelptext(bundle.getString("vehicleModel.property_routeColor.helptext"));
    setProperty(ElementPropKeys.VEHICLE_ROUTE_COLOR, pColor);

    SpeedProperty pMaximumVelocity = new SpeedProperty(this, 1000, SpeedProperty.Unit.MM_S);
    pMaximumVelocity.setDescription(
        bundle.getString("vehicleModel.property_maximumVelocity.description")
    );
    pMaximumVelocity.setHelptext(
        bundle.getString("vehicleModel.property_maximumVelocity.helptext")
    );
    setProperty(MAXIMUM_VELOCITY, pMaximumVelocity);

    SpeedProperty pMaximumReverseVelocity = new SpeedProperty(this, 1000, SpeedProperty.Unit.MM_S);
    pMaximumReverseVelocity.setDescription(
        bundle.getString("vehicleModel.property_maximumReverseVelocity.description")
    );
    pMaximumReverseVelocity.setHelptext(
        bundle.getString("vehicleModel.property_maximumReverseVelocity.helptext")
    );
    setProperty(MAXIMUM_REVERSE_VELOCITY, pMaximumReverseVelocity);

    EnergyLevelThresholdSetProperty pEnergyLevelThresholdSet = new EnergyLevelThresholdSetProperty(
        this, new EnergyLevelThresholdSetModel(30, 90, 40, 95)
    );
    pEnergyLevelThresholdSet.setDescription(
        bundle.getString("vehicleModel.property_energyLevelThresholdSet.description")
    );
    pEnergyLevelThresholdSet.setHelptext(
        bundle.getString("vehicleModel.property_energyLevelThresholdSet.helptext")
    );
    pEnergyLevelThresholdSet.setModellingEditable(true);
    pEnergyLevelThresholdSet.setOperatingEditable(true);
    setProperty(ENERGY_LEVEL_THRESHOLD_SET, pEnergyLevelThresholdSet);

    PercentProperty pEnergyLevel = new PercentProperty(this, true);
    pEnergyLevel.setDescription(bundle.getString("vehicleModel.property_energyLevel.description"));
    pEnergyLevel.setHelptext(bundle.getString("vehicleModel.property_energyLevel.helptext"));
    pEnergyLevel.setModellingEditable(false);
    setProperty(ENERGY_LEVEL, pEnergyLevel);

    BooleanProperty pLoaded = new BooleanProperty(this);
    pLoaded.setDescription(bundle.getString("vehicleModel.property_loaded.description"));
    pLoaded.setHelptext(bundle.getString("vehicleModel.property_loaded.helptext"));
    pLoaded.setModellingEditable(false);
    setProperty(LOADED, pLoaded);

    SelectionProperty<Vehicle.State> pState
        = new SelectionProperty<>(
            this,
            Arrays.asList(Vehicle.State.values()),
            Vehicle.State.UNKNOWN
        );
    pState.setDescription(bundle.getString("vehicleModel.property_state.description"));
    pState.setHelptext(bundle.getString("vehicleModel.property_state.helptext"));
    pState.setModellingEditable(false);
    setProperty(STATE, pState);

    SelectionProperty<Vehicle.ProcState> pProcState
        = new SelectionProperty<>(
            this,
            Arrays.asList(Vehicle.ProcState.values()),
            Vehicle.ProcState.IDLE
        );
    pProcState.setDescription(
        bundle.getString("vehicleModel.property_processingState.description")
    );
    pProcState.setHelptext(bundle.getString("vehicleModel.property_processingState.helptext"));
    pProcState.setModellingEditable(false);
    setProperty(PROC_STATE, pProcState);

    SelectionProperty<Vehicle.IntegrationLevel> pIntegrationLevel
        = new SelectionProperty<>(
            this,
            Arrays.asList(Vehicle.IntegrationLevel.values()),
            Vehicle.IntegrationLevel.TO_BE_RESPECTED
        );
    pIntegrationLevel.setDescription(
        bundle.getString("vehicleModel.property_integrationLevel.description")
    );
    pIntegrationLevel.setHelptext(
        bundle.getString("vehicleModel.property_integrationLevel.helptext")
    );
    pIntegrationLevel.setModellingEditable(false);
    setProperty(INTEGRATION_LEVEL, pIntegrationLevel);

    BooleanProperty pPaused = new BooleanProperty(this);
    pPaused.setDescription(bundle.getString("vehicleModel.property_paused.description"));
    pPaused.setHelptext(bundle.getString("vehicleModel.property_paused.helptext"));
    pPaused.setModellingEditable(false);
    pPaused.setOperatingEditable(true);
    setProperty(PAUSED, pPaused);

    StringProperty pPoint = new StringProperty(this);
    pPoint.setDescription(bundle.getString("vehicleModel.property_currentPoint.description"));
    pPoint.setHelptext(bundle.getString("vehicleModel.property_currentPoint.helptext"));
    pPoint.setModellingEditable(false);
    setProperty(POINT, pPoint);

    StringProperty pNextPoint = new StringProperty(this);
    pNextPoint.setDescription(bundle.getString("vehicleModel.property_nextPoint.description"));
    pNextPoint.setHelptext(bundle.getString("vehicleModel.property_nextPoint.helptext"));
    pNextPoint.setModellingEditable(false);
    setProperty(NEXT_POINT, pNextPoint);

    TripleProperty pPrecisePosition = new TripleProperty(this);
    pPrecisePosition.setDescription(
        bundle.getString("vehicleModel.property_precisePosition.description")
    );
    pPrecisePosition.setHelptext(
        bundle.getString("vehicleModel.property_precisePosition.helptext")
    );
    pPrecisePosition.setModellingEditable(false);
    setProperty(PRECISE_POSITION, pPrecisePosition);

    AngleProperty pOrientationAngle = new AngleProperty(this);
    pOrientationAngle.setDescription(
        bundle.getString("vehicleModel.property_orientationAngle.description")
    );
    pOrientationAngle.setHelptext(
        bundle.getString("vehicleModel.property_orientationAngle.helptext")
    );
    pOrientationAngle.setModellingEditable(false);
    setProperty(ORIENTATION_ANGLE, pOrientationAngle);

    StringProperty pEnvelopeKey = new StringProperty(this);
    pEnvelopeKey.setDescription(bundle.getString("vehicleModel.property_envelopeKey.description"));
    pEnvelopeKey.setHelptext(bundle.getString("vehicleModel.property_envelopeKey.helptext"));
    pEnvelopeKey.setModellingEditable(true);
    pEnvelopeKey.setOperatingEditable(false);
    setProperty(ENVELOPE_KEY, pEnvelopeKey);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(
        bundle.getString("vehicleModel.property_miscellaneous.description")
    );
    pMiscellaneous.setHelptext(bundle.getString("vehicleModel.property_miscellaneous.helptext"));
    pMiscellaneous.setOperatingEditable(true);
    setProperty(MISCELLANEOUS, pMiscellaneous);

    StringProperty curTransportOrderName = new StringProperty(this);
    curTransportOrderName.setDescription(
        bundle.getString("vehicleModel.property_currentTransportOrder.description")
    );
    curTransportOrderName.setHelptext(
        bundle.getString("vehicleModel.property_currentTransportOrder.helptext")
    );
    curTransportOrderName.setModellingEditable(false);
    setProperty(CURRENT_TRANSPORT_ORDER_NAME, curTransportOrderName);

    StringProperty curOrderSequenceName = new StringProperty(this);
    curOrderSequenceName.setDescription(
        bundle.getString("vehicleModel.property_currentOrderSequence.description")
    );
    curOrderSequenceName.setHelptext(
        bundle.getString("vehicleModel.property_currentOrderSequence.helptext")
    );
    curOrderSequenceName.setModellingEditable(false);
    setProperty(CURRENT_SEQUENCE_NAME, curOrderSequenceName);

    OrderTypesProperty pAcceptableOrderTypes = new OrderTypesProperty(this);
    pAcceptableOrderTypes.setDescription(
        bundle.getString("vehicleModel.property_acceptableOrderTypes.description")
    );
    pAcceptableOrderTypes.setHelptext(
        bundle.getString("vehicleModel.property_acceptableOrderTypes.helptext")
    );
    pAcceptableOrderTypes.setModellingEditable(false);
    pAcceptableOrderTypes.setOperatingEditable(true);
    setProperty(ACCEPTABLE_ORDER_TYPES, pAcceptableOrderTypes);

    ResourceProperty allocatedResources = new ResourceProperty(this);
    allocatedResources.setDescription(
        bundle.getString("vehicleModel.property_allocatedResources.description")
    );
    allocatedResources.setHelptext(
        bundle.getString("vehicleModel.property_allocatedResources.helptext")
    );
    allocatedResources.setModellingEditable(false);
    allocatedResources.setOperatingEditable(true);
    setProperty(ALLOCATED_RESOURCES, allocatedResources);

    ResourceProperty claimedResources = new ResourceProperty(this);
    claimedResources.setDescription(
        bundle.getString("vehicleModel.property_claimedResources.description")
    );
    claimedResources.setHelptext(
        bundle.getString("vehicleModel.property_claimedResources.helptext")
    );
    claimedResources.setModellingEditable(false);
    claimedResources.setOperatingEditable(true);
    setProperty(CLAIMED_RESOURCES, claimedResources);
  }
}
