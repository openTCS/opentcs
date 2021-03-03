/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import java.io.IOException;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.access.Kernel;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.application.StatusPanel;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.JOptionPaneUtil;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists data kept in {@link SystemModel}s to the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelKernelPersistor {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ModelKernelPersistor.class);
  /**
   * The status panel for logging error messages.
   */
  private final StatusPanel statusPanel;
  /**
   * Provides new instances to validate a system model.
   */
  private final Provider<ModelValidator> validatorProvider;

  /**
   * Creates a new instance.
   *
   * @param statusPanel A status panel for logging error messages.
   * @param validatorProvider Provides validators for system models.
   */
  @Inject
  public ModelKernelPersistor(@Nonnull StatusPanel statusPanel,
                              @Nonnull Provider<ModelValidator> validatorProvider) {
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    this.validatorProvider = requireNonNull(validatorProvider, "validatorProvider");
  }

  /**
   * Persists the given model to the given kernel.
   *
   * @param systemModel The model to be persisted.
   * @param kernel The kernel to persist to.
   * @param ignoreValidationErrors Whether to ignore any validation errors.
   * @return {@code true} if, and only if, the model was valid or validation errors were to be
   * ignored.
   * @throws IOException If there was a problem persisting the model on the kernel side.
   */
  public boolean persist(SystemModel systemModel, Kernel kernel, boolean ignoreValidationErrors)
      throws IOException {
    requireNonNull(systemModel, "systemModel");
    requireNonNull(kernel, "kernel");

    LOG.debug("Validating model...");
    long timeBefore = System.currentTimeMillis();
    if (!valid(systemModel) && !ignoreValidationErrors) {
      return false;
    }
    LOG.debug("Validating took {} milliseconds.", System.currentTimeMillis() - timeBefore);

    LOG.debug("Persisting model...");
    timeBefore = System.currentTimeMillis();
    persistModelElements(kernel, systemModel);
    LOG.debug("Persisting to kernel took {} milliseconds.", System.currentTimeMillis() - timeBefore);

    return true;
  }

  private boolean valid(SystemModel systemModel) {
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
    }
    return valid;
  }

  private void persistModelElements(Kernel kernel, SystemModel systemModel)
      throws IOException {
    // Start with a clean, empty model.
    kernel.createModel(systemModel.getName());

    PlantModelCache plantModelCache = new PlantModelCache();

    long timeBefore = System.currentTimeMillis();
    for (LayoutModel model : systemModel.getLayoutModels()) {
      persist(model, systemModel.getEventDispatcher(), kernel, plantModelCache);
    }
    LOG.debug("Persisting LayoutModels to kernel took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (PointModel model : systemModel.getPointModels()) {
      persist(model, systemModel.getEventDispatcher(), kernel, plantModelCache);
    }
    LOG.debug("Persisting PointModels to kernel took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (PathModel model : systemModel.getPathModels()) {
      persist(model, systemModel.getEventDispatcher(), kernel, plantModelCache);
    }
    LOG.debug("Persisting PathModels to kernel took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (LocationTypeModel model : systemModel.getLocationTypeModels()) {
      persist(model, systemModel.getEventDispatcher(), kernel, plantModelCache);
    }
    LOG.debug("Persisting LocationTypeModels to kernel took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (LocationModel model : systemModel.getLocationModels()) {
      persist(model, systemModel.getEventDispatcher(), kernel, plantModelCache);
    }
    LOG.debug("Persisting LocationModels to kernel took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (LinkModel model : systemModel.getLinkModels()) {
      persist(model, systemModel.getEventDispatcher(), kernel, plantModelCache);
    }
    LOG.debug("Persisting LinkModels to kernel took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (BlockModel model : systemModel.getBlockModels()) {
      persist(model, systemModel.getEventDispatcher(), kernel, plantModelCache);
    }
    LOG.debug("Persisting BlockModels to kernel took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (GroupModel model : systemModel.getGroupModels()) {
      persist(model, systemModel.getEventDispatcher(), kernel, plantModelCache);
    }
    LOG.debug("Persisting GroupModels to kernel took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (StaticRouteModel model : systemModel.getStaticRouteModels()) {
      persist(model, systemModel.getEventDispatcher(), kernel, plantModelCache);
    }
    LOG.debug("Persisting StaticRouteModels to kernel took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    timeBefore = System.currentTimeMillis();
    for (VehicleModel model : systemModel.getVehicleModels()) {
      persist(model, systemModel.getEventDispatcher(), kernel, plantModelCache);
    }
    LOG.debug("Persisting VehicleModels to kernel took {} milliseconds.",
              System.currentTimeMillis() - timeBefore);

    for (VisualLayout layout : plantModelCache.getVisualLayouts()) {
      kernel.setVisualLayoutElements(layout.getReference(), layout.getLayoutElements());
    }

    kernel.saveModel(systemModel.getName());
  }

  private void persist(ModelComponent component, EventDispatcher eventDispatcher, Kernel kernel,
                       PlantModelCache plantModel) {
    ProcessAdapter adapter = eventDispatcher.findProcessAdapter(component);
    if (adapter != null) {
      adapter.updateProcessProperties(kernel, plantModel);
    }
    else {
      LOG.warn("No process adapter for model component {} was found.", component.getName());
    }
  }
}
