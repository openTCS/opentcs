// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * Binds JSON strings to objects and vice versa.
 * Optionally, a filter can be applied before serializing to JSON.
 */
public class JsonBinder {

  /**
   * Maps between objects and their JSON representations.
   */
  private final ObjectMapper objectMapper
      = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  private Function<JsonNode, JsonNode> filter = Function.identity();

  /**
   * Creates a new instance.
   */
  public JsonBinder() {
  }

  /**
   * Sets the filter that will be applied before serializing to JSON.
   *
   * @param filter The filter to be applied.
   */
  public void setFilter(
      @Nonnull
      Function<JsonNode, JsonNode> filter
  ) {
    this.filter = requireNonNull(filter, "filter");
  }

  /**
   * Maps the given JSON string to an object.
   *
   * @param <T> The type of object to map to.
   * @param jsonString The JSON string.
   * @param clazz The type of object to map to.
   * @return The object created from the JSON string.
   * @throws IllegalArgumentException In case there was a problem mapping the given object from
   * JSON.
   */
  public <T> T fromJson(String jsonString, Class<T> clazz)
      throws IllegalArgumentException {
    try {
      return objectMapper.readValue(jsonString, clazz);
    }
    catch (IOException exc) {
      throw new IllegalArgumentException("Could not parse JSON input", exc);
    }
  }

  /**
   * Applies the filter set via {@link #setFilter(Function)} and maps the given
   * object to a JSON string.
   *
   * @param object The object to be mapped.
   * @return The JSON string representation of the object.
   * @throws IllegalArgumentException In case there was a problem mapping the given object to JSON.
   */
  public String toJson(Object object)
      throws IllegalArgumentException {
    try {
      return objectMapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(filter.apply(objectMapper.valueToTree(object)));
    }
    catch (JsonProcessingException exc) {
      throw new IllegalArgumentException("Could not produce JSON output", exc);
    }
  }
}
