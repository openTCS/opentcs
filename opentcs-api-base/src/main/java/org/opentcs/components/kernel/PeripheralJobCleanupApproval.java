// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel;

import java.util.function.Predicate;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Implementations of this interface check whether a peripheral job may be removed.
 */
public interface PeripheralJobCleanupApproval
    extends
      Predicate<PeripheralJob> {

}
