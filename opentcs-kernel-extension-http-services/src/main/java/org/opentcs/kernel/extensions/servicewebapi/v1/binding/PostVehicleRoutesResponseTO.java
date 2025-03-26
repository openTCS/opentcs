// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

  public PostVehicleRoutesResponseTO setRoutes(
      @Nonnull
      List<RouteTO> routes
  ) {
    this.routes = requireNonNull(routes, "routes");
    return this;
  }

  public static PostVehicleRoutesResponseTO fromMap(
      Map<TCSObjectReference<Point>, Set<Route>> routeMap
  ) {
    return new PostVehicleRoutesResponseTO()
        .setRoutes(
            routeMap.entrySet().stream()
                .flatMap(entry -> toRouteTOs(entry).stream())
                .collect(Collectors.toList())
        );
  }

  private static List<RouteTO> toRouteTOs(Map.Entry<TCSObjectReference<Point>, Set<Route>> entry) {
    if (entry.getValue().isEmpty()) {
      return List.of(
          new RouteTO()
              .setDestinationPoint(entry.getKey().getName())
              .setCosts(-1)
              .setSteps(null)
      );
    }

    return entry.getValue().stream().map(
        route -> new RouteTO()
            .setDestinationPoint(entry.getKey().getName())
            .setCosts(route.getCosts())
            .setSteps(toSteps(route.getSteps()))
    ).toList();
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
