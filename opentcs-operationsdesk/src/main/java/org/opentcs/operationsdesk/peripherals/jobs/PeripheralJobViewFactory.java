// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
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
