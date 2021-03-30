/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.routing;

import java.util.function.Function;
import org.opentcs.data.model.Vehicle;

/**
 * Determines the routing group for a {@link Vehicle} instance.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface GroupMapper
    extends Function<Vehicle, String> {
}
