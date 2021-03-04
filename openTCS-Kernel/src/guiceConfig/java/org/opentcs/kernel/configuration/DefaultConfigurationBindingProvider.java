/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.configuration;

import java.nio.file.Paths;
import java.util.Arrays;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.compose.MergeConfigurationSource;
import org.cfg4j.source.context.environment.DefaultEnvironment;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.opentcs.customizations.ConfigurationBindingProvider;

/**
 * The default implementation of a provider for configuration bindings.
 * This implementation uses the cfg4j framework to bind configuration interfaces.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultConfigurationBindingProvider
    implements ConfigurationBindingProvider {

  /**
   * The (cfg4j) configuration provider.
   */
  private final ConfigurationProvider provider;

  /**
   * Creates a new instance.
   */
  public DefaultConfigurationBindingProvider() {
    Environment environment = new DefaultEnvironment();
    
    ConfigurationSource defaultSource = new FilesConfigurationSource(() -> Arrays.asList(
        Paths.get(System.getProperty("opentcs.home", "."),
                  "config",
                  "opentcs-kernel-defaults.properties")
            .toAbsolutePath())
    );

    ConfigurationSource overrideSource = new FilesConfigurationSource(() -> Arrays.asList(
        Paths.get(System.getProperty("opentcs.home", "."),
                  "config",
                  "opentcs-kernel.properties")
            .toAbsolutePath())
    );
    
    ConfigurationSource mergedSources = new MergeConfigurationSource(defaultSource, overrideSource);
    
    ConfigurationSource source = new CachedConfigurationSource(mergedSources, environment);
    
    provider = new ConfigurationProviderBuilder()
        .withConfigurationSource(source)
        .withEnvironment(environment)
        .withReloadStrategy(new PeriodicalReloadStrategy(10000))
        .build();
  }

  @Override
  public <T> T get(String prefix, Class<T> type) {
    return provider.bind(prefix, type);
  }
}
