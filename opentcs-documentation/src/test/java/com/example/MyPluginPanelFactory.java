// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
// tag::tutorial_gettingstarted_MyPluginPanelFactory[]
package com.example;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.opentcs.access.Kernel;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.opentcs.components.plantoverview.PluggablePanelFactory;

/**
 * An exemplary plugin panel factory.
 */
public class MyPluginPanelFactory
    implements
      PluggablePanelFactory {

  private final Provider<MyPluginPanel> panelProvider;

  @Inject
  MyPluginPanelFactory(Provider<MyPluginPanel> panelProvider) {
    this.panelProvider = requireNonNull(panelProvider, "panelProvider");
  }

  @Override
  @Nonnull
  public String getPanelDescription() {
    return "My plugin panel";
  }

  @Override
  public boolean providesPanel(Kernel.State state) {
    requireNonNull(state, "state");

    return state == Kernel.State.OPERATING;
  }

  @Override
  @Nullable
  public PluggablePanel createPanel(Kernel.State state) {
    requireNonNull(state, "state");

    if (!providesPanel(state)) {
      return null;
    }
    return panelProvider.get();
  }
}
// end::tutorial_gettingstarted_MyPluginPanelFactory[]
