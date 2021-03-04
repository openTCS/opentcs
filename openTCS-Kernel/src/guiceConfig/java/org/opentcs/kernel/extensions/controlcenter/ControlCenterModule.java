/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.controlcenter;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import java.util.Locale;
import javax.inject.Singleton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.kernel.extensions.controlcenter.vehicles.DriverGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the control center extension.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ControlCenterModule
    extends KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ControlCenterModule.class);

  @Override
  protected void configure() {
    ControlCenterConfiguration configuration
        = getConfigBindingProvider().get(ControlCenterConfiguration.PREFIX,
                                         ControlCenterConfiguration.class);

    if (!configuration.enable()) {
      LOG.info("Control center disabled by configuration.");
      return;
    }

    bind(ControlCenterConfiguration.class)
        .toInstance(configuration);

    // Bindings for modelling mode panels.
    // No extensions for modelling mode, yet.
    controlCenterPanelBinderModelling();

    // Bindings for operating mode panels.
    controlCenterPanelBinderOperating().addBinding().to(DriverGUI.class);

    install(new FactoryModuleBuilder().build(ControlCenterInfoHandlerFactory.class));

    configureControlCenterLocale(configuration);
    configureControlCenterLookAndFeel();

    extensionsBinderAllModes().addBinding()
        .to(KernelControlCenter.class)
        .in(Singleton.class);
  }

  private void configureControlCenterLocale(ControlCenterConfiguration configuration) {
    if (configuration.language().toLowerCase().equals("german")) {
      Locale.setDefault(Locale.GERMAN);
    }
    else {
      Locale.setDefault(Locale.ENGLISH);
    }

  }

  private void configureControlCenterLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException
               | UnsupportedLookAndFeelException ex) {
      LOG.warn("Exception setting look and feel", ex);
    }
  }
}
