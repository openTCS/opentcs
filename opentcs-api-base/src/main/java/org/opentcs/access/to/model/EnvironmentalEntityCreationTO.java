// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.model;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing an environmental entity.
 */
public class EnvironmentalEntityCreationTO
    extends
      CreationTO
    implements
      Serializable {

  private final boolean incompleteName;
  private final EnvelopeCreationTO envelope;
  private final PoseCreationTO pose;
  private final Type type;
  private final IntegrationLevel integrationLevel;
  private final Layout layout;

  /**
   * Creates a new instance.
   *
   * @param name The name of this entity.
   * @param envelope The envelope / spatial extent of the entity.
   * @param pose The pose of the entity.
   */
  public EnvironmentalEntityCreationTO(
      @Nonnull
      String name,
      @Nonnull
      EnvelopeCreationTO envelope,
      @Nonnull
      PoseCreationTO pose
  ) {
    super(name);
    this.incompleteName = false;
    this.envelope = requireNonNull(envelope, "envelope");
    this.pose = requireNonNull(pose, "pose");
    this.type = Type.OBJECT;
    this.integrationLevel = IntegrationLevel.TO_BE_RESPECTED;
    this.layout = new Layout();
  }

  private EnvironmentalEntityCreationTO(
      @Nonnull
      String name,
      @Nonnull
      Map<String, String> properties,
      boolean incompleteName,
      @Nonnull
      EnvelopeCreationTO envelope,
      @Nonnull
      PoseCreationTO pose,
      @Nonnull
      Type type,
      @Nonnull
      IntegrationLevel integrationLevel,
      @Nonnull
      Layout layout
  ) {
    super(name, properties);
    this.incompleteName = incompleteName;
    this.envelope = requireNonNull(envelope, "envelope");
    this.pose = requireNonNull(pose, "pose");
    this.type = requireNonNull(type, "type");
    this.integrationLevel = requireNonNull(integrationLevel, "integrationLevel");
    this.layout = requireNonNull(layout, "layout");
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public EnvironmentalEntityCreationTO withName(
      @Nonnull
      String name
  ) {
    return new EnvironmentalEntityCreationTO(
        name,
        getModifiableProperties(),
        incompleteName,
        envelope,
        pose,
        type,
        integrationLevel,
        layout
    );
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  public EnvironmentalEntityCreationTO withProperties(
      @Nonnull
      Map<String, String> properties
  ) {
    return new EnvironmentalEntityCreationTO(
        getName(),
        properties,
        incompleteName,
        envelope,
        pose,
        type,
        integrationLevel,
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
  public EnvironmentalEntityCreationTO withProperty(
      @Nonnull
      String key,
      @Nonnull
      String value
  ) {
    return new EnvironmentalEntityCreationTO(
        getName(),
        propertiesWith(key, value),
        incompleteName,
        envelope,
        pose,
        type,
        integrationLevel,
        layout
    );
  }

  /**
   * Indicates whether the name is incomplete and requires to be completed when creating the actual
   * environmental entity.
   * (How exactly this is done is decided by the kernel.)
   *
   * @return <code>true</code> if, and only if, the name is incomplete and requires to be completed
   * by the kernel.
   */
  public boolean hasIncompleteName() {
    return incompleteName;
  }

  /**
   * Creates a copy of this object with the given <em>incompleteName</em> flag.
   *
   * @param incompleteName Whether the name is incomplete and requires to be completed when creating
   * the actual environmental entity.
   * @return A copy of this object, differing in the given value.
   */
  public EnvironmentalEntityCreationTO withIncompleteName(boolean incompleteName) {
    return new EnvironmentalEntityCreationTO(
        getName(),
        getModifiableProperties(),
        incompleteName,
        envelope,
        pose,
        type,
        integrationLevel,
        layout
    );
  }


  /**
   * Returns the envelope / spatial extent of the entity.
   *
   * @return The envelope.
   */
  public EnvelopeCreationTO getEnvelope() {
    return envelope;
  }

  /**
   * Creates a copy of this object, with the given envelope.
   *
   * @param envelope The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public EnvironmentalEntityCreationTO withEnvelope(EnvelopeCreationTO envelope) {
    return new EnvironmentalEntityCreationTO(
        getName(),
        getModifiableProperties(),
        incompleteName,
        envelope,
        pose,
        type,
        integrationLevel,
        layout
    );
  }

  /**
   * Returns the pose of the entity.
   *
   * @return The pose.
   */
  public PoseCreationTO getPose() {
    return pose;
  }

  /**
   * Creates a copy of this object, with the given pose.
   *
   * @param pose The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public EnvironmentalEntityCreationTO withPose(PoseCreationTO pose) {
    return new EnvironmentalEntityCreationTO(
        getName(),
        getModifiableProperties(),
        incompleteName,
        envelope,
        pose,
        type,
        integrationLevel,
        layout
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
  public EnvironmentalEntityCreationTO withType(Type type) {
    return new EnvironmentalEntityCreationTO(
        getName(),
        getModifiableProperties(),
        incompleteName,
        envelope,
        pose,
        type,
        integrationLevel,
        layout
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
  public EnvironmentalEntityCreationTO withIntegrationLevel(IntegrationLevel integrationLevel) {
    return new EnvironmentalEntityCreationTO(
        getName(),
        getModifiableProperties(),
        incompleteName,
        envelope,
        pose,
        type,
        integrationLevel,
        layout
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
  public EnvironmentalEntityCreationTO withLayout(Layout layout) {
    return new EnvironmentalEntityCreationTO(
        getName(),
        getModifiableProperties(),
        incompleteName,
        envelope,
        pose,
        type,
        integrationLevel,
        layout
    );
  }


  @Override
  public String toString() {
    return "EnvironmentalEntityCreationTO{" +
        "incompleteName=" + incompleteName +
        ", envelope=" + envelope +
        ", pose=" + pose +
        ", type=" + type +
        ", integrationLevel=" + integrationLevel +
        ", layout=" + layout +
        ", properties=" + getProperties() +
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

    public Layout(int layerId) {
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
