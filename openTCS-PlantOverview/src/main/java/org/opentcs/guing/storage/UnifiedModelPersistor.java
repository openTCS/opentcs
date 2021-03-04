/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.filechooser.FileFilter;
import org.opentcs.guing.application.StatusPanel;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.persistence.UnifiedModelComponentConverter;
import org.opentcs.guing.util.JOptionPaneUtil;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.persistence.binding.PlantModelTO;

/**
 * Synchronizes data kept in <code>ModelComponents</code> to a xml file.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class UnifiedModelPersistor
    implements ModelFilePersistor {

  /**
   * The status panel for logging error messages.
   */
  private final StatusPanel statusPanel;
  /**
   * Provides new instances to validate a system model.
   */
  private final Provider<ModelValidator> validatorProvider;

  /**
   * Create a new instance.
   *
   * @param statusPanel A status panel for logging error messages.
   * @param validatorProvider Provides validator instances.
   */
  @Inject
  public UnifiedModelPersistor(StatusPanel statusPanel,
                               Provider<ModelValidator> validatorProvider) {
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    this.validatorProvider = requireNonNull(validatorProvider, "validatorProvider");
  }

  @Override
  public boolean serialize(SystemModel systemModel, String modelName, File file, boolean ignoreError)
      throws IOException {
    requireNonNull(systemModel, "systemModel");
    requireNonNull(file, "file");

    UnifiedModelComponentConverter modelConverter = new UnifiedModelComponentConverter();
    ModelValidator validator = validatorProvider.get();

    boolean valid = true;
    for (ModelComponent component : systemModel.getAll()) {
      valid &= validator.isValidWith(systemModel, component);
    }
    //Report possible duplicates if we persist to the kernel
    if (!valid) {
      //Use a hash set to avoid duplicate errors
      Set<String> errors = new HashSet<>(validator.getErrors());
      ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
      JOptionPaneUtil.showDialogWithTextArea(
          statusPanel,
          bundle.getString("ValidationWarning.title"),
          bundle.getString("ValidationWarning.descriptionSavingKernel"),
          errors);
      if (!ignoreError) {
        return false;
      }
    }

    PlantModelTO drivingCourse = modelConverter.convertSystemModel(systemModel, modelName);
    writeFile(drivingCourse, file);

    return true;
  }

  @Override
  public FileFilter getDialogFileFilter() {
    return UnifiedModelConstants.DIALOG_FILE_FILTER;
  }

  private void writeFile(PlantModelTO plantModel, File file)
      throws IOException {
    if (!file.getName().endsWith(UnifiedModelConstants.FILE_ENDING_XML)) {
      file = new File(file.getParentFile(),
                      file.getName() + "." + UnifiedModelConstants.FILE_ENDING_XML);
    }

    try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                                                                   Charset.forName("UTF-8")))) {
      plantModel.toXml(writer);
    }
  }
}
