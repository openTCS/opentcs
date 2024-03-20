/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.configuration.gestalt;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ServiceLoader;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.decoder.ProxyDecoderMode;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reload.TimedConfigReloadStrategy;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.FileConfigSourceBuilder;
import org.github.gestalt.config.source.ConfigSource;
import org.opentcs.configuration.ConfigurationBindingProvider;
import org.opentcs.configuration.ConfigurationException;
import org.opentcs.configuration.gestalt.decoders.ClassPathDecoder;
import org.opentcs.configuration.gestalt.decoders.MapLiteralDecoder;
import static org.opentcs.util.Assertions.checkState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A configuration binding provider implementation using gestalt.
 */
public class GestaltConfigurationBindingProvider
    implements ConfigurationBindingProvider {

  /**
   * This class's logger.
   */
  private static final Logger LOG
      = LoggerFactory.getLogger(GestaltConfigurationBindingProvider.class);
  /**
   * The key of the (system) property containing the reload interval.
   */
  private static final String PROPKEY_RELOAD_INTERVAL = "opentcs.configuration.reload.interval";
  /**
   * The default reload interval.
   */
  private static final long DEFAULT_RELOAD_INTERVAL = 10000;
  /**
   * Default configuration file name.
   */
  private final Path defaultsPath;
  /**
   * Supplementary configuration files.
   */
  private final Path[] supplementaryPaths;
  /**
   * The configuration entry point.
   */
  private final Gestalt gestalt;

  /**
   * Creates a new instance.
   *
   * @param defaultsPath Default configuration file name.
   * @param supplementaryPaths Supplementary configuration file names.
   */
  public GestaltConfigurationBindingProvider(Path defaultsPath, Path... supplementaryPaths) {
    this.defaultsPath = requireNonNull(defaultsPath, "defaultsPath");
    this.supplementaryPaths = requireNonNull(supplementaryPaths, "supplementaryPaths");

    this.gestalt = buildGestalt();
  }

  @Override
  public <T> T get(String prefix, Class<T> type) {
    try {
      return gestalt.getConfig(prefix, type);
    }
    catch (GestaltException e) {
      throw new ConfigurationException(
          String.format("Cannot get configuration value for prefix: '%s'", prefix),
          e
      );
    }
  }

  private Gestalt buildGestalt() {
    GestaltConfig gestaltConfig = new GestaltConfig();
    gestaltConfig.setTreatMissingValuesAsErrors(true);
    gestaltConfig.setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH);

    try {
      Gestalt provider = new GestaltBuilder()
          .setGestaltConfig(gestaltConfig)
          .useCacheDecorator(true)
          .addDefaultDecoders()
          .addDecoder(new ClassPathDecoder())
          .addDecoder(new MapLiteralDecoder())
          .addSources(buildSources())
          .build();
      provider.loadConfigs();

      return provider;
    }
    catch (GestaltException e) {
      throw new ConfigurationException(
          "An error occured while creating gestalt configuration binding provider",
          e);
    }
  }

  private List<ConfigSourcePackage> buildSources()
      throws GestaltException {
    Duration reloadInterval = reloadInterval();
    List<ConfigSourcePackage> sources = new ArrayList<>();

    // A file for baseline defaults MUST exist in the distribution.
    checkState(defaultsPath.toFile().isFile(),
               "Required default configuration file {} does not exist.",
               defaultsPath.toFile().getAbsolutePath());
    LOG.info("Using default configuration file {}...",
             defaultsPath.toFile().getAbsolutePath());
    sources.add(
        FileConfigSourceBuilder.builder()
            .setPath(defaultsPath)
            .addConfigReloadStrategy(new TimedConfigReloadStrategy(reloadInterval))
            .build()
    );

    // Files with supplementary configuration MAY exist in the distribution.
    for (Path supplementaryPath : supplementaryPaths) {
      if (supplementaryPath.toFile().isFile()) {
        LOG.info("Using overrides from supplementary configuration file {}...",
                 supplementaryPath.toFile().getAbsolutePath());
        sources.add(
            FileConfigSourceBuilder.builder()
                .setPath(supplementaryPath)
                .addConfigReloadStrategy(new TimedConfigReloadStrategy(reloadInterval))
                .build()
        );
      }
      else {
        LOG.warn("Supplementary configuration file {} not found, skipped.",
                 supplementaryPath.toFile().getAbsolutePath());
      }
    }

    for (ConfigSource source : ServiceLoader.load(SupplementaryConfigSource.class)) {
      LOG.info("Using overrides from additional configuration source implementation {}...",
               source.getClass());
      sources.add(new ConfigSourcePackage(
          source,
          List.of(new TimedConfigReloadStrategy(reloadInterval))
      ));
    }
    return sources;
  }

  private Duration reloadInterval() {
    String valueString = System.getProperty(PROPKEY_RELOAD_INTERVAL);

    if (valueString == null) {
      LOG.info("Using default configuration reload interval ({} ms).", DEFAULT_RELOAD_INTERVAL);
      return Duration.ofMillis(DEFAULT_RELOAD_INTERVAL);
    }

    try {
      long value = Long.parseLong(valueString);
      LOG.info("Using configuration reload interval of {} ms.", value);
      return Duration.ofMillis(value);
    }
    catch (NumberFormatException exc) {
      LOG.warn("Could not parse '{}', using default configuration reload interval ({} ms).",
               valueString,
               DEFAULT_RELOAD_INTERVAL,
               exc);
      return Duration.ofMillis(DEFAULT_RELOAD_INTERVAL);
    }
  }
}
