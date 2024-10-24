// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel;

/**
 * A responder for generic queries.
 */
public interface QueryResponder {

  /**
   * Executes the specified query.
   *
   * @param <T> The query/result type.
   * @param query The query/parameter object.
   * @return The query result.
   */
  <T> T query(Query<T> query);
}
