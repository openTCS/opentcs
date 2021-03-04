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
import org.opentcs.guing.exchange.EventDispatcher;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for handling of process adapters.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ProcessAdapterUtil {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ProcessAdapterUtil.class);
  /**
   * A factory for process adapters.
   */
  private final ProcessAdapterFactory procAdapterFactory;

  /**
   * Creates a new instance.
   *
   * @param procAdapterFactory A factory for process adapters.
   */
  @Inject
  public ProcessAdapterUtil(ProcessAdapterFactory procAdapterFactory) {
    this.procAdapterFactory = requireNonNull(procAdapterFactory,
                                             "procAdapterFactory");
  }

  /**
   * Creates a new process adapter for the given model component and registers
   * it with the given event dispatcher.
   *
   * @param model The model component.
   * @param dispatcher The event dispatcher.
   */
  public void createProcessAdapter(ModelComponent model,
                                   EventDispatcher dispatcher) {
    requireNonNull(model, "model");
    requireNonNull(dispatcher, "dispatcher");

    if (dispatcher.findProcessAdapter(model) != null) {
      LOG.warn("There is already a process adapter for model {}", model.getName());
    }

    ProcessAdapter adapter = basicCreateProcessAdapter(model, dispatcher);
    dispatcher.addProcessAdapter(adapter);
  }

  /**
   * Removes the process adapter of the given model component.
   *
   * @param model The model component.
   * @param dispatcher The event dispatcher.
   */
  public void removeProcessAdapter(ModelComponent model,
                                   EventDispatcher dispatcher) {
    requireNonNull(model, "model");
    requireNonNull(dispatcher, "dispatcher");

    ProcessAdapter adapter = dispatcher.findProcessAdapter(model);

    if (adapter != null) {
      dispatcher.removeProcessAdapter(adapter);
    }
  }

  private ProcessAdapter basicCreateProcessAdapter(
      ModelComponent model, EventDispatcher dispatcher) {

    if (model instanceof PointModel) {
      return procAdapterFactory.createPointAdapter((PointModel) model,
                                                   dispatcher);
    }
    else if (model instanceof PathModel) {
      return procAdapterFactory.createPathAdapter((PathModel) model,
                                                  dispatcher);
    }
    else if (model instanceof LocationTypeModel) {
      return procAdapterFactory.createLocTypeAdapter((LocationTypeModel) model,
                                                     dispatcher);
    }
    else if (model instanceof LocationModel) {
      return procAdapterFactory.createLocationAdapter((LocationModel) model,
                                                      dispatcher);
    }
    else if (model instanceof StaticRouteModel) {
      return procAdapterFactory.createStaticRouteAdapter((StaticRouteModel) model,
                                                         dispatcher);
    }
    else if (model instanceof BlockModel) {
      return procAdapterFactory.createBlockAdapter((BlockModel) model,
                                                   dispatcher);
    }
    else if (model instanceof GroupModel) {
      return procAdapterFactory.createGroupAdapter((GroupModel) model,
                                                   dispatcher);
    }
    else if (model instanceof VehicleModel) {
      return procAdapterFactory.createVehicleAdapter((VehicleModel) model,
                                                     dispatcher);
    }
    else if (model instanceof LinkModel) {
      return procAdapterFactory.createLinkAdapter((LinkModel) model,
                                                  dispatcher);
    }
    else if (model instanceof LayoutModel) {
      return procAdapterFactory.createLayoutAdapter((LayoutModel) model,
                                                    dispatcher);
    }
    else {
      // Just in case the set of model classes ever changes.
      throw new IllegalArgumentException("Unhandled model class: " + model.getClass());
    }
  }
}
