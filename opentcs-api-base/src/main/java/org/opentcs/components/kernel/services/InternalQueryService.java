// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.services;

import jakarta.annotation.Nonnull;
import org.opentcs.components.kernel.Query;
import org.opentcs.components.kernel.QueryResponder;

/**
 * Declares query-related methods not accessible to remote peers.
 */
public interface InternalQueryService
    extends
      QueryService {

  /**
   * Registers the given responder for handling queries of the given type.
   *
   * @param clazz The query type.
   * @param responder The responder to handle the queries.
   */
  void registerResponder(
      @Nonnull
      Class<? extends Query<?>> clazz,
      @Nonnull
      QueryResponder responder
  );

  /**
   * Unregisters the responder for the given type.
   *
   * @param clazz The query type.
   */
  void unregisterResponder(
      @Nonnull
      Class<? extends Query<?>> clazz
  );
}
