/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import java.util.function.Predicate;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Implementations of this interface check whether a peripheral job may be removed.
 */
public interface PeripheralJobCleanupApproval
    extends Predicate<PeripheralJob> {

}
