// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.model;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.visualization.LocationRepresentation;

/**
 * Describes the type of a {@link Location}.
 */
public class LocationType
    extends
      TCSObject<LocationType>
    implements
      Serializable {

  /**
   * The operations allowed at locations of this type.
   */
  private final List<String> allowedOperations;
  /**
   * The peripheral operations allowed at locations of this type.
   */
  private final List<String> allowedPeripheralOperations;
  /**
   * The information regarding the graphical representation of this location type.
   */
  private final Layout layout;

  /**
   * Creates a new LocationType.
   *
   * @param name The new location type's name.
   */
  public LocationType(String name) {
    super(name);
    this.allowedOperations = List.of();
    this.allowedPeripheralOperations = List.of();
    this.layout = new Layout();
  }

  private LocationType(
      String name,
      Map<String, String> properties,
      ObjectHistory history,
      List<String> allowedOperations,
      List<String> allowedPeripheralOperations,
      Layout layout
  ) {
    super(name, properties, history);
    this.allowedOperations = requireNonNull(allowedOperations, "allowedOperations");
    this.allowedPeripheralOperations
        = requireNonNull(allowedPeripheralOperations, "allowedPeripheralOperations");
    this.layout = requireNonNull(layout, "layout");
  }

  @Override
  public LocationType withProperty(String key, String value) {
    return new LocationType(
        getName(),
        propertiesWith(key, value),
        getHistory(),
        allowedOperations,
        allowedPeripheralOperations,
        layout
    );
  }

  @Override
  public LocationType withProperties(Map<String, String> properties) {
    return new LocationType(
        getName(),
        mapWithoutNullValues(properties),
        getHistory(),
        allowedOperations,
        allowedPeripheralOperations,
        layout
    );
  }

  @Override
  public LocationType withHistoryEntry(ObjectHistory.Entry entry) {
    return new LocationType(
        getName(),
        getProperties(),
        getHistory().withEntryAppended(entry),
        allowedOperations,
        allowedPeripheralOperations,
        layout
    );
  }

  @Override
  public LocationType withHistory(ObjectHistory history) {
    return new LocationType(
        getName(),
        getProperties(),
        history,
        allowedOperations,
        allowedPeripheralOperations,
        layout
    );
  }

  /**
   * Returns a set of operations allowed with locations of this type.
   *
   * @return A set of operations allowed with locations of this type.
   */
  public List<String> getAllowedOperations() {
    return allowedOperations;
  }

  /**
   * Checks if a given operation is allowed with locations of this type.
   *
   * @param operation The operation to be checked for.
   * @return <code>true</code> if, and only if, the given operation is allowed
   * with locations of this type.
   */
  public boolean isAllowedOperation(String operation) {
    requireNonNull(operation, "operation");
    return allowedOperations.contains(operation);
  }

  /**
   * Creates a copy of this object, with the given allowed operations.
   *
   * @param allowedOperations The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public LocationType withAllowedOperations(List<String> allowedOperations) {
    return new LocationType(
        getName(),
        getProperties(),
        getHistory(),
        listWithoutNullValues(allowedOperations),
        allowedPeripheralOperations,
        layout
    );
  }

  /**
   * Returns a set of peripheral operations allowed with locations of this type.
   *
   * @return A set of peripheral operations allowed with locations of this type.
   */
  public List<String> getAllowedPeripheralOperations() {
    return allowedPeripheralOperations;
  }

  /**
   * Creates a copy of this object, with the given allowed peripheral operations.
   *
   * @param allowedPeripheralOperations The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public LocationType withAllowedPeripheralOperations(List<String> allowedPeripheralOperations) {
    return new LocationType(
        getName(),
        getProperties(),
        getHistory(),
        allowedOperations,
        listWithoutNullValues(allowedPeripheralOperations),
        layout
    );
  }

  /**
   * Returns the information regarding the graphical representation of this location type.
   *
   * @return The information regarding the graphical representation of this location type.
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
  public LocationType withLayout(Layout layout) {
    return new LocationType(
        getName(),
        getProperties(),
        getHistory(),
        allowedOperations,
        allowedPeripheralOperations,
        layout
    );
  }

  @Override
  public String toString() {
    return "LocationType{"
        + "name=" + getName()
        + ", allowedOperations=" + allowedOperations
        + ", allowedPeripheralOperations=" + allowedPeripheralOperations
        + ", layout=" + layout
        + ", properties=" + getProperties()
        + ", history=" + getHistory()
        + '}';
  }

  /**
   * Contains information regarding the graphical representation of a location type.
   */
  public static class Layout
      implements
        Serializable {

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
      this.locationRepresentation = requireNonNull(
          locationRepresentation,
          "locationRepresentation"
      );
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
  }
}
