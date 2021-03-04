/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

/**
 * A {@link Configuration} implementation that does not persist configuration data.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class InMemoryConfiguration
    extends Configuration {

  @Override
  void persist() {
  }
}
