/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.kernel.workingset.Model;
import static org.opentcs.util.Assertions.checkState;
import org.opentcs.util.FileSystems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ModelPersister implementation realizing persistence of models with XML
 * files.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class XMLFileModelPersister
    implements ModelPersister {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(XMLFileModelPersister.class);
  /**
   * The name of the model file in the model directory.
   */
  private static final String MODEL_FILE_NAME = "model.xml";
  /**
   * The directory path for the persisted model.
   */
  private final File dataDirectory;
  /**
   * Provider for an XMLModelReader, used to instanciate a new reader every time
   * a model is read from file.
   */
  private final Provider<XMLModelReader> readerProvider;
  /**
   * Provider for an XMLModelWriter, used to instanciate a new writer every time
   * a model is written to file.
   */
  private final Provider<XMLModelWriter> writerProvider;

  /**
   * Creates a new XMLFileModelPersister.
   *
   * @param directory The application's home directory.
   * @param readerProvider Provider for XMLModelReaders.
   * @param writerProvider Porivder for XMLModelWriters.
   */
  @Inject
  public XMLFileModelPersister(@ApplicationHome File directory,
                               Provider<XMLModelReader> readerProvider,
                               Provider<XMLModelWriter> writerProvider) {
    this.readerProvider = requireNonNull(readerProvider, "readerProvider");
    this.writerProvider = requireNonNull(writerProvider, "writerProvider");
    this.dataDirectory = new File(requireNonNull(directory, "directory"), "data");
  }

  @Override
  public Optional<String> getPersistentModelName()
      throws IllegalStateException {
    if (!hasSavedModel()) {
      return Optional.empty();
    }
    File modelFile = new File(dataDirectory, MODEL_FILE_NAME);
    return Optional.of(readXMLModelName(modelFile));
  }

  @Override
  public void saveModel(Model model, @Nullable String modelName)
      throws IllegalStateException {
    requireNonNull(model, "model");

    LOG.debug("Saving model '{}', modelName is '{}'.", model.getName(), modelName);

    File modelFile = new File(dataDirectory, MODEL_FILE_NAME);
    // Check if writing the model is possible.
    checkState(dataDirectory.isDirectory() || dataDirectory.mkdirs(),
               "%s is not an existing directory and could not be created, either.",
               dataDirectory.getPath());
    checkState(!modelFile.exists() || modelFile.isFile(),
               "%s exists, but is not a regular file",
               modelFile.getPath());
    try {
      if (modelFile.exists()) {
        createBackup();
      }
      try (OutputStream outStream = new FileOutputStream(modelFile)) {
        XMLModelWriter writer = writerProvider.get();
        writer.writeXMLModel(model, modelName, outStream);
      }
    }
    catch (IOException exc) {
      throw new IllegalStateException("Exception saving model", exc);
    }
  }

  @Override
  public void loadModel(Model model)
      throws IllegalStateException {
    requireNonNull(model, "model");
    // Return empty model if there is no saved model
    if (!hasSavedModel()) {
      model.clear();
      return;
    }
    if (!modelFileExists()) {
      LOG.debug("There is no model file, doing nothing.");
      return;
    }
    try {
      LOG.debug("Loading model. '{}'", getPersistentModelName());
      // Read the model from the file.
      File modelFile = new File(dataDirectory, MODEL_FILE_NAME);
      readXMLModel(modelFile, model);
      LOG.debug("Successfully loaded model '" + model.getName() + "'");
    }
    catch (IOException exc) {
      throw new IllegalArgumentException("Exception loading model", exc);
    }
  }

  @Override
  public boolean hasSavedModel() {
    return modelFileExists();
  }

  /**
   * Creates a backup of the currently saved model file by copying it to the
   * "backups" subdirectory.
   *
   * Assumes that the model file exists.
   *
   * @throws IOException If the backup directory is not accessible or copying
   * the file fails.
   */
  private void createBackup()
      throws IOException {
    // Generate backup file name
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
    String time = sdf.format(cal.getTime());
    String modelBackupName = MODEL_FILE_NAME + "_backup_" + time;
    // Make sure backup directory exists
    File modelBackupDirectory = new File(dataDirectory, "backups");
    if (modelBackupDirectory.exists()) {
      if (!modelBackupDirectory.isDirectory()) {
        throw new IOException(
            modelBackupDirectory.getPath() + " exists, but is not a directory");
      }
    }
    else {
      if (!modelBackupDirectory.mkdir()) {
        throw new IOException(
            "Could not create model directory " + modelBackupDirectory.getPath());
      }
    }
    // Backup the model file
    Files.copy(new File(dataDirectory, MODEL_FILE_NAME).toPath(),
               new File(modelBackupDirectory, modelBackupName).toPath());
  }

  /**
   * Test if the data directory with a model file exist. If not, throw an
   * exception.
   *
   * @throws IOException If check failed.
   */
  private boolean modelFileExists() {
    File modelFile = new File(dataDirectory, MODEL_FILE_NAME);
    if (!modelFile.exists()) {
      return false;
    }
    if (modelFile.exists() && !modelFile.isFile()) {
      return false;
    }
    return true;
  }

  @Override
  public void removeModel()
      throws IllegalStateException {
    LOG.debug("Removing model...");
    File modelFile = new File(dataDirectory, MODEL_FILE_NAME);
    // If the model file does not exist, don't do anything
    if (!modelFileExists()) {
      return;
    }
    try {
      createBackup();
      if (!FileSystems.deleteRecursively(modelFile)) {
        throw new IOException("Cannot delete " + modelFile.getPath());
      }
    }
    catch (IOException exc) {
      throw new IllegalStateException("Exception removing model", exc);
    }
  }

  /**
   * Reads a model's name from a given InputStream.
   *
   * @param modelFile The file containing the model.
   * @throws IOException If an exception occured while loading
   */
  private String readXMLModelName(File modelFile)
      throws IllegalStateException {
    try (InputStream inStream = new FileInputStream(modelFile)) {
      XMLModelReader reader = readerProvider.get();
      return reader.readModelName(inStream);
    }
    catch (IOException | InvalidModelException exc) {
      throw new IllegalStateException("Exception parsing input", exc);
    }
  }

  /**
   * Reads a model from a given InputStream.
   *
   * @param modelFile The file containing the model.
   * @param model The model to be built.
   * @throws IOException If an exception occured while loading
   */
  private void readXMLModel(File modelFile, Model model)
      throws IOException {
    try (InputStream inStream = new FileInputStream(modelFile)) {
      XMLModelReader reader = readerProvider.get();
      reader.readXMLModel(inStream, model);
    }
    catch (InvalidModelException exc) {
      LOG.error("Exception parsing input", exc);
      throw new IOException("Exception parsing input: " + exc.getMessage());
    }
  }

}
