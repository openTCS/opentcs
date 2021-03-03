/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.guing.util;

/**
 * Defines some reserved/commonly used config keys and values.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public interface ConfigConstants {
  
  /**
   * A configuration key for ignoring precise positions of vehicles.
   * <p>
   * Type: boolean (true: precise positions will be ignored,
   * false: vehicles will be set on precise positions if available).
   * </p>
   */
  String IGNORE_VEHICLE_PRECISE_POSITION = "IGNORE_PRECISE_POSITION";
  /**
   * A configuration key for ignoring orientation angles of vehicles.
   * <p>
   * Type: boolean (true: orientation angles will be ignored,
   * false: orientation angle will be considered if set).
   */
  String IGNORE_VEHICLE_ORIENTATION_ANGLE = "IGNORE_ORIENTATION_ANGLE";
  /**
   * A configuration key for the client value of the location theme.
   */
  String LOCATION_THEME = "LOCATION_THEME";
  /**
   * A configuration key for the client value of the vehicle theme.
   */
  String VEHICLE_THEME = "VEHICLE_THEME";
}
