// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.configuration;

/**
 * A provider to get bindings (implementations) for configuration interfaces.
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
