// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.model;

import java.io.Serializable;
import org.opentcs.data.TCSObjectReference;

/**
 * A transient reference to a {@link TCSResource}.
 *
 * @param <E> The actual resource class.
 */
public class TCSResourceReference<E extends TCSResource<E>>
    extends
      TCSObjectReference<E>
    implements
      Serializable {

  /**
   * Creates a new TCSResourceReference.
   *
   * @param newReferent The resource this reference references.
   */
  protected TCSResourceReference(TCSResource<E> newReferent) {
    super(newReferent);
  }
}
