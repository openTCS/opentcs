// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.routing;

import java.util.function.Function;
import org.opentcs.data.model.Vehicle;

/**
 * Determines the routing group for a {@link Vehicle} instance.
 */
public interface GroupMapper
    extends
      Function<Vehicle, String> {
}
