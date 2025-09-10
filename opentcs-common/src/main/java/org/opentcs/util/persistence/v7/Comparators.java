// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v7;

import java.util.Comparator;

/**
 * Some comparator implementations for JAXB classes.
 */
public final class Comparators {

  /**
   * Prevents instantiation.
   */
  private Comparators() {
  }

  /**
   * Returns a comparator for ordering <code>PlantModelElementTO</code>s ascendingly by their names.
   *
   * @return A comparator for ordering <code>PlantModelElementTO</code>s ascendingly by their names.
   */
  public static Comparator<PlantModelElementTO> elementsByName() {
    return Comparator.comparing(PlantModelElementTO::getName);
  }

  /**
   * Returns a comparator for ordering <code>OutgoingPath</code>s ascendingly by their names.
   *
   * @return A comparator for ordering <code>OutgoingPath</code>s ascendingly by their names.
   */
  public static Comparator<PointTO.OutgoingPath> outgoingPathsByName() {
    return Comparator.comparing(PointTO.OutgoingPath::getName);
  }

  /**
   * Returns a comparator for ordering <code>Link</code>s ascendingly by their point names.
   *
   * @return A comparator for ordering <code>Link</code>s ascendingly by their point names.
   */
  public static Comparator<LocationTO.Link> linksByPointName() {
    return Comparator.comparing(LocationTO.Link::getPoint);
  }

  /**
   * Returns a comparator for ordering <code>Properties</code>s ascendingly by their names.
   *
   * @return A comparator for ordering <code>Properties</code> ascendingly by their names.
   */
  public static Comparator<PropertyTO> propertiesByName() {
    return Comparator.comparing(PropertyTO::getName);
  }
}
