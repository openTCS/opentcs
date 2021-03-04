/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapterUtil;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts {@link SystemModel} instances to plant model data.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelExportAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ModelKernelPersistor.class);
  /**
   * Creates process adapter instances.
   */
  private final ProcessAdapterUtil processAdapterUtil;

  /**
   * Creates a new instance.
   *
   * @param processAdapterUtil Creates process adapter instances.
   */
  @Inject
  public ModelExportAdapter(ProcessAdapterUtil processAdapterUtil) {
    this.processAdapterUtil = requireNonNull(processAdapterUtil, "processAdapterUtil");
  }

  /**
   * Converts the given {@link SystemModel} instance to plant model data.
   *
   * @param systemModel The model to be converted.
   * @return The converted model data.
   * @throws IllegalArgumentException If the given plant model was inconsistent in some way.
   */
  @Nonnull
  public PlantModelCreationTO convert(SystemModel systemModel)
      throws IllegalArgumentException {
    requireNonNull(systemModel, "model");

    PlantModelCreationTO plantModel = new PlantModelCreationTO(systemModel.getName())
        .withProperties(convertProperties(systemModel.getPropertyMiscellaneous()));

    long timeBefore = System.currentTimeMillis();
    for (LayoutModel model : systemModel.getLayoutModels()) {
      plantModel = persist(model, systemModel, plantModel);
    }
    LOG.debug("Converting LayoutModels took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (PointModel model : systemModel.getPointModels()) {
      plantModel = persist(model, systemModel, plantModel);
    }
    LOG.debug("Converting PointModels took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (PathModel model : systemModel.getPathModels()) {
      plantModel = persist(model, systemModel, plantModel);
    }
    LOG.debug("Converting PathModels took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (LocationTypeModel model : systemModel.getLocationTypeModels()) {
      plantModel = persist(model, systemModel, plantModel);
    }
    LOG.debug("Converting LocationTypeModels took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (LocationModel model : systemModel.getLocationModels()) {
      plantModel = persist(model, systemModel, plantModel);
    }
    LOG.debug("Converting LocationModels took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (LinkModel model : systemModel.getLinkModels()) {
      plantModel = persist(model, systemModel, plantModel);
    }
    LOG.debug("Converting LinkModels took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (BlockModel model : systemModel.getBlockModels()) {
      plantModel = persist(model, systemModel, plantModel);
    }
    LOG.debug("Converting BlockModels took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (GroupModel model : systemModel.getGroupModels()) {
      plantModel = persist(model, systemModel, plantModel);
    }
    LOG.debug("Converting GroupModels took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (StaticRouteModel model : systemModel.getStaticRouteModels()) {
      plantModel = persist(model, systemModel, plantModel);
    }
    LOG.debug("Converting StaticRouteModels took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (VehicleModel model : systemModel.getVehicleModels()) {
      plantModel = persist(model, systemModel, plantModel);
    }
    LOG.debug("Converting VehicleModels took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    return plantModel;
  }

  private Map<String, String> convertProperties(KeyValueSetProperty kvsp) {
    return kvsp.getItems().stream()
        .collect(Collectors.toMap(KeyValueProperty::getKey, KeyValueProperty::getValue));
  }

  private PlantModelCreationTO persist(ModelComponent component,
                                       SystemModel systemModel,
                                       PlantModelCreationTO plantModel) {
    ProcessAdapter adapter = processAdapterUtil.processAdapterFor(component);
    return adapter.storeToPlantModel(component, systemModel, plantModel);
  }
}
