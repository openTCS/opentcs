/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.math.path;

import com.google.inject.AbstractModule;
import javax.inject.Singleton;

/**
 * A Guice configuration module for this package.
 */
public class PathLengthFunctionInjectionModule
    extends AbstractModule {

  /**
   * Creates a new instance.
   */
  public PathLengthFunctionInjectionModule() {
  }

  @Override
  protected void configure() {
    bind(PathLengthFunction.class)
        .to(EuclideanDistance.class)
        .in(Singleton.class);
  }
}
