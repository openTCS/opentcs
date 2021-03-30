/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import javax.annotation.Nonnull;
import org.opentcs.components.kernel.Query;
import org.opentcs.components.kernel.QueryResponder;

/**
 * Declares query-related methods not accessible to remote peers.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface InternalQueryService
    extends QueryService {

  /**
   * Registers the given responder for handling queries of the given type.
   *
   * @param clazz The query type.
   * @param responder The responder to handle the queries.
   */
  void registerResponder(@Nonnull Class<? extends Query<?>> clazz,
                         @Nonnull QueryResponder responder);

  /**
   * Unregisters the responder for the given type.
   *
   * @param clazz The query type.
   */
  void unregisterResponder(@Nonnull Class<? extends Query<?>> clazz);
}
