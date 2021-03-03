/*
 * openTCS copyright information:
 * Copyright (c) 2005 Fraunhofer IML
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
import java.util.logging.Level;
import java.util.logging.Logger;

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
 */
public abstract class Configuration {

  /**
   * This class's Logger instance.
   */
  private static final Logger log =
      Logger.getLogger(Configuration.class.getName());
  /**
   * The name of the property which may contain the name of the desired
   * configuration implementation class.
   */
  private static final String instanceClassProperty =
      "org.opentcs.util.configuration.class";
  /**
   * The default configuration implementation.
   */
  private static final String instanceClassDefault =
      XMLConfiguration.class.getName();
  /**
   * The name of the property which enables/disables saving of the configuration
   * on exit.
   */
  private static final String saveOnExitProperty =
      "org.opentcs.util.configuration.saveonexit";
  /**
   * The actual configuration instance.
   */
  private static final Configuration configurationInstance;
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
        System.getProperty(instanceClassProperty, instanceClassDefault);
    try {
      Class newInstanceClass = Class.forName(instanceClass);
      configurationInstance = (Configuration) newInstanceClass.newInstance();
    }
    catch (ClassNotFoundException exc) {
      log.log(Level.SEVERE, "Configuration class not found", exc);
      throw new IllegalStateException("Configuration class not found", exc);
    }
    catch (InstantiationException | IllegalAccessException exc) {
      log.log(Level.SEVERE, "Could not instantiate configuration class", exc);
      throw new IllegalStateException(
          "Could not instantiate configuration class", exc);
    }
    // Add a shutdown hook for saving the configuration if wanted.
    if (System.getProperty(saveOnExitProperty, "").equalsIgnoreCase("true")) {
      Runtime.getRuntime().addShutdownHook(new Thread(new SaveOnExitTask()));
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
    return configurationInstance;
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
    log.fine("method entry");
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

  /**
   * A task for saving the configuration data on exit.
   */
  private static class SaveOnExitTask
      implements Runnable {

    /**
     * Creates a new SaveOnExitTask.
     */
    public SaveOnExitTask() {
    }

    @Override
    public void run() {
      saveConfiguration();
    }
  }
}
