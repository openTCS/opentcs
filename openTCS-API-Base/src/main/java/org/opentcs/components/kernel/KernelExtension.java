/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import org.opentcs.components.Lifecycle;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Declares the methods that a generic kernel extension must implement.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface KernelExtension
    extends Lifecycle {
  
  /**
   * Returns a name/brief human-readable description for this extension.
   *
   * @return The name/brief description.
   * @deprecated Unused. Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  default String getName() {
    return getClass().getName();
  };
}
