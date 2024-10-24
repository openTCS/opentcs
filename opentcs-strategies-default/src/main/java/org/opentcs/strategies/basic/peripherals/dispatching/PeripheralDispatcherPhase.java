// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.peripherals.dispatching;

import org.opentcs.components.Lifecycle;

/**
 * Describes a reusable dispatching (sub-)task with a life cycle.
 */
public interface PeripheralDispatcherPhase
    extends
      Runnable,
      Lifecycle {
}
