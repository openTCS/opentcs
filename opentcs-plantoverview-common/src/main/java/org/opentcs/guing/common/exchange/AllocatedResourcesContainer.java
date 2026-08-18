// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.exchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.opentcs.guing.base.model.FigureDecorationDetails;

/**
 * Maintains a set of all currently allocated resources.
 */
public class AllocatedResourcesContainer {
  /**
   * Maps model components names to allocated components of instance FigureDecorationDetails.
   */
  private final Map<String, FigureDecorationDetails> allocatedResources = new HashMap<>();
  /**
   * An unmodifiable view on {@link AllocatedResourcesContainer#allocatedResources}.
   */
  private final Map<String, FigureDecorationDetails> allocatedResourcesUnmodifiable
      = Collections.unmodifiableMap(allocatedResources);

  /**
   * Creates a new instance.
   */
  public AllocatedResourcesContainer() {
  }

  /**
   * Returns all allocated points and paths as FigureDecorationDetails, mapped to their name.
   *
   * @return A map of names of allocated points and paths and the respective
   * FigureDecorationDetails.
   */
  public Map<String, FigureDecorationDetails> getAllocatedResources() {
    return allocatedResourcesUnmodifiable;
  }

  /**
   * Adds a new component with its name to the map of allocated resources.
   *
   * @param name The name of the component
   * @param component The figure to add
   */
  public void addAllocatedResources(String name, FigureDecorationDetails component) {
    allocatedResources.put(name, component);
  }

  /**
   * Removes a component with its name from the map of allocated resources.
   *
   * @param name The name of the component to remove
   */
  public void removeAllocatedResource(String name) {
    allocatedResources.remove(name);
  }


}
