/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import java.util.Set;
import javax.annotation.Nonnull;
import org.locationtech.jts.geom.GeometryCollection;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.TCSResource;

/**
 * Provides areas related to resources.
 */
public interface AreaProvider
    extends Lifecycle {

  /**
   * Provides the areas related to the given envelope key and the given set of resources as a
   * {@link GeometryCollection}.
   *
   * @param envelopeKey The envelope key.
   * @param resources The set of resources.
   * @return The areas related to the given envelope key and the given set of resources.
   */
  GeometryCollection getAreas(@Nonnull String envelopeKey, @Nonnull Set<TCSResource<?>> resources);
}
