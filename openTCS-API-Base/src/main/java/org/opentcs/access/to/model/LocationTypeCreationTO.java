/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.data.model.visualization.LocationRepresentation;

/**
 * A transfer object describing a location type in the plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LocationTypeCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The allowed operations for this location type.
   */
  private List<String> allowedOperations = new LinkedList<>();
  /**
   * The allowed peripheral operations for this location type.
   */
  private List<String> allowedPeripheralOperations = new LinkedList<>();
  /**
   * The information regarding the grahical representation of this location type.
   */
  private Layout layout = new Layout();

  /**
   * Creates a new instance.
   *
   * @param name The name of this location type.
   */
  public LocationTypeCreationTO(@Nonnull String name) {
    super(name);
  }

  private LocationTypeCreationTO(@Nonnull String name,
                                 @Nonnull Map<String, String> properties,
                                 @Nonnull List<String> allowedOperations,
                                 @Nonnull List<String> allowedPeripheralOperations,
                                 @Nonnull Layout layout) {
    super(name, properties);
    this.allowedOperations = requireNonNull(allowedOperations, "allowedOperations");
    this.allowedPeripheralOperations = requireNonNull(allowedPeripheralOperations,
                                                      "allowedPeripheralOperations");
    this.layout = requireNonNull(layout, "layout");
  }

  /**
   * Returns the allowed operations for this location type.
   *
   * @return The allowed operations for this location type.
   */
  @Nonnull
  public List<String> getAllowedOperations() {
    return Collections.unmodifiableList(allowedOperations);
  }

  /**
   * Creates a copy of this object with the given allowed operations.
   *
   * @param allowedOperations the new allowed operations.
   * @return A copy of this object, differing in the given value.
   */
  public LocationTypeCreationTO withAllowedOperations(@Nonnull List<String> allowedOperations) {
    return new LocationTypeCreationTO(getName(),
                                      getModifiableProperties(),
                                      allowedOperations,
                                      allowedPeripheralOperations,
                                      layout);
  }

  /**
   * Returns the allowed peripheral operations for this location type.
   *
   * @return The allowed peripheral operations for this location type.
   */
  @Nonnull
  public List<String> getAllowedPeripheralOperations() {
    return Collections.unmodifiableList(allowedPeripheralOperations);
  }

  /**
   * Creates a copy of this object with the given allowed peripheral operations.
   *
   * @param allowedPeripheralOperations the new allowed peripheral operations.
   * @return A copy of this object, differing in the given value.
   */
  public LocationTypeCreationTO withAllowedPeripheralOperations(
      @Nonnull List<String> allowedPeripheralOperations) {
    return new LocationTypeCreationTO(getName(),
                                      getModifiableProperties(),
                                      allowedOperations,
                                      allowedPeripheralOperations,
                                      layout);
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public LocationTypeCreationTO withName(@Nonnull String name) {
    return new LocationTypeCreationTO(name,
                                      getProperties(),
                                      allowedOperations,
                                      allowedPeripheralOperations,
                                      layout);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public LocationTypeCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new LocationTypeCreationTO(getName(),
                                      properties,
                                      allowedOperations,
                                      allowedPeripheralOperations,
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
  public LocationTypeCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new LocationTypeCreationTO(getName(),
                                      propertiesWith(key, value),
                                      allowedOperations,
                                      allowedPeripheralOperations,
                                      layout);
  }

  /**
   * Returns the information regarding the grahical representation of this location type.
   *
   * @return The information regarding the grahical representation of this location type.
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
  public LocationTypeCreationTO withLayout(Layout layout) {
    return new LocationTypeCreationTO(getName(),
                                      getModifiableProperties(),
                                      allowedOperations,
                                      allowedPeripheralOperations,
                                      layout);
  }

  @Override
  public String toString() {
    return "LocationTypeCreationTO{"
        + "name=" + getName()
        + ", allowedOperations=" + allowedOperations
        + ", allowedPeripheralOperations" + allowedPeripheralOperations
        + ", layout=" + layout
        + ", properties=" + getProperties()
        + '}';
  }

  /**
   * Contains information regarding the grahical representation of a location type.
   */
  public static class Layout
      implements Serializable {

    /**
     * The location representation to use for locations with this location type.
     */
    private final LocationRepresentation locationRepresentation;

    /**
     * Creates a new instance.
     */
    public Layout() {
      this(LocationRepresentation.NONE);
    }

    /**
     * Creates a new instance.
     *
     * @param locationRepresentation The location representation to use for locations with this
     * location type.
     */
    public Layout(LocationRepresentation locationRepresentation) {
      this.locationRepresentation = requireNonNull(locationRepresentation,
                                                   "locationRepresentation");
    }

    /**
     * Returns the location representation to use for locations with this location type.
     *
     * @return The location representation to use for locations with this location type.
     */
    public LocationRepresentation getLocationRepresentation() {
      return locationRepresentation;
    }

    /**
     * Creates a copy of this object, with the given location representation.
     *
     * @param locationRepresentation The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withLocationRepresentation(LocationRepresentation locationRepresentation) {
      return new Layout(locationRepresentation);
    }

    @Override
    public String toString() {
      return "Layout{"
          + "locationRepresentation=" + locationRepresentation
          + '}';
    }
  }
}
