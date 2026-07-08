// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.Route;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.RouteTO;
import org.opentcs.util.Comparators;

/**
 * Provides methods for converting routes
 */
public class RouteConverter {

  public RouteConverter() {
  }

  /**
   * Converts the given map of routes to their web API representation.
   *
   * @param routeMap The routes to convert.
   * @return The converted routes.
   */
  public List<RouteTO> convert(Map<TCSObjectReference<Point>, Set<Route>> routeMap) {
    return routeMap.entrySet().stream()
        .sorted((e1, e2) -> Comparators.referencesByName().compare(e1.getKey(), e2.getKey()))
        .flatMap(entry -> toRouteTOs(entry).stream())
        .toList();
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

  private static List<RouteTO.Step> toSteps(List<Route.Step> steps) {
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
