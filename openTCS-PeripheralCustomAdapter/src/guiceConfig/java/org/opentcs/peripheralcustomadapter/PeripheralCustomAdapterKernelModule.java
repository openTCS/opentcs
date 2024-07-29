package org.opentcs.peripheralcustomadapter;

import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeripheralCustomAdapterKernelModule
    extends
      KernelInjectionModule {

  private static final Logger LOG = LoggerFactory.getLogger(
      PeripheralCustomAdapterKernelModule.class
  );

  /**
   * A class that represents a custom adapter kernel module.
   * This module is responsible for configuring bindings for the kernel application.
   */
  public PeripheralCustomAdapterKernelModule() {
  }
}
