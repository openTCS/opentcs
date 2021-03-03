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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.opentcs.access.ApplicationHome;
import org.opentcs.data.model.Layout;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.util.FileSystems;
import org.opentcs.util.Streams;

/**
 * A ModelPersister implementation realizing persistence of models with XML
 * files.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class XMLFileModelPersister
    implements ModelPersister {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(XMLFileModelPersister.class.getName());
  /**
   * The name of the model file in the model directory.
   */
  private static final String modelFileName = "model.xml";
  /**
   * The prefix of all layout file names.
   */
  private static final String layoutFileNamePrefix = "layout_";
  /**
   * The suffix of all layout file names.
   */
  private static final String layoutFileNameSuffix = ".xml";
  /**
   * A <code>FileFilter</code> that only accepts directories which contain a
   * model file.
   */
  private static final FileFilter modelDirectoryFileFilter =
      new ModelDirectoryFileFilter();
  /**
   * A <code>FileFilter</code> that only accepts files with layout file
   * characteristics.
   */
  private static final FileFilter layoutFileFilter = new LayoutFileFilter();
  /**
   * The parent directory for all persistent models.
   */
  private final File dataDirectory;

  /**
   * Creates a new XMLFileModelPersister.
   *
   * @param directory The application's home directory.
   */
  @Inject
  public XMLFileModelPersister(@ApplicationHome File directory) {
    log.finer("method entry");
    Objects.requireNonNull(directory, "directory is null");
    dataDirectory = new File(directory, "data");
    if (!dataDirectory.isDirectory() && !dataDirectory.mkdirs()) {
      throw new IllegalArgumentException(dataDirectory.getPath()
          + " is not an existing directory and could not be created, either.");
    }
  }

  @Override
  public Set<String> getModelNames() {
    log.finer("method entry");
    File[] dataDirContents = dataDirectory.listFiles(modelDirectoryFileFilter);
    Set<String> result = new HashSet<>(dataDirContents.length);
    for (File dataDirContent : dataDirContents) {
      result.add(dataDirContent.getName());
    }
    return result;
  }

  @Override
  public void saveModel(Model model, String modelName, boolean overwrite)
      throws IOException {
    log.finer("method entry");
    Objects.requireNonNull(model, "model is null");
    Objects.requireNonNull(modelName, "modelName is null");
    log.fine("Saving model '" + modelName + "'");
    // Clean the model's name from all occurrences of name separators.
    String modelDirName = modelName.replace(File.separatorChar, '_');
    File modelDirectory = new File(dataDirectory, modelDirName);
    File modelFile = new File(modelDirectory, modelFileName);
    // Check if writing the model is possible.
    if (modelDirectory.exists()) {
      if (!overwrite) {
        throw new IOException(
            modelDirectory.getPath() + " exists and overwriting is not allowed");
      }
      if (!modelDirectory.isDirectory()) {
        throw new IOException(
            modelDirectory.getPath() + " exists, but is not a directory");
      }
      if (modelFile.exists() && !modelFile.isFile()) {
        throw new IOException(
            modelFile.getPath() + " exists, but is not a regular file");
      }
      if (modelFile.exists()) {
        createBackup(modelName, modelDirectory);
      }
    }
    else {
      FileSystems.deleteRecursively(modelDirectory);
      if (!modelDirectory.mkdir()) {
        throw new IOException(
            "Could not create model directory " + modelDirectory.getPath());
      }
    }
    try (OutputStream outStream = new FileOutputStream(modelFile)) {
      XMLModelWriter writer = new XMLModel002Builder();
      writer.writeXMLModel(model, outStream);
    }

    // XXX Layouts are part of the model. This code should be moved to
    // XMLModel001Builder.
//    // Persist the layouts contained in the model.
//    for (Layout curLayout : model.getLayouts(null)) {
//      // XXX Sanitize layout name before using it as a file name!
//      String layoutFileName =
//          layoutFileNamePrefix + curLayout.getName() + layoutFileNameSuffix;
//      File layoutFile = new File(modelDirectory, layoutFileName);
//      outStream = new FileOutputStream(layoutFile);
//      outStream.write(curLayout.getData());
//      outStream.close();
//    }
  }

  @Override
  public void loadModel(String modelName, Model model)
      throws IOException {
    log.finer("method entry");
    Objects.requireNonNull(modelName, "modelName is null");
    Objects.requireNonNull(model, "model is null");
    log.fine("Loading model '" + modelName + "'");
    // Clean the model's name from all occurrences of name separators.
    String modelDirName = modelName.replace(File.separatorChar, '_');
    File modelDirectory = new File(dataDirectory, modelDirName);
    File modelFile = new File(modelDirectory, modelFileName);
    // Check if reading the given model is possible.
    if (modelDirectory.exists()) {
      if (!modelDirectory.isDirectory()) {
        throw new IOException(
            modelDirectory.getPath() + " exists, but is not a directory");
      }
      if (modelFile.exists()) {
        if (!modelFile.isFile()) {
          throw new IOException(
              modelFile.getPath() + " exists, but is not a regular file");
        }
      }
      else {
        throw new IOException(modelFile.getPath() + " does not exist");
      }
    }
    else {
      throw new IOException(modelDirectory.getPath() + " does not exist");
    }
    // Read the model from the file.
    readXMLModel(modelFile, model);
    // Read layouts.
    File[] layoutFiles = modelDirectory.listFiles(layoutFileFilter);
    for (File layoutFile : layoutFiles) {
      // XXX Maybe we should not extract the layout name from the file name.
      String layoutName = layoutFile.getName().substring(
          layoutFileNamePrefix.length(),
          layoutFile.getName().length() - layoutFileNameSuffix.length());
      InputStream inStream = new FileInputStream(layoutFile);
      byte[] layoutData = Streams.getCompleteInputStream(inStream);
      Layout layout = model.createLayout(null, layoutData);
      model.getObjectPool().renameObject(layout.getReference(), layoutName);
    }
    model.setName(modelName);
    log.fine("Successfully loaded model '" + modelName + "'");
  }

  /**
   * Creates a backup.
   * 
   * @param modelName The model name
   * @param modelDirectory The model directory
   * @throws IOException 
   */
  private void createBackup(String modelName, File modelDirectory)
      throws IOException {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
    String time = sdf.format(cal.getTime());
    String modelBackupName = modelName + "_backup_" + time;

    File modelBackupDirectory = new File(modelDirectory.getParent(), "backups");
    if (modelBackupDirectory.exists()) {
      if (!modelBackupDirectory.isDirectory()) {
        throw new IOException(
            modelDirectory.getPath() + " exists, but is not a directory");
      }
    }
    else {
      if (!modelBackupDirectory.mkdir()) {
        throw new IOException(
            "Could not create model directory " + modelBackupDirectory.getPath());
      }
    }

    File curModelBackupDirectory = new File(modelBackupDirectory, modelBackupName);
    if (!curModelBackupDirectory.mkdir()) {
      throw new IOException(
          "Could not create model directory " + curModelBackupDirectory.getPath());
    }


    DirectoryStream<Path> dirStream = Files.newDirectoryStream(modelDirectory.toPath());
    List<Path> paths = new ArrayList<>();
    Iterator<Path> it = dirStream.iterator();
    while (it.hasNext()) {
      paths.add(it.next());
    }

    for (Path curPath : paths) {
      Files.copy(curPath, curModelBackupDirectory.toPath().resolve(curPath.getFileName()));
    }
  }

  @Override
  public void removeModel(String modelName)
      throws IOException {
    log.finer("method entry");
    if (modelName == null) {
      throw new NullPointerException("modelName is null");
    }
    log.fine("Removing model '" + modelName + "'");
    // Clean the model's name from all occurrences of name separators.
    String modelDirName = modelName.replace(File.separatorChar, '_');
    File modelDirectory = new File(dataDirectory, modelDirName);
    if (modelDirectory.exists()) {
      if (!modelDirectory.isDirectory()) {
        throw new IOException(
            modelDirectory.getPath() + " exists, but is not a directory");
      }
    }
    else {
      throw new IOException(modelDirectory.getPath() + " does not exist");
    }
    // Delete the model directory.
    FileSystems.deleteRecursively(modelDirectory);
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
    log.finer("method entry");
    Document document;
    try {
      // Check which parser version is appropriate, and choose it.
      InputStream inStream;
      inStream = new FileInputStream(modelFile);
      SAXBuilder builder = new SAXBuilder();
      document = builder.build(inStream);
      XMLModelReader reader = getModelReader(document);
      inStream.close();

      // Now use the actual chosen parser to read the model.
      inStream = new FileInputStream(modelFile);
      reader.readXMLModel(inStream, model);
      inStream.close();
    }
    catch (JDOMException | InvalidModelException exc) {
      log.log(Level.SEVERE, "Exception parsing input", exc);
      throw new IOException("Exception parsing input: " + exc.getMessage());
    }
  }

  /**
   * Returns a fitting reader/interpreter for a given XML model.
   * 
   * @param xmlModel The model
   * @return A fitting reader/interpreter
   * @throws IOException If an exception occured while reading
   */
  private XMLModelReader getModelReader(Document xmlModel)
      throws IOException {
    log.finer("method entry");
    // Find out which model version is used so we can use the correct builder
    // for it.
    Element rootElement = xmlModel.getRootElement();
    String modelVersion = rootElement.getAttributeValue("version");
    if (modelVersion.equals(XMLModel001Builder.versionString)) {
      return new XMLModel001Builder();
    }
    else if (modelVersion.equals(XMLModel002Builder.versionString)) {
      return new XMLModel002Builder();
    }
    throw new IllegalArgumentException(
        "Unknown model version: " + modelVersion);
  }

  // Private classes start here.
  /**
   * A <code>FileFilter</code> that only accepts directories which contain a
   * model file.
   */
  private static final class ModelDirectoryFileFilter
      implements FileFilter {

    /**
     * Creates a new instance.
     */
    private ModelDirectoryFileFilter() {
      // Do nada.
    }

    @Override
    public boolean accept(File pathname) {
      boolean result = false;
      if (pathname.isDirectory()) {
        File[] contents = pathname.listFiles();
        for (File curEntry : contents) {
          if (curEntry.isFile() && curEntry.getName().equals(modelFileName)) {
            result = true;
            break;
          }
        }
      }
      return result;
    }
  }

  /**
   * A <code>FileFilter</code> that only accepts files with layout file
   * characteristics.
   */
  private static final class LayoutFileFilter
      implements FileFilter {

    /**
     * Creates a new instance.
     */
    private LayoutFileFilter() {
      // Do nada.
    }

    @Override
    public boolean accept(File pathname) {
      String fileName = pathname.getName();
      return pathname.isFile()
          && fileName.startsWith(layoutFileNamePrefix)
          && fileName.endsWith(layoutFileNameSuffix);
    }
  }
}
