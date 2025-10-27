// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.PlantModelCreationTO;

/**
 * Test for {@link ModelParser}.
 */
public class ModelParserTest {

  private static final String WRITE_PATH = "src/test/java/org/opentcs/util/persistence/";
  private final ModelParser modelParser = new ModelParser();

  @Test
  public void readModelV7AndWriteLatestVersion()
      throws URISyntaxException,
        IOException {
    PlantModelCreationTO parsedModel = modelParser.readModel(
        new File(
            Thread.currentThread().getContextClassLoader()
                .getResource("org/opentcs/util/persistence/PlantModelV7.sample.xml").toURI()
        )
    );

    File writtenModel = new File(
        WRITE_PATH + "ModelParserTest.readModelV7AndWriteLatestVersion.received.xml"
    );
    modelParser.writeModel(parsedModel, writtenModel);

    Approvals.verify(writtenModel);
  }

  @Test
  public void readModelV6AndWriteLatestVersion()
      throws URISyntaxException,
        IOException {
    PlantModelCreationTO parsedModel = modelParser.readModel(
        new File(
            Thread.currentThread().getContextClassLoader()
                .getResource("org/opentcs/util/persistence/PlantModelV6.sample.xml").toURI()
        )
    );

    File writtenModel = new File(
        WRITE_PATH + "ModelParserTest.readModelV6AndWriteLatestVersion.received.xml"
    );
    modelParser.writeModel(parsedModel, writtenModel);

    Approvals.verify(writtenModel);
  }
}
