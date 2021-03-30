/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import org.opentcs.components.kernel.Query;

/**
 * Provides generic/pluggable query functionality.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface QueryService {

  /**
   * Executes a query with the kernel and delivers the result.
   *
   * @param <T> The query/result type.
   * @param query The query/parameter object.
   * @return The query result.
   */
  <T> T query(Query<T> query);
}
