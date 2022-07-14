/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.model;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.model.StandardSystemModel;
import org.opentcs.guing.common.util.ModelComponentFactory;

/**
 * Extends the standard system model with a cache of model components
 * to provide a more efficient lookup of components by name.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class CachedSystemModel
    extends StandardSystemModel {

  /**
   * A map from component name to the actual component.
   */
  private final Map<String, ModelComponent> components = new HashMap<>();

  @Inject
  public CachedSystemModel(ModelComponentFactory modelComponentFactory) {
    super(modelComponentFactory);
  }

  @Override
  public void onRestorationComplete() {
    components.clear();
    for (ModelComponent component : getAll()) {
      components.put(component.getName(), component);
    }
  }

  @Override
  public BlockModel getBlockModel(String name) {
    if (components.isEmpty()) {
      return super.getBlockModel(name);
    }

    ModelComponent block = components.get(name);
    if (block instanceof BlockModel) {
      return (BlockModel) block;
    }
    return null;
  }

  @Override
  public LocationModel getLocationModel(String name) {
    if (components.isEmpty()) {
      return super.getLocationModel(name);
    }

    ModelComponent location = components.get(name);
    if (location instanceof LocationModel) {
      return (LocationModel) location;
    }
    return null;
  }

  @Override
  public LocationTypeModel getLocationTypeModel(String name) {
    if (components.isEmpty()) {
      return super.getLocationTypeModel(name);
    }

    ModelComponent locationType = components.get(name);
    if (locationType instanceof LocationTypeModel) {
      return (LocationTypeModel) locationType;
    }
    return null;
  }

  @Override
  public ModelComponent getModelComponent(String name) {
    if (components.isEmpty()) {
      return super.getModelComponent(name);
    }
    return components.get(name);
  }

  @Override
  public PathModel getPathModel(String name) {
    if (components.isEmpty()) {
      return super.getPathModel(name);
    }

    ModelComponent path = components.get(name);
    if (path instanceof PathModel) {
      return (PathModel) path;
    }
    return null;
  }

  @Override
  public PointModel getPointModel(String name) {
    if (components.isEmpty()) {
      return super.getPointModel(name);
    }

    ModelComponent point = components.get(name);
    if (point instanceof PointModel) {
      return (PointModel) point;
    }
    return null;
  }

  @Override
  public VehicleModel getVehicleModel(String name) {
    if (components.isEmpty()) {
      return super.getVehicleModel(name);
    }

    ModelComponent vehicle = components.get(name);
    if (vehicle instanceof VehicleModel) {
      return (VehicleModel) vehicle;
    }
    return null;
  }

}
