/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a statistics log file and creates log record instances from it.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatisticsLogParser {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StatisticsLogParser.class);

  /**
   * Prevents undesired instantiation.
   */
  private StatisticsLogParser() {
  }

  /**
   * Parses the given log file and returns a list of records contained in it.
   *
   * @param inputFile The file to be parsed.
   * @return A list of records contained in the file.
   * @throws FileNotFoundException If the given file was not found.
   * @throws IOException If there was a problem reading the file.
   */
  public static List<StatisticsRecord> parseLog(File inputFile)
      throws FileNotFoundException, IOException {
    requireNonNull(inputFile, "inputFile");

    List<StatisticsRecord> result = new LinkedList<>();
    try (BufferedReader inputReader = new BufferedReader(
        new InputStreamReader(new FileInputStream(inputFile), Charset.forName("UTF-8")))) {
      String inputLine = inputReader.readLine();
      while (inputLine != null) {
        StatisticsRecord record = StatisticsRecord.parseRecord(inputLine);
        if (record != null) {
          result.add(record);
        }
        inputLine = inputReader.readLine();
      }
    }
    catch (IOException exc) {
      LOG.warn("Exception parsing input file", exc);
      throw exc;
    }

    return result;
  }
}
