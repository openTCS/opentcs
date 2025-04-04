// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods for extracting values from {@link Map}s.
 */
public class MapValueExtractor {

  private static final Logger LOG = LoggerFactory.getLogger(MapValueExtractor.class);

  /**
   * Creates a new instance.
   */
  public MapValueExtractor() {
  }

  /**
   * Extracts a string value from the given map.
   *
   * @param key The key of the map entry to extract the value from.
   * @param map The map to extract the value from.
   * @return An {@link Optional} containing the extracted value as a string, or an empty
   * {@link Optional} if the map does not contain an entry with the specified key.
   */
  public Optional<String> extractString(
      @Nonnull
      String key,
      @Nonnull
      Map<String, String> map
  ) {
    requireNonNull(key, "key");
    requireNonNull(map, "map");

    return Optional.ofNullable(map.get(key));
  }

  /**
   * Extracts an integer value from the given map.
   *
   * @param key The key of the map entry to extract the value from.
   * @param map The map to extract the value from.
   * @return An {@link Optional} containing the extracted value parsed as an integer, or an empty
   * {@link Optional} if the map does not contain an entry with the specified key, or it's value
   * cannot be parsed.
   */
  public Optional<Integer> extractInteger(
      @Nonnull
      String key,
      @Nonnull
      Map<String, String> map
  ) {
    requireNonNull(key, "key");
    requireNonNull(map, "map");

    return Optional.ofNullable(map.get(key))
        .map(value -> {
          try {
            return Integer.valueOf(value);
          }
          catch (NumberFormatException e) {
            LOG.warn("Could not parse integer value for key '{}': {}", key, value);
            return null;
          }
        });
  }

  /**
   * Extracts a long value from the given map.
   *
   * @param key The key of the map entry to extract the value from.
   * @param map The map to extract the value from.
   * @return An {@link Optional} containing the extracted value parsed as a long, or an empty
   * {@link Optional} if the map does not contain an entry with the specified key, or it's value
   * cannot be parsed.
   */
  public Optional<Long> extractLong(
      @Nonnull
      String key,
      @Nonnull
      Map<String, String> map
  ) {
    requireNonNull(key, "key");
    requireNonNull(map, "map");

    return Optional.ofNullable(map.get(key))
        .map(value -> {
          try {
            return Long.valueOf(value);
          }
          catch (NumberFormatException e) {
            LOG.warn("Could not parse long value for key '{}': {}", key, value);
            return null;
          }
        });
  }

  /**
   * Extracts a double value from the given map.
   *
   * @param key The key of the map entry to extract the value from.
   * @param map The map to extract the value from.
   * @return An {@link Optional} containing the extracted value parsed as a double, or an empty
   * {@link Optional} if the map does not contain an entry with the specified key, or it's value
   * cannot be parsed.
   */
  public Optional<Double> extractDouble(
      @Nonnull
      String key,
      @Nonnull
      Map<String, String> map
  ) {
    requireNonNull(key, "key");
    requireNonNull(map, "map");

    return Optional.ofNullable(map.get(key))
        .map(value -> {
          try {
            return Double.valueOf(value);
          }
          catch (NumberFormatException e) {
            LOG.warn("Could not parse double value for key '{}': {}", key, value);
            return null;
          }
        });
  }

  /**
   * Extracts a boolean value from the given map.
   *
   * @param key The key of the map entry to extract the value from.
   * @param map The map to extract the value from.
   * @return An {@link Optional} containing the extracted value parsed as a boolean, or an empty
   * {@link Optional} if the map does not contain an entry with the specified key, or it's value
   * cannot be parsed.
   */
  public Optional<Boolean> extractBoolean(
      @Nonnull
      String key,
      @Nonnull
      Map<String, String> map
  ) {
    requireNonNull(key, "key");
    requireNonNull(map, "map");

    return Optional.ofNullable(map.get(key))
        .map(Boolean::valueOf);
  }

  /**
   * Extracts an enum value from the given map.
   *
   * @param key The key of the map entry to extract the value from.
   * @param map The map to extract the value from.
   * @param enumClass The class of the enum to extract.
   * @return An {@link Optional} containing the extracted value mapped to an enum of the
   * specified class, or an empty {@link Optional} if the map does not contain an entry with the
   * specified key, or it's value cannot be mapped.
   * @param <E> The type of the enum.
   */
  public <E extends Enum<E>> Optional<E> extractEnum(
      @Nonnull
      String key,
      @Nonnull
      Map<String, String> map,
      @Nonnull
      Class<E> enumClass
  ) {
    requireNonNull(key, "key");
    requireNonNull(map, "map");
    requireNonNull(enumClass, "enumClass");

    return Optional.ofNullable(map.get(key))
        .map(value -> {
          try {
            return Enum.valueOf(enumClass, value);
          }
          catch (IllegalArgumentException e) {
            LOG.warn("Could not map enum value for key '{}': {}", key, value);
            return null;
          }
        });
  }
}
