/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.configuration;

import static java.util.Objects.requireNonNull;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.reload.Reloadable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ConfigurationSource} caching the properties of another {@link ConfigurationSource}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class CachedConfigurationSource
    implements ConfigurationSource,
               Reloadable {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CachedConfigurationSource.class);
  /**
   * The {@link ConfigurationSource} to cache.
   */
  private final ConfigurationSource delegate;
  /**
   * The configuration's environment.
   */
  private final Environment environment;
  /**
   * The cached properties.
   */
  private final AtomicReference<Properties> properties = new AtomicReference<>(new Properties());

  /**
   * Creates a new instance.
   *
   * @param delegate The {@link ConfigurationSource} to cache.
   * @param environment The configuration's environment.
   */
  public CachedConfigurationSource(ConfigurationSource delegate, Environment environment) {
    this.delegate = requireNonNull(delegate, "delegate");
    this.environment = requireNonNull(environment, "environment");
  }

  @Override
  public void reload() {
    try {
      properties.set(delegate.getConfiguration(this.environment));
      LOG.debug("Reloaded properties : {}", properties);
    }
    catch (Exception e) {
      LOG.error("Error reloading properties from delegate source : keep old properties", e);
    }
  }

  @Override
  public Properties getConfiguration(Environment environment) {
    return properties.get();
  }

  @Override
  public void init() {
    delegate.init();
    Properties props = delegate.getConfiguration(environment);
    properties.set(props);
  }
}
