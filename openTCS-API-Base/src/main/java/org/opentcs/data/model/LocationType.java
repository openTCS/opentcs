/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.TCSObject;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Describes the type of a {@link Location}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
public class LocationType
    extends TCSObject<LocationType>
    implements Serializable,
               Cloneable {

  /**
   * The operations allowed at locations of this type.
   */
  private final List<String> allowedOperations;

  /**
   * Creates a new LocationType.
   *
   * @param objectID The new location type's object ID.
   * @param name The new location type's name.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public LocationType(int objectID, String name) {
    super(objectID, name);
    this.allowedOperations = new ArrayList<>();
  }

  /**
   * Creates a new LocationType.
   *
   * @param name The new location type's name.
   */
  public LocationType(String name) {
    super(name);
    this.allowedOperations = new ArrayList<>();
  }

  @SuppressWarnings("deprecation")
  private LocationType(int objectID,
                       String name,
                       Map<String, String> properties,
                       List<String> allowedOperations) {
    super(objectID, name, properties);
    this.allowedOperations = listWithoutNullValues(requireNonNull(allowedOperations,
                                                                  "allowedOperations"));
  }

  @Override
  public LocationType withProperty(String key, String value) {
    return new LocationType(getIdWithoutDeprecationWarning(),
                            getName(),
                            propertiesWith(key, value),
                            allowedOperations);
  }

  @Override
  public LocationType withProperties(Map<String, String> properties) {
    return new LocationType(getIdWithoutDeprecationWarning(),
                            getName(),
                            properties,
                            allowedOperations);
  }

  /**
   * Returns a set of operations allowed with locations of this type.
   *
   * @return A set of operations allowed with locations of this type.
   */
  public List<String> getAllowedOperations() {
    return Collections.unmodifiableList(allowedOperations);
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
   * Adds an allowed operation.
   *
   * @param operation The operation to be allowed.
   * @return <code>true</code> if, and only if, the given operation wasn't
   * already allowed with this location type.
   * @deprecated Set via constructor instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean addAllowedOperation(String operation) {
    requireNonNull(operation, "operation");
    if (allowedOperations.contains(operation)) {
      return false;
    }
    else {
      return allowedOperations.add(operation);
    }
  }

  /**
   * Removes an allowed operation.
   *
   * @param operation The operation to be disallowed.
   * @return <code>true</code> if, and only if, the given operation was allowed
   * with this location type before.
   * @deprecated Set via constructor instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean removeAllowedOperation(String operation) {
    requireNonNull(operation, "operation");
    return allowedOperations.remove(operation);
  }

  /**
   * Creates a copy of this object, with the given allowed operations.
   *
   * @param allowedOperations The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public LocationType withAllowedOperations(List<String> allowedOperations) {
    return new LocationType(getIdWithoutDeprecationWarning(),
                            getName(),
                            getProperties(),
                            allowedOperations);
  }

  @SuppressWarnings("deprecation")
  private int getIdWithoutDeprecationWarning() {
    return getId();
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated Will become immutable and not implement Cloneable any more.
   */
  @Override
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public LocationType clone() {
    return new LocationType(getIdWithoutDeprecationWarning(),
                            getName(),
                            getProperties(),
                            allowedOperations);
  }
}
