// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkState;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Validates JSON inputs against registered schemas.
 */
public class JsonValidator {

  private final Map<Class<?>, Schema> schemasByClass;

  /**
   * Creates a new instance.
   *
   * @param schemaReadersByClass Readers of JSON schemas, mapped by the JSON binding classes the
   * schemas belong to.
   * @throws IllegalArgumentException If there was any problem reading a schema from a given reader.
   */
  public JsonValidator(
      @Nonnull
      Map<Class<?>, Reader> schemaReadersByClass
  )
      throws IllegalArgumentException {
    requireNonNull(schemaReadersByClass, "schemaReadersByClass");

    schemasByClass = schemaReadersByClass.entrySet().stream()
        .collect(
            Collectors.toMap(entry -> entry.getKey(), entry -> createSchema(entry.getValue()))
        );
  }

  /**
   * Validates the given JSON input against a schema registered for the given JSON binding class.
   *
   * @param json The JSON input.
   * @param clazz The JSON binding class.
   * @throws IllegalStateException If a schema is not registered for the given class.
   * @throws IllegalArgumentException If the given JSON input is not valid for the schema registered
   * for the given class.
   */
  public void validate(
      @Nonnull
      String json,
      @Nonnull
      Class<?> clazz
  )
      throws IllegalStateException,
        IllegalArgumentException {
    requireNonNull(json, "json");
    requireNonNull(clazz, "clazz");

    Schema schema = schemasByClass.get(clazz);
    checkState(schema != null, "Schema not registered for class %s", clazz.getName());

    try {
      schema.validate(new JSONObject(json));
    }
    catch (ValidationException e) {
      throw new IllegalArgumentException(
          e.getMessage() + '\n' + String.join("\n", e.getAllMessages()),
          e
      );
    }
    catch (JSONException e) {
      throw new IllegalArgumentException("Invalid JSON input", e);
    }
  }

  private static Schema createSchema(
      @Nonnull
      Reader schemaReader
  )
      throws IllegalArgumentException {
    try (schemaReader) {
      return SchemaLoader.load(new JSONObject(new JSONTokener(schemaReader)));
    }
    catch (IOException e) {
      throw new IllegalArgumentException("Exception reading JSON schema", e);
    }
  }
}
