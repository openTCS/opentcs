/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * This abstract class declares methods for storing and loading configuration
 * data as key/value pairs.
 * <p>
 * The configuration data is grouped by namespaces (typically equalling the
 * fully qualified names of the classes using the configuration data, though no
 * guarantees are made that the used namespace are verified to really belong to
 * the calling class).
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class ConfigurationStore
    implements Serializable {

  /**
   * This store's namespace.
   */
  private final String namespace;
  /**
   * This store's configuration items, mapped by their keys.
   */
  private final Map<String, ConfigurationItem> configurationItems = new TreeMap<>();

  /**
   * Creates a new ConfigurationStore.
   *
   * @param newNamespace The store's namespace.
   */
  ConfigurationStore(String newNamespace) {
    namespace = Objects.requireNonNull(newNamespace, "newNamespace is null");
  }

  /**
   * Returns a <code>ConfigurationStore</code> instance for the given namespace.
   *
   * @param namespace The namespace for which a <code>ConfigurationStore</code>
   * is requested.
   * @return A <code>ConfigurationStore</code> instance for the given namespace.
   */
  public static ConfigurationStore getStore(String namespace) {
    return Configuration.getInstance().getStore(namespace);
  }

  /**
   * Make the configuration data persistent.
   */
  public static void saveConfiguration() {
    Configuration.saveConfiguration();
  }

  /**
   * Returns this store's namespace.
   *
   * @return This store's namespace.
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Returns all configuration items in this configuration store, mapped by
   * their keys.
   *
   * @return All configuration items in this configuration store, mapped by
   * their keys.
   */
  public Map<String, ConfigurationItem> getConfigurationItems() {
    return new TreeMap<>(configurationItems);
  }

  /**
   * Returns all keys in this configuration store.
   *
   * @return All keys in this configuration store.
   */
  public Set<String> getKeys() {
    return new TreeSet<>(configurationItems.keySet());
  }

  /**
   * Returns <code>true</code> if this store does not contain any configuration
   * items.
   * 
   * @return <code>true</code> if this store does not contain any configuration
   * items.
   */
  public boolean isEmpty() {
    return configurationItems.isEmpty();
  }

  /**
   * Return the value associated with the given key as a boolean.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @param description A comment describing the function of the configuration
   * @param constraint Contains the constraints on the item.
   * item. May be <code>null</code>.
   * @return the value associated with the given key, or the given default
   * value.
   */
  public boolean getBoolean(String key,
                            boolean defaultValue,
                            String description, ItemConstraint constraint) {
    Objects.requireNonNull(key, "key is null");
    ConfigurationItem configItem = configurationItems.get(key);
    if (configItem == null) {
      setBoolean(key, defaultValue, description, constraint);
      return defaultValue;
    }
    else {
      return Boolean.parseBoolean(configItem.getValue());
    }
  }

  /**
   * Return the value associated with the given key as a boolean.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @return the value associated with the given key, or the given default
   * value.
   */
  public boolean getBoolean(String key, boolean defaultValue) {
    return getBoolean(key, defaultValue, null, new ItemConstraintBoolean());
  }

  /**
   * Set the boolean value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   * @param description A comment describing the function of the configuration
   * item.
   * @param constraint Contains the constraints on the item.
   */
  public void setBoolean(String key, boolean value, String description,
                         ItemConstraint constraint) {
    Objects.requireNonNull(key, "key is null");
    setConfigurationItem(key, String.valueOf(value), description,
                         constraint);
  }

  /**
   * Set the boolean value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   */
  public void setBoolean(String key, boolean value) {
    setBoolean(key, value, null, new ItemConstraintBoolean());
  }

  /**
   * Return the value associated with the given key as a byte.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid byte value.
   */
  public byte getByte(String key, byte defaultValue, String description,
                      ItemConstraint constraint)
      throws NumberFormatException {
    Objects.requireNonNull(key, "key is null");
    ConfigurationItem configItem = configurationItems.get(key);
    if (configItem == null) {
      setByte(key, defaultValue, description,constraint);
      return defaultValue;
    }
    else {
      return Byte.parseByte(configItem.getValue());
    }
  }

  /**
   * Return the value associated with the given key as a byte.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid byte value.
   */
  public byte getByte(String key, byte defaultValue)throws NumberFormatException {
    return getByte(key, defaultValue, null, new ItemConstraintByte());
  }

  /**
   * Set the byte value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   */
  public void setByte(String key, byte value, String description,
                      ItemConstraint constraint) {
    Objects.requireNonNull(key, "key is null");
    setConfigurationItem(key, String.valueOf(value), description,constraint);
  }

  /**
   * Set the byte value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   */
  public void setByte(String key, byte value) {
    setByte(key, value, null,new ItemConstraintByte());
  }

  /**
   * Return the value associated with the given key as an int.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid int value.
   */
  public int getInt(String key, int defaultValue, String description,
                    ItemConstraint constraint)
      throws NumberFormatException {
    Objects.requireNonNull(key, "key is null");
    ConfigurationItem configItem = configurationItems.get(key);
    if (configItem == null) {
      setInt(key, defaultValue, description,constraint);
      return defaultValue;
    }
    else {
      return Integer.parseInt(configItem.getValue());
    }
  }

  /**
   * Return the value associated with the given key as an int.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid int value.
   */
  public int getInt(String key, int defaultValue)throws NumberFormatException {
    return getInt(key, defaultValue, null, new ItemConstraintInteger());
  }

  /**
   * Set the int value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   */
  public void setInt(String key, int value, String description,
                     ItemConstraint constraint) {
    Objects.requireNonNull(key, "key is null");
    setConfigurationItem(key, String.valueOf(value), description,constraint);
  }

  /**
   * Set the int value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   */
  public void setInt(String key, int value) {
    setInt(key, value, null,new ItemConstraintInteger());
  }

  /**
   * Return the value associated with the given key as a short.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid short value.
   */
  public short getShort(String key, short defaultValue, String description,
                        ItemConstraint constraint)
      throws NumberFormatException {
    Objects.requireNonNull(key, "key is null");
    ConfigurationItem configItem = configurationItems.get(key);
    if (configItem == null) {
      setInt(key, defaultValue, description,constraint);
      return defaultValue;
    }
    else {
      return Short.parseShort(configItem.getValue());
    }
  }

  /**
   * Return the value associated with the given key as a short.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid short value.
   */
  public short getShort(String key, short defaultValue)throws NumberFormatException {
    return getShort(key, defaultValue, null, new ItemConstraintShort());
  }

  /**
   * Set the short value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   */
  public void setShort(String key, short value, String description,
                       ItemConstraint constraint) {
    Objects.requireNonNull(key, "key is null");
    setConfigurationItem(key, String.valueOf(value), description,constraint);
  }

  /**
   * Set the short value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   */
  public void setShort(String key, short value) {
    setShort(key, value, null,new ItemConstraintShort());
  }

  /**
   * Return the value associated with the given key as a long.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid long value.
   */
  public long getLong(String key, long defaultValue, String description,
                      ItemConstraint constraint)
      throws NumberFormatException {
    Objects.requireNonNull(key, "key is null");
    ConfigurationItem configItem = configurationItems.get(key);
    if (configItem == null) {
      setLong(key, defaultValue, description,constraint);
      return defaultValue;
    }
    else {
      return Long.parseLong(configItem.getValue());
    }
  }

  /**
   * Return the value associated with the given key as a long.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid long value.
   */
  public long getLong(String key, long defaultValue)throws NumberFormatException {
    return getLong(key, defaultValue, null, new ItemConstraintLong());
  }


  /**
   * Set the long value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   */
  public void setLong(String key, long value, String description,
                      ItemConstraint constraint) {
    Objects.requireNonNull(key, "key is null");
    setConfigurationItem(key, String.valueOf(value), description,constraint);
  }

  /**
   * Set the long value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   */
  public void setLong(String key, long value) {
    setLong(key, value, null,new ItemConstraintLong());
  }

  /**
   * Return the value associated with the given key as a float.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid float value.
   */
  public float getFloat(String key, float defaultValue, String description,
                        ItemConstraint constraint)
      throws NumberFormatException {
    Objects.requireNonNull(key, "key is null");
    ConfigurationItem configItem = configurationItems.get(key);
    if (configItem == null) {
      setFloat(key, defaultValue, description,constraint);
      return defaultValue;
    }
    else {
      return Float.parseFloat(configItem.getValue());
    }
  }

  /**
   * Return the value associated with the given key as a float.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid float value.
   */
  public float getFloat(String key, float defaultValue)
      throws NumberFormatException {
    return getFloat(key, defaultValue, null, new ItemConstraintFloat());
  }

  /**
   * Set the float value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   */
  public void setFloat(String key, float value, String description,
                       ItemConstraint constraint) {
    Objects.requireNonNull(key, "key is null");
    setConfigurationItem(key, String.valueOf(value), description,
                         constraint);
  }

  /**
   * Set the float value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   */
  public void setFloat(String key, float value) {
    setFloat(key, value, null,new ItemConstraintFloat());
  }

  /**
   * Return the value associated with the given key as a double.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid double value.
   */
  public double getDouble(String key, double defaultValue,
                          String description,
                          ItemConstraint constraint) throws NumberFormatException {
    Objects.requireNonNull(key, "key is null");
    ConfigurationItem configItem = configurationItems.get(key);
    if (configItem == null) {
      setDouble(key, defaultValue, description,constraint);
      return defaultValue;
    }
    else {
      return Double.parseDouble(configItem.getValue());
    }
  }

  /**
   * Return the value associated with the given key as a double.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @return the value associated with the given key, or the given default
   * value.
   * @throws NumberFormatException if the value associated with the given key is
   * not a valid double value.
   */
  public double getDouble(String key, double defaultValue)
      throws NumberFormatException {
    return getDouble(key, defaultValue, null, new ItemConstraintDouble());
  }

  /**
   * Set the double value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   */
  public void setDouble(String key, double value, String description,
                        ItemConstraint constraint) {
    Objects.requireNonNull(key, "key is null");
    setConfigurationItem(key, String.valueOf(value), description, constraint);
  }

  /**
   * Set the double value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   */
  public void setDouble(String key, double value) {
    setDouble(key, value, null,new ItemConstraintDouble());
  }

  /**
   * Return the value associated with the given key as a String.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   * @return the value associated with the given key, or the given default
   * value.
   */
  public String getString(String key, String defaultValue,
                          String description,ItemConstraint constraint) {
    Objects.requireNonNull(key, "key is null");
    ConfigurationItem configItem = configurationItems.get(key);
    if (configItem == null) {
      setString(key, defaultValue, description,constraint);
      return defaultValue;
    }
    else {
      return configItem.getValue();
    }
  }

  /**
   * Return the value associated with the given key as a String.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @return the value associated with the given key, or the given default
   * value.
   */
  public String getString(String key, String defaultValue) {
    return getString(key, defaultValue, null, new ItemConstraintString());
  }

  /**
   * Set the String value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   * @param description A description for this configuration item.
   * @param constraint Contains the constraints on the item.
   */
  public void setString(String key, String value, String description,
                        ItemConstraint constraint) {
    Objects.requireNonNull(key, "key is null");
    setConfigurationItem(key, value, description, constraint);
  }

  /**
   * Set the String value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   */
  public void setString(String key, String value) {
    setString(key, value, null, new ItemConstraintString());
  }

  /**
   * Return the value associated with the given key as a String.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @param description A description for this configuration item.
   * @param enumClass The Enum class to which the enum value belongs.
   * @return the value associated with the given key, or the given default
   * value.
   */
  public String getEnum(String key,
                        String defaultValue,
                        String description,
                        Class<? extends Enum<?>> enumClass) {
    Objects.requireNonNull(key, "key is null");
    ConfigurationItem configItem = configurationItems.get(key);
    if (configItem == null) {
      setEnum(key, defaultValue, description, enumClass);
      return defaultValue;
    }
    else {
      return configItem.getValue();
    }
  }

  /**
   * Return the value associated with the given key as a String.
   * If no value is associated with the key, the given default value is set and
   * returned.
   *
   * @param key The key specifying the value to return.
   * @param defaultValue The default value to return.
   * @param enumClass The enum class to return.
   * @return the value associated with the given key, or the given default
   * value.
   */
  public String getEnum(String key,
                        String defaultValue,
                        Class<? extends Enum<?>> enumClass) {
    return getEnum(key, defaultValue, null, enumClass);
  }

  /**
   * Set the String value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   * @param enumClass The Enum class to which the enum value belongs.
   * @param description A description for this configuration item.
   */
  public void setEnum(String key, String value, String description,
                      Class<? extends Enum<?>> enumClass) {
    Objects.requireNonNull(key, "key is null");
    setConfigurationItem(key, value, description,
                         new ItemConstraintEnum(enumClass));
  }

  /**
   * Set the String value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   * @param values The set of values of the enum.
   * @param description A description for this configuration item.
   */
  public void setEnum(String key, String value, String description,
                      Set<String> values) {
    Objects.requireNonNull(key, "key is null");
    setConfigurationItem(key, value, description,
                         new ItemConstraintEnum(values));
  }

  /**
   * Set the String value associated with the given key.
   * If the given key is already associated with a value, it is replaced by the
   * given one (regardless of its previous type).
   *
   * @param key The key specifying the value to set.
   * @param value The value to associate with the key.
   * @param enumClass The Enum class to which the enum value belongs.
   */
  public void setEnum(String key, String value,
                                  Class<? extends Enum<?>> enumClass) {
    setEnum(key, value, null, enumClass);
  }

  /**
   * Sets a configuration item's value to the given one, or creates a new one
   * with the given content.
   * 
   * @param key The key of the configuration item to be set.
   * @param value The value of the configuration item to be set.
   * @param description A comment describing the configuration item.
   * @param itemConstraint A constraint containing the data type of an Item.
   */
  void setConfigurationItem(String key,
                            String value,
                            String description,
                            ItemConstraint itemConstraint) {
    Objects.requireNonNull(key, "key is null");
    Objects.requireNonNull(value, "value is null");
    String validDescription = description == null ? "" : description;

    ConfigurationItem configItem = configurationItems.get(key);
    if (configItem == null) {
      configItem = new ConfigurationItem(namespace,
                                         key,
                                         validDescription,
                                         itemConstraint,
                                         value);
      configurationItems.put(key, configItem);
    }
    else {
      configItem.setValue(value);
      configItem.setConstraint(itemConstraint);
      configItem.setDescription(validDescription);
    }
  }

  /**
   * Removes the configuration item with the specified key if it is present.
   * @param key Key of the item 
   * @return The removed ConfigurationItem or null if there was no item with the
   *          key.
   */
  public ConfigurationItem removeItem(String key) {
    Objects.requireNonNull(key, "key is null");
    return configurationItems.remove(key);
  }

  /**
   * A Comparator that compares the given stores' namespaces.
   */
  public static class NamespaceComparator
      implements Comparator<ConfigurationStore> {

    /**
     * Creates a new NamespaceComparator.
     */
    public NamespaceComparator() {
      // Do nada.
    }

    @Override
    public int compare(ConfigurationStore o1, ConfigurationStore o2) {
      return o1.namespace.compareTo(o2.namespace);
    }
  }
}
