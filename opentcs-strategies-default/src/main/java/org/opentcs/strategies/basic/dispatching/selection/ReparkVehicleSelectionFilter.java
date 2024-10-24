// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection;

import java.util.Collection;
import java.util.function.Function;
import org.opentcs.data.model.Vehicle;

/**
 * A filter for selecting {@link Vehicle}s for reparking.
 * Returns a collection of reasons for filtering the vehicle.
 * If the returned collection is empty, no reason to filter it was encountered.
 */
public interface ReparkVehicleSelectionFilter
    extends
      Function<Vehicle, Collection<String>> {
}
