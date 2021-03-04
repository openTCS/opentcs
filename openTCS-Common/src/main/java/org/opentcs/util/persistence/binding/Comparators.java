/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.binding;

import java.util.Comparator;

/**
 * Some comparator implementations for JAXB classes.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public final class Comparators {

  /**
   * A <code>Comparator</code> for ordering <code>PlantModelElementTO</code>s ascendingly by their
   * names.
   */
  private static final Comparator<PlantModelElementTO> ELEMENTS_BY_NAME
      = (PlantModelElementTO o1, PlantModelElementTO o2) -> o1.getName().compareTo(o2.getName());
  /**
   * A <code>Comparator</code> for ordering <code>OutgoingPath</code>s ascendingly by their names.
   */
  private static final Comparator<PointTO.OutgoingPath> OUTGOING_PATHS_BY_NAME
      = (PointTO.OutgoingPath o1, PointTO.OutgoingPath o2) -> o1.getName().compareTo(o2.getName());
  /**
   * A <code>Comparator</code> for ordering <code>Link</code>s ascendingly by their point names.
   */
  private static final Comparator<LocationTO.Link> LINKS_BY_POINT_NAME
      = (LocationTO.Link o1, LocationTO.Link o2) -> o1.getPoint().compareTo(o2.getPoint());
  /**
   * A <code>Comparator</code> for ordering <code>Hop</code>s ascendingly by their names.
   */
  private static final Comparator<StaticRouteTO.Hop> HOPS_BY_NAME
      = (StaticRouteTO.Hop o1, StaticRouteTO.Hop o2) -> o1.getName().compareTo(o2.getName());
  /**
   * A <code>Comparator</code> for ordering <code>ModelLayoutelements</code>s ascendingly by their
   * names.
   */
  private static final Comparator<VisualLayoutTO.ModelLayoutElement> MODEL_LAYOUT_ELEMENTS_BY_NAME
      = (VisualLayoutTO.ModelLayoutElement o1, VisualLayoutTO.ModelLayoutElement o2)
      -> o1.getVisualizedObjectName().compareTo(o2.getVisualizedObjectName());
  /**
   * A <code>Comparator</code> for ordering <code>Properties</code> ascendingly by their
   * names.
   */
  private static final Comparator<PropertyTO> PROPERTIES_BY_NAME
      = (PropertyTO o1, PropertyTO o2) -> o1.getName().compareTo(o2.getName());

  private Comparators() {
  }

  /**
   * Returns a comparator for ordering <code>PlantModelElementTO</code>s ascendingly by their names.
   *
   * @return A comparator for ordering <code>PlantModelElementTO</code>s ascendingly by their names.
   */
  public static Comparator<PlantModelElementTO> elementsByName() {
    return ELEMENTS_BY_NAME;
  }

  /**
   * Returns a comparator for ordering <code>OutgoingPath</code>s ascendingly by their names.
   *
   * @return A comparator for ordering <code>OutgoingPath</code>s ascendingly by their names.
   */
  public static Comparator<PointTO.OutgoingPath> outgoingPathsByName() {
    return OUTGOING_PATHS_BY_NAME;
  }

  /**
   * Returns a comparator for ordering <code>Link</code>s ascendingly by their point names.
   *
   * @return A comparator for ordering <code>Link</code>s ascendingly by their point names.
   */
  public static Comparator<LocationTO.Link> linksByPointName() {
    return LINKS_BY_POINT_NAME;
  }

  /**
   * Returns a comparator for ordering <code>Hop</code>s ascendingly by their names.
   *
   * @return A comparator for ordering <code>Hop</code>s ascendingly by their names.
   */
  public static Comparator<StaticRouteTO.Hop> hopsByName() {
    return HOPS_BY_NAME;
  }

  /**
   * Returns a comparator for ordering <code>ModelLayoutElements</code>s ascendingly by their names.
   *
   * @return A comparator for ordering <code>ModelLayoutElements</code>s ascendingly by their names.
   */
  public static Comparator<VisualLayoutTO.ModelLayoutElement> modelLayoutelementsByName() {
    return MODEL_LAYOUT_ELEMENTS_BY_NAME;
  }

  /**
   * Returns a comparator for ordering <code>Properties</code>s ascendingly by their names.
   *
   * @return A comparator for ordering <code>Properties</code> ascendingly by their names.
   */
  public static Comparator<PropertyTO> propertiesByName() {
    return PROPERTIES_BY_NAME;
  }
}
