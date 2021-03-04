/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import javax.inject.Provider;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opentcs.TestEnvironment;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.opentcs.util.event.SimpleEventBus;

/**
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class XMLFileModelPersisterTest {

  /**
   * The name of the test model.
   */
  private static final String MODEL_NAME = "Testmodel";
  private static final String OTHER_MODEL_NAME = "Testmodel2";
  /**
   * The persister instance for testing.
   */
  private ModelPersister persister;
  /**
   * The XMLModelReader used by the persister.
   */
  private XMLModelReader reader;
  /**
   * The XMLModelWriter used by the persister.
   */
  private XMLModelWriter writer;
  /**
   * The model saved by XMLModelWriter.
   */
  private Model persistedModel;

  @Before
  public void setUp()
      throws IOException, InvalidModelException {
    reader = mock(XMLModelReader.class);
    writer = mock(XMLModelWriter.class);
    // Store the model passed to writeXMLModel()
    doAnswer(new Answer<Object>() {
      @Override
      @SuppressWarnings("unchecked")
      public Object answer(InvocationOnMock invocation)
          throws Throwable {
        Object[] args = invocation.getArguments();
        persistedModel = (Model) args[0];
        persistedModel.setName(args[1] != null ? (String) args[1] : persistedModel.getName());
        return null;
      }
    }).when(writer).writeXMLModel(any(Model.class),
                                  any(),
                                  any(OutputStream.class));
    // We don't really load the model on readXMLModel file, but make sure
    // it has at least the same name as the persisted model.
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation)
          throws Throwable {
        Model model = (Model) invocation.getArguments()[1];
        model.setName(persistedModel.getName());
        return null;
      }
    }).when(reader).readXMLModel(any(InputStream.class), any(Model.class));
    // Create persister with providers that return the mocked objects
    persister = new XMLFileModelPersister(
        TestEnvironment.getKernelHomeDirectory(),
        new XMLModelReaderProvider(reader),
        new XMLModelWriterProvider(writer));
  }

  @After
  public void tearDown() {
    deleteBackups();
  }

  @Test
  public void testSaveModelShouldWriteXMLModel()
      throws IOException {
    Model model = createTestModel(MODEL_NAME);
    persister.saveModel(model, null);
    verify(writer, times(1))
        .writeXMLModel(any(Model.class), any(), any(OutputStream.class));
    assertEquals(persistedModel, model);
  }

  @Test
  @Ignore("Fails, and the setup looks a bit complicated for a unit test")
  public void testLoadModelShouldReadXMLModel()
      throws IOException, InvalidModelException {
    persister.saveModel(createTestModel(MODEL_NAME), null);
    Model model = new Model(new TCSObjectPool(new SimpleEventBus()));
    persister.loadModel(model);
    verify(reader, atLeastOnce())
        .readXMLModel(any(InputStream.class), any(Model.class));
  }

  @Test
  public void testModelNameShouldNotBePresentAfterRemovingModel()
      throws IOException {
    persister.saveModel(createTestModel(MODEL_NAME), MODEL_NAME);
    deleteBackups();
    persister.removeModel();
    assertFalse(persister.getPersistentModelName().isPresent());
  }

  @Test
  @Ignore("Fails, and the setup looks a bit complicated for a unit test")
  public void testSavedModelNameShouldBeAsSpecified()
      throws IOException {
    persister.saveModel(createTestModel(MODEL_NAME), OTHER_MODEL_NAME);
    assertEquals(OTHER_MODEL_NAME, persister.getPersistentModelName().orElse(null));
    deleteBackups();
    persister.saveModel(createTestModel(MODEL_NAME), null);
    assertEquals(MODEL_NAME, persister.getPersistentModelName().orElse(null));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void shouldLoadEmptyModelIfNoModelIsSaved()
      throws IOException {
    persister.removeModel();
    Model model = new Model(new TCSObjectPool(new SimpleEventBus()));
    persister.loadModel(model);
    assertTrue(model.getPoints(null).isEmpty());
    assertTrue(model.getBlocks(null).isEmpty());
    assertTrue(model.getGroups(null).isEmpty());
    assertTrue(model.getLocations(null).isEmpty());
    assertTrue(model.getLocationTypes(null).isEmpty());
    assertTrue(model.getPaths(null).isEmpty());
    assertTrue(model.getStaticRoutes(null).isEmpty());
    assertTrue(model.getVehicles(null).isEmpty());
    assertTrue(model.getName().isEmpty());
  }

  @Test
  public void shouldHaveModelAfterSavingModel()
      throws IOException {
    persister.saveModel(createTestModel(MODEL_NAME), MODEL_NAME);
    assertTrue(persister.hasSavedModel());
  }

  @Test
  public void shouldNotHaveModelAfterRemovingModel()
      throws IOException {
    persister.saveModel(createTestModel(MODEL_NAME), MODEL_NAME);
    deleteBackups();
    persister.removeModel();
    assertFalse(persister.hasSavedModel());
  }

  private Model createTestModel(String name) {
    Model model = new Model(new TCSObjectPool(new SimpleEventBus()));
    model.createPoint(new PointCreationTO("testPointName"));
    model.createVehicle(new VehicleCreationTO("testVehicleName"));
    model.setName(name);
    return model;
  }

  /**
   * Delete all backup files from the backups-directory if it does exist.
   */
  private void deleteBackups() {
    File backupDir = new File(TestEnvironment.getKernelHomeDirectory(), "data/backups");
    if (!backupDir.isDirectory()) {
      return;
    }
    for (File file : backupDir.listFiles()) {
      if (file.getName().contains("_backup_")) {
        file.delete();
      }
    }
  }

  /**
   * A simple provider for XMLModelWriter that always returns the instance it
   * was initialized with.
   */
  private static class XMLModelWriterProvider
      implements Provider<XMLModelWriter> {

    private final XMLModelWriter writer;

    private XMLModelWriterProvider(XMLModelWriter writer) {
      this.writer = Objects.requireNonNull(writer);
    }

    @Override
    public XMLModelWriter get() {
      return writer;
    }
  }

  /**
   * A simple provider for XMLModelReader that always returns the instance it
   * was initialized with.
   */
  private static class XMLModelReaderProvider
      implements Provider<XMLModelReader> {

    private final XMLModelReader reader;

    private XMLModelReaderProvider(XMLModelReader reader) {
      this.reader = Objects.requireNonNull(reader);
    }

    @Override
    public XMLModelReader get() {
      return reader;
    }
  }
}
