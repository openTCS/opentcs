// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Set;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.Scheduler.Client;
import org.opentcs.data.model.TCSResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A module implementation that forwards method calls to all submodules.
 */
public class AllocationAdvisor
    implements
      Scheduler.Module {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AllocationAdvisor.class);
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
    if (isInitialized()) {
      LOG.debug("Already initialized, doing nothing.");
      return;
    }

    for (Scheduler.Module module : modules) {
      module.initialize();
    }

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      LOG.debug("Not initialized, doing nothing.");
      return;
    }

    for (Scheduler.Module module : modules) {
      module.terminate();
    }

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void setAllocationState(
      @Nonnull
      Scheduler.Client client,
      @Nonnull
      Set<TCSResource<?>> alloc,
      @Nonnull
      List<Set<TCSResource<?>>> remainingClaim
  ) {
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

  @Override
  public void prepareAllocation(
      Scheduler.Client client,
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    for (Scheduler.Module module : modules) {
      LOG.debug(
          "Module {}: Preparing allocation for resources {} for client {}.",
          module,
          resources,
          client
      );
      module.prepareAllocation(client, resources);
    }
  }

  @Override
  public boolean hasPreparedAllocation(
      Scheduler.Client client,
      Set<TCSResource<?>> resources
  ) {
    boolean result = true;
    for (Scheduler.Module module : modules) {
      result = result && module.hasPreparedAllocation(client, resources);
    }
    return result;
  }

  @Override
  public void allocationReleased(Client client, Set<TCSResource<?>> resources) {
    requireNonNull(resources, "resources");

    for (Scheduler.Module module : modules) {
      LOG.debug(
          "Module {}: Allocation released for resources {} for client {}.",
          module,
          resources,
          client
      );
      module.allocationReleased(client, resources);
    }
  }
}
