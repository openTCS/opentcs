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
    String prefix = extractPrefix(clazz);

    SortedSet<Entry> configurationEntries = new TreeSet<>();
    for (Method method : clazz.getMethods()) {
      configurationEntries.add(extractEntry(method, prefix));
    }

    checkArgument(!configurationEntries.isEmpty(),
                  "No configuration keys in {}.",
                  clazz.getName());

    generateFile(outputFilePath, configurationEntries);
  }

  private static String extractPrefix(Class<?> clazz) {
    ConfigurationPrefix annotation = clazz.getAnnotation(ConfigurationPrefix.class);
    checkArgument(annotation != null, "Missing prefix annotation at class %s", clazz.getName());
    return annotation.value();
  }

  private static Entry extractEntry(Method method, String prefix) {
    ConfigurationEntry annotation = method.getAnnotation(ConfigurationEntry.class);
    checkArgument(annotation != null, "Missing entry annotation at method %s", method.getName());
    return new Entry(prefix,
                     method.getName(),
                     annotation.type(),
                     changesAppliedWhen(annotation.changesApplied()),
                     annotation.description(),
                     annotation.orderKey());
  }

  /**
   * Writes the configuration entries to a file using AsciiDoc syntax.
   *
   * @param outputFilePath The output file path to write to.
   * @param configurationEntries The configuration entries.
   */
  private static void generateFile(String outputFilePath,
                                   Collection<Entry> configurationEntries) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath, true))) {
      for (Entry entry : configurationEntries) {
        writeEntry(writer, entry);
      }

      writer.println();
    }
    catch (IOException ex) {
      LOG.error("", ex);
    }
  }

  private static void writeEntry(final PrintWriter writer, Entry entry) {
    writer.print("`");
    writer.print(entry.prefix);
    writer.print('.');
    writer.print(entry.name);
    writer.println("`::");

    writer.print("* Type: ");
    writer.println(entry.type);

    writer.print("* Trigger for changes to be applied: ");
    writer.println(entry.changesApplied);

    writer.print("* Description: ");
    writer.println(String.join(" +\n", entry.description));
  }

  private static String changesAppliedWhen(ConfigurationEntry.ChangesApplied changesApplied) {
    switch (changesApplied) {
      case ON_APPLICATION_START:
        return "on application start";
      case ON_NEW_PLANT_MODEL:
        return "when/after plant model is loaded";
      case INSTANTLY:
        return "instantly";
      default:
      case UNSPECIFIED:
        return "unspecified";
    }
  }

  /**
   * Describes a configuration entry.
   */
  private static class Entry
      implements Comparable<Entry> {

    /**
     * The prefix of this configuration entry.
     */
    private final String prefix;
    /**
     * The name of this configuration entry.
     */
    private final String name;
    /**
     * A description for the data type of this configuration entry.
     */
    private final String type;
    /**
     * Whether a change of the configuration value requires a restart of the application.
     */
    private final String changesApplied;
    /**
     * A description for this configuration entry.
     */
    private final String[] description;
    /**
     * A key for sorting entries.
     */
    private final String orderKey;

    Entry(String prefix,
          String name,
          String type,
          String changesApplied,
          String[] description,
          String orderKey) {
      this.prefix = prefix;
      this.name = name;
      this.type = type;
      this.changesApplied = changesApplied;
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
