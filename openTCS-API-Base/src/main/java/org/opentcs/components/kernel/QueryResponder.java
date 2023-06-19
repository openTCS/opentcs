/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
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
