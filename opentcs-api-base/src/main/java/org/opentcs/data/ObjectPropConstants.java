/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.visualization.LocationRepresentation;
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
   * A property key for {@link LocationType} instances used to provide a hint for the visualization
   * how locations of the type should be visualized.
   * <p>
   * Type: String (any element of {@link LocationRepresentation})
   * </p>
   */
  String LOCTYPE_DEFAULT_REPRESENTATION = "tcs:defaultLocationTypeSymbol";
  /**
   * A property key for {@link Location} instances used to provide a hint for the visualization how
   * the locations should be visualized.
   * <p>
   * Type: String (any element of {@link LocationRepresentation})
   * </p>
   */
  String LOC_DEFAULT_REPRESENTATION = "tcs:defaultLocationSymbol";
  /**
   * A property key for {@link TransportOrder} instances used to define resources (i.e., points,
   * paths or locations) that should be avoided by vehicles processing transport orders with such a
   * property.
   * <p>
   * Type: String (a comma-separated list of resource names)
   * </p>
   */
  String TRANSPORT_ORDER_RESOURCES_TO_AVOID = "tcs:resourcesToAvoid";
}
