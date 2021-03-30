/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching;

import org.opentcs.components.Lifecycle;

/**
 * Describes a reusable dispatching (sub-)task with a life cycle.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PeripheralDispatcherPhase
    extends Runnable,
            Lifecycle {
}
