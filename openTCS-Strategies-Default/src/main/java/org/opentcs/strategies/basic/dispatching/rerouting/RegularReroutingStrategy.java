/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.rerouting;

import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The regular/default {@link ReroutingStrategy}.
 * <p>
 * Reroutes a {@link Vehicle} from its future or current position according to
 * {@link VehiclePositionResolver#getFutureOrCurrentPosition(org.opentcs.data.model.Vehicle)}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RegularReroutingStrategy
    extends AbstractReroutingStrategy
    implements ReroutingStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(RegularReroutingStrategy.class);
  private final VehiclePositionResolver vehiclePositionResolver;

  @Inject
  public RegularReroutingStrategy(Router router,
                                  TCSObjectService objectService,
                                  RegularDriveOrderMerger driveOrderMerger,
                                  VehiclePositionResolver vehiclePositionResolver) {
    super(router, objectService, driveOrderMerger);
    this.vehiclePositionResolver = requireNonNull(vehiclePositionResolver,
                                                  "vehiclePositionResolver");
  }

  @Override
  protected Optional<Point> determineRerouteSource(Vehicle vehicle) {
    return Optional.of(vehiclePositionResolver.getFutureOrCurrentPosition(vehicle));
  }
}
