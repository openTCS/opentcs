/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import java.io.Serializable;

/**
 * Marks a query (parameter) object.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <T> The result type.
 */
public interface Query<T> {

  /**
   * A convenience class to be used as the result type for queries that do not return any result.
   */
  public static class Void
      implements Serializable {

  }
}
