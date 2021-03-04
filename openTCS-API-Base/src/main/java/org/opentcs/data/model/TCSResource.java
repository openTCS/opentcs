/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.Map;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.data.TCSObject;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Describes a resource that {@link Vehicle}s may claim for exclusive usage.
 *
 * @see Scheduler
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual resource class.
 */
@ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
public abstract class TCSResource<E extends TCSResource<E>>
    extends TCSObject<E>
    implements Serializable,
               Cloneable {

  /**
   * Creates a new TCSResource.
   *
   * @param objectID The new resource's object ID.
   * @param name The new resource's name.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  protected TCSResource(int objectID, String name) {
    super(objectID, name);
    reference = new TCSResourceReference<>(this);
  }

  /**
   * Creates a new TCSResource.
   *
   * @param name The new resource's name.
   */
  protected TCSResource(String name) {
    super(name);
    reference = new TCSResourceReference<>(this);
  }

  /**
   * Creates a new TCSResource.
   *
   * @param objectID The new object's ID.
   * @param name The new resource's name.
   * @param properties A set of properties (key-value pairs) associated with this object.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  protected TCSResource(int objectID, String name, Map<String, String> properties) {
    super(objectID, name, properties);
    reference = new TCSResourceReference<>(this);
  }

  /**
   * Creates a new TCSResource.
   *
   * @param name The new resource's name.
   * @param properties A set of properties (key-value pairs) associated with this object.
   */
  protected TCSResource(String name, Map<String, String> properties) {
    super(name, properties);
    reference = new TCSResourceReference<>(this);
  }

  // Methods inherited from TCSObject<E> start here.
  @Override
  public TCSResourceReference<E> getReference() {
    return (TCSResourceReference<E>) reference;
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated Will become immutable and not implement Cloneable any more.
   */
  @Override
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TCSResource<E> clone() {
    TCSResource<E> clone = (TCSResource<E>) super.clone();
    return clone;
  }
}
