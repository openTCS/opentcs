/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import javax.annotation.Nonnull;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Location;

/**
 * This interface declares the methods a peripheral job dispatcher module for the openTCS kernel 
 * must implement.
 * <p>
 * A peripheral job dispatcher manages the distribution of peripheral jobs among the perihperal 
 * devices represented by locations in a system. It is basically event-driven, where an event can 
 * be a new peripheral job being introduced into the system or a peripheral device becoming
 * available for processing existing jobs.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PeripheralJobDispatcher
    extends Lifecycle {

  /**
   * Notifies the dispatcher that it should start the dispatching process.
   */
  void dispatch();

  /**
   * Notifies the dispatcher that any job a peripheral device (represented by the given location)
   * might be processing is to be withdrawn.
   *
   * @param location The location representing a peripheral device whose job is withdrawn.
   */
  void withdrawJob(@Nonnull Location location);
}
