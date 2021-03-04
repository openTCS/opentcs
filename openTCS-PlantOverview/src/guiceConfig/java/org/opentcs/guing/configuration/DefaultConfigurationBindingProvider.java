/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
   * Baseline defaults file name.
   */
  private static final Path DEFAULTS_BASELINE_PATH
      = Paths.get(System.getProperty("opentcs.base", "."),
                  "config",
                  "opentcs-plantoverview-defaults-baseline.properties")
      .toAbsolutePath();
  /**
   * Customization defaults file name.
   */
  private static final Path DEFAULTS_CUSTOM_PATH
      = Paths.get(System.getProperty("opentcs.base", "."),
                  "config",
                  "opentcs-plantoverview-defaults-custom.properties")
      .toAbsolutePath();
  /**
   * User overrides file name.
   */
  private static final Path OVERRIDES_USER_PATH
      = Paths.get(System.getProperty("opentcs.home", "."),
                  "config",
                  "opentcs-plantoverview.properties")
      .toAbsolutePath();

  /**
   * The (cfg4j) configuration provider.
   */
  private final ConfigurationProvider provider;

  /**
   * Creates a new instance.
   */
  public DefaultConfigurationBindingProvider() {
    provider = buildProvider();
  }

  @Override
  public <T> T get(String prefix, Class<T> type) {
    return provider.bind(prefix, type);
  }

  private static ConfigurationProvider buildProvider() {
    Environment environment = new DefaultEnvironment();

    return new ConfigurationProviderBuilder()
        .withConfigurationSource(buildSource(environment))
        .withEnvironment(environment)
        .withReloadStrategy(new PeriodicalReloadStrategy(10000))
        .build();
  }

  private static ConfigurationSource buildSource(Environment environment) {
    List<ConfigurationSource> sources = new ArrayList<>();
    ConfigurationSource source;

    // A file for baseline defaults MUST exist in the distribution.
    source = new FilesConfigurationSource(() -> Arrays.asList(DEFAULTS_BASELINE_PATH));
    sources.add(source);

    // A file for customization defaults MAY exist in the distribution.
    if (DEFAULTS_CUSTOM_PATH.toFile().isFile()) {
      source = new FilesConfigurationSource(() -> Arrays.asList(DEFAULTS_CUSTOM_PATH));
      sources.add(source);
    }

    // A file for user overrides MAY exist in the home directory.
    if (OVERRIDES_USER_PATH.toFile().isFile()) {
      source = new FilesConfigurationSource(() -> Arrays.asList(OVERRIDES_USER_PATH));
      sources.add(source);
    }

    ConfigurationSource mergedSource
        = new MergeConfigurationSource(sources.toArray(new ConfigurationSource[sources.size()]));

    ConfigurationSource cachedSource = new CachedConfigurationSource(mergedSource, environment);

    return cachedSource;
  }
}
