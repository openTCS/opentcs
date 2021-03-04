/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.multibindings.Multibinder;
import javax.inject.Singleton;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.kernel.controlcenter.KernelConfigurationPanel;
import org.opentcs.kernel.controlcenter.KernelControlCenter;
import org.opentcs.kernel.controlcenter.vehicles.DriverGUI;
import org.opentcs.kernel.statistics.StatisticsCollector;
import org.opentcs.kernel.xmlorders.XMLTelegramOrderReceiver;
import org.opentcs.kernel.xmlstatus.StatusMessageDispatcher;
import org.opentcs.util.configuration.ConfigurationStore;

/**
 * Configures the default extensions of the openTCS kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultKernelExtensionsModule
    extends KernelInjectionModule {

  @Override
  protected void configure() {
    configureControlCenterDependencies();
    // Kernel extensions for StandardKernel (permanent extensions)
    Multibinder<KernelExtension> allStatesBinder = extensionsBinderAllModes();
    allStatesBinder.addBinding()
        .to(KernelControlCenter.class)
        .in(Singleton.class);
    allStatesBinder.addBinding()
        .to(StandardRemoteKernel.class)
        .in(Singleton.class);
    configureStatusChannel();

    // Kernel extensions for KernelStateModelling
    Multibinder<KernelExtension> modellingBinder = extensionsBinderModelling();
    // No extensions for modelling mode, yet.

    // Kernel extensions for KernelStateOperating
    Multibinder<KernelExtension> operatingBinder = extensionsBinderOperating();
    configureOrderChannel();
    operatingBinder.addBinding().to(StatisticsCollector.class);
  }

  private void configureControlCenterDependencies() {
    controlCenterPanelBinderModelling().addBinding().to(KernelConfigurationPanel.class);

    controlCenterPanelBinderOperating().addBinding().to(KernelConfigurationPanel.class);
    controlCenterPanelBinderOperating().addBinding().to(DriverGUI.class);
  }

  private void configureOrderChannel() {
    Multibinder<KernelExtension> operatingBinder = extensionsBinderOperating();
    ConfigurationStore xmlOrderConfigStore
        = ConfigurationStore.getStore(XMLTelegramOrderReceiver.class.getName());
    bindConstant()
        .annotatedWith(XMLTelegramOrderReceiver.ListenPort.class)
        .to(xmlOrderConfigStore.getInt("listenPort", 55555));
    bindConstant()
        .annotatedWith(XMLTelegramOrderReceiver.InputTimeout.class)
        .to(xmlOrderConfigStore.getInt("inputTimeout", 10000));
    bindConstant()
        .annotatedWith(XMLTelegramOrderReceiver.MaxInputLength.class)
        .to(xmlOrderConfigStore.getInt("maxInputLength", 100 * 1024));

    operatingBinder.addBinding().to(XMLTelegramOrderReceiver.class);
  }

  private void configureStatusChannel() {
    Multibinder<KernelExtension> allStatesBinder = extensionsBinderAllModes();
    ConfigurationStore xmlStatusConfigStore
        = ConfigurationStore.getStore(StatusMessageDispatcher.class.getName());
    bindConstant()
        .annotatedWith(StatusMessageDispatcher.ListenPort.class)
        .to(xmlStatusConfigStore.getInt("listenPort", 44444));
    bindConstant()
        .annotatedWith(StatusMessageDispatcher.MessageSeparator.class)
        .to(xmlStatusConfigStore.getString("messageSeparator", "|"));

    allStatesBinder.addBinding()
        .to(StatusMessageDispatcher.class)
        .in(Singleton.class);
  }
}
