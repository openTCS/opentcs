/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.peripherals.jobs;

import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Creates peripheral job-related GUI elements.
 */
public interface PeripheralJobViewFactory {

  /**
   * Creates a peripheral job details panel.
   *
   * @param job The job to create a panel for.
   * @return The panel.
   */
  PeripheralJobView createPeripheralJobView(PeripheralJob job);

}
