/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface's method that provides a configuration value.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@Target({METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationEntry {

  /**
   * Returns a description for the data type of this configuration key's values.
   *
   * @return A description for the data type of this configuration key's values.
   */
  String type();

  /**
   * Returns a list of paragraphs describing what the key/value configures.
   *
   * @return A list of paragraphs describing what the key/value configures.
   */
  String[] description();

  /**
   * Returns the optional ordering key that this entry belongs to (for grouping/sorting of entries).
   *
   * @return The optional ordering key that this entry belongs to (for grouping/sorting of entries).
   */
  String orderKey() default "";
}
