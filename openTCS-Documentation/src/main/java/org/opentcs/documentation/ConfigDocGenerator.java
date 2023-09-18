/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;
import static org.opentcs.util.Assertions.checkArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a file documenting an application's configuration entries.
 */
public class ConfigDocGenerator {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ConfigDocGenerator.class);

  /**
   * Prevents instantiation.
   */
  private ConfigDocGenerator() {
  }

  /**
   * Generates a file documenting an application's configuration entries.
   *
   * @param args The arguments are expected to be pairs of (1) the fully qualified name of a
   * configuration interface and (2) the name of a file to write the documentation to.
   * @throws Exception In case there was a problem processing the input.
   */
  public static void main(String[] args)
      throws Exception {
    checkArgument(args.length >= 2, "Expected at least 2 arguments, got %d.", args.length);
    checkArgument(args.length % 2 == 0, "Expected even number of arguments, got %d.", args.length);

    for (int i = 0; i < args.length; i += 2) {
      processConfigurationInterface(args[i], args[i + 1]);
    }
  }

  private static void processConfigurationInterface(String className, String outputFilePath)
      throws ClassNotFoundException {
    Class<?> clazz = ConfigDocGenerator.class.getClassLoader().loadClass(className);

    SortedSet<Entry> configurationEntries = new TreeSet<>();
    for (Method method : clazz.getMethods()) {
      configurationEntries.add(extractEntry(method));
    }

    checkArgument(!configurationEntries.isEmpty(),
                  "No configuration keys in {}.",
                  clazz.getName());

    generateFile(outputFilePath, extractPrefix(clazz), configurationEntries);
  }

  private static String extractPrefix(Class<?> clazz) {
    ConfigurationPrefix annotation = clazz.getAnnotation(ConfigurationPrefix.class);
    checkArgument(annotation != null, "Missing prefix annotation at class %s", clazz.getName());
    return annotation.value();
  }

  private static Entry extractEntry(Method method) {
    ConfigurationEntry annotation = method.getAnnotation(ConfigurationEntry.class);
    checkArgument(annotation != null, "Missing entry annotation at method %s", method.getName());
    return new Entry(method.getName(),
                     annotation.type(),
                     annotation.description(),
                     annotation.orderKey());
  }

  /**
   * Writes the configurationEntries to a file using AsciiDoc syntax for a table.
   *
   * @param outputFilePath The output file path to write to.
   * @param configurationPrefix The configuration entries' prefix.
   * @param configurationEntries The configuration entries.
   */
  private static void generateFile(String outputFilePath,
                                   String configurationPrefix,
                                   Collection<Entry> configurationEntries) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath, true))) {
      writeTableHeader(writer, configurationPrefix);

      for (Entry entry : configurationEntries) {
        writeTableContent(writer, entry);
      }

      writer.println("|===");
      writer.println();
    }
    catch (IOException ex) {
      LOG.error("", ex);
    }
  }

  private static void writeTableHeader(final PrintWriter writer, String configurationPrefix) {
    writer.print(".Configuration options with prefix '");
    writer.print(configurationPrefix);
    writer.println('\'');
    writer.println("[cols=\"2,1,3\", options=\"header\"]");
    writer.println("|===");
    writer.println("|Key");
    writer.println("|Type");
    writer.println("|Description");
    writer.println();
  }

  private static void writeTableContent(final PrintWriter writer, Entry entry) {
    writer.print('|');
    writer.println(entry.name);

    writer.print('|');
    writer.println(entry.type);

    writer.print('|');
    for (int i = 0; i < (entry.description.length - 1); i++) {
      writer.print(entry.description[i]);
      writer.println(" +");
    }
    writer.println(entry.description[entry.description.length - 1]);
    writer.println();
  }

  /**
   * Describes a configuration entry.
   */
  private static class Entry
      implements Comparable<Entry> {

    /**
     * The name of this configuration entry.
     */
    private final String name;
    /**
     * A description for the data type of this configuration entry.
     */
    private final String type;
    /**
     * A description for this configuration entry.
     */
    private final String[] description;
    /**
     * A key for sorting entries.
     */
    private final String orderKey;

    Entry(String name,
          String type,
          String[] description,
          String orderKey) {
      this.name = name;
      this.type = type;
      this.description = description;
      this.orderKey = orderKey;
    }

    @Override
    public int compareTo(Entry entry) {
      int result = this.orderKey.compareTo(entry.orderKey);
      if (result == 0) {
        result = this.name.compareTo(entry.name);
      }
      return result;
    }
  }
}
