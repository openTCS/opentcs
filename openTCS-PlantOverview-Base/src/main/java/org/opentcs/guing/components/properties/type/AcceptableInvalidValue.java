/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.type;

/**
 * An invalid but still acceptable value for a property.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface AcceptableInvalidValue {

  /**
   * Returns a description for the value.
   *
   * @return A description.
   */
  String getDescription();

  /**
   * Returns a helptext for the value.
   *
   * @return A helptext.
   */
  String getHelptext();
}
