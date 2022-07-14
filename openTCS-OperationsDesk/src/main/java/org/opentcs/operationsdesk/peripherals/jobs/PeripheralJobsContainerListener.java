/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.peripherals.jobs;

import java.util.Collection;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Listens for changes to the {@link PeripheralJobsContainer}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PeripheralJobsContainerListener {

  /**
   * Notifies the listener that the container has been initialized.
   *
   * @param jobs The jobs the container has been initialized with.
   */
  void containerInitialized(Collection<PeripheralJob> jobs);

  /**
   * Notifies the listener that a job has been added.
   *
   * @param job The job that has been added.
   */
  void peripheralJobAdded(PeripheralJob job);

  /**
   * Notifies the listener that a job has been updated.
   *
   * @param job The job that has been updated.
   */
  void peripheralJobUpdated(PeripheralJob job);

  /**
   * Notifies the listener that a job has been removed.
   *
   * @param job The job that has been removed.
   */
  void peripheralJobRemoved(PeripheralJob job);
}
