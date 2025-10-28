// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.model;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing a location in a plant model.
 */
public class LocationCreationTO
    extends
      CreationTO
    implements
      Serializable {

  /**
   * The name of this location's type.
   */
  @Nonnull
  private final String typeName;
  /**
   * This location's position (in mm).
   */
  @Nonnull
  private final TripleCreationTO position;
  /**
   * The links attaching points to this location.
   * This is a map of point names to allowed operations.
   */
  @Nonnull
  private final Map<String, Set<String>> links;
  /**
   * A flag for marking this location as locked (i.e. to prevent vehicles from using it).
   */
  private final boolean locked;
  /**
   * The information regarding the graphical representation of this location.
   */
  private final Layout layout;

  /**
   * Creates a new instance.
   *
   * @param name The name of this location.
   * @param typeName The name of this location's type.
   * @param position The position of this location.
   */
  public LocationCreationTO(
      @Nonnull
      String name,
      @Nonnull
      String typeName,
      @Nonnull
      TripleCreationTO position
  ) {
    super(name);
    this.typeName = requireNonNull(typeName, "typeName");
    this.position = position;
    this.links = Map.of();
    this.locked = false;
    this.layout = new Layout();
  }

  private LocationCreationTO(
      @Nonnull
      String name,
      @Nonnull
      Map<String, String> properties,
      @Nonnull
      String typeName,
      @Nonnull
      TripleCreationTO position,
      @Nonnull
      Map<String, Set<String>> links,
      boolean locked,
      @Nonnull
      Layout layout
  ) {
    super(name, properties);
    this.typeName = requireNonNull(typeName, "typeName");
    this.position = requireNonNull(position, "position");
    this.links = requireNonNull(links, "links");
    this.locked = locked;
    this.layout = requireNonNull(layout, "layout");
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public LocationCreationTO withName(
      @Nonnull
      String name
  ) {
    return new LocationCreationTO(
        name,
        getModifiableProperties(),
        typeName,
        position,
        links,
        locked,
        layout
    );
  }

  /**
   * Returns the name of this location's type.
   *
   * @return The name of this location's type.
   */
  @Nonnull
  public String getTypeName() {
    return typeName;
  }

  /**
   * Creates a copy of this object with the location's type.
   *
   * @param typeName The location type.
   * @return A copy of this object, differing in the given type.
   */
  public LocationCreationTO withTypeName(
      @Nonnull
      String typeName
  ) {
    return new LocationCreationTO(
        getName(),
        getModifiableProperties(),
        typeName,
        position,
        links,
        locked,
        layout
    );
  }

  /**
   * Returns the position of this location (in mm).
   *
   * @return The position of this location (in mm).
   */
  @Nonnull
  public TripleCreationTO getPosition() {
    return position;
  }

  /**
   * Creates a copy of this object with the given position (in mm).
   *
   * @param position the new position of this location (in mm).
   * @return A copy of this object, differing in the given position.
   */
  public LocationCreationTO withPosition(
      @Nonnull
      TripleCreationTO position
  ) {
    return new LocationCreationTO(
        getName(),
        getModifiableProperties(),
        typeName,
        position,
        links,
        locked,
        layout
    );
  }

  /**
   * Returns the links attaching points to this location.
   * This is a map of point names to allowed operations.
   *
   * @return The links attaching points to this location.
   */
  @Nonnull
  public Map<String, Set<String>> getLinks() {
    return Collections.unmodifiableMap(links);
  }

  /**
   * Creates a copy of this object with the given links that attach points to this location.
   *
   * @param links the new links. This is supposed to be a map of point names to allowed operations.
   * @return A copy of this object, differing in the given links.
   */
  public LocationCreationTO withLinks(
      @Nonnull
      Map<String, Set<String>> links
  ) {
    return new LocationCreationTO(
        getName(),
        getModifiableProperties(),
        typeName,
        position,
        links,
        locked,
        layout
    );
  }

  /**
   * Creates a copy of this object with the given links that attach points to this location.
   *
   * @param pointName The name of the point linked to.
   * @param allowedOperations The operations allowed at the point.
   * @return A copy of this object, differing in the given link.
   */
  public LocationCreationTO withLink(
      @Nonnull
      String pointName,
      @Nonnull
      Set<String> allowedOperations
  ) {
    return new LocationCreationTO(
        getName(),
        getModifiableProperties(),
        typeName,
        position,
        mapWithMapping(links, pointName, allowedOperations),
        locked,
        layout
    );
  }

  /**
   * Returns the lock status of this location (i.e. whether it my be used by vehicles or not).
   *
   * @return {@code true} if this location is currently locked (i.e. it may not be used
   * by vehicles), else {@code false}.
   */
  public boolean isLocked() {
    return locked;
  }

  /**
   * Creates a copy of this object with the given locked flag.
   *
   * @param locked The new locked attribute.
   * @return A copy of this object, differing in the locked attribute.
   */
  public LocationCreationTO withLocked(boolean locked) {
    return new LocationCreationTO(
        getName(),
        getModifiableProperties(),
        typeName,
        position,
        links,
        locked,
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
  public LocationCreationTO withProperties(
      @Nonnull
      Map<String, String> properties
  ) {
    return new LocationCreationTO(
        getName(),
        properties,
        typeName,
        position,
        links,
        locked,
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
   * includes the given entry in it's current properties, if value != null or
   * excludes the entry otherwise.
   */
  @Override
  public LocationCreationTO withProperty(
      @Nonnull
      String key,
      @Nonnull
      String value
  ) {
    return new LocationCreationTO(
        getName(),
        propertiesWith(key, value),
        typeName,
        position,
        links,
        locked,
        layout
    );
  }

  /**
   * Returns the information regarding the grahical representation of this location.
   *
   * @return The information regarding the grahical representation of this location.
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
  public LocationCreationTO withLayout(Layout layout) {
    return new LocationCreationTO(
        getName(),
        getModifiableProperties(),
        typeName,
        position,
        links,
        locked,
        layout
    );
  }

  @Override
  public String toString() {
    return "LocationCreationTO{"
        + "name=" + getName()
        + ", typeName=" + typeName
        + ", position=" + position
        + ", links=" + links
        + ", locked=" + locked
        + ", layout=" + layout
        + ", properties=" + getProperties()
        + '}';
  }

  /**
   * Contains information regarding the graphical representation of a location.
   */
  public static class Layout
      implements
        Serializable {

    /**
     * The offset of the label's position to the location's position (in lu).
     */
    private final CoupleCreationTO labelOffset;
    /**
     * The location representation to use.
     */
    private final LocationRepresentationTO locationRepresentation;
    /**
     * The ID of the layer on which the location is to be drawn.
     */
    private final int layerId;

    /**
     * Creates a new instance.
     */
    public Layout() {
      this(
          new CoupleCreationTO(0, 0),
          LocationRepresentationTO.DEFAULT,
          0
      );
    }

    /**
     * Creates a new instance.
     *
     * @param labelOffset The offset of the label's location to the point's position (in lu).
     * @param locationRepresentation The location representation to use.
     * @param layerId The ID of the layer on which the location is to be drawn.
     */
    public Layout(
        CoupleCreationTO labelOffset,
        LocationRepresentationTO locationRepresentation,
        int layerId
    ) {
      this.labelOffset = requireNonNull(labelOffset, "labelOffset");
      this.locationRepresentation = requireNonNull(
          locationRepresentation,
          "locationRepresentation"
      );
      this.layerId = layerId;
    }

    /**
     * Returns the offset of the label's position to the location's position (in lu).
     *
     * @return The offset of the label's position to the location's position (in lu).
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
          labelOffset,
          locationRepresentation,
          layerId
      );
    }

    /**
     * Returns the location representation to use.
     *
     * @return The location representation to use.
     */
    public LocationRepresentationTO getLocationRepresentation() {
      return locationRepresentation;
    }

    /**
     * Creates a copy of this object, with the given location representation.
     *
     * @param locationRepresentation The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withLocationRepresentation(
        LocationRepresentationTO locationRepresentation
    ) {
      return new Layout(
          labelOffset,
          locationRepresentation,
          layerId
      );
    }

    /**
     * Returns the ID of the layer on which the location is to be drawn.
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
          labelOffset,
          locationRepresentation,
          layerId
      );
    }

    @Override
    public String toString() {
      return "Layout{"
          + ", labelOffset=" + labelOffset
          + ", locationRepresentation=" + locationRepresentation
          + ", layerId=" + layerId
          + '}';
    }
  }
}
