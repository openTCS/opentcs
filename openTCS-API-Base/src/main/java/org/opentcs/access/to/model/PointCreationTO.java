/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A transfer object describing a point in the plant model.
 */
public class PointCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The pose of the vehicle at this point.
   */
  private final Pose pose;
  /**
   * This point's type.
   */
  @Nonnull
  private final Point.Type type;
  /**
   * A map of envelope keys to envelopes that vehicles located at this point may occupy.
   */
  private final Map<String, Envelope> vehicleEnvelopes;
  /**
   * The information regarding the grahical representation of this point.
   */
  private final Layout layout;

  /**
   * Creates a new instance.
   *
   * @param name The name of this point.
   */
  public PointCreationTO(@Nonnull String name) {
    super(name);
    this.pose = new Pose(new Triple(0, 0, 0), Double.NaN);
    this.type = Point.Type.HALT_POSITION;
    this.vehicleEnvelopes = Map.of();
    this.layout = new Layout();
  }

  private PointCreationTO(@Nonnull String name,
                          @Nonnull Map<String, String> properties,
                          @Nonnull Pose pose,
                          @Nonnull Point.Type type,
                          @Nonnull Map<String, Envelope> vehicleEnvelopes,
                          @Nonnull Layout layout) {
    super(name, properties);
    this.pose = requireNonNull(pose, "pose");
    this.type = requireNonNull(type, "type");
    this.vehicleEnvelopes = requireNonNull(vehicleEnvelopes, "vehicleEnvelopes");
    this.layout = requireNonNull(layout, "layout");
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public PointCreationTO withName(@Nonnull String name) {
    return new PointCreationTO(name,
                               getModifiableProperties(),
                               pose,
                               type,
                               vehicleEnvelopes,
                               layout);
  }

  /**
   * Returns the pose of the vehicle at this point.
   *
   * @return The pose of the vehicle at this point.
   */
  @Nonnull
  public Pose getPose() {
    return pose;
  }

  /**
   * Creates a copy of this object with the given pose.
   *
   * @param pose The new pose.
   * @return A copy of this object, differing in the given position.
   */
  public PointCreationTO withPose(@Nonnull Pose pose) {
    return new PointCreationTO(getName(),
                               getModifiableProperties(),
                               pose,
                               type,
                               vehicleEnvelopes,
                               layout);
  }

  /**
   * Returns the position of this point (in mm).
   *
   * @return The position of this point (in mm).
   * @deprecated Use {@link #getPose()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  @Nonnull
  public Triple getPosition() {
    return pose.getPosition();
  }

  /**
   * Creates a copy of this object with the given position (in mm).
   *
   * @param position The new position.
   * @return A copy of this object, differing in the given position.
   * @deprecated Use {@link #withPose(org.opentcs.data.model.Pose)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public PointCreationTO withPosition(@Nonnull Triple position) {
    return new PointCreationTO(getName(),
                               getModifiableProperties(),
                               pose.withPosition(position),
                               type,
                               vehicleEnvelopes,
                               layout);
  }

  /**
   * Returns a vehicle's orientation angle at this position.
   * (-360..360, or {@code Double.NaN}, if an orientation angle is not specified for this point.)
   *
   * @return The vehicle's orientation angle when it's at this position.
   * @deprecated Use {@link #getPose()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public double getVehicleOrientationAngle() {
    return pose.getOrientationAngle();
  }

  /**
   * Creates a copy of this object with the vehicle's (assumed) orientation angle when it's at this
   * position.
   * Allowed value range: [-360..360], or {@code Double.NaN} to indicate that there is no specific
   * orientation angle for this point.
   *
   * @param vehicleOrientationAngle The new angle.
   * @return A copy of this object, differing in the given angle.
   * @deprecated Use {@link #withPose(org.opentcs.data.model.Pose)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public PointCreationTO withVehicleOrientationAngle(double vehicleOrientationAngle) {
    return new PointCreationTO(getName(),
                               getModifiableProperties(),
                               pose.withOrientationAngle(vehicleOrientationAngle),
                               type,
                               vehicleEnvelopes,
                               layout);
  }

  /**
   * Returns the type of this point.
   *
   * @return The type of this point.
   */
  @Nonnull
  public Point.Type getType() {
    return type;
  }

  /**
   * Creates a copy of this object with the given type.
   *
   * @param type The new type.
   * @return A copy of this object, differing in the given type.
   */
  public PointCreationTO withType(@Nonnull Point.Type type) {
    return new PointCreationTO(getName(),
                               getProperties(),
                               pose,
                               type,
                               vehicleEnvelopes,
                               layout);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public PointCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new PointCreationTO(getName(),
                               properties,
                               pose,
                               type,
                               vehicleEnvelopes,
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
  public PointCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new PointCreationTO(getName(),
                               propertiesWith(key, value),
                               pose,
                               type,
                               vehicleEnvelopes,
                               layout);
  }

  /**
   * Returns a map of envelope keys to envelopes that vehicles located at this point may occupy.
   *
   * @return A map of envelope keys to envelopes that vehicles located at this point may occupy.
   */
  public Map<String, Envelope> getVehicleEnvelopes() {
    return vehicleEnvelopes;
  }

  /**
   * Creates a copy of this object, with the given vehicle envelopes.
   *
   * @param vehicleEnvelopes The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PointCreationTO withVehicleEnvelopes(@Nonnull Map<String, Envelope> vehicleEnvelopes) {
    return new PointCreationTO(getName(),
                               getModifiableProperties(),
                               pose,
                               type,
                               vehicleEnvelopes,
                               layout);
  }

  /**
   * Returns the information regarding the grahical representation of this point.
   *
   * @return The information regarding the grahical representation of this point.
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
  public PointCreationTO withLayout(Layout layout) {
    return new PointCreationTO(getName(),
                               getModifiableProperties(),
                               pose,
                               type,
                               vehicleEnvelopes,
                               layout);
  }

  @Override
  public String toString() {
    return "PointCreationTO{"
        + "name=" + getName()
        + ", pose=" + pose
        + ", type=" + type
        + ", vehicleEnvelopes=" + vehicleEnvelopes
        + ", layout=" + layout
        + ", properties=" + getProperties()
        + '}';
  }

  /**
   * Contains information regarding the grahical representation of a point.
   */
  public static class Layout
      implements Serializable {

    /**
     * The coordinates at which the point is to be drawn (in mm).
     */
    private final Couple position;
    /**
     * The offset of the label's position to the point's position (in lu).
     */
    private final Couple labelOffset;
    /**
     * The ID of the layer on which the point is to be drawn.
     */
    private final int layerId;

    /**
     * Creates a new instance.
     */
    public Layout() {
      this(new Couple(0, 0), new Couple(0, 0), 0);
    }

    /**
     * Creates a new instance.
     *
     * @param position The coordinates at which the point is to be drawn (in mm).
     * @param labelOffset The offset of the label's position to the point's position (in lu).
     * @param layerId The ID of the layer on which the point is to be drawn.
     */
    public Layout(Couple position,
                  Couple labelOffset,
                  int layerId) {
      this.position = requireNonNull(position, "position");
      this.labelOffset = requireNonNull(labelOffset, "labelOffset");
      this.layerId = layerId;
    }

    /**
     * Returns the coordinates at which the point is to be drawn (in mm).
     *
     * @return The coordinates at which the point is to be drawn (in mm).
     */
    public Couple getPosition() {
      return position;
    }

    /**
     * Creates a copy of this object, with the given position.
     *
     * @param position The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withPosition(Couple position) {
      return new Layout(position,
                        labelOffset,
                        layerId);
    }

    /**
     * Returns the offset of the label's position to the point's position (in lu).
     *
     * @return The offset of the label's position to the point's position (in lu).
     */
    public Couple getLabelOffset() {
      return labelOffset;
    }

    /**
     * Creates a copy of this object, with the given X label offset.
     *
     * @param labelOffset The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withLabelOffset(Couple labelOffset) {
      return new Layout(position,
                        labelOffset,
                        layerId);
    }

    /**
     * Returns the ID of the layer on which the point is to be drawn.
     *
     * @return The layer ID.
     */
    public int getLayerId() {
      return layerId;
    }

    /**
     * Creates a copy of this object, with the given layer ID.
     *
     * @param layerId The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withLayerId(int layerId) {
      return new Layout(position,
                        labelOffset,
                        layerId);
    }

    @Override
    public String toString() {
      return "Layout{"
          + "position=" + position
          + ", labelOffset=" + labelOffset
          + ", layerId=" + layerId
          + '}';
    }
  }
}
