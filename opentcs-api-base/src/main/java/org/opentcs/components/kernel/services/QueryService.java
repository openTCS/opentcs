// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.services;

import org.opentcs.components.kernel.Query;

/**
 * Provides generic/pluggable query functionality.
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
