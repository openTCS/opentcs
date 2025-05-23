// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.Map;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;

/**
 * Describes a resource that {@link Vehicle}s may claim for exclusive usage.
 *
 * @see Scheduler
 * @param <E> The actual resource class.
 */
public abstract class TCSResource<E extends TCSResource<E>>
    extends
      TCSObject<E>
    implements
      Serializable {

  /**
   * Creates a new TCSResource.
   *
   * @param name The new resource's name.
   */
  @SuppressWarnings("this-escape")
  protected TCSResource(String name) {
    super(name);
    reference = new TCSResourceReference<>(this);
  }

  /**
   * Creates a new TCSResource.
   *
   * @param name The new resource's name.
   * @param properties A set of properties (key-value pairs) associated with this object.
   * @param history A history of events related to this object.
   */
  @SuppressWarnings("this-escape")
  protected TCSResource(String name, Map<String, String> properties, ObjectHistory history) {
    super(name, properties, history);
    reference = new TCSResourceReference<>(this);
  }

  // Methods inherited from TCSObject<E> start here.
  @Override
  public TCSResourceReference<E> getReference() {
    return (TCSResourceReference<E>) reference;
  }
}
