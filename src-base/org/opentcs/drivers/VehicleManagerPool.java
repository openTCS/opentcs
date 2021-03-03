/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

/**
 * Produces <code>VehicleManager</code>s for remote
 * <code>CommunicationAdapter</code>s.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleManagerPool {

  /**
   * Returns a <code>VehicleManager</code> instance for a named vehicle and
   * associates the given <code>CommunicationAdapter</code> with it.
   *
   * @param vehicleName The name of the vehicle for which to return the vehicle
   * manager.
   * @param commAdapter The communication adapter that is going to control the
   * physical vehicle.
   * @return A <code>VehicleManager</code> instance for the named vehicle.
   * @throws IllegalArgumentException If a vehicle with the given name does not
   * exist.
   */
  VehicleManager getVehicleManager(String vehicleName,
                                   CommunicationAdapter commAdapter)
      throws IllegalArgumentException;

  /**
   * Disassociates a <code>VehicleManager</code> and
   * <code>CommunicationAdapter</code> from a vehicle and removes all references
   * to them.
   *
   * @param vehicleName The name of the vehicle from which to detach the manager
   * and communication adapter.
   * @throws IllegalArgumentException If a vehicle with the given name does not
   * exist or if it is not associated with a vehicle manager/communication
   * adapter pair.
   */
  void detachVehicleManager(String vehicleName)
      throws IllegalArgumentException;
}
