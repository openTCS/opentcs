/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection;

import java.util.function.Predicate;
import org.opentcs.data.model.Vehicle;

/**
 * A predicate for selecting {@link Vehicle}s for recharge orders.
 * Returns {@code true} if the given {@link Vehicle} should be selected.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RechargeVehicleSelectionFilter
    extends Predicate<Vehicle> {
}
