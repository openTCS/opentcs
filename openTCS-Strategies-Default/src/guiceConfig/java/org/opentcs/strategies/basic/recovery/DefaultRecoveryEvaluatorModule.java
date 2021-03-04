/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.recovery;

import org.opentcs.customizations.kernel.KernelInjectionModule;

/**
 * Guice configuration for the default recovery evaluator.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultRecoveryEvaluatorModule
    extends KernelInjectionModule {

  @Override
  protected void configure() {
    configureRecoveryEvaluator();
  }

  @SuppressWarnings("deprecation")
  private void configureRecoveryEvaluator() {
    bind(DefaultRecoveryEvaluatorConfiguration.class)
        .toInstance(getConfigBindingProvider().get(DefaultRecoveryEvaluatorConfiguration.PREFIX,
                                                   DefaultRecoveryEvaluatorConfiguration.class));
    bindRecoveryEvaluator(org.opentcs.strategies.basic.recovery.DefaultRecoveryEvaluator.class);
  }
}
