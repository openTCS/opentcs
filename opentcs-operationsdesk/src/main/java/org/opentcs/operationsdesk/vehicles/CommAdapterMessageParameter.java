// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.vehicles;

import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;

/**
 * Describes a single parameter of a {@link VehicleCommAdapterMessage}.
 */
public record CommAdapterMessageParameter(String key, String value) {
}
