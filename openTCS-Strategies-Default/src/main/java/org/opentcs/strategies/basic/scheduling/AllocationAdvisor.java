/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.data.model.TCSResource;

/**
 * A module implementation that forwards method calls to all submodules.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AllocationAdvisor
    implements Scheduler.Module {

  /**
   * The submodules.
   */
  private final Set<Scheduler.Module> modules;
  /**
   * This instance's initialized flag.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param modules The submodules.
   */
  @Inject
  public AllocationAdvisor(Set<Scheduler.Module> modules) {
    this.modules = requireNonNull(modules, "modules");
  }

  @Override
  public void initialize() {
    if (initialized) {
      return;
    }

    for (Scheduler.Module module : modules) {
      module.initialize();
    }
  }

  @Override
  public void terminate() {
    if (!initialized) {
      return;
    }

    for (Scheduler.Module module : modules) {
      module.terminate();
    }
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void claim(Scheduler.Client client, List<Set<TCSResource<?>>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    for (Scheduler.Module module : modules) {
      module.claim(client, resources);
    }
  }

  @Override
  public void unclaim(Scheduler.Client client) {
    requireNonNull(client, "client");

    for (Scheduler.Module module : modules) {
      module.unclaim(client);
    }
  }

  @Override
  public void setAllocationState(@Nonnull Scheduler.Client client,
                                 @Nonnull Set<TCSResource<?>> alloc,
                                 @Nonnull List<Set<TCSResource<?>>> remainingClaim) {
    requireNonNull(client, "client");
    requireNonNull(alloc, "alloc");
    requireNonNull(remainingClaim, "remainingClaim");

    for (Scheduler.Module module : modules) {
      module.setAllocationState(client, alloc, remainingClaim);
    }
  }

  @Override
  public boolean mayAllocate(Scheduler.Client client, Set<TCSResource<?>> resources) {
    boolean result = true;
    for (Scheduler.Module module : modules) {
      result = result && module.mayAllocate(client, resources);
    }
    return result;
  }
}
