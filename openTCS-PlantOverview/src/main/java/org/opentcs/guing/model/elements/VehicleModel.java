/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
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
import javax.annotation.Nonnull;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;
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
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Basic implementation of a vehicle. A vehicle has an unique number.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleModel
    extends AbstractFigureComponent {

  public static final String LENGTH = "Length";
  public static final String ENERGY_LEVEL_CRITICAL = "EnergyLevelCritical";
  public static final String ENERGY_LEVEL_GOOD = "EnergyLevelGood";
  public static final String LOADED = "Loaded";
  public static final String STATE = "State";
  public static final String PROC_STATE = "ProcState";
  public static final String INTEGRATION_LEVEL = "IntegrationLevel";
  public static final String POINT = "Point";
  public static final String NEXT_POINT = "NextPoint";
  public static final String PRECISE_POSITION = "PrecisePosition";
  public static final String ORIENTATION_ANGLE = "OrientationAngle";
  public static final String ENERGY_LEVEL = "EnergyLevel";
  public static final String ENERGY_STATE = "EnergyState";
  public static final String CURRENT_TRANSPORT_ORDER_NAME = "currentTransportOrderName";
  public static final String CURRENT_SEQUENCE_NAME = "currentOrderSequenceName";
  public static final String MAXIMUM_VELOCITY = "MaximumVelocity";
  public static final String MAXIMUM_REVERSE_VELOCITY = "MaximumReverseVelocity";
  public static final String PROCESSABLE_CATEGORIES = "ProcessableCategories";

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
  private List<FigureComponent> fDriveOrderComponents;
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

  @Override // AbstractFigureComponent
  public VehicleFigure getFigure() {
    return (VehicleFigure) super.getFigure();
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
  public List<FigureComponent> getDriveOrderComponents() {
    return fDriveOrderComponents;
  }

  /**
   * Sets the drive order components.
   *
   * @param driveOrderComponents A list with the components.
   */
  public void setDriveOrderComponents(List<FigureComponent> driveOrderComponents) {
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
    return ResourceBundleUtil.getBundle().getString("vehicle.description");
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

  public PercentProperty getPropertyEnergyLevel() {
    return (PercentProperty) getProperty(ENERGY_LEVEL);
  }

  @SuppressWarnings("unchecked")
  public SelectionProperty<EnergyState> getPropertyEnergyState() {
    return (SelectionProperty<EnergyState>) getProperty(ENERGY_STATE);
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
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("vehicle.name.text"));
    pName.setHelptext(bundle.getString("vehicle.name.helptext"));
    setProperty(NAME, pName);

    LengthProperty pLength = new LengthProperty(this, 1000, LengthProperty.Unit.MM);
    pLength.setDescription(bundle.getString("vehicle.length.text"));
    pLength.setHelptext(bundle.getString("vehicle.length.helptext"));
    setProperty(LENGTH, pLength);

    ColorProperty pColor = new ColorProperty(this, Color.red);
    pColor.setDescription(bundle.getString("vehicle.routeColor.text"));
    pColor.setHelptext(bundle.getString("vehicle.routeColor.helptext"));
    setProperty(ElementPropKeys.VEHICLE_ROUTE_COLOR, pColor);

    PercentProperty pEnergyLevelCritical = new PercentProperty(this,
                                                               30,
                                                               PercentProperty.Unit.PERCENT,
                                                               true);
    pEnergyLevelCritical.setDescription(bundle.getString("vehicle.energyLevelCritical.text"));
    pEnergyLevelCritical.setHelptext(bundle.getString("vehicle.energyLevelCritical.helptext"));
    setProperty(ENERGY_LEVEL_CRITICAL, pEnergyLevelCritical);

    PercentProperty pEnergyLevelGood = new PercentProperty(this,
                                                           90,
                                                           PercentProperty.Unit.PERCENT,
                                                           true);
    pEnergyLevelGood.setDescription(bundle.getString("vehicle.energyLevelGood.text"));
    pEnergyLevelGood.setHelptext(bundle.getString("vehicle.energyLevelGood.helptext"));
    setProperty(ENERGY_LEVEL_GOOD, pEnergyLevelGood);

    SpeedProperty pMaximumVelocity = new SpeedProperty(this, 1000, SpeedProperty.Unit.MM_S);
    pMaximumVelocity.setDescription(bundle.getString("vehicle.maximumVelocity.text"));
    pMaximumVelocity.setHelptext(bundle.getString("vehicle.maximumVelocity.helptext"));
    setProperty(MAXIMUM_VELOCITY, pMaximumVelocity);

    SpeedProperty pMaximumReverseVelocity = new SpeedProperty(this, 1000, SpeedProperty.Unit.MM_S);
    pMaximumReverseVelocity.setDescription(bundle.getString("vehicle.maximumReverseVelocity.text"));
    pMaximumReverseVelocity.setHelptext(bundle.getString("vehicle.maximumReverseVelocity.helptext"));
    setProperty(MAXIMUM_REVERSE_VELOCITY, pMaximumReverseVelocity);

    PercentProperty pEnergyLevel = new PercentProperty(this, true);
    pEnergyLevel.setDescription(bundle.getString("vehicle.energyLevel.text"));
    pEnergyLevel.setHelptext(bundle.getString("vehicle.energyLevel.helptext"));
    pEnergyLevel.setModellingEditable(false);
    setProperty(ENERGY_LEVEL, pEnergyLevel);

    SelectionProperty<EnergyState> pEnergyState
        = new SelectionProperty<>(this, Arrays.asList(EnergyState.values()), EnergyState.CRITICAL);
    pEnergyState.setDescription(bundle.getString("vehicle.energyState.text"));
    pEnergyState.setHelptext(bundle.getString("vehicle.energyState.helptext"));
    pEnergyState.setModellingEditable(false);
    setProperty(ENERGY_STATE, pEnergyState);

    BooleanProperty pLoaded = new BooleanProperty(this);
    pLoaded.setDescription(bundle.getString("vehicle.loaded.text"));
    pLoaded.setHelptext(bundle.getString("vehicle.loaded.helptext"));
    pLoaded.setModellingEditable(false);
    setProperty(LOADED, pLoaded);

    SelectionProperty<Vehicle.State> pState
        = new SelectionProperty<>(this, Arrays.asList(Vehicle.State.values()), Vehicle.State.UNKNOWN);
    pState.setDescription(bundle.getString("vehicle.state.text"));
    pState.setHelptext(bundle.getString("vehicle.state.helptext"));
    pState.setModellingEditable(false);
    setProperty(STATE, pState);

    SelectionProperty<Vehicle.ProcState> pProcState
        = new SelectionProperty<>(this, Arrays.asList(Vehicle.ProcState.values()), Vehicle.ProcState.UNAVAILABLE);
    pProcState.setDescription(bundle.getString("vehicle.procState.text"));
    pProcState.setHelptext(bundle.getString("vehicle.procState.helptext"));
    pProcState.setModellingEditable(false);
    setProperty(PROC_STATE, pProcState);

    SelectionProperty<Vehicle.IntegrationLevel> pIntegrationLevel
        = new SelectionProperty<>(this,
                                  Arrays.asList(Vehicle.IntegrationLevel.values()),
                                  Vehicle.IntegrationLevel.TO_BE_RESPECTED);
    pIntegrationLevel.setDescription(bundle.getString("vehicle.integrationLevel.text"));
    pIntegrationLevel.setHelptext(bundle.getString("vehicle.integrationLevel.helptext"));
    pIntegrationLevel.setModellingEditable(false);
    setProperty(INTEGRATION_LEVEL, pIntegrationLevel);

    StringProperty pPoint = new StringProperty(this);
    pPoint.setDescription(bundle.getString("vehicle.point.text"));
    pPoint.setHelptext(bundle.getString("vehicle.point.helptext"));
    pPoint.setModellingEditable(false);
    setProperty(POINT, pPoint);

    StringProperty pNextPoint = new StringProperty(this);
    pNextPoint.setDescription(bundle.getString("vehicle.nextPoint.text"));
    pNextPoint.setHelptext(bundle.getString("vehicle.nextPoint.helptext"));
    pNextPoint.setModellingEditable(false);
    setProperty(NEXT_POINT, pNextPoint);

    TripleProperty pPrecisePosition = new TripleProperty(this);
    pPrecisePosition.setDescription(bundle.getString("vehicle.precisePosition.text"));
    pPrecisePosition.setHelptext(bundle.getString("vehicle.precisePosition.helptext"));
    pPrecisePosition.setModellingEditable(false);
    setProperty(PRECISE_POSITION, pPrecisePosition);

    AngleProperty pOrientationAngle = new AngleProperty(this);
    pOrientationAngle.setDescription(bundle.getString("vehicle.orientationAngle.text"));
    pOrientationAngle.setHelptext(bundle.getString("vehicle.orientationAngle.helptext"));
    pOrientationAngle.setModellingEditable(false);
    setProperty(ORIENTATION_ANGLE, pOrientationAngle);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("vehicle.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("vehicle.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);

    StringProperty curTransportOrderName = new StringProperty(this);
    curTransportOrderName.setDescription(bundle.getString("vehicle.currentTransportOrder.text"));
    curTransportOrderName.setHelptext(bundle.getString("vehicle.currentTransportOrder.helptext"));
    curTransportOrderName.setModellingEditable(false);
    setProperty(CURRENT_TRANSPORT_ORDER_NAME, curTransportOrderName);

    StringProperty curOrderSequenceName = new StringProperty(this);
    curOrderSequenceName.setDescription(bundle.getString("vehicle.currentOrderSequence.text"));
    curOrderSequenceName.setHelptext(bundle.getString("vehicle.currentOrderSequence.helptext"));
    curOrderSequenceName.setModellingEditable(false);
    setProperty(CURRENT_SEQUENCE_NAME, curOrderSequenceName);

    OrderCategoriesProperty pProcessableCategories = new OrderCategoriesProperty(this);
    pProcessableCategories.setDescription(bundle.getString("vehicle.processableCategories.text"));
    pProcessableCategories.setHelptext(bundle.getString("vehicle.processableCategories.helptext"));
    pProcessableCategories.setModellingEditable(false);
    pProcessableCategories.setOperatingEditable(true);
    setProperty(PROCESSABLE_CATEGORIES, pProcessableCategories);
  }

  public enum EnergyState {

    CRITICAL,
    DEGRADED,
    GOOD
  }
}
