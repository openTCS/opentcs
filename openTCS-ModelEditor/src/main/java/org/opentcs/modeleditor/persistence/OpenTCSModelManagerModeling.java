/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.persistence;

import java.io.File;
import java.io.IOException;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.logging.Level;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JFileChooser;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.plantoverview.PlantModelExporter;
import org.opentcs.components.plantoverview.PlantModelImporter;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.guing.common.application.ProgressIndicator;
import org.opentcs.guing.common.application.StatusPanel;
import org.opentcs.guing.common.exchange.adapter.ProcessAdapterUtil;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.guing.common.persistence.ModelExportAdapter;
import org.opentcs.guing.common.persistence.ModelFilePersistor;
import org.opentcs.guing.common.persistence.ModelFileReader;
import org.opentcs.guing.common.persistence.OpenTCSModelManager;
import org.opentcs.guing.common.util.CourseObjectFactory;
import org.opentcs.guing.common.util.ModelComponentFactory;
import org.opentcs.guing.common.util.SynchronizedFileChooser;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages (loads, persists and keeps) the driving course model.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OpenTCSModelManagerModeling
    extends OpenTCSModelManager
    implements ModelManagerModeling {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OpenTCSModelManagerModeling.class);
  /**
   * A file chooser for selecting model files to be opened.
   */
  private final JFileChooser modelReaderFileChooser;
  /**
   * A file chooser for selecting model files to be saved.
   */
  private final JFileChooser modelPersistorFileChooser;
  /**
   * Persists a model to a kernel.
   */
  private final ModelKernelPersistor kernelPersistor;
  /**
   * The file filters for different model readers that can be used to load models from a file.
   */
  private final ModelFileReader modelReader;
  /**
   * Converts model data on import.
   */
  private final ModelImportAdapter modelImportAdapter;

  /**
   * Creates a new instance.
   *
   * @param crsObjFactory A course object factory to be used.
   * @param modelComponentFactory The model component factory to be used.
   * @param procAdapterUtil A utility class for process adapters.
   * @param systemModelProvider Provides instances of SystemModel.
   * @param statusPanel StatusPanel to log messages.
   * @param homeDir The application's home directory.
   * @param kernelPersistor Persists a model to a kernel.
   * @param modelReader The model reader.
   * @param modelPersistor The model persistor.
   * @param modelImportAdapter Converts model data on import.
   * @param modelExportAdapter Converts model data on export.
   * @param progressIndicator The progress indicator to be used.
   */
  @Inject
  public OpenTCSModelManagerModeling(CourseObjectFactory crsObjFactory,
                                     ModelComponentFactory modelComponentFactory,
                                     ProcessAdapterUtil procAdapterUtil,
                                     Provider<SystemModel> systemModelProvider,
                                     StatusPanel statusPanel,
                                     @ApplicationHome File homeDir,
                                     ModelKernelPersistor kernelPersistor,
                                     ModelFileReader modelReader,
                                     ModelFilePersistor modelPersistor,
                                     ModelImportAdapter modelImportAdapter,
                                     ModelExportAdapter modelExportAdapter,
                                     ProgressIndicator progressIndicator) {
    super(crsObjFactory,
          modelComponentFactory,
          procAdapterUtil,
          systemModelProvider,
          statusPanel,
          homeDir,
          modelPersistor,
          modelExportAdapter,
          progressIndicator);
    this.kernelPersistor = requireNonNull(kernelPersistor, "kernelPersistor");

    this.modelReader = requireNonNull(modelReader, "modelReader");
    this.modelReaderFileChooser = new SynchronizedFileChooser(new File(homeDir, "data"));
    this.modelReaderFileChooser.setAcceptAllFileFilterUsed(false);
    this.modelReaderFileChooser.setFileFilter(modelReader.getDialogFileFilter());

    this.modelPersistorFileChooser = new SynchronizedFileChooser(new File(homeDir, "data"));
    this.modelPersistorFileChooser.setAcceptAllFileFilterUsed(false);
    this.modelPersistorFileChooser.setFileFilter(modelPersistor.getDialogFileFilter());

    this.modelImportAdapter = requireNonNull(modelImportAdapter, "modelImportAdapter");
  }

  @Override
  public boolean loadModel(@Nullable File modelFile) {
    File file = modelFile != null ? modelFile : showOpenDialog();
    if (file == null) {
      return false;
    }

    return loadModel(file, modelReader);
  }

  @Override
  public boolean loadModel(@Nullable File modelFile, ModelFileReader reader) {
    requireNonNull(reader, "reader");
    File file = modelFile != null ? modelFile : showOpenDialog();
    if (file == null) {
      return false;
    }

    try {
      Optional<PlantModelCreationTO> opt = reader.deserialize(file);
      if (!opt.isPresent()) {
        LOG.debug("Loading model canceled.");
        return false;
      }
      setModel(modelImportAdapter.convert(opt.get()));
      setCurrentModelFile(file);
      initializeSystemModel(getModel());
      return true;
    }
    catch (IOException | IllegalArgumentException ex) {
      getStatusPanel().setLogMessage(Level.SEVERE,
                                     ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.STATUS_PATH)
                                         .getFormatted("openTcsModelManagerModeling.message_notLoaded.text",
                                                       file.getName()));
      LOG.info("Error reading file", ex);
    }
    return false;
  }

  @Override
  public boolean importModel(PlantModelImporter importer) {
    requireNonNull(importer, "importer");

    try {
      Optional<PlantModelCreationTO> opt = importer.importPlantModel();
      if (!opt.isPresent()) {
        LOG.debug("Model import cancelled.");
        return false;
      }
      SystemModel newSystemModel = modelImportAdapter.convert(opt.get());
      setModel(newSystemModel);
      setCurrentModelFile(null);
      initializeSystemModel(getModel());
      return true;
    }
    catch (IOException | IllegalArgumentException ex) {
      getStatusPanel().setLogMessage(Level.SEVERE,
                                     ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.STATUS_PATH)
                                         .getFormatted("openTcsModelManagerModeling.message_notImported.text"));
      LOG.warn("Exception importing model", ex);
      return false;
    }
  }

  @Override
  public boolean uploadModel(KernelServicePortal portal) {
    try {
      setModelName(getModel().getName());
      getStatusPanel().clear();
      return persistModel(getModel(), portal, kernelPersistor, false);
    }
    catch (IllegalStateException | CredentialsException e) {
      getStatusPanel().setLogMessage(Level.SEVERE,
                                     ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.STATUS_PATH)
                                         .getString("openTcsModelManagerModeling.message_notSaved.text"));
      LOG.warn("Exception persisting model", e);
      return false;
    }
    catch (IllegalArgumentException e) {
      getStatusPanel().setLogMessage(Level.SEVERE,
                                     e.getMessage());
      LOG.warn("Exception persisting model", e);
      return false;
    }
  }

  @Override
  public boolean exportModel(PlantModelExporter exporter) {
    requireNonNull(exporter, "exporter");

    try {
      exporter.exportPlantModel(getModelExportAdapter().convert(getModel()));
      return true;
    }
    catch (IOException | IllegalArgumentException ex) {
      getStatusPanel().setLogMessage(Level.SEVERE,
                                     ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.STATUS_PATH)
                                         .getString("openTcsModelManagerModeling.message_notExported.text"));
      LOG.warn("Exception exporting model", ex);
      return false;
    }
  }

  /**
   * Persist model with the persistor.
   *
   * @param systemModel The system model to be persisted.
   * @param persistor The persistor to be used.
   * @param ignoreError whether the model should be persisted when duplicates exist
   * @return Whether the model was actually saved.
   * @throws IllegalStateException If there was a problem persisting the model
   */
  private boolean persistModel(SystemModel systemModel,
                               KernelServicePortal portal,
                               ModelKernelPersistor persistor,
                               boolean ignoreError)
      throws IllegalStateException, KernelRuntimeException {
    requireNonNull(systemModel, "systemModel");
    requireNonNull(persistor, "persistor");

    if (!persistor.persist(systemModel, portal, ignoreError)) {
      return false;
    }

    systemModel.setName(getModelName());
    return true;
  }

  /**
   * Shows a dialog to select a model to load.
   *
   * @return The selected file or <code>null</code>, if nothing was selected.
   */
  private File showOpenDialog() {
    if (!modelReaderFileChooser.getCurrentDirectory().isDirectory()) {
      modelReaderFileChooser.getCurrentDirectory().mkdir();
    }
    if (modelReaderFileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    return modelReaderFileChooser.getSelectedFile();
  }
}
