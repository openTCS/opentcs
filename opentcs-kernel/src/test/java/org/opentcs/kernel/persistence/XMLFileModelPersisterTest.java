// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.opentcs.TestEnvironment;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.util.persistence.ModelParser;

/**
 * Tests the {@link XMLFileModelPersister}.
 */
public class XMLFileModelPersisterTest {

  /**
   * The name of the test model.
   */
  private static final String MODEL_NAME = "Testmodel";

  /**
   * Reads and writes the model.
   */
  private ModelParser modelParser;

  /**
   * The persister instance for testing.
   */
  private XMLFileModelPersister persister;

  /**
   * Captures the first argument when the method writeModelMethod of the modelParser is called.
   */
  @Captor
  private ArgumentCaptor<PlantModelCreationTO> modelCaptor;
  /**
   * Captures the second argument when the method writeModelMethod of the modelParser is called.
   */

  @Captor
  private ArgumentCaptor<File> fileCaptor;

  @BeforeEach
  void setUp()
      throws IOException {
    modelParser = mock(ModelParser.class);
    persister = new XMLFileModelPersister(
        TestEnvironment.getKernelHomeDirectory(),
        modelParser
    );
    modelCaptor = ArgumentCaptor.forClass(PlantModelCreationTO.class);
    fileCaptor = ArgumentCaptor.forClass(File.class);
  }

  @Test
  void createXmlFileInGivenDirectory()
      throws IOException {
    persister.saveModel(createTestModel(MODEL_NAME));

    Mockito.verify(modelParser).writeModel(modelCaptor.capture(), fileCaptor.capture());

    assertEquals(
        TestEnvironment.getKernelHomeDirectory(),
        fileCaptor.getValue().getParentFile().getParentFile()
    );
    assertEquals(".xml", getFileExtension(fileCaptor.getValue()));
  }

  private PlantModelCreationTO createTestModel(String name) {
    return new PlantModelCreationTO(name)
        .withPoint(new PointCreationTO("testPointName"))
        .withVehicle(new VehicleCreationTO("testVehicleName"));
  }

  private String getFileExtension(File file) {
    return file.getName().substring(file.getName().lastIndexOf("."));
  }

}
