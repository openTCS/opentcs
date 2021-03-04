/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class declares central configuration management methods that
 * provide the possibility of making configuration data persistent and loading
 * such persistent data.
 * The form of the persistent configuration data depends on the actual
 * implementation.
 * <hr>
 * <p>
 * System properties:
 * </p>
 * <dl>
 * <dt><b>de.fraunhofer.iml.toolbox.configuration.class:</b></dt>
 * <dd>May be set to the name of the actual implementing class to be used. The
 * default class is <code>PropertiesConfiguration</code> in this package.</dd>
 * <dt><b>de.fraunhofer.iml.toolbox.configuration.saveonexit</b></dt>
 * <dd>If set to <code>true</code>, persists the configuration when the JVM is
 * shut down.
 * </dd>
 * </dl>
 * <hr>
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public abstract class Configuration {

  /**
   * The name of the property which may contain the name of the desired
   * configuration implementation class.
   */
  public static final String PROPKEY_IMPL_CLASS = "org.opentcs.util.configuration.class";
  /**
   * This class's Logger instance.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
  /**
   * The default configuration implementation.
   */
  private static final String IMPL_CLASS_DEFAULT = InMemoryConfiguration.class.getName();
  /**
   * The name of the property which enables/disables saving of the configuration
   * on exit.
   */
  private static final String PROPKEY_SAVE_ON_EXIT = "org.opentcs.util.configuration.saveonexit";
  /**
   * The actual configuration instance.
   */
  private static final Configuration INSTANCE;
  /**
   * A cache of existing ConfigurationStore instances, identified by their
   * namespaces.
   */
  protected final Map<String, ConfigurationStore> stores = new HashMap<>();

  /**
   * Static initializer.
   */
  static {
    String instanceClass =
        System.getProperty(PROPKEY_IMPL_CLASS, IMPL_CLASS_DEFAULT);
    try {
      Class<?> newInstanceClass = Class.forName(instanceClass);
      INSTANCE = (Configuration) newInstanceClass.newInstance();
    }
    catch (ClassNotFoundException exc) {
      LOG.error("Configuration class not found", exc);
      throw new IllegalStateException("Configuration class not found", exc);
    }
    catch (InstantiationException | IllegalAccessException exc) {
      LOG.error("Could not instantiate configuration class", exc);
      throw new IllegalStateException(
          "Could not instantiate configuration class", exc);
    }
    // Add a shutdown hook for saving the configuration if wanted.
    if (System.getProperty(PROPKEY_SAVE_ON_EXIT, "").equalsIgnoreCase("true")) {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> saveConfiguration()));
    }
  }

  /**
   * Creates a new Configuration.
   */
  protected Configuration() {
  }

  /**
   * Returns the configuration instance.
   *
   * @return The configuration instance.
   */
  public static Configuration getInstance() {
    return INSTANCE;
  }

  /**
   * Make all key/value pairs for all namespaces persistent.
   * How and where the data is saved depends on the implementation - this method
   * delegates the actual persistence to the class realizing the configuration
   * store.
   */
  public static void saveConfiguration() {
    getInstance().persist();
  }

  /**
   * Returns the <code>ConfigurationStore</code> for the given namespace.
   * If none exists, yet, one is created.
   *
   * @param namespace The namespace for which a <code>ConfigurationStore</code>
   * is requested.
   * @return The <code>ConfigurationStore</code> for the given namespace.
   */
  public final ConfigurationStore getStore(String namespace) {
    LOG.debug("method entry");
    Objects.requireNonNull(namespace, "namespace is null");
    ConfigurationStore store = stores.get(namespace);
    if (store == null) {
      store = new ConfigurationStore(namespace);
      stores.put(namespace, store);
    }
    return store;
  }

  /**
   * Returns all existing stores/namespaces.
   *
   * @return All existing stores/namespaces.
   */
  public final SortedSet<ConfigurationStore> getStores() {
    SortedSet<ConfigurationStore> result =
        new TreeSet<>(new ConfigurationStore.NamespaceComparator());
    result.addAll(stores.values());
    return result;
  }

  /**
   * Returns all existing configuration items (from all stores/namespaces).
   *
   * @return All existing configuration items.
   */
  public final Set<ConfigurationItem> getConfigurationItems() {
    Set<ConfigurationItem> allItems = new HashSet<>();
    for (ConfigurationStore store : stores.values()) {
      allItems.addAll(store.getConfigurationItems().values());
    }
    return allItems;
  }

  /**
   * Sets a configuration item.
   *
   * @param item The configuration item.
   */
  public final void setConfigurationItem(ConfigurationItem item) {
    Objects.requireNonNull(item, "item is null");
    ConfigurationStore store = getStore(item.getNamespace());
    store.setConfigurationItem(item.getKey(),
                               item.getValue(),
                               item.getDescription(),
                               item.getConstraint());
  }

  /**
   * Perform the actual persistence of the configuration data.
   */
  abstract void persist();
}
