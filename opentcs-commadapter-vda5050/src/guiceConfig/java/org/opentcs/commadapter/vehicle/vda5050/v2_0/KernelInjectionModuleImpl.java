// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.inject.Singleton;
import org.opentcs.commadapter.vehicle.vda5050.Vda5050CommAdapterFactory;
import org.opentcs.customizations.kernel.KernelInjectionModule;

public class KernelInjectionModuleImpl
    extends
      KernelInjectionModule {

  /**
   * Creates a new instance.
   */
  public KernelInjectionModuleImpl() {
  }

  @Override
  protected void configure() {
    bind(MessageValidator.class).in(Singleton.class);

    bind(Vda5050CommAdapterFactory.class)
        .annotatedWith(CommAdapterFactory.V2dot0.class)
        .to(CommAdapterFactory.class);

    install(new FactoryModuleBuilder().build(CommAdapterComponentsFactory.class));

    bind(PositionDeviationPolicyFactoryImpl.class)
        .in(Singleton.class);
    positionDeviationPolicyFactoryBinder()
        .addBinding()
        .to(PositionDeviationPolicyFactoryImpl.class);
  }
}
