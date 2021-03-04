/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.configuration;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.reload.ReloadStrategy;
import org.cfg4j.source.reload.Reloadable;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.CyclicTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reload strategy for reloading {@link ConfigurationSource}s periodically.
 * Once a {@link ConfigurationSource} is registered to this strategy, a task is started reloading
 * all registered {@link ConfigurationSource}s periodically at the specified interval.
 * Once there are no more {@link ConfigurationSource}s registered, the task is terminated.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeriodicalReloadStrategy
    implements ReloadStrategy {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeriodicalReloadStrategy.class);
  /**
   * The resources to reload.
   */
  private final List<Reloadable> resources = new ArrayList<>();
  /**
   * The task that's actually reloading the resources.
   */
  private final ReloadTask reloadTask;
  /**
   * The thread the {@link ReloadTask} runs in.
   */
  private Thread reloadThread;

  /**
   * Creates a new instance.
   *
   * @param duration The time (in ms) to wait between the reloads.
   */
  public PeriodicalReloadStrategy(long duration) {
    checkArgument(duration >= 0, "duration is %s, has to be => 0", duration);
    reloadTask = new ReloadTask(duration);
  }

  @Override
  public void register(Reloadable resource) {
    requireNonNull(resource, "resource");
    boolean emptyBefore = resources.isEmpty();

    if (resources.contains(resource)) {
      LOG.debug("Resource {} already registered.", resource);
      return;
    }

    LOG.debug("Registering resource {}.", resource);
    resources.add(resource);

    if (emptyBefore) {
      reloadThread = new Thread(reloadTask, getClass().getSimpleName() + "-reloadThread");
      reloadThread.setDaemon(true);
      reloadThread.start();
    }
  }

  @Override
  public void deregister(Reloadable resource) {
    if (resources.remove(resource)) {
      LOG.debug("Deregistered resource {}.", resource);
    }

    if (resources.isEmpty()) {
      reloadTask.terminateAndWait();
    }
  }

  /**
   * The task that's actually reloading the resources.
   */
  private class ReloadTask
      extends CyclicTask {

    public ReloadTask(long tSleep) {
      super(tSleep);
    }

    @Override
    protected void runActualTask() {
      for (Reloadable resource : resources) {
        resource.reload();
      }
    }
  }
}
