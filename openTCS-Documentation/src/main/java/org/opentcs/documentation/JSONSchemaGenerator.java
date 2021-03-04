/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jsonSchema.customProperties.ValidationSchemaFactoryWrapper;
import static com.google.common.base.Preconditions.checkArgument;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Generates JSON Schema files for a given list of classes.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class JSONSchemaGenerator {

  /**
   * Prevents instantiation.
   */
  private JSONSchemaGenerator() {
  }

  /**
   * Generates a JSON Schema files for the given list of classes and writes them to the directory
   * with the given name.
   *
   * @param args The first argument is expected to be the name of the directory to write the schema
   * files to. All following arguments are expected to be fully qualified names of classes to
   * generate schemas for.
   * @throws Exception If an exception occurs.
   */
  public static void main(String[] args)
      throws Exception {
    checkArgument(args.length >= 2,
                  "Expected at least 2 arguments, got %s.",
                  args.length);

    File outputFile = new File(args[0]);
    outputFile.mkdir();

    List<Class<?>> classes = new LinkedList<>();
    for (int i = 1; i < args.length; i++) {
      classes.add(JSONSchemaGenerator.class.getClassLoader().loadClass(args[i]));
    }

    ValidationSchemaFactoryWrapper visitor = new ValidationSchemaFactoryWrapper();
    ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(SerializationFeature.INDENT_OUTPUT);

    for (Class<?> clazz : classes) {
      mapper.acceptJsonFormatVisitor(clazz, visitor);
      JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
      JsonNode schemaNode = generator.generateJsonSchema(clazz);
//      JsonSchema schemaNode = visitor.finalSchema();
      String schema = mapper.writeValueAsString(schemaNode);
      File schemaFile = new File(outputFile, fileName(clazz));
      schemaFile.createNewFile();
      writeToFile(schemaFile, schema);
    }
  }

  private static void writeToFile(File file, String content)
      throws IOException {
    try (FileWriter writer = new FileWriter(file, false)) {
      writer.write(content);
    }
  }

  private static String fileName(Class<?> clazz) {
    return clazz.getName() + ".json";
  }
}
