/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

import java.util.List;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;

/**
 * This interface declares methods that a communication adapter may call
 * to let the control system know about status changes regarding itself or the
 * vehicle it controls.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleManager {

  /**
   * Informs the vehicle manager about the capacity of the communication
   * adapter's command queue.
   *
   * @param capacity The communication adapter's command queue capacity.
   */
  void setAdapterCommandQueueCapacity(int capacity);

  /**
   * Informs the vehicle manager about the physical vehicle's current position.
   *
   * @param position The name of the vehicle's current position.
   */
  void setVehiclePosition(String position);
  
  /**
   * Informs the vehicle manager about the physical vehicle's current precise
   * position (that is, its actual coordinates).
   *
   * @param position The vehicle's current precise position.
   */
  void setVehiclePrecisePosition(Triple position);
  
  /**
   * Informs the vehicle manager about the physical vehicle's current
   * orientation angle.
   *
   * @param angle The vehicle's current orientation angle.
   */
  void setVehicleOrientationAngle(double angle);
  
  /**
   * Informs the vehicle manager about the physical vehicle's current remaining
   * energy (in percent of the maximum).
   *
   * @param energyLevel The vehicle's new energy level.
   */
  void setVehicleEnergyLevel(int energyLevel);
  
  /**
   * Informs the vehicle manager about the communication adapter's accepted
   * recharge operation.
   *
   * @param rechargeOperation The vehicle's new recharge action.
   */
  void setVehicleRechargeOperation(String rechargeOperation);
  
  /**
   * Informs the vehicle manager about the physical vehicle's current (state of
   * the) load handling devices.
   *
   * @param devices The vehicle's load handling devices.
   */
  void setVehicleLoadHandlingDevices(List<LoadHandlingDevice> devices);
  
  /**
   * Informs the vehicle manager about the physical vehicle's maximum velocity.
   *
   * @param velocity The vehicle's new maximum velocity.
   */
  void setVehicleMaxVelocity(int velocity);
  
  /**
   * Informs the vehicle manager about the physical vehicle's maximum reverse
   * velocity.
   *
   * @param velocity The vehicle's new maximum reverse velocity.
   */
  void setVehicleMaxReverseVelocity(int velocity);
  
  /**
   * Sets a property of the vehicle.
   * If the given value is <code>null</code>, the property is removed from the
   * vehicle.
   * 
   * @param key The property's key.
   * @param value The property's (new) value. If <code>null</code>, the property
   * is removed from the vehicle.
   */
  void setVehicleProperty(String key, String value);

  /**
   * Notifies the vehicle manager about a change of state regarding the vehicle.
   *
   * @param newState The vehicle's new state.
   */
  void setVehicleState(Vehicle.State newState);

  /**
   * Notifies the vehicle manager about a change of state regarding the
   * communication adapter.
   *
   * @param newState The communication adapter's new state.
   */
  void setAdapterState(CommunicationAdapter.State newState);
  
  /**
   * Sets a property of the transport order the vehicle is currently processing.
   * If the given value is <code>null</code>, the property is removed from the
   * order. If the vehicle is currently not processing an order, this method
   * does nothing.
   *
   * @param key The property's key.
   * @param value The property's (new) value. If <code>null</code>, the property
   * is removed from the order.
   */
  void setOrderProperty(String key, String value);

  /**
   * Confirms that a given command has been successfully executed by a
   * communication adapter/vehicle.
   *
   * @param executedCommand The command that has been successfully executed.
   */
  void commandExecuted(AdapterCommand executedCommand);
}
