/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange.adapter;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;

/**
 * A utility class providing process adapters.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ProcessAdapterUtil {

  private final BlockAdapter blockAdapter;
  private final LayoutAdapter layoutAdapter;
  private final LinkAdapter linkAdapter;
  private final LocationAdapter locationAdapter;
  private final LocationTypeAdapter locationTypeAdapter;
  private final PathAdapter pathAdapter;
  private final PointAdapter pointAdapter;
  private final VehicleAdapter vehicleAdapter;

  @Inject
  @SuppressWarnings("deprecation")
  public ProcessAdapterUtil(BlockAdapter blockAdapter,
                            LayoutAdapter layoutAdapter,
                            LinkAdapter linkAdapter,
                            LocationAdapter locationAdapter,
                            LocationTypeAdapter locationTypeAdapter,
                            PathAdapter pathAdapter,
                            PointAdapter pointAdapter,
                            VehicleAdapter vehicleAdapter) {
    this.blockAdapter = requireNonNull(blockAdapter, "blockAdapter");
    this.layoutAdapter = requireNonNull(layoutAdapter, "layoutAdapter");
    this.linkAdapter = requireNonNull(linkAdapter, "linkAdapter");
    this.locationAdapter = requireNonNull(locationAdapter, "locationAdapter");
    this.locationTypeAdapter = requireNonNull(locationTypeAdapter, "locationTypeAdapter");
    this.pathAdapter = requireNonNull(pathAdapter, "pathAdapter");
    this.pointAdapter = requireNonNull(pointAdapter, "pointAdapter");
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
    else if (model instanceof BlockModel) {
      return blockAdapter;
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
