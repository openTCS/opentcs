/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * A transfer object describing a block in the plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The vehicle's length (in mm).
   */
  private int length = 1000;
  /**
   * The energy level value at/below which the vehicle should be recharged.
   */
  private int energyLevelCritical = 30;
  /**
   * The energy level value at/above which the vehicle can be dispatched again when charging.
   */
  private int energyLevelGood = 90;
  /**
   * The energy level value at/above which the vehicle is considered fully recharged.
   */
  private int energyLevelFullyRecharged = 90;
  /**
   * The energy level value at/above which the vehicle is considered sufficiently recharged.
   */
  private int energyLevelSufficientlyRecharged = 30;
  /**
   * The vehicle's maximum velocity (in mm/s).
   */
  private int maxVelocity = 1000;
  /**
   * The vehicle's maximum reverse velocity (in mm/s).
   */
  private int maxReverseVelocity = 1000;
  /**
   * The information regarding the grahical representation of this vehicle.
   */
  private Layout layout = new Layout();

  /**
   * Creates a new instance.
   *
   * @param name The name of this vehicle.
   */
  public VehicleCreationTO(@Nonnull String name) {
    super(name);
  }

  private VehicleCreationTO(@Nonnull String name,
                            @Nonnull Map<String, String> properties,
                            int length,
                            int energyLevelCritical,
                            int energyLevelGood,
                            int energyLevelFullyRecharged,
                            int energyLevelSufficientlyRecharged,
                            int maxVelocity,
                            int maxReverseVelocity,
                            @Nonnull Layout layout) {
    super(name, properties);
    this.length = length;
    this.energyLevelCritical = energyLevelCritical;
    this.energyLevelGood = energyLevelGood;
    this.energyLevelFullyRecharged = energyLevelFullyRecharged;
    this.energyLevelSufficientlyRecharged = energyLevelSufficientlyRecharged;
    this.maxVelocity = maxVelocity;
    this.maxReverseVelocity = maxReverseVelocity;
    this.layout = requireNonNull(layout, "layout");
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new instance.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public VehicleCreationTO withName(@Nonnull String name) {
    return new VehicleCreationTO(name,
                                 getModifiableProperties(),
                                 length,
                                 energyLevelCritical,
                                 energyLevelGood,
                                 energyLevelFullyRecharged,
                                 energyLevelSufficientlyRecharged,
                                 maxVelocity,
                                 maxReverseVelocity,
                                 layout);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public VehicleCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new VehicleCreationTO(getName(),
                                 properties,
                                 length,
                                 energyLevelCritical,
                                 energyLevelGood,
                                 energyLevelFullyRecharged,
                                 energyLevelSufficientlyRecharged,
                                 maxVelocity,
                                 maxReverseVelocity,
                                 layout);
  }

  /**
   * Creates a copy of this object and adds the given property.
   * If value == null, then the key-value pair is removed from the properties.
   *
   * @param key the key.
   * @param value the value
   * @return A copy of this object that either
   * includes the given entry in it's current properties, if value != null or
   * excludes the entry otherwise.
   */
  @Override
  public VehicleCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new VehicleCreationTO(getName(),
                                 propertiesWith(key, value),
                                 length,
                                 energyLevelCritical,
                                 energyLevelGood,
                                 energyLevelFullyRecharged,
                                 energyLevelSufficientlyRecharged,
                                 maxVelocity,
                                 maxReverseVelocity,
                                 layout);
  }

  /**
   * Returns the vehicle's length (in mm).
   *
   * @return The vehicle's length (in mm).
   */
  public int getLength() {
    return length;
  }

  /**
   * Creates a copy of this object with the vehicle's given length (in mm).
   *
   * @param length The new length. Must be at least 1.
   * @return A copy of this object, differing in the given vehicle length.
   */
  public VehicleCreationTO withLength(int length) {
    checkArgument(length >= 1, "length must be at least 1: " + length);
    return new VehicleCreationTO(getName(),
                                 getModifiableProperties(),
                                 length,
                                 energyLevelCritical,
                                 energyLevelGood,
                                 energyLevelFullyRecharged,
                                 energyLevelSufficientlyRecharged,
                                 maxVelocity,
                                 maxReverseVelocity,
                                 layout);
  }

  /**
   * Returns this vehicle's critical energy level (in percent of the maximum).
   * The critical energy level is the one at/below which the vehicle should be recharged.
   *
   * @return This vehicle's critical energy level.
   */
  public int getEnergyLevelCritical() {
    return energyLevelCritical;
  }

  /**
   * Creates a copy of this object with the given critical energy level.
   * The critical energy level is the one at/below which the vehicle should be recharged.
   *
   * @param energyLevelCritical The new critical energy level. Must not be smaller than 0 or
   * greater than 100.
   * @return A copy of this object, differing in the given value.
   */
  public VehicleCreationTO withEnergyLevelCritical(int energyLevelCritical) {
    checkInRange(energyLevelCritical, 0, 100);
    return new VehicleCreationTO(getName(),
                                 getModifiableProperties(),
                                 length,
                                 energyLevelCritical,
                                 energyLevelGood,
                                 energyLevelFullyRecharged,
                                 energyLevelSufficientlyRecharged,
                                 maxVelocity,
                                 maxReverseVelocity,
                                 layout);
  }

  /**
   * Returns this vehicle's good energy level (in percent of the maximum).
   * The good energy level is the one at/above which the vehicle can be dispatched again when
   * charging.
   *
   * @return This vehicle's good energy level.
   */
  public int getEnergyLevelGood() {
    return energyLevelGood;
  }

  /**
   * Creates a copy of this object with the vehicle's good energy level (in percent of the maximum).
   * The good energy level is the one at/above which the vehicle can be dispatched again when
   * charging.
   *
   * @param energyLevelGood The new good energy level. Must not be smaller than 0 or greater than
   * 100.
   * @return A copy of this object, differing in the given value.
   */
  public VehicleCreationTO withEnergyLevelGood(int energyLevelGood) {
    checkInRange(energyLevelGood, 0, 100);
    return new VehicleCreationTO(getName(),
                                 getModifiableProperties(),
                                 length,
                                 energyLevelCritical,
                                 energyLevelGood,
                                 energyLevelFullyRecharged,
                                 energyLevelSufficientlyRecharged,
                                 maxVelocity,
                                 maxReverseVelocity,
                                 layout);
  }

  /**
   * Returns this vehicle's fully recharged energy level (in percent of the maximum).
   *
   * @return This vehicle's fully recharged energy level.
   */
  public int getEnergyLevelFullyRecharged() {
    return energyLevelFullyRecharged;
  }

  /**
   * Creates a copy of this object with the vehicle's fully recharged energy level (in percent of
   * the maximum).
   *
   * @param energyLevelFullyRecharged The new fully recharged energy level.
   * Must not be smaller than 0 or greater than 100.
   * @return A copy of this object, differing in the given value.
   */
  public VehicleCreationTO withEnergyLevelFullyRecharged(int energyLevelFullyRecharged) {
    checkInRange(energyLevelFullyRecharged, 0, 100);
    return new VehicleCreationTO(getName(),
                                 getModifiableProperties(),
                                 length,
                                 energyLevelCritical,
                                 energyLevelGood,
                                 energyLevelFullyRecharged,
                                 energyLevelSufficientlyRecharged,
                                 maxVelocity,
                                 maxReverseVelocity,
                                 layout);
  }

  /**
   * Returns this vehicle's sufficiently recharged energy level (in percent of the maximum).
   *
   * @return This vehicle's sufficiently recharged energy level.
   */
  public int getEnergyLevelSufficientlyRecharged() {
    return energyLevelSufficientlyRecharged;
  }

  /**
   * Creates a copy of this object with the vehicle's sufficiently recharged energy level (in
   * percent of the maximum).
   *
   * @param energyLevelSufficientlyRecharged The new sufficiently recharged energy level.
   * Must not be smaller than 0 or greater than 100.
   * @return A copy of this object, differing in the given value.
   */
  public VehicleCreationTO withEnergyLevelSufficientlyRecharged(
      int energyLevelSufficientlyRecharged) {
    checkInRange(energyLevelSufficientlyRecharged, 0, 100);
    return new VehicleCreationTO(getName(),
                                 getModifiableProperties(),
                                 length,
                                 energyLevelCritical,
                                 energyLevelGood,
                                 energyLevelFullyRecharged,
                                 energyLevelSufficientlyRecharged,
                                 maxVelocity,
                                 maxReverseVelocity,
                                 layout);
  }

  public int getMaxVelocity() {
    return maxVelocity;
  }

  /**
   * Creates a copy of this object with the given maximum velocity (in mm/s).
   *
   * @param maxVelocity the new max velocity.
   * @return A copy of this object, differing in the given value.
   */
  public VehicleCreationTO withMaxVelocity(int maxVelocity) {
    this.maxVelocity = checkInRange(maxVelocity, 0, Integer.MAX_VALUE);
    return new VehicleCreationTO(getName(),
                                 getModifiableProperties(),
                                 length,
                                 energyLevelCritical,
                                 energyLevelGood,
                                 energyLevelFullyRecharged,
                                 energyLevelSufficientlyRecharged,
                                 maxVelocity,
                                 maxReverseVelocity,
                                 layout);
  }

  public int getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  /**
   * Creates a copy of this object with the given maximum reverse velocity (in mm/s).
   *
   * @param maxReverseVelocity the new maximum reverse velocity.
   * @return A copy of this object, differing in the given value.
   */
  public VehicleCreationTO withMaxReverseVelocity(int maxReverseVelocity) {
    checkInRange(maxReverseVelocity, 0, Integer.MAX_VALUE);
    return new VehicleCreationTO(getName(),
                                 getModifiableProperties(),
                                 length,
                                 energyLevelCritical,
                                 energyLevelGood,
                                 energyLevelFullyRecharged,
                                 energyLevelSufficientlyRecharged,
                                 maxVelocity,
                                 maxReverseVelocity,
                                 layout);
  }

  /**
   * Returns the information regarding the grahical representation of this vehicle.
   *
   * @return The information regarding the grahical representation of this vehicle.
   */
  public Layout getLayout() {
    return layout;
  }

  /**
   * Creates a copy of this object, with the given layout.
   *
   * @param layout The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VehicleCreationTO withLayout(Layout layout) {
    return new VehicleCreationTO(getName(),
                                 getModifiableProperties(),
                                 length,
                                 energyLevelCritical,
                                 energyLevelGood,
                                 energyLevelFullyRecharged,
                                 energyLevelSufficientlyRecharged,
                                 maxVelocity,
                                 maxReverseVelocity,
                                 layout);
  }

  @Override
  public String toString() {
    return "VehicleCreationTO{"
        + "name=" + getName()
        + ", length=" + length
        + ", energyLevelCritical=" + energyLevelCritical
        + ", energyLevelGood=" + energyLevelGood
        + ", energyLevelFullyRecharged=" + energyLevelFullyRecharged
        + ", energyLevelSufficientlyRecharged=" + energyLevelSufficientlyRecharged
        + ", maxVelocity=" + maxVelocity
        + ", maxReverseVelocity=" + maxReverseVelocity
        + ", layout=" + layout
        + ", properties=" + getProperties()
        + '}';
  }

  /**
   * Contains information regarding the grahical representation of a vehicle.
   */
  public static class Layout
      implements Serializable {

    /**
     * The color in which vehicle routes are to be emphasized.
     */
    private final Color routeColor;

    /**
     * Creates a new instance.
     */
    public Layout() {
      this(Color.RED);
    }

    /**
     * Creates a new instance.
     *
     * @param routeColor The color in which vehicle routes are to be emphasized.
     */
    public Layout(Color routeColor) {
      this.routeColor = requireNonNull(routeColor, "routeColor");
    }

    /**
     * Returns the color in which vehicle routes are to be emphasized.
     *
     * @return The color in which vehicle routes are to be emphasized.
     */
    public Color getRouteColor() {
      return routeColor;
    }

    /**
     * Creates a copy of this object, with the given color.
     *
     * @param routeColor The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withRouteColor(Color routeColor) {
      return new Layout(routeColor);
    }

    @Override
    public String toString() {
      return "Layout{"
          + "routeColor=" + routeColor
          + '}';
    }
  }
}
