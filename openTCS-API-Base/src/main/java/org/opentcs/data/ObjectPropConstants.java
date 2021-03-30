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

/**
 * Defines some reserved/commonly used property keys and values.
 *
 * @author Stefan Walter (Fraunhofer IML)
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
}
