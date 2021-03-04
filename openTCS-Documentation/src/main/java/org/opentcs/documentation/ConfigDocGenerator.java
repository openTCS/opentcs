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
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a file documenting an application's configuration entries.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ConfigDocGenerator {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ConfigDocGenerator.class);

  private ConfigDocGenerator() {
  }

  /**
   * Generates a file documenting an application's configuration entries.
   *
   * @param args The first argument is expected to be the name of the file to write the
   * documentation to. The second argument is expected to be the fully qualified name of the
   * configuration interface.
   * @throws Exception In case there was a problem processing the input.
   */
  public static void main(String[] args)
      throws Exception {
    checkArgument(args.length >= 2,
                  "Expected at least 2 arguments, got %s.",
                  args.length);

    for (int i = 1; i < args.length; i++) {
      Class<?> clazz = ConfigDocGenerator.class.getClassLoader().loadClass(args[1]);

      checkArgument(clazz.isAnnotationPresent(ConfigurationPrefix.class),
                    "Missing prefix annotation in %s.",
                    clazz.getName());

      SortedSet<Entry> configurationEntries = new TreeSet<>();
      for (Method method : clazz.getMethods()) {
        if (!method.isAnnotationPresent(ConfigurationEntry.class)) {
          continue;
        }

        ConfigurationEntry annotation = method.getAnnotation(ConfigurationEntry.class);
        configurationEntries.add(new Entry(method.getName(),
                                           annotation.type(),
                                           annotation.description(),
                                           annotation.orderKey())
        );
      }

      checkArgument(!configurationEntries.isEmpty(),
                    "No configuration keys in {}.",
                    clazz.getName());

      generateFile(args[0],
                   clazz.getAnnotation(ConfigurationPrefix.class).value(),
                   configurationEntries);
    }
  }

  /**
   * Writes the configurationEntries to a file using AsciiDoc syntax for a table.
   *
   * @param filePath The output file path to wirte to.
   * @param configurationPrefix The prefix of the configuration entries.
   * @param configurationEntries The set of all configuration entries.
   */
  private static void generateFile(String filePath,
                                   String configurationPrefix,
                                   Collection<Entry> configurationEntries) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
      writeTableHeader(writer, configurationPrefix);
      configurationEntries.forEach(entry -> writeTableContent(writer, entry));
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

    public Entry(String name,
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
