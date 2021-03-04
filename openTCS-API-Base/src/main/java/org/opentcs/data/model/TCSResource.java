/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import org.opentcs.data.TCSObject;

/**
 * Instances of this class represent resources that vehicles may claim for
 * exclusive usage.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual resource class.
 */
public class TCSResource<E extends TCSResource<E>>
extends TCSObject<E>
implements Serializable, Cloneable {
  
  /**
   * Creates a new TCSResource.
   *
   * @param objectID The new resource's object ID.
   * @param name The new resource's name.
   */
  protected TCSResource(int objectID, String name) {
    super(objectID, name);
    reference = new TCSResourceReference<>(this);
  }
  
  // Methods inherited from TCSObject<E> start here.
  
  @Override
  public TCSResourceReference<E> getReference() {
    return (TCSResourceReference<E>) reference;
  }
  
  @Override
  public TCSResource<E> clone() {
    TCSResource<E> clone = (TCSResource<E>) super.clone();
    return clone;
  }
}
