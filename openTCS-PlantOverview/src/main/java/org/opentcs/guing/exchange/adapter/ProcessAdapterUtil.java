/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * A utility class providing process adapters.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ProcessAdapterUtil {

  private final BlockAdapter blockAdapter;
  private final GroupAdapter groupAdapter;
  private final LayoutAdapter layoutAdapter;
  private final LinkAdapter linkAdapter;
  private final LocationAdapter locationAdapter;
  private final LocationTypeAdapter locationTypeAdapter;
  private final PathAdapter pathAdapter;
  private final PointAdapter pointAdapter;
  private final StaticRouteAdapter staticRouteAdapter;
  private final VehicleAdapter vehicleAdapter;

  @Inject
  public ProcessAdapterUtil(BlockAdapter blockAdapter,
                            GroupAdapter groupAdapter,
                            LayoutAdapter layoutAdapter,
                            LinkAdapter linkAdapter,
                            LocationAdapter locationAdapter,
                            LocationTypeAdapter locationTypeAdapter,
                            PathAdapter pathAdapter,
                            PointAdapter pointAdapter,
                            StaticRouteAdapter staticRouteAdapter,
                            VehicleAdapter vehicleAdapter) {
    this.blockAdapter = requireNonNull(blockAdapter, "blockAdapter");
    this.groupAdapter = requireNonNull(groupAdapter, "groupAdapter");
    this.layoutAdapter = requireNonNull(layoutAdapter, "layoutAdapter");
    this.linkAdapter = requireNonNull(linkAdapter, "linkAdapter");
    this.locationAdapter = requireNonNull(locationAdapter, "locationAdapter");
    this.locationTypeAdapter = requireNonNull(locationTypeAdapter, "locationTypeAdapter");
    this.pathAdapter = requireNonNull(pathAdapter, "pathAdapter");
    this.pointAdapter = requireNonNull(pointAdapter, "pointAdapter");
    this.staticRouteAdapter = requireNonNull(staticRouteAdapter, "staticRouteAdapter");
    this.vehicleAdapter = requireNonNull(vehicleAdapter, "vehicleAdapter");
  }

  public ProcessAdapter processAdapterFor(ModelComponent model) {

    if (model instanceof PointModel) {
      return pointAdapter;
    }
    else if (model instanceof PathModel) {
      return pathAdapter;
    }
    else if (model instanceof LocationTypeModel) {
      return locationTypeAdapter;
    }
    else if (model instanceof LocationModel) {
      return locationAdapter;
    }
    else if (model instanceof StaticRouteModel) {
      return staticRouteAdapter;
    }
    else if (model instanceof BlockModel) {
      return blockAdapter;
    }
    else if (model instanceof GroupModel) {
      return groupAdapter;
    }
    else if (model instanceof VehicleModel) {
      return vehicleAdapter;
    }
    else if (model instanceof LinkModel) {
      return linkAdapter;
    }
    else if (model instanceof LayoutModel) {
      return layoutAdapter;
    }
    else {
      // Just in case the set of model classes ever changes.
      throw new IllegalArgumentException("Unhandled model class: " + model.getClass());
    }
  }
}
