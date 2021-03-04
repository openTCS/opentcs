/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.TCSObject;

/**
 * Instances of this class describe the attributes of location types relevant
 * to the openTCS system.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LocationType
extends TCSObject<LocationType>
implements Serializable, Cloneable {
  /**
   * The operations allowed at locations of this type.
   */
  private List<String> allowedOperations = new LinkedList<>();
  
  /**
   * Creates a new LocationType.
   *
   * @param objectID The new location type's object ID.
   * @param name The new location type's name.
   */
  public LocationType(int objectID, String name) {
    super(objectID, name);
  }
  
  /**
   * Returns a set of operations allowed with locations of this type.
   *
   * @return A set of operations allowed with locations of this type.
   */
  public List<String> getAllowedOperations() {
    return new LinkedList<>(allowedOperations);
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
   */
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
   */
  public boolean removeAllowedOperation(String operation) {
    requireNonNull(operation, "operation");
    return allowedOperations.remove(operation);
  }
  
  @Override
  public LocationType clone() {
    LocationType clone = (LocationType) super.clone();
    clone.allowedOperations = new LinkedList<>(allowedOperations);
    return clone;
  }
}
