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
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * This class is used for calculating the costs to travel from one location to another.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @deprecated Providing travel costs to external clients will not be part of the standard kernel
 * API any more.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
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
