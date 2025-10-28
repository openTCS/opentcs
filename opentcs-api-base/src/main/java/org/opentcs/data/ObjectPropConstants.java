// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data;

import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;

/**
 * Defines some reserved/commonly used property keys and values.
 */
public interface ObjectPropConstants {

  /**
   * A property key for models used to store the last-modified time stamp.
   * <p>
   * Type: A time stamp, encoded using ISO 8601. (Can be parsed using {@code java.time.Instant}.)
   * </p>
   */
  String MODEL_FILE_LAST_MODIFIED = "tcs:modelFileLastModified";
  /**
   * A property key for {@link TransportOrder} instances used to define resources (i.e., points,
   * paths or locations) that should be avoided by vehicles processing transport orders with such a
   * property.
   * <p>
   * Type: String (a comma-separated list of resource names)
   * </p>
   */
  String TRANSPORT_ORDER_RESOURCES_TO_AVOID = "tcs:resourcesToAvoid";
  /**
   * A property key for {@link Vehicle} instances used to select the data transformer to be used.
   * <p>
   * Type: String (the name of a data transformer factory)
   * </p>
   */
  String VEHICLE_DATA_TRANSFORMER = "tcs:vehicleDataTransformer";
}
