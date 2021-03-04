/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customizations;

/**
 * A provider to get bindings (implementations) for configuration interfaces.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface ConfigurationBindingProvider {

  /**
   * Returns a binding for a configuration interface.
   *
   * @param <T> The configuration interface to get an instance for.
   * @param prefix Relative path to configuration values.
   * @param type The class for {@literal <T>}.
   * @return The corresponding binding.
   */
  <T> T get(String prefix, Class<T> type);
}
