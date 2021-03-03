/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import java.io.IOException;
import static java.util.Objects.requireNonNull;
import org.opentcs.access.Kernel;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.model.ModelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronizes data kept in <code>ModelComponents</code> to the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelKernelPersistor
    implements ModelPersistor {

  /**
   * This class's logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(ModelKernelPersistor.class);

  /**
   * The event dispatcher providing the process adapters.
   */
  private final EventDispatcher eventDispatcher;
  /**
   * The kernel.
   */
  private final Kernel kernel;
  /**
   * The model name.
   */
  private final String modelName;

  /**
   * Creates a new instance.
   *
   * @param eventDispatcher The event dispatcher providing the process adapters.
   * @param kernel The kernel.
   * @param modelName The model name.
   */
  public ModelKernelPersistor(EventDispatcher eventDispatcher,
                              Kernel kernel,
                              String modelName) {
    this.eventDispatcher = requireNonNull(eventDispatcher, "eventDispatcher");
    this.kernel = requireNonNull(kernel, "kernel");
    this.modelName = requireNonNull(modelName, "modelName");
  }

  @Override
  public void init() {
    // Start with a clean, empty model.
    kernel.createModel(modelName);
  }

  @Override
  public void persist(ModelComponent component) {
    requireNonNull(component, "model component");

    ProcessAdapter adapter = eventDispatcher.findProcessAdapter(component);
    if (adapter != null) {
      adapter.updateProcessProperties(kernel);
    }
    else {
      log.warn("No process adapter for model component " + component.getName()
          + " was found.");
    }
  }

  @Override
  public void close()
      throws IOException {
    // Let the kernel persist the model under the given name.
    kernel.saveModel(modelName);
  }
}
