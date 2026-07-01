// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import jakarta.annotation.Nonnull;
import java.util.Set;
import org.locationtech.jts.geom.GeometryCollection;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.TCSResource;
import org.opentcs.strategies.basic.util.MultiPlaneGeometryCollection;

/**
 * Provides areas related to resources.
 */
public interface AreaProvider
    extends
      Lifecycle {

  /**
   * Provides the areas related to the given envelope key and the given set of resources as a
   * {@link GeometryCollection}.
   *
   * @param envelopeKey The envelope key.
   * @param resources The set of resources.
   * @return The areas related to the given envelope key and the given set of resources.
   */
  MultiPlaneGeometryCollection getAreas(
      @Nonnull
      String envelopeKey,
      @Nonnull
      Set<TCSResource<?>> resources
  );
}
