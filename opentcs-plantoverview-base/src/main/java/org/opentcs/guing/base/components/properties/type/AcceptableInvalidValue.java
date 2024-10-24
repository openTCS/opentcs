// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.type;

/**
 * An invalid but still acceptable value for a property.
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
