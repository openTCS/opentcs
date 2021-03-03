/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
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
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.kernel.workingset.Model;
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
  private static final Logger log
      = LoggerFactory.getLogger(XMLFileModelPersister.class);
  /**
   * The name of the model file in the model directory.
   */
  private static final String modelFileName = "model.xml";
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
    log.debug("method entry");
    this.readerProvider = Objects.requireNonNull(readerProvider);
    this.writerProvider = Objects.requireNonNull(writerProvider);
    Objects.requireNonNull(directory, "directory is null");
    dataDirectory = new File(directory, "data");
    if (!dataDirectory.isDirectory() && !dataDirectory.mkdirs()) {
      throw new IllegalArgumentException(dataDirectory.getPath()
          + " is not an existing directory and could not be created, either.");
    }
  }

  @Override
  public Optional<String> getPersistentModelName()
      throws IOException {
    log.debug("method entry");
    if (!hasSavedModel()) {
      return Optional.empty();
    }
    File modelFile = new File(dataDirectory, modelFileName);
    return Optional.of(readXMLModelName(modelFile));
  }

  @Override
  public void saveModel(Model model, @Nullable String modelName)
      throws IOException {
    log.debug("method entry");
    Objects.requireNonNull(model, "model is null");

    StringBuilder message = new StringBuilder();
    message.append("Saving model '").append(model.getName()).append("'");
    if (modelName != null) {
      message.append(" as ").append(modelName);
    }
    log.debug(message.toString());

    File modelFile = new File(dataDirectory, modelFileName);
    // Check if writing the model is possible.
    if (!dataDirectory.exists()) {
      throw new IOException(dataDirectory.getPath() + " does not exist");
    }
    if (!dataDirectory.isDirectory()) {
      throw new IOException(
          dataDirectory.getPath() + " exists, but is not a directory");
    }
    if (modelFile.exists() && !modelFile.isFile()) {
      throw new IOException(
          modelFile.getPath() + " exists, but is not a regular file");
    }
    if (modelFile.exists()) {
      createBackup();
    }
    try (OutputStream outStream = new FileOutputStream(modelFile)) {
      XMLModelWriter writer = writerProvider.get();
      writer.writeXMLModel(model, modelName, outStream);
    }
  }

  @Override
  public void loadModel(Model model)
      throws IOException {
    log.debug("method entry");
    Objects.requireNonNull(model, "model is null");
    // Return empty model if there is no saved model
    if (!hasSavedModel()) {
      model.clear();
      return;
    }
    log.debug("Loading model. '" + getPersistentModelName() + "'");
    checkIfModelFileExists();
    // Read the model from the file.
    File modelFile = new File(dataDirectory, modelFileName);
    readXMLModel(modelFile, model);
    log.debug("Successfully loaded model '" + model.getName() + "'");
  }

  @Override
  public boolean hasSavedModel() {
    try {
      checkIfModelFileExists();
      return true;
    }
    catch (IOException ex) {
      return false;
    }
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
    String modelBackupName = modelFileName + "_backup_" + time;
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
    Files.copy(new File(dataDirectory, modelFileName).toPath(),
               new File(modelBackupDirectory, modelBackupName).toPath());
  }

  /**
   * Test if the data directory with a model file exist. If not, throw an
   * exception.
   *
   * @throws IOException If check failed.
   */
  private void checkIfModelFileExists()
      throws IOException {
    log.debug("method entry");
    File modelFile = new File(dataDirectory, modelFileName);
    if (!dataDirectory.exists()) {
      throw new IOException(dataDirectory.getPath() + " does not exist");
    }
    if (!dataDirectory.isDirectory()) {
      throw new IOException(
          dataDirectory.getPath() + " exists, but is not a directory");
    }
    if (!modelFile.exists()) {
      throw new IOException(modelFile.getPath() + " does not exist.");
    }
    if (modelFile.exists() && !modelFile.isFile()) {
      throw new IOException(
          modelFile.getPath() + " exists, but is not a regular file");
    }
  }

  @Override
  public void removeModel()
      throws IOException {
    log.debug("method entry");
    log.debug("Removing model.");
    File modelFile = new File(dataDirectory, modelFileName);
    // If the model file does not exist, don't do anything
    try {
      checkIfModelFileExists();
    }
    catch (IOException exc) {
      return;
    }
    createBackup();
    if (!FileSystems.deleteRecursively(modelFile)) {
      throw new IOException("Cannot delete " + modelFile.getPath());
    }
  }

  /**
   * Reads a model's name from a given InputStream.
   *
   * @param modelFile The file containing the model.
   * @throws IOException If an exception occured while loading
   */
  private String readXMLModelName(File modelFile)
      throws IOException {
    log.debug("method entry");
    try (InputStream inStream = new FileInputStream(modelFile)) {
      XMLModelReader reader = readerProvider.get();
      return reader.readModelName(inStream);
    }
    catch (InvalidModelException exc) {
      throw new IOException("Exception parsing input", exc);
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
    log.debug("method entry");
    try (InputStream inStream = new FileInputStream(modelFile)) {
      XMLModelReader reader = readerProvider.get();
      reader.readXMLModel(inStream, model);
    }
    catch (InvalidModelException exc) {
      log.error("Exception parsing input", exc);
      throw new IOException("Exception parsing input: " + exc.getMessage());
    }
  }

}
