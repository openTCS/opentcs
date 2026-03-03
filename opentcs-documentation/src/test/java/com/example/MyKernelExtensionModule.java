// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
// tag::tutorial_gettingstarted_MyKernelExtensionModule[]
package com.example;

import jakarta.inject.Singleton;
import org.opentcs.customizations.kernel.KernelInjectionModule;

public class MyKernelExtensionModule
    extends
      KernelInjectionModule {

  public MyKernelExtensionModule() {
  }

  @Override
  protected void configure() {
    bind(MyKernelExtension.class).in(Singleton.class);

    extensionsBinderOperating().addBinding().to(MyKernelExtension.class);
  }
}
// end::tutorial_gettingstarted_MyKernelExtensionModule[]
