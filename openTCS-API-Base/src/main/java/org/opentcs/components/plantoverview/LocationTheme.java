/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.plantoverview;

import java.awt.Image;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides a location theme.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface LocationTheme {

  /**
   * Returns the image for the given location representation.
   *
   * @param representation The representation for which to return the image.
   * @return The image for the given location representation.
   */
  @Nonnull
  Image getImageFor(@Nonnull LocationRepresentation representation);

  /**
   * Returns the image for the given location (type).
   *
   * @param location The location to base the image on.
   * @param locationType The location type for the location.
   * @return The image for the give location.
   */
  @Nonnull
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default Image getImageFor(@Nonnull Location location, @Nonnull LocationType locationType) {
    return getImageFor(LocationRepresentation.NONE);
  }
}
