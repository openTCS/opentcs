// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.model;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;

/**
 * An entity in the environment, not controlled by us, merely known.
 */
public class EnvironmentalEntity
    extends
      TCSObject<EnvironmentalEntity>
    implements
      Serializable {

  private final Envelope envelope;
  private final Pose pose;
  private final Type type;
  private final IntegrationLevel integrationLevel;
  private final boolean retired;
  private final Layout layout;
  private final Instant createdTime;
  private final Instant retiredTime;

  /**
   * Creates a new instance.
   *
   * @param name The name.
   * @param envelope The envelope.
   * @param pose The pose.
   */
  public EnvironmentalEntity(
      @Nonnull
      String name,
      @Nonnull
      Envelope envelope,
      @Nonnull
      Pose pose
  ) {
    super(name);
    this.createdTime = Instant.EPOCH;
    this.envelope = requireNonNull(envelope, "envelope");
    this.pose = requireNonNull(pose, "pose");
    this.type = Type.OBJECT;
    this.integrationLevel = IntegrationLevel.TO_BE_RESPECTED;
    this.retired = false;
    this.retiredTime = Instant.MAX;
    this.layout = new Layout();
  }

  private EnvironmentalEntity(
      String name,
      Map<String, String> properties,
      ObjectHistory history,
      Envelope envelope,
      Pose pose,
      Type type,
      IntegrationLevel integrationLevel,
      boolean retired,
      Layout layout,
      Instant createdTime,
      Instant retiredTime
  ) {
    super(name, properties, history);
    this.envelope = requireNonNull(envelope, "envelope");
    this.pose = requireNonNull(pose, "pose");
    requireNonNull(
        pose.getPosition(),
        "An environmental entity requires a pose with a position."
    );
    checkArgument(
        !Double.isNaN(pose.getOrientationAngle()),
        "An environmental entity requires an orientation angle."
    );
    this.type = requireNonNull(type, "type");
    this.integrationLevel = requireNonNull(integrationLevel, "integrationLevel");
    this.retired = retired;
    this.layout = requireNonNull(layout, "layout");
    this.createdTime = requireNonNull(createdTime, "createdTime");
    this.retiredTime = requireNonNull(retiredTime, "retiredTime");
  }

  @Override
  public EnvironmentalEntity withProperty(String key, String value) {
    return new EnvironmentalEntity(
        getName(),
        propertiesWith(key, value),
        getHistory(),
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  @Override
  public EnvironmentalEntity withProperties(Map<String, String> properties) {
    return new EnvironmentalEntity(
        getName(),
        mapWithoutNullValues(properties),
        getHistory(),
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  @Override
  public EnvironmentalEntity withHistoryEntry(ObjectHistory.Entry entry) {
    return new EnvironmentalEntity(
        getName(),
        getProperties(),
        getHistory().withEntryAppended(entry),
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  @Override
  public EnvironmentalEntity withHistory(ObjectHistory history) {
    return new EnvironmentalEntity(
        getName(),
        getProperties(),
        history,
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  /**
   * Returns the envelope / spatial extent of the entity.
   * <p>
   * Note that the envelope is defined with vertex coordinates relative to the plant coordinate
   * system's origin, and the pose rotates and translates the envelope to the actual position of the
   * entity.
   * </p>
   *
   * @return The envelope.
   */
  public Envelope getEnvelope() {
    return envelope;
  }

  /**
   * Creates a copy of this object, with the given envelope.
   * <p>
   * Note that the envelope is defined with vertex coordinates relative to the plant coordinate
   * system's origin, and the pose rotates and translates the envelope to the actual position of the
   * entity.
   * </p>
   *
   * @param envelope The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public EnvironmentalEntity withEnvelope(Envelope envelope) {
    return new EnvironmentalEntity(
        getName(),
        getProperties(),
        getHistory(),
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  /**
   * Returns the pose of the entity.
   * <p>
   * Note that the envelope is defined with vertex coordinates relative to the plant coordinate
   * system's origin, and the pose rotates and translates the envelope to the actual position of the
   * entity.
   * </p>
   *
   * @return The pose.
   */
  public Pose getPose() {
    return pose;
  }

  /**
   * Creates a copy of this object, with the given pose.
   * <p>
   * Note that the envelope is defined with vertex coordinates relative to the plant coordinate
   * system's origin, and the pose rotates and translates the envelope to the actual position of the
   * entity.
   * </p>
   *
   * @param pose The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public EnvironmentalEntity withPose(Pose pose) {
    return new EnvironmentalEntity(
        getName(),
        getProperties(),
        getHistory(),
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  /**
   * Returns the type of the entity.
   *
   * @return The type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Creates a copy of this object, with the given type.
   *
   * @param type The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public EnvironmentalEntity withType(Type type) {
    return new EnvironmentalEntity(
        getName(),
        getProperties(),
        getHistory(),
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  /**
   * Returns the integration level of the entity.
   *
   * @return The integration level.
   */
  public IntegrationLevel getIntegrationLevel() {
    return integrationLevel;
  }

  /**
   * Creates a copy of this object, with the given integration level.
   *
   * @param integrationLevel The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public EnvironmentalEntity withIntegrationLevel(IntegrationLevel integrationLevel) {
    return new EnvironmentalEntity(
        getName(),
        getProperties(),
        getHistory(),
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  /**
   * Indicates whether this entity is retired.
   *
   * @return true if this entity is retired.
   */
  public boolean isRetired() {
    return retired;
  }

  /**
   * Creates a copy of this object, with the given retired flag.
   *
   * @param retired The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public EnvironmentalEntity withRetired(boolean retired) {
    return new EnvironmentalEntity(
        getName(),
        getProperties(),
        getHistory(),
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  /**
   * Returns the layout of the entity.
   *
   * @return The layout.
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
  public EnvironmentalEntity withLayout(Layout layout) {
    return new EnvironmentalEntity(
        getName(),
        getProperties(),
        getHistory(),
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  /**
   * Returns this entity's creation time.
   *
   * @return This entity's creation time.
   */
  public Instant getCreatedTime() {
    return createdTime;
  }

  /**
   * Creates a copy of this object, with the given creation time.
   *
   * @param createdTime The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public EnvironmentalEntity withCreatedTime(Instant createdTime) {
    return new EnvironmentalEntity(
        getName(),
        getProperties(),
        getHistory(),
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  /**
   * Returns the time at which this entity was retired.
   *
   * @return The time at which this entity was retired.
   */
  public Instant getRetiredTime() {
    return retiredTime;
  }

  /**
   * Creates a copy of this object, with the given retired time.
   *
   * @param retiredTime The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public EnvironmentalEntity withRetiredTime(Instant retiredTime) {
    return new EnvironmentalEntity(
        getName(),
        getProperties(),
        getHistory(),
        envelope,
        pose,
        type,
        integrationLevel,
        retired,
        layout,
        createdTime,
        retiredTime
    );
  }

  @Override
  public String toString() {
    return "EnvironmentalEntity{" +
        "envelope=" + envelope +
        ", pose=" + pose +
        ", type=" + type +
        ", integrationLevel=" + integrationLevel +
        ", retired=" + retired +
        ", layout=" + layout +
        ", createdTime=" + createdTime +
        ", retiredTime=" + retiredTime +
        '}';
  }

  /**
   * The type of entity.
   */
  public enum Type {
    /**
     * The entity refers to a (physical/tangible) object.
     */
    OBJECT,
    /**
     * The entity refers to a zone.
     */
    ZONE
  }

  /**
   * An indication of how the entity is to be integrated into the system.
   */
  public enum IntegrationLevel {
    /**
     * Will be ignored.
     */
    TO_BE_IGNORED,
    /**
     * Will not be taken into account with e.g. traffic management, but may e.g. be shown.
     */
    TO_BE_NOTICED,
    /**
     * Will be taken into account with e.g. traffic management.
     */
    TO_BE_RESPECTED
  }

  /**
   * Contains information regarding the graphical representation of an environmental entity.
   */
  public static class Layout
      implements
        Serializable {

    /**
     * The ID of the layer on which the entity is to be drawn.
     */
    private final int layerId;

    /**
     * Creates a new instance.
     */
    public Layout() {
      this(0);
    }

    private Layout(int layerId) {
      this.layerId = layerId;
    }

    /**
     * Returns the ID of the layer on which the entity is to be drawn.
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
      return new Layout(layerId);
    }

    @Override
    public String toString() {
      return "Layout{" +
          "layerId=" + layerId +
          '}';
    }
  }
}
