// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.configuration;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface that provides some configuration for a specific class.
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationPrefix {

  /**
   * Returns the name of the class the interface configures.
   *
   * @return The name of the class the interface configures.
   */
  String value();
}
