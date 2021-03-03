/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.util.gui.plugins.PanelFactory;

/**
 * A registry for all plugin panel factories.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PanelRegistry {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(PanelRegistry.class.getName());
  /**
   * The registered factories.
   */
  private final List<PanelFactory> factories = new LinkedList<>();

  /**
   * Creates a new instance.
   *
   * @param kernelProvider The shared kernel provider to be used.
   */
  @Inject
  public PanelRegistry(SharedKernelProvider kernelProvider) {
    requireNonNull(kernelProvider, "kernelProvider");

    // Auto-detect generic client panel factories.
    for (PanelFactory factory : ServiceLoader.load(PanelFactory.class)) {
      factory.setKernelProvider(kernelProvider);
      factories.add(factory);
      log.info("Found plugin panel factory: " + factory.getClass().getName());
    }
    if (factories.isEmpty()) {
      log.info("No plugin panel factories found.");
    }
  }

  /**
   * Returns the registered factories.
   *
   * @return The registered factories.
   */
  public List<PanelFactory> getFactories() {
    return new LinkedList<>(factories);
  }
}
