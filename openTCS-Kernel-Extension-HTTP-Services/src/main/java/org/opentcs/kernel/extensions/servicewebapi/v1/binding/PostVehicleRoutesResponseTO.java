/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getvehicleroutes.RouteTO;

/**
 *
 */
public class PostVehicleRoutesResponseTO {

  private List<RouteTO> routes = List.of();

  public PostVehicleRoutesResponseTO() {

  }

  @Nonnull
  public List<RouteTO> getRoutes() {
    return routes;
  }

  public PostVehicleRoutesResponseTO setRoutes(@Nonnull List<RouteTO> routes) {
    this.routes = requireNonNull(routes, "routes");
    return this;
  }

  public static PostVehicleRoutesResponseTO fromMap(
      Map<TCSObjectReference<Point>, Route> routeMap) {
    return new PostVehicleRoutesResponseTO()
        .setRoutes(
            routeMap.entrySet().stream()
                .map(PostVehicleRoutesResponseTO::toRouteTO)
                .collect(Collectors.toList())
        );
  }

  private static RouteTO toRouteTO(Map.Entry<TCSObjectReference<Point>, Route> entry) {
    if (entry.getValue() == null) {
      return new RouteTO()
          .setDestinationPoint(entry.getKey().getName())
          .setCosts(-1)
          .setSteps(null);
    }

    return new RouteTO()
        .setDestinationPoint(entry.getKey().getName())
        .setCosts(entry.getValue().getCosts())
        .setSteps(toSteps(entry.getValue().getSteps()));
  }

  private static List<RouteTO.Step> toSteps(List<Step> steps) {
    return steps.stream()
        .map(
            step -> new RouteTO.Step()
                .setDestinationPoint(step.getDestinationPoint().getName())
                .setSourcePoint(
                    (step.getSourcePoint() != null) ? step.getSourcePoint().getName() : null
                )
                .setPath((step.getPath() != null) ? step.getPath().getName() : null)
                .setVehicleOrientation(step.getVehicleOrientation().name())
        )
        .collect(Collectors.toList());
  }
}
