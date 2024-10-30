// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.model;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.awt.Color;
import java.io.Serializable;
import java.util.Map;
import org.opentcs.access.to.CreationTO;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A transfer object describing a block in the plant model.
 */
public class VehicleCreationTO
    extends
      CreationTO
    implements
      Serializable {

  /**
   * The vehicle's bounding box (in mm).
   */
  private final BoundingBoxCreationTO boundingBox;
  /**
   * Contains information regarding the energy level threshold values of the vehicle.
   */
  private final EnergyLevelThresholdSet energyLevelThresholdSet;
  /**
   * The vehicle's maximum velocity (in mm/s).
   */
  private final int maxVelocity;
  /**
   * The vehicle's maximum reverse velocity (in mm/s).
   */
  private final int maxReverseVelocity;
  /**
   * The key for selecting the envelope to be used for resources the vehicle occupies.
   */
  private final String envelopeKey;
  /**
   * The information regarding the graphical representation of this vehicle.
   */
  private final Layout layout;

  /**
   * Creates a new instance.
   *
   * @param name The name of this vehicle.
   */
  public VehicleCreationTO(
      @Nonnull
      String name
  ) {
    super(name);
    this.boundingBox = new BoundingBoxCreationTO(1000, 1000, 1000);
    this.energyLevelThresholdSet = new EnergyLevelThresholdSet(30, 90, 30, 90);
    this.maxVelocity = 1000;
    this.maxReverseVelocity = 1000;
    this.envelopeKey = null;
    this.layout = new Layout();
  }

  private VehicleCreationTO(
      @Nonnull
      String name,
      @Nonnull
      Map<String, String> properties,
      @Nonnull
      BoundingBoxCreationTO boundingBox,
      @Nonnull
      EnergyLevelThresholdSet energyLevelThresholdSet,
      int maxVelocity,
      int maxReverseVelocity,
      @Nullable
      String envelopeKey,
      @Nonnull
      Layout layout
  ) {
    super(name, properties);
    this.boundingBox = requireNonNull(boundingBox, "boundingBox");
    this.energyLevelThresholdSet
        = requireNonNull(energyLevelThresholdSet, "energyLevelThresholdSet");
    this.maxVelocity = maxVelocity;
    this.maxReverseVelocity = maxReverseVelocity;
    this.envelopeKey = envelopeKey;
    this.layout = requireNonNull(layout, "layout");
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new instance.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public VehicleCreationTO withName(
      @Nonnull
      String name
  ) {
    return new VehicleCreationTO(
        name,
        getModifiableProperties(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        envelopeKey,
        layout
    );
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public VehicleCreationTO withProperties(
      @Nonnull
      Map<String, String> properties
  ) {
    return new VehicleCreationTO(
        getName(),
        properties,
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        envelopeKey,
        layout
    );
  }

  /**
   * Creates a copy of this object and adds the given property.
   * If value == null, then the key-value pair is removed from the properties.
   *
   * @param key the key.
   * @param value the value
   * @return A copy of this object that either
   * includes the given entry in its current properties, if value != null or
   * excludes the entry otherwise.
   */
  @Override
  public VehicleCreationTO withProperty(
      @Nonnull
      String key,
      @Nonnull
      String value
  ) {
    return new VehicleCreationTO(
        getName(),
        propertiesWith(key, value),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the vehicle's current bounding box (in mm).
   *
   * @return The vehicle's current bounding box (in mm).
   */
  public BoundingBoxCreationTO getBoundingBox() {
    return boundingBox;
  }

  /**
   * Creates a copy of this object, with the given bounding box (in mm).
   *
   * @param boundingBox The new bounding box.
   * @return A copy of this object, differing in the given vehicle bounding box.
   */
  public VehicleCreationTO withBoundingBox(BoundingBoxCreationTO boundingBox) {
    return new VehicleCreationTO(
        getName(),
        getModifiableProperties(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the vehicle's length (in mm).
   *
   * @return The vehicle's length (in mm).
   * @deprecated Use {@link #getBoundingBox()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public int getLength() {
    return (int) boundingBox.getLength();
  }

  /**
   * Creates a copy of this object with the vehicle's given length (in mm).
   *
   * @param length The new length. Must be at least 1.
   * @return A copy of this object, differing in the given vehicle length.
   * @deprecated Use {@link #withBoundingBox(BoundingBoxCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public VehicleCreationTO withLength(int length) {
    return withBoundingBox(boundingBox.withLength(length));
  }

  /**
   * Returns this vehicle's critical energy level (in percent of the maximum).
   * The critical energy level is the one at/below which the vehicle should be recharged.
   *
   * @return This vehicle's critical energy level.
   * @deprecated Use {@link #getEnergyLevelThresholdSet()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public int getEnergyLevelCritical() {
    return energyLevelThresholdSet.getEnergyLevelCritical();
  }

  /**
   * Creates a copy of this object with the given critical energy level.
   * The critical energy level is the one at/below which the vehicle should be recharged.
   *
   * @param energyLevelCritical The new critical energy level. Must not be smaller than 0 or
   * greater than 100.
   * @return A copy of this object, differing in the given value.
   * @deprecated Use {@link #withEnergyLevelThresholdSet(EnergyLevelThresholdSet)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public VehicleCreationTO withEnergyLevelCritical(int energyLevelCritical) {
    return withEnergyLevelThresholdSet(
        getEnergyLevelThresholdSet().withEnergyLevelCritical(energyLevelCritical)
    );
  }

  /**
   * Returns this vehicle's good energy level (in percent of the maximum).
   * The good energy level is the one at/above which the vehicle can be dispatched again when
   * charging.
   *
   * @return This vehicle's good energy level.
   * @deprecated Use {@link #getEnergyLevelThresholdSet()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public int getEnergyLevelGood() {
    return energyLevelThresholdSet.getEnergyLevelGood();
  }

  /**
   * Creates a copy of this object with the vehicle's good energy level (in percent of the maximum).
   * The good energy level is the one at/above which the vehicle can be dispatched again when
   * charging.
   *
   * @param energyLevelGood The new good energy level. Must not be smaller than 0 or greater than
   * 100.
   * @return A copy of this object, differing in the given value.
   * @deprecated Use {@link #withEnergyLevelThresholdSet(EnergyLevelThresholdSet)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public VehicleCreationTO withEnergyLevelGood(int energyLevelGood) {
    return withEnergyLevelThresholdSet(
        getEnergyLevelThresholdSet().withEnergyLevelGood(energyLevelGood)
    );
  }

  /**
   * Returns this vehicle's fully recharged energy level (in percent of the maximum).
   *
   * @return This vehicle's fully recharged energy level.
   * @deprecated Use {@link #getEnergyLevelThresholdSet()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public int getEnergyLevelFullyRecharged() {
    return energyLevelThresholdSet.getEnergyLevelFullyRecharged();
  }

  /**
   * Creates a copy of this object with the vehicle's fully recharged energy level (in percent of
   * the maximum).
   *
   * @param energyLevelFullyRecharged The new fully recharged energy level.
   * Must not be smaller than 0 or greater than 100.
   * @return A copy of this object, differing in the given value.
   * @deprecated Use {@link #withEnergyLevelThresholdSet(EnergyLevelThresholdSet)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public VehicleCreationTO withEnergyLevelFullyRecharged(int energyLevelFullyRecharged) {
    return withEnergyLevelThresholdSet(
        getEnergyLevelThresholdSet().withEnergyLevelFullyRecharged(energyLevelFullyRecharged)
    );
  }

  /**
   * Returns this vehicle's sufficiently recharged energy level (in percent of the maximum).
   *
   * @return This vehicle's sufficiently recharged energy level.
   * @deprecated Use {@link #getEnergyLevelThresholdSet()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public int getEnergyLevelSufficientlyRecharged() {
    return energyLevelThresholdSet.getEnergyLevelSufficientlyRecharged();
  }

  /**
   * Creates a copy of this object with the vehicle's sufficiently recharged energy level (in
   * percent of the maximum).
   *
   * @param energyLevelSufficientlyRecharged The new sufficiently recharged energy level.
   * Must not be smaller than 0 or greater than 100.
   * @return A copy of this object, differing in the given value.
   * @deprecated Use {@link #withEnergyLevelThresholdSet(EnergyLevelThresholdSet)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public VehicleCreationTO withEnergyLevelSufficientlyRecharged(
      int energyLevelSufficientlyRecharged
  ) {
    return withEnergyLevelThresholdSet(
        getEnergyLevelThresholdSet()
            .withEnergyLevelSufficientlyRecharged(energyLevelSufficientlyRecharged)
    );
  }

  /**
   * Returns this vehicle's energy level threshold set.
   *
   * @return This vehicle's energy level threshold set.
   */
  @Nonnull
  public EnergyLevelThresholdSet getEnergyLevelThresholdSet() {
    return energyLevelThresholdSet;
  }

  /**
   * Creates a copy of this object, with the given EnergyLevelThresholdSet.
   *
   * @param energyLevelThresholdSet The new EnergyLevelThresholdSet.
   * @return A copy of this object, differing in the given value.
   */
  public VehicleCreationTO withEnergyLevelThresholdSet(
      @Nonnull
      EnergyLevelThresholdSet energyLevelThresholdSet
  ) {
    return new VehicleCreationTO(
        getName(),
        getModifiableProperties(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        envelopeKey,
        layout
    );
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
    checkInRange(maxVelocity, 0, Integer.MAX_VALUE);
    return new VehicleCreationTO(
        getName(),
        getModifiableProperties(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        envelopeKey,
        layout
    );
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
    return new VehicleCreationTO(
        getName(),
        getModifiableProperties(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the key for selecting the envelope to be used for resources the vehicle occupies.
   *
   * @return The key for selecting the envelope to be used for resources the vehicle occupies.
   */
  @ScheduledApiChange(when = "7.0", details = "Envelope key will become non-null.")
  @Nullable
  public String getEnvelopeKey() {
    return envelopeKey;
  }

  /**
   *
   * Creates a copy of this object, with the given envelope key.
   *
   * @param envelopeKey The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  @ScheduledApiChange(when = "7.0", details = "Envelope key will become non-null.")
  public VehicleCreationTO withEnvelopeKey(
      @Nullable
      String envelopeKey
  ) {
    return new VehicleCreationTO(
        getName(),
        getModifiableProperties(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the information regarding the graphical representation of this vehicle.
   *
   * @return The information regarding the graphical representation of this vehicle.
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
    return new VehicleCreationTO(
        getName(),
        getModifiableProperties(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        envelopeKey,
        layout
    );
  }

  @Override
  public String toString() {
    return "VehicleCreationTO{"
        + "name=" + getName()
        + ", boundingBox=" + boundingBox
        + ", energyLevelThresholdSet=" + energyLevelThresholdSet
        + ", maxVelocity=" + maxVelocity
        + ", maxReverseVelocity=" + maxReverseVelocity
        + ", envelopeKey=" + envelopeKey
        + ", layout=" + layout
        + ", properties=" + getProperties()
        + '}';
  }

  /**
   * Contains information regarding the graphical representation of a vehicle.
   */
  public static class Layout
      implements
        Serializable {

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

  /**
   * Contains information regarding the energy level threshold values of a vehicle.
   */
  public static class EnergyLevelThresholdSet
      implements
        Serializable {

    private final int energyLevelCritical;
    private final int energyLevelGood;
    private final int energyLevelSufficientlyRecharged;
    private final int energyLevelFullyRecharged;

    /**
     * Creates a new instance.
     *
     * @param energyLevelCritical The value at/below which the vehicle's energy level is considered
     * "critical".
     * @param energyLevelGood The value at/above which the vehicle's energy level is considered
     * "good".
     * @param energyLevelSufficientlyRecharged The value at/above which the vehicle's energy level
     * is considered fully recharged.
     * @param energyLevelFullyRecharged The value at/above which the vehicle's energy level is
     * considered sufficiently recharged.
     */
    public EnergyLevelThresholdSet(
        int energyLevelCritical,
        int energyLevelGood,
        int energyLevelSufficientlyRecharged,
        int energyLevelFullyRecharged
    ) {
      this.energyLevelCritical = checkInRange(
          energyLevelCritical,
          0,
          100,
          "energyLevelCritical"
      );
      this.energyLevelGood = checkInRange(
          energyLevelGood,
          0,
          100,
          "energyLevelGood"
      );
      this.energyLevelSufficientlyRecharged = checkInRange(
          energyLevelSufficientlyRecharged,
          0,
          100,
          "energyLevelSufficientlyRecharged"
      );
      this.energyLevelFullyRecharged = checkInRange(
          energyLevelFullyRecharged,
          0,
          100,
          "energyLevelFullyRecharged"
      );
    }

    /**
     * Returns the vehicle's critical energy level (in percent of the maximum).
     * <p>
     * The critical energy level is the one at/below which the vehicle should be recharged.
     * </p>
     *
     * @return The vehicle's critical energy level.
     */
    public int getEnergyLevelCritical() {
      return energyLevelCritical;
    }

    /**
     * Creates a copy of this object, with the given critical energy level.
     *
     * @param energyLevelCritical The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public EnergyLevelThresholdSet withEnergyLevelCritical(int energyLevelCritical) {
      return new EnergyLevelThresholdSet(
          energyLevelCritical,
          energyLevelGood,
          energyLevelSufficientlyRecharged,
          energyLevelFullyRecharged
      );
    }

    /**
     * Returns the vehicle's good energy level (in percent of the maximum).
     * <p>
     * The good energy level is the one at/above which the vehicle can be dispatched again when
     * charging.
     * </p>
     *
     * @return The vehicle's good energy level.
     */
    public int getEnergyLevelGood() {
      return energyLevelGood;
    }

    /**
     * Creates a copy of this object, with the given good energy level.
     *
     * @param energyLevelGood The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public EnergyLevelThresholdSet withEnergyLevelGood(int energyLevelGood) {
      return new EnergyLevelThresholdSet(
          energyLevelCritical,
          energyLevelGood,
          energyLevelSufficientlyRecharged,
          energyLevelFullyRecharged
      );
    }

    /**
     * Returns the vehicle's energy level for being sufficiently recharged (in percent of the
     * maximum).
     *
     * @return This vehicle's sufficiently recharged energy level.
     */
    public int getEnergyLevelSufficientlyRecharged() {
      return energyLevelSufficientlyRecharged;
    }

    /**
     * Creates a copy of this object, with the given sufficiently recharged energy level.
     *
     * @param energyLevelSufficientlyRecharged The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public EnergyLevelThresholdSet withEnergyLevelSufficientlyRecharged(
        int energyLevelSufficientlyRecharged
    ) {
      return new EnergyLevelThresholdSet(
          energyLevelCritical,
          energyLevelGood,
          energyLevelSufficientlyRecharged,
          energyLevelFullyRecharged
      );
    }

    /**
     * Returns the vehicle's energy level for being fully recharged (in percent of the maximum).
     *
     * @return The vehicle's fully recharged threshold.
     */
    public int getEnergyLevelFullyRecharged() {
      return energyLevelFullyRecharged;
    }

    /**
     * Creates a copy of this object, with the given fully recharged energy level.
     *
     * @param energyLevelFullyRecharged The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public EnergyLevelThresholdSet withEnergyLevelFullyRecharged(int energyLevelFullyRecharged) {
      return new EnergyLevelThresholdSet(
          energyLevelCritical,
          energyLevelGood,
          energyLevelSufficientlyRecharged,
          energyLevelFullyRecharged
      );
    }

    @Override
    public String toString() {
      return "EnergyLevelThresholdSet{"
          + "energyLevelCritical=" + energyLevelCritical
          + ", energyLevelGood=" + energyLevelGood
          + ", energyLevelSufficientlyRecharged=" + energyLevelSufficientlyRecharged
          + ", energyLevelFullyRecharged=" + energyLevelFullyRecharged
          + '}';
    }
  }
}
