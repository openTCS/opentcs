/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.guing.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.OrderCategoriesProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.TripleProperty;
import org.opentcs.guing.model.AbstractModelComponent;
import org.opentcs.guing.model.ModelComponent;

/**
 * Basic implementation of a vehicle. A vehicle has an unique number.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleModel
    extends AbstractModelComponent {

  public static final String LENGTH = "Length";
  public static final String ENERGY_LEVEL_CRITICAL = "EnergyLevelCritical";
  public static final String ENERGY_LEVEL_GOOD = "EnergyLevelGood";
  public static final String ENERGY_LEVEL_FULLY_RECHARGED = "EnergyLevelFullyRecharged";
  public static final String ENERGY_LEVEL_SUFFICIENTLY_RECHARGED = "EnergyLevelSufficientlyRecharged";
  public static final String LOADED = "Loaded";
  public static final String STATE = "State";
  public static final String PROC_STATE = "ProcState";
  public static final String INTEGRATION_LEVEL = "IntegrationLevel";
  public static final String POINT = "Point";
  public static final String NEXT_POINT = "NextPoint";
  public static final String PRECISE_POSITION = "PrecisePosition";
  public static final String ORIENTATION_ANGLE = "OrientationAngle";
  public static final String ENERGY_LEVEL = "EnergyLevel";
  public static final String CURRENT_TRANSPORT_ORDER_NAME = "currentTransportOrderName";
  public static final String CURRENT_SEQUENCE_NAME = "currentOrderSequenceName";
  public static final String MAXIMUM_VELOCITY = "MaximumVelocity";
  public static final String MAXIMUM_REVERSE_VELOCITY = "MaximumReverseVelocity";
  public static final String PROCESSABLE_CATEGORIES = "ProcessableCategories";
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
   * The current drive order.
   */
  private List<ModelComponent> fDriveOrderComponents;
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
   * Creates a new instance.
   */
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
   * Sets the current position
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
   * Returns a list with all drive order components.
   *
   * @return The drive order components.
   */
  public List<ModelComponent> getDriveOrderComponents() {
    return fDriveOrderComponents;
  }

  /**
   * Sets the drive order components.
   *
   * @param driveOrderComponents A list with the components.
   */
  public void setDriveOrderComponents(List<ModelComponent> driveOrderComponents) {
    fDriveOrderComponents = driveOrderComponents;
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
  public void setVehicle(@Nonnull Vehicle vehicle) {
    this.vehicle = requireNonNull(vehicle, "vehicle");
  }

  /**
   * Checks whether the last reported processing state of the vehicle would
   * allow it to be assigned an order.
   *
   * @return <code>true</code> if, and only if, the vehicle's processing state
   * is not UNAVAILABLE.
   */
  @SuppressWarnings("deprecation")
  public boolean isAvailableForOrder() {
    return vehicle != null
        && vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && !vehicle.hasProcState(Vehicle.ProcState.UNAVAILABLE);
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

  public LengthProperty getPropertyLength() {
    return (LengthProperty) getProperty(LENGTH);
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

  public PercentProperty getPropertyEnergyLevelCritical() {
    return (PercentProperty) getProperty(ENERGY_LEVEL_CRITICAL);
  }

  public PercentProperty getPropertyEnergyLevelGood() {
    return (PercentProperty) getProperty(ENERGY_LEVEL_GOOD);
  }

  public PercentProperty getPropertyEnergyLevelFullyRecharged() {
    return (PercentProperty) getProperty(ENERGY_LEVEL_FULLY_RECHARGED);
  }

  public PercentProperty getPropertyEnergyLevelSufficientlyRecharged() {
    return (PercentProperty) getProperty(ENERGY_LEVEL_SUFFICIENTLY_RECHARGED);
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

  public OrderCategoriesProperty getPropertyProcessableCategories() {
    return (OrderCategoriesProperty) getProperty(PROCESSABLE_CATEGORIES);
  }

  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }

  private void createProperties() {
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("vehicleModel.property_name.description"));
    pName.setHelptext(bundle.getString("vehicleModel.property_name.helptext"));
    setProperty(NAME, pName);

    LengthProperty pLength = new LengthProperty(this, 1000, LengthProperty.Unit.MM);
    pLength.setDescription(bundle.getString("vehicleModel.property_length.description"));
    pLength.setHelptext(bundle.getString("vehicleModel.property_length.helptext"));
    setProperty(LENGTH, pLength);

    ColorProperty pColor = new ColorProperty(this, Color.red);
    pColor.setDescription(bundle.getString("vehicleModel.property_routeColor.description"));
    pColor.setHelptext(bundle.getString("vehicleModel.property_routeColor.helptext"));
    setProperty(ElementPropKeys.VEHICLE_ROUTE_COLOR, pColor);

    PercentProperty pEnergyLevelCritical = new PercentProperty(this,
                                                               30,
                                                               PercentProperty.Unit.PERCENT,
                                                               true);
    pEnergyLevelCritical.setDescription(
        bundle.getString("vehicleModel.property_energyLevelCritical.description")
    );
    pEnergyLevelCritical.setHelptext(
        bundle.getString("vehicleModel.property_energyLevelCritical.helptext")
    );
    setProperty(ENERGY_LEVEL_CRITICAL, pEnergyLevelCritical);

    PercentProperty pEnergyLevelGood = new PercentProperty(this,
                                                           90,
                                                           PercentProperty.Unit.PERCENT,
                                                           true);
    pEnergyLevelGood.setDescription(
        bundle.getString("vehicleModel.property_energyLevelGood.description")
    );
    pEnergyLevelGood.setHelptext(
        bundle.getString("vehicleModel.property_energyLevelGood.helptext")
    );
    setProperty(ENERGY_LEVEL_GOOD, pEnergyLevelGood);

    PercentProperty pEnergyLevelFullyRecharged = new PercentProperty(this,
                                                                     30,
                                                                     PercentProperty.Unit.PERCENT,
                                                                     true);
    pEnergyLevelFullyRecharged.setDescription(
        bundle.getString("vehicleModel.property_energyLevelFullyRecharged.description")
    );
    pEnergyLevelFullyRecharged.setHelptext(
        bundle.getString("vehicleModel.property_energyLevelFullyRecharged.helptext")
    );
    setProperty(ENERGY_LEVEL_FULLY_RECHARGED, pEnergyLevelFullyRecharged);

    PercentProperty pEnergyLevelSufficientlyRecharged
        = new PercentProperty(this,
                              90,
                              PercentProperty.Unit.PERCENT,
                              true);
    pEnergyLevelSufficientlyRecharged.setDescription(
        bundle.getString("vehicleModel.property_energyLevelSufficientlyRecharged.description")
    );
    pEnergyLevelSufficientlyRecharged.setHelptext(
        bundle.getString("vehicleModel.property_energyLevelSufficientlyRecharged.helptext")
    );
    setProperty(ENERGY_LEVEL_SUFFICIENTLY_RECHARGED, pEnergyLevelSufficientlyRecharged);

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
        = new SelectionProperty<>(this,
                                  Arrays.asList(Vehicle.State.values()),
                                  Vehicle.State.UNKNOWN);
    pState.setDescription(bundle.getString("vehicleModel.property_state.description"));
    pState.setHelptext(bundle.getString("vehicleModel.property_state.helptext"));
    pState.setModellingEditable(false);
    setProperty(STATE, pState);

    @SuppressWarnings("deprecation")
    SelectionProperty<Vehicle.ProcState> pProcState
        = new SelectionProperty<>(this,
                                  Arrays.asList(Vehicle.ProcState.values()),
                                  Vehicle.ProcState.UNAVAILABLE);
    pProcState.setDescription(
        bundle.getString("vehicleModel.property_processingState.description")
    );
    pProcState.setHelptext(bundle.getString("vehicleModel.property_processingState.helptext"));
    pProcState.setModellingEditable(false);
    setProperty(PROC_STATE, pProcState);

    SelectionProperty<Vehicle.IntegrationLevel> pIntegrationLevel
        = new SelectionProperty<>(this,
                                  Arrays.asList(Vehicle.IntegrationLevel.values()),
                                  Vehicle.IntegrationLevel.TO_BE_RESPECTED);
    pIntegrationLevel.setDescription(
        bundle.getString("vehicleModel.property_integrationLevel.description")
    );
    pIntegrationLevel.setHelptext(
        bundle.getString("vehicleModel.property_integrationLevel.helptext")
    );
    pIntegrationLevel.setModellingEditable(false);
    setProperty(INTEGRATION_LEVEL, pIntegrationLevel);

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

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(
        bundle.getString("vehicleModel.property_miscellaneous.description")
    );
    pMiscellaneous.setHelptext(bundle.getString("vehicleModel.property_miscellaneous.helptext"));
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

    OrderCategoriesProperty pProcessableCategories = new OrderCategoriesProperty(this);
    pProcessableCategories.setDescription(
        bundle.getString("vehicleModel.property_processableCategories.description")
    );
    pProcessableCategories.setHelptext(
        bundle.getString("vehicleModel.property_processableCategories.helptext")
    );
    pProcessableCategories.setModellingEditable(false);
    pProcessableCategories.setOperatingEditable(true);
    setProperty(PROCESSABLE_CATEGORIES, pProcessableCategories);
  }
}
