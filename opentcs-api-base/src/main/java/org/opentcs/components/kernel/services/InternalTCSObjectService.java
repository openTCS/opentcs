// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.services;

import java.util.stream.Stream;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.TCSObject;

/**
 * Declares {@link org.opentcs.components.kernel.services.TCSObjectService} methods only accessible
 * inside the kernel application.
 */
public interface InternalTCSObjectService
    extends
      TCSObjectService {

  /**
   * Returns a stream of all {@link TCSObject}s of the given class.
   * <p>
   * Note:
   * </p>
   * <ul>
   * <li>This method should only be called from a kernel executor task, and the returned stream
   * should be consumed only within that very same task. Continuing to use the stream outside the
   * scope of the task in which this method was called may lead to undefined behaviour.</li>
   * <li>Service methods modifying kernel objects should not be called while processing the returned
   * stream. Doing so may also lead to undefined behaviour.</li>
   * </ul>
   *
   * @param <T> The TCSObjects' actual type.
   * @param clazz The class of the objects to be returned.
   * @return Copies of all existing objects of the given class.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  <T extends TCSObject<T>> Stream<T> stream(Class<T> clazz)
      throws KernelRuntimeException;
}
