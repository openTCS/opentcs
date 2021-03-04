/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;

/**
 * This class is used for calculating the costs to travel from one location to another.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class TravelCosts
    implements Serializable {

  /**
   * The destination reference.
   */
  private final TCSObjectReference<Location> ref;
  /**
   * The costs to travel to this location.
   */
  private final long costs;

  /**
   * Creates new travel costs.
   *
   * @param ref A reference to the destination
   * @param costs The costs
   */
  public TravelCosts(TCSObjectReference<Location> ref, long costs) {
    this.ref = requireNonNull(ref, "ref");
    this.costs = costs;
  }

  /**
   * Returns the reference to the destination.
   *
   * @return The destination's reference
   */
  public TCSObjectReference<Location> getLocation() {
    return ref;
  }

  /**
   * Returns the costs.
   *
   * @return The costs
   */
  public long getCosts() {
    return costs;
  }
}
