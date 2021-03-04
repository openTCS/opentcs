/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.plantoverview;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.Kernel;

/**
 * Produces plugin panels to extend an openTCS user interface.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface PluggablePanelFactory {

  /**
   * Checks whether this factory produces panels that are available in the
   * passed <code>Kernel.State</code>.
   *
   * @param state The kernel state.
   * @return <code>true</code> if, and only if, this factory returns panels that
   * are available in the passed kernel state.
   */
  boolean providesPanel(Kernel.State state);

  /**
   * Returns a string describing the factory/the panels provided.
   * This should be a short string that can be displayed e.g. as a menu item for
   * selecting a factory/plugin panel to be displayed.
   *
   * @return A string describing the factory/the panels provided.
   */
  @Nonnull
  String getPanelDescription();

  /**
   * Returns a newly created panel.
   * If a reference to the kernel provider has not been set, yet, or has been
   * set to <code>null</code>, this method returns <code>null</code>.
   *
   * @param state The kernel state for which to create the panel.
   * @return A newly created panel.
   */
  @Nullable
  PluggablePanel createPanel(Kernel.State state);
}
