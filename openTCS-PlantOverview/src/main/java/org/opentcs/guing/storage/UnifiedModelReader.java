/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.filechooser.FileFilter;
import org.opentcs.guing.application.StatusPanel;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.Property;
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
import org.opentcs.guing.persistence.UnifiedModelComponentConverter;
import org.opentcs.guing.util.JOptionPaneUtil;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.persistence.binding.BlockTO;
import org.opentcs.util.persistence.binding.GroupTO;
import org.opentcs.util.persistence.binding.LocationTO;
import org.opentcs.util.persistence.binding.LocationTypeTO;
import org.opentcs.util.persistence.binding.PathTO;
import org.opentcs.util.persistence.binding.PlantModelTO;
import org.opentcs.util.persistence.binding.PointTO;
import org.opentcs.util.persistence.binding.StaticRouteTO;
import org.opentcs.util.persistence.binding.VehicleTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of <code>ModelReader</code> to deserialize a <code>SystemModel</code> from a xml
 * file.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class UnifiedModelReader
    implements ModelReader {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(UnifiedModelReader.class);
  /**
   * The <code>SystemModel</code> that will contain the read model.
   */
  private final Provider<SystemModel> systemModelProvider;
  /**
   * Validates model components and the system model.
   */
  private final ModelValidator validator;
  /**
   * The status panel of the plant overview.
   */
  private final StatusPanel statusPanel;
  /**
   * The errors occured during the deserialization process.
   */
  private final Set<String> deserializationErrors = new HashSet<>();

  @Inject
  public UnifiedModelReader(Provider<SystemModel> systemModelProvider, ModelValidator validator,
                            StatusPanel statusPanel) {
    this.systemModelProvider = requireNonNull(systemModelProvider, "systemModelProvider");
    this.validator = requireNonNull(validator, "validator");
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
  }

  @Override
  public Optional<SystemModel> deserialize(File file)
      throws IOException {
    requireNonNull(file, "file");

    deserializationErrors.clear();
    SystemModel systemModel = systemModelProvider.get();

    String modelName = file.getName().replaceFirst("[.][^.]+$", ""); // remove extension
    if (modelName != null && !modelName.isEmpty()) {
      systemModel.setName(modelName);
    }

    PlantModelTO plantModel;
    try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                                                                  Charset.forName("UTF-8")))) {
      plantModel = PlantModelTO.fromXml(reader);
    }

    plantModel.getProperties().stream()
        .forEach(
            propertyTo -> systemModel.getPropertyMiscellaneous().addItem(
                new KeyValueProperty(systemModel, propertyTo.getName(), propertyTo.getValue())
            )
        );

    if (plantModel.getVisualLayouts().size() > 1) {
      LOG.warn("There is more than one visual layout. Using only the first one.");
    }

    UnifiedModelComponentConverter modelConverter = new UnifiedModelComponentConverter();
    for (PointTO point : plantModel.getPoints()) {
      validateAndAddModelComponent(
          modelConverter.convertPointTO(point, plantModel.getVisualLayouts().get(0)),
          systemModel);
    }
    for (PathTO path : plantModel.getPaths()) {
      validateAndAddModelComponent(
          modelConverter.convertPathTO(path, plantModel.getVisualLayouts().get(0)),
          systemModel);
    }
    for (VehicleTO vehicle : plantModel.getVehicles()) {
      validateAndAddModelComponent(
          modelConverter.convertVehicleTO(vehicle, plantModel.getVisualLayouts().get(0)),
          systemModel);
    }
    for (LocationTypeTO locationType : plantModel.getLocationTypes()) {
      validateAndAddModelComponent(modelConverter.convertLocationTypeTO(locationType),
                                   systemModel);
    }
    for (LocationTO location : plantModel.getLocations()) {
      validateAndAddModelComponent(
          modelConverter.convertLocationTO(location,
                                           plantModel.getLocations(),
                                           plantModel.getVisualLayouts().get(0)),
          systemModel);

      for (LocationTO.Link link : location.getLinks()) {
        validateAndAddModelComponent(modelConverter.convertLinkTO(link, location),
                                     systemModel);
      }
    }
    for (BlockTO block : plantModel.getBlocks()) {
      validateAndAddModelComponent(
          modelConverter.convertBlockTO(block, plantModel.getVisualLayouts().get(0)),
          systemModel);
    }
    for (StaticRouteTO staticRoute : plantModel.getStaticRoutes()) {
      validateAndAddModelComponent(
          modelConverter.convertStaticRouteTO(staticRoute, plantModel.getVisualLayouts().get(0)),
          systemModel);
    }
    for (GroupTO group : plantModel.getGroups()) {
      validateAndAddModelComponent(
          modelConverter.convertGroupTO(group),
          systemModel);
    }

    validateAndAddModelComponent(
        modelConverter.convertVisualLayoutTO(plantModel.getVisualLayouts().get(0)),
        systemModel);

    // XXX get OtherGrapgicalElements from ModelLayoutElementTOs, ShapeLayoutElementTOs and 
    // ImageLayoutElementTOs
    // If any errors occurred, show the dialog with all errors listed
    if (!deserializationErrors.isEmpty()) {
      ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
      JOptionPaneUtil
          .showDialogWithTextArea(statusPanel,
                                  bundle.getString("ValidationWarning.title"),
                                  bundle.getString("ValidationWarning.descriptionLoading"),
                                  deserializationErrors.stream().collect(Collectors.toList()));
    }

    return Optional.of(systemModel);
  }

  @Override
  public FileFilter getDialogFileFilter() {
    return UnifiedModelConstants.DIALOG_FILE_FILTER;
  }

  private void validateAndAddModelComponent(ModelComponent modelComponent, SystemModel systemModel) {
    if (validator.isValidWith(systemModel, modelComponent)) {
      addModelComponentToSystemModel(modelComponent, systemModel);
    }
    else {
      String deserializationError = ResourceBundleUtil.getBundle()
          .getFormatted("UnifiedModelReader.deserialization.error",
                        modelComponent.getName(),
                        validator.getErrors());
      validator.resetErrors();
      LOG.warn("Deserialization error: {}", deserializationError);
      deserializationErrors.add(deserializationError);
    }
  }

  private void addModelComponentToSystemModel(ModelComponent component, SystemModel model) {
    if (component instanceof PointModel) {
      model.getMainFolder(SystemModel.FolderKey.POINTS).add(component);
    }
    if (component instanceof PathModel) {
      model.getMainFolder(SystemModel.FolderKey.PATHS).add(component);
    }
    if (component instanceof VehicleModel) {
      model.getMainFolder(SystemModel.FolderKey.VEHICLES).add(component);
    }
    if (component instanceof LocationTypeModel) {
      model.getMainFolder(SystemModel.FolderKey.LOCATION_TYPES).add(component);
    }
    if (component instanceof LocationModel) {
      model.getMainFolder(SystemModel.FolderKey.LOCATIONS).add(component);
    }
    if (component instanceof LinkModel) {
      model.getMainFolder(SystemModel.FolderKey.LINKS).add(component);
    }
    if (component instanceof BlockModel) {
      model.getMainFolder(SystemModel.FolderKey.BLOCKS).add(component);
    }
    if (component instanceof StaticRouteModel) {
      model.getMainFolder(SystemModel.FolderKey.STATIC_ROUTES).add(component);
    }
    if (component instanceof GroupModel) {
      model.getMainFolder(SystemModel.FolderKey.GROUPS).add(component);
    }
    if (component instanceof LayoutModel) {
      // SystemModel already contains a LayoutModel, just copy the properties
      ModelComponent layoutComponent = model.getMainFolder(SystemModel.FolderKey.LAYOUT);
      for (Map.Entry<String, Property> property : component.getProperties().entrySet()) {
        layoutComponent.setProperty(property.getKey(), property.getValue());
      }
    }
  }
}
