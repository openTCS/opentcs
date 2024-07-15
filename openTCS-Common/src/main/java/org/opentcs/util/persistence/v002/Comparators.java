/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v002;

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
   * Returns a comparator for ordering <code>Hop</code>s ascendingly by their names.
   *
   * @return A comparator for ordering <code>Hop</code>s ascendingly by their names.
   */
  public static Comparator<StaticRouteTO.Hop> hopsByName() {
    return Comparator.comparing(StaticRouteTO.Hop::getName);
  }

  /**
   * Returns a comparator for ordering <code>ModelLayoutElements</code>s ascendingly by their names.
   *
   * @return A comparator for ordering <code>ModelLayoutElements</code>s ascendingly by their names.
   */
  public static Comparator<VisualLayoutTO.ModelLayoutElement> modelLayoutelementsByName() {
    return Comparator.comparing(VisualLayoutTO.ModelLayoutElement::getVisualizedObjectName);
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
