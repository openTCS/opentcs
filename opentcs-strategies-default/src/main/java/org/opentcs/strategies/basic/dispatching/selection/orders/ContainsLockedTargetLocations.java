// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection.orders;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Objects;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.selection.TransportOrderSelectionFilter;

/**
 * Filters transport orders that contain locked target locations.
 */
public class ContainsLockedTargetLocations
    implements
      TransportOrderSelectionFilter {

  /**
   * The object service.
   */
  private final TCSObjectService objectService;

  @Inject
  public ContainsLockedTargetLocations(TCSObjectService objectService) {
    this.objectService = requireNonNull(objectService, "objectService");
  }

  @Override
  public Collection<String> apply(TransportOrder order) {
    return !lockedLocations(order) ? new ArrayList<>() : Arrays.asList(getClass().getName());
  }

  @SuppressWarnings("unchecked")
  private boolean lockedLocations(TransportOrder order) {
    return order.getAllDriveOrders().stream()
        .map(driveOrder -> driveOrder.getDestination().getDestination())
        .filter(destination -> Objects.equal(destination.getReferentClass(), Location.class))
        .map(destination -> (TCSObjectReference<Location>) destination)
        .map(locationReference -> objectService.fetchObject(Location.class, locationReference))
        .anyMatch(location -> location.isLocked());
  }
}
