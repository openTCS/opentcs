// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.model;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;
import org.opentcs.access.to.CreationTO;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A transfer object describing a point in the plant model.
 */
public class PointCreationTO
    extends
      CreationTO
    implements
      Serializable {

  /**
   * The pose of the vehicle at this point.
   */
  private final PoseCreationTO pose;
  /**
   * This point's type.
   */
  @Nonnull
  private final Type type;
  /**
   * A map of envelope keys to envelopes that vehicles located at this point may occupy.
   */
  private final Map<String, EnvelopeCreationTO> vehicleEnvelopes;
  /**
   * The maximum bounding box (in mm) that a vehicle at this point is allowed to have.
   */
  private final BoundingBoxCreationTO maxVehicleBoundingBox;
  /**
   * The information regarding the graphical representation of this point.
   */
  private final Layout layout;

  /**
   * Creates a new instance.
   *
   * @param name The name of this point.
   */
  public PointCreationTO(
      @Nonnull
      String name
  ) {
    super(name);
    this.pose = new PoseCreationTO(new TripleCreationTO(0, 0, 0), Double.NaN);
    this.type = Type.HALT_POSITION;
    this.vehicleEnvelopes = Map.of();
    this.maxVehicleBoundingBox
        = new BoundingBoxCreationTO(1000, 1000, 1000);
    this.layout = new Layout();
  }

  private PointCreationTO(
      @Nonnull
      String name,
      @Nonnull
      Map<String, String> properties,
      @Nonnull
      PoseCreationTO pose,
      @Nonnull
      Type type,
      @Nonnull
      Map<String, EnvelopeCreationTO> vehicleEnvelopes,
      @Nonnull
      BoundingBoxCreationTO maxVehicleBoundingBox,
      @Nonnull
      Layout layout
  ) {
    super(name, properties);
    this.pose = requireNonNull(pose, "pose");
    requireNonNull(pose.getPosition(), "A point requires a pose with a position.");
    this.type = requireNonNull(type, "type");
    this.vehicleEnvelopes = requireNonNull(vehicleEnvelopes, "vehicleEnvelopes");
    this.maxVehicleBoundingBox = requireNonNull(maxVehicleBoundingBox, "maxVehicleBoundingBox");
    this.layout = requireNonNull(layout, "layout");
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public PointCreationTO withName(
      @Nonnull
      String name
  ) {
    return new PointCreationTO(
        name,
        getModifiableProperties(),
        pose,
        type,
        vehicleEnvelopes,
        maxVehicleBoundingBox,
        layout
    );
  }

  /**
   * Returns the pose of the vehicle at this point.
   *
   * @return The pose of the vehicle at this point.
   */
  @Nonnull
  public PoseCreationTO getPose() {
    return pose;
  }

  /**
   * Creates a copy of this object with the given pose.
   *
   * @param pose The new pose.
   * @return A copy of this object, differing in the given position.
   */
  public PointCreationTO withPose(
      @Nonnull
      PoseCreationTO pose
  ) {
    return new PointCreationTO(
        getName(),
        getModifiableProperties(),
        pose,
        type,
        vehicleEnvelopes,
        maxVehicleBoundingBox,
        layout
    );
  }

  /**
   * Returns the type of this point.
   *
   * @return The type of this point.
   */
  @Nonnull
  public Type getType() {
    return type;
  }

  /**
   * Creates a copy of this object with the given type.
   *
   * @param type The new type.
   * @return A copy of this object, differing in the given type.
   */
  public PointCreationTO withType(
      @Nonnull
      Type type
  ) {
    return new PointCreationTO(
        getName(),
        getProperties(),
        pose,
        type,
        vehicleEnvelopes,
        maxVehicleBoundingBox,
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
  public PointCreationTO withProperties(
      @Nonnull
      Map<String, String> properties
  ) {
    return new PointCreationTO(
        getName(),
        properties,
        pose,
        type,
        vehicleEnvelopes,
        maxVehicleBoundingBox,
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
  public PointCreationTO withProperty(
      @Nonnull
      String key,
      @Nonnull
      String value
  ) {
    return new PointCreationTO(
        getName(),
        propertiesWith(key, value),
        pose,
        type,
        vehicleEnvelopes,
        maxVehicleBoundingBox,
        layout
    );
  }

  /**
   * Returns a map of envelope keys to envelopes that vehicles located at this point may occupy.
   *
   * @return A map of envelope keys to envelopes that vehicles located at this point may occupy.
   */
  public Map<String, EnvelopeCreationTO> getVehicleEnvelopes() {
    return vehicleEnvelopes;
  }

  /**
   * Creates a copy of this object, with the given vehicle envelopes.
   *
   * @param vehicleEnvelopes The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PointCreationTO withVehicleEnvelopes(
      @Nonnull
      Map<String, EnvelopeCreationTO> vehicleEnvelopes
  ) {
    return new PointCreationTO(
        getName(),
        getModifiableProperties(),
        pose,
        type,
        vehicleEnvelopes,
        maxVehicleBoundingBox,
        layout
    );
  }

  /**
   * Returns the maximum bounding box (in mm) that a vehicle at this point is allowed to have.
   *
   * @return The maximum bounding box (in mm) that a vehicle at this point is allowed to have.
   */
  public BoundingBoxCreationTO getMaxVehicleBoundingBox() {
    return maxVehicleBoundingBox;
  }

  /**
   * Creates a copy of this object, with the given maximum vehicle bounding box.
   *
   * @param maxVehicleBoundingBox The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PointCreationTO withMaxVehicleBoundingBox(BoundingBoxCreationTO maxVehicleBoundingBox) {
    return new PointCreationTO(
        getName(),
        getModifiableProperties(),
        pose,
        type,
        vehicleEnvelopes,
        maxVehicleBoundingBox,
        layout
    );
  }

  /**
   * Returns the information regarding the graphical representation of this point.
   *
   * @return The information regarding the graphical representation of this point.
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
    return new PointCreationTO(
        getName(),
        getModifiableProperties(),
        pose,
        type,
        vehicleEnvelopes,
        maxVehicleBoundingBox,
        layout
    );
  }

  @Override
  public String toString() {
    return "PointCreationTO{"
        + "name=" + getName()
        + ", pose=" + pose
        + ", type=" + type
        + ", vehicleEnvelopes=" + vehicleEnvelopes
        + ", layout=" + layout
        + ", maxVehicleBoundingBox=" + maxVehicleBoundingBox
        + ", properties=" + getProperties()
        + '}';
  }

  /**
   * Describes the types of positions in a driving course.
   */
  public enum Type {

    /**
     * Indicates a position at which a vehicle may halt temporarily, e.g. for executing an
     * operation.
     * The vehicle is also expected to report in when it arrives at such a position.
     * It may not park here for longer than necessary, though.
     */
    HALT_POSITION,
    /**
     * Indicates a position at which a vehicle may halt for longer periods of time when it is not
     * processing orders.
     * The vehicle is also expected to report in when it arrives at such a position.
     */
    PARK_POSITION;
  }

  /**
   * Contains information regarding the graphical representation of a point.
   */
  public static class Layout
      implements
        Serializable {

    /**
     * The coordinates at which the point is to be drawn (in mm).
     */
    private final CoupleCreationTO position;
    /**
     * The offset of the label's position to the point's position (in lu).
     */
    private final CoupleCreationTO labelOffset;
    /**
     * The ID of the layer on which the point is to be drawn.
     */
    private final int layerId;

    /**
     * Creates a new instance.
     */
    public Layout() {
      this(new CoupleCreationTO(0, 0), 0);
    }

    /**
     * Creates a new instance.
     *
     * @param position The coordinates at which the point is to be drawn (in mm).
     * @param labelOffset The offset of the label's position to the point's position (in lu).
     * @param layerId The ID of the layer on which the point is to be drawn.
     * @deprecated Use {@link Layout#Layout(CoupleCreationTO, int)} instead.
     */
    @Deprecated
    @ScheduledApiChange(when = "7.0", details = "Will be removed")
    public Layout(
        CoupleCreationTO position,
        CoupleCreationTO labelOffset,
        int layerId
    ) {
      this.position = requireNonNull(position, "position");
      this.labelOffset = requireNonNull(labelOffset, "labelOffset");
      this.layerId = layerId;
    }

    /**
     * Creates a new instance.
     *
     * @param labelOffset The offset of the label's position to the point's position (in lu).
     * @param layerId The ID of the layer on which the point is to be drawn.
     */
    public Layout(
        CoupleCreationTO labelOffset,
        int layerId
    ) {
      this.position = new CoupleCreationTO(0, 0);
      this.labelOffset = requireNonNull(labelOffset, "labelOffset");
      this.layerId = layerId;
    }

    /**
     * Returns the coordinates at which the point is to be drawn (in mm).
     *
     * @return The coordinates at which the point is to be drawn (in mm).
     * @deprecated Will be removed without replacement. {@link PointCreationTO#getPose()}
     * should be used instead.
     */
    @Deprecated
    @ScheduledApiChange(when = "7.0", details = "Will be removed")
    public CoupleCreationTO getPosition() {
      return position;
    }

    /**
     * Creates a copy of this object, with the given position.
     *
     * @param position The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     * @deprecated Will be removed without replacement.
     * {@link PointCreationTO#withPose(PoseCreationTO)} should be used instead.
     */
    @Deprecated
    @ScheduledApiChange(when = "7.0", details = "Will be removed")
    public Layout withPosition(CoupleCreationTO position) {
      return new Layout(
          position,
          labelOffset,
          layerId
      );
    }

    /**
     * Returns the offset of the label's position to the point's position (in lu).
     *
     * @return The offset of the label's position to the point's position (in lu).
     */
    public CoupleCreationTO getLabelOffset() {
      return labelOffset;
    }

    /**
     * Creates a copy of this object, with the given X label offset.
     *
     * @param labelOffset The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withLabelOffset(CoupleCreationTO labelOffset) {
      return new Layout(
          position,
          labelOffset,
          layerId
      );
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
      return new Layout(
          position,
          labelOffset,
          layerId
      );
    }

    @Override
    public String toString() {
      return "Layout{"
          + ", labelOffset=" + labelOffset
          + ", layerId=" + layerId
          + '}';
    }
  }
}
