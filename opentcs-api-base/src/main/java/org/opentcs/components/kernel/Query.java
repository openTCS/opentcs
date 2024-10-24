// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel;

import java.io.Serializable;

/**
 * Marks a query (parameter) object.
 *
 * @param <T> The result type.
 */
public interface Query<T> {

  /**
   * A convenience class to be used as the result type for queries that do not return any result.
   */
  class Void
      implements
        Serializable {

    /**
     * Creates a new instance.
     */
    public Void() {
    }
  }
}
