/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;

/**
 * Binds JSON strings to objects and vice versa.
 */
public class JsonBinder {

  /**
   * Maps between objects and their JSON representations.
   */
  private final ObjectMapper objectMapper
      = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  /**
   * Creates a new instance.
   */
  public JsonBinder() {
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
   * (An IllegalArgumentException is mapped to HTTP status code 400, indicating a client error.)
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
   * Maps the given object to a JSON string.
   *
   * @param object The object to be mapped.
   * @return The JSON string representation of the object.
   * @throws IllegalStateException In case there was a problem mapping the given object to JSON.
   * (An IllegalStateException is mapped to HTTP status code 500, indicating an internal error.)
   */
  public String toJson(Object object)
      throws IllegalStateException {
    try {
      return objectMapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(object);
    }
    catch (JsonProcessingException exc) {
      throw new IllegalStateException("Could not produce JSON output", exc);
    }
  }

  /**
   * Maps the given throwable to a JSON string.
   *
   * @param t The throwable to be mapped.
   * @return A JSON string for the given throwable, consisting of a single-element array containing
   * the throwable's message.
   * @throws IllegalStateException In case there was a problem mapping the given object to JSON.
   * (An IllegalStateException is mapped to HTTP status code 500, indicating an internal error.)
   */
  public String toJson(Throwable t)
      throws IllegalStateException {
    try {
      return objectMapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(objectMapper.createArrayNode().add(t.getMessage()));
    }
    catch (JsonProcessingException exc) {
      throw new IllegalStateException("Could not produce JSON output", exc);
    }
  }

}
