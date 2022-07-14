/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.persistence;

import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.guing.base.components.properties.type.KeyValueProperty;
import org.opentcs.guing.base.components.properties.type.Property;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.application.StatusPanel;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.modeleditor.persistence.unified.PlantModelElementConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts plant model data to {@link SystemModel} instances.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelImportAdapter {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ModelImportAdapter.class);

  private final Provider<SystemModel> systemModelProvider;
  /**
   * Converts single elements of a plant model.
   */
  private final PlantModelElementConverter elementConverter;
  /**
   * Validates model components and the system model.
   */
  private final ModelValidator validator;
  /**
   * The status panel of the plant overview.
   */
  private final StatusPanel statusPanel;

  @Inject
  public ModelImportAdapter(Provider<SystemModel> systemModelProvider,
                            PlantModelElementConverter elementConverter,
                            ModelValidator validator,
                            StatusPanel statusPanel) {
    this.systemModelProvider = requireNonNull(systemModelProvider, "systemModelProvider");
    this.elementConverter = requireNonNull(elementConverter, "elementConverter");
    this.validator = requireNonNull(validator, "validator");
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
  }

  /**
   * Converts the given plant model data to a {@link SystemModel} instance.
   *
   * @param model The plant model data to be converted.
   * @return The converted model.
   * @throws IllegalArgumentException If the given plant model data was inconsistent in some way.
   */
  @Nonnull
  public SystemModel convert(PlantModelCreationTO model)
      throws IllegalArgumentException {
    requireNonNull(model, "model");
//    checkArgument(!model.getVisualLayouts().isEmpty(), "Model does not contain a visual layout");

    SystemModel systemModel = systemModelProvider.get();
    systemModel.setName(model.getName());

    VisualLayoutCreationTO layoutTO = model.getVisualLayout();

    Set<String> collectedErrorMessages = new HashSet<>();

    importVisualLayout(layoutTO, systemModel, collectedErrorMessages);
    importPoints(model, systemModel, collectedErrorMessages);
    importPaths(model, systemModel, collectedErrorMessages);
    importVehicles(model, systemModel, collectedErrorMessages);
    importLocationTypes(model, systemModel, collectedErrorMessages);
    importLocations(model, systemModel, collectedErrorMessages);
    importBlocks(model, systemModel, collectedErrorMessages);

    importProperties(model, systemModel);

    // If any errors occurred, show the dialog with all errors listed
    if (!collectedErrorMessages.isEmpty()) {
      validator.showLoadingValidationWarning(statusPanel, collectedErrorMessages);
    }

    return systemModel;
  }

  private void importProperties(PlantModelCreationTO model, SystemModel systemModel) {
    for (Map.Entry<String, String> property : model.getProperties().entrySet()) {
      systemModel.getPropertyMiscellaneous().addItem(new KeyValueProperty(systemModel,
                                                                          property.getKey(),
                                                                          property.getValue()));
    }
  }

  private void importVisualLayout(VisualLayoutCreationTO layoutTO, SystemModel systemModel,
                                  Set<String> collectedErrorMessages) {
    LayoutModel layoutModel = elementConverter.importLayout(layoutTO);
    if (validModelComponent(layoutModel, systemModel, collectedErrorMessages)) {
      updateLayoutInModel(layoutModel, systemModel);
    }
  }

  private void importBlocks(PlantModelCreationTO model,
                            SystemModel systemModel,
                            Set<String> collectedErrorMessages) {
    for (BlockCreationTO blockTO : model.getBlocks()) {
      BlockModel blockModel = elementConverter.importBlock(blockTO);
      if (validModelComponent(blockModel, systemModel, collectedErrorMessages)) {
        addBlockToModel(blockModel, systemModel);
      }
    }
  }

  private void importLocations(PlantModelCreationTO model,
                               SystemModel systemModel,
                               Set<String> collectedErrorMessages) {
    for (LocationCreationTO locationTO : model.getLocations()) {
      LocationModel locationModel = elementConverter.importLocation(locationTO,
                                                                    model.getLocationTypes(),
                                                                    systemModel);
      if (validModelComponent(locationModel, systemModel, collectedErrorMessages)) {
        addLocationToModel(locationModel, systemModel);

        for (Map.Entry<String, Set<String>> entry : locationTO.getLinks().entrySet()) {
          LinkModel linkModel = elementConverter.importLocationLink(locationTO,
                                                                    entry.getKey(),
                                                                    entry.getValue(),
                                                                    systemModel);
          if (validModelComponent(linkModel, systemModel, collectedErrorMessages)) {
            addLinkToModel(linkModel, systemModel);
          }
        }
      }
    }
  }

  private void importLocationTypes(PlantModelCreationTO model,
                                   SystemModel systemModel,
                                   Set<String> collectedErrorMessages) {
    for (LocationTypeCreationTO locTypeTO : model.getLocationTypes()) {
      LocationTypeModel locTypeModel = elementConverter.importLocationType(locTypeTO);
      if (validModelComponent(locTypeModel, systemModel, collectedErrorMessages)) {
        addLocationTypeToModel(locTypeModel, systemModel);
      }
    }
  }

  private void importVehicles(PlantModelCreationTO model,
                              SystemModel systemModel,
                              Set<String> collectedErrorMessages) {
    for (VehicleCreationTO vehicleTO : model.getVehicles()) {
      VehicleModel vehicleModel = elementConverter.importVehicle(vehicleTO);
      if (validModelComponent(vehicleModel, systemModel, collectedErrorMessages)) {
        addVehicleToModel(vehicleModel, systemModel);
      }
    }
  }

  private void importPaths(PlantModelCreationTO model,
                           SystemModel systemModel,
                           Set<String> collectedErrorMessages) {
    for (PathCreationTO pathTO : model.getPaths()) {
      PathModel pathModel = elementConverter.importPath(pathTO, systemModel);
      if (validModelComponent(pathModel, systemModel, collectedErrorMessages)) {
        addPathToModel(pathModel, systemModel);
      }
    }
  }

  private void importPoints(PlantModelCreationTO model,
                            SystemModel systemModel,
                            Set<String> collectedErrorMessages) {
    for (PointCreationTO pointTO : model.getPoints()) {
      PointModel pointModel = elementConverter.importPoint(pointTO, systemModel);
      if (validModelComponent(pointModel, systemModel, collectedErrorMessages)) {
        addPointToModel(pointModel, systemModel);
      }
    }
  }

  private boolean validModelComponent(ModelComponent modelComponent,
                                      SystemModel systemModel,
                                      Set<String> collectedErrorMessages) {
    if (validator.isValidWith(systemModel, modelComponent)) {
      return true;
    }
    else {
      String deserializationError = validator.formatDeserializationErrors(modelComponent,
                                                                          validator.getErrors());
      validator.formatDeserializationErrors(modelComponent, validator.getErrors());
      validator.resetErrors();
      LOG.warn("Deserialization error: {}", deserializationError);
      collectedErrorMessages.add(deserializationError);
      return false;
    }
  }

  private void addPointToModel(PointModel point, SystemModel systemModel) {
    systemModel.getMainFolder(SystemModel.FolderKey.POINTS).add(point);
  }

  private void addPathToModel(PathModel path, SystemModel systemModel) {
    systemModel.getMainFolder(SystemModel.FolderKey.PATHS).add(path);
  }

  private void addVehicleToModel(VehicleModel vehicle, SystemModel systemModel) {
    systemModel.getMainFolder(SystemModel.FolderKey.VEHICLES).add(vehicle);
  }

  private void addLocationTypeToModel(LocationTypeModel locType, SystemModel systemModel) {
    systemModel.getMainFolder(SystemModel.FolderKey.LOCATION_TYPES).add(locType);
  }

  private void addLocationToModel(LocationModel location, SystemModel systemModel) {
    systemModel.getMainFolder(SystemModel.FolderKey.LOCATIONS).add(location);
  }

  private void addLinkToModel(LinkModel link, SystemModel systemModel) {
    systemModel.getMainFolder(SystemModel.FolderKey.LINKS).add(link);
  }

  private void addBlockToModel(BlockModel block, SystemModel systemModel) {
    systemModel.getMainFolder(SystemModel.FolderKey.BLOCKS).add(block);
  }

  private void updateLayoutInModel(LayoutModel layout, SystemModel systemModel) {
    // SystemModel already contains a LayoutModel, just copy the properties
    ModelComponent layoutComponent = systemModel.getMainFolder(SystemModel.FolderKey.LAYOUT);
    for (Map.Entry<String, Property> property : layout.getProperties().entrySet()) {
      layoutComponent.setProperty(property.getKey(), property.getValue());
    }
  }

}
