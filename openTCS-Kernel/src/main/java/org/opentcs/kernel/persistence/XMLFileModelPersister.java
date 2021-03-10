/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import static com.google.common.base.Strings.emptyToNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import javax.inject.Inject;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.customizations.ApplicationHome;
import static org.opentcs.util.Assertions.checkState;
import org.opentcs.util.FileSystems;
import org.opentcs.util.persistence.ModelParser;
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
   * The model file.
   */
  private final File modelFile;
  /**
   * Reads and writes models into xml files.
   */
  private final ModelParser modelParser;

  /**
   * Creates a new XMLFileModelPersister.
   *
   * @param directory The application's home directory.
   * @param modelParser Reads and writes into the xml file.
   */
  @Inject
  public XMLFileModelPersister(@ApplicationHome File directory,
                               ModelParser modelParser) {
    this.modelParser = requireNonNull(modelParser, "modelParser");
    this.dataDirectory = new File(requireNonNull(directory, "directory"), "data");

    this.modelFile = new File(dataDirectory, MODEL_FILE_NAME);
  }

  @Override
  public Optional<String> getPersistentModelName()
      throws IllegalStateException {
    if (!hasSavedModel()) {
      return Optional.empty();
    }
    return Optional.of(readXMLModelName(modelFile));
  }

  @Override
  public void saveModel(PlantModelCreationTO model)
      throws IllegalStateException {
    requireNonNull(model, "model");

    LOG.debug("Saving model '{}'.", model.getName());

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

      modelParser.writeModel(model, modelFile);
    }
    catch (IOException exc) {
      throw new IllegalStateException("Exception saving model", exc);
    }
  }

  @Override
  public PlantModelCreationTO readModel()
      throws IllegalStateException {
    // Return empty model if there is no saved model
    if (!hasSavedModel()) {
      return new PlantModelCreationTO("empty model");
    }

    // Read the model from the file.
    return readXMLModel(modelFile);
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
    Files.copy(modelFile.toPath(),
               new File(modelBackupDirectory, modelBackupName).toPath());
  }

  /**
   * Test if the data directory with a model file exist. If not, throw an
   * exception.
   *
   * @throws IOException If check failed.
   */
  private boolean modelFileExists() {
    if (!modelFile.exists()) {
      return false;
    }
    if (!modelFile.isFile()) {
      return false;
    }
    return true;
  }

  @Override
  public void removeModel()
      throws IllegalStateException {
    LOG.debug("Removing model...");
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
    return ofNullable(emptyToNull(readXMLModel(modelFile).getName()))
        .orElse("ModelNameMissing");
  }

  /**
   * Reads a model from a given InputStream.
   *
   * @param modelFile The file containing the model.
   * @param model The model to be built.
   * @throws IOException If an exception occured while loading
   */
  private PlantModelCreationTO readXMLModel(File modelFile)
      throws IllegalStateException {
    try {
      return modelParser.readModel(modelFile);
    }
    catch (IOException exc) {
      LOG.error("Exception parsing input", exc);
      throw new IllegalStateException("Exception parsing input", exc);
    }
  }

}
