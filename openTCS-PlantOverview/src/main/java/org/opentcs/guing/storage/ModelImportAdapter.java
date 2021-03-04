/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.GroupCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.guing.application.StatusPanel;
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
import org.opentcs.guing.util.JOptionPaneUtil;
import org.opentcs.guing.util.ResourceBundleUtil;
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
  @SuppressWarnings("deprecation")
  public SystemModel convert(PlantModelCreationTO model)
      throws IllegalArgumentException {
    requireNonNull(model, "model");
//    checkArgument(!model.getVisualLayouts().isEmpty(), "Model does not contain a visual layout");

    SystemModel systemModel = systemModelProvider.get();
    systemModel.setName(model.getName());

    if (model.getVisualLayouts().size() > 1) {
      LOG.warn("There is more than one visual layout. Using only the first one.");
    }
    VisualLayoutCreationTO layoutTO = model.getVisualLayouts().isEmpty()
        ? new VisualLayoutCreationTO("VLayout")
        : model.getVisualLayouts().get(0);

    Set<String> collectedErrorMessages = new HashSet<>();

    importPoints(model, layoutTO, systemModel, collectedErrorMessages);
    importPaths(model, layoutTO, systemModel, collectedErrorMessages);
    importVehicles(model, layoutTO, systemModel, collectedErrorMessages);
    importLocationTypes(model, layoutTO, systemModel, collectedErrorMessages);
    importLocations(model, layoutTO, systemModel, collectedErrorMessages);
    importBlocks(model, layoutTO, systemModel, collectedErrorMessages);
    importStaticRoutes(model, layoutTO, systemModel, collectedErrorMessages);
    importGroups(model, layoutTO, systemModel, collectedErrorMessages);
    importVisualLayout(layoutTO, systemModel, collectedErrorMessages);

    // If any errors occurred, show the dialog with all errors listed
    if (!collectedErrorMessages.isEmpty()) {
      ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
      JOptionPaneUtil
          .showDialogWithTextArea(statusPanel,
                                  bundle.getString("ValidationWarning.title"),
                                  bundle.getString("ValidationWarning.descriptionLoading"),
                                  collectedErrorMessages.stream().collect(Collectors.toList()));
    }

    return systemModel;
  }

  private void importVisualLayout(VisualLayoutCreationTO layoutTO, SystemModel systemModel,
                                  Set<String> collectedErrorMessages) {
    LayoutModel layoutModel = elementConverter.importLayout(layoutTO);
    if (validModelComponent(layoutModel, systemModel, collectedErrorMessages)) {
      updateLayoutInModel(layoutModel, systemModel);
    }
  }

  private void importGroups(PlantModelCreationTO model, VisualLayoutCreationTO layoutTO,
                            SystemModel systemModel, Set<String> collectedErrorMessages) {
    for (GroupCreationTO groupTO : model.getGroups()) {
      GroupModel groupModel = elementConverter.importGroup(groupTO, layoutTO);
      if (validModelComponent(groupModel, systemModel, collectedErrorMessages)) {
        addGroupToModel(groupModel, systemModel);
      }
    }
  }

  @SuppressWarnings("deprecation")
  private void importStaticRoutes(PlantModelCreationTO model, VisualLayoutCreationTO layoutTO,
                                  SystemModel systemModel, Set<String> collectedErrorMessages) {
    for (org.opentcs.access.to.model.StaticRouteCreationTO staticRouteTO
             : model.getStaticRoutes()) {
      StaticRouteModel staticRouteModel = elementConverter.importStaticRoute(staticRouteTO,
                                                                             layoutTO);
      if (validModelComponent(staticRouteModel, systemModel, collectedErrorMessages)) {
        addStaticRouteToModel(staticRouteModel, systemModel);
      }
    }
  }

  private void importBlocks(PlantModelCreationTO model, VisualLayoutCreationTO layoutTO,
                            SystemModel systemModel, Set<String> collectedErrorMessages) {
    for (BlockCreationTO blockTO : model.getBlocks()) {
      BlockModel blockModel = elementConverter.importBlock(blockTO, layoutTO);
      if (validModelComponent(blockModel, systemModel, collectedErrorMessages)) {
        addBlockToModel(blockModel, systemModel);
      }
    }
  }

  private void importLocations(PlantModelCreationTO model, VisualLayoutCreationTO layoutTO,
                               SystemModel systemModel, Set<String> collectedErrorMessages) {
    for (LocationCreationTO locationTO : model.getLocations()) {
      LocationModel locationModel = elementConverter.importLocation(locationTO,
                                                                    model.getLocationTypes(),
                                                                    layoutTO);
      if (validModelComponent(locationModel, systemModel, collectedErrorMessages)) {
        addLocationToModel(locationModel, systemModel);

        for (Map.Entry<String, Set<String>> entry : locationTO.getLinks().entrySet()) {
          LinkModel linkModel = elementConverter.importLocationLink(locationTO,
                                                                    entry.getKey(),
                                                                    entry.getValue());
          if (validModelComponent(linkModel, systemModel, collectedErrorMessages)) {
            addLinkToModel(linkModel, systemModel);
          }
        }
      }
    }
  }

  private void importLocationTypes(PlantModelCreationTO model, VisualLayoutCreationTO layoutTO,
                                   SystemModel systemModel, Set<String> collectedErrorMessages) {
    for (LocationTypeCreationTO locTypeTO : model.getLocationTypes()) {
      LocationTypeModel locTypeModel = elementConverter.importLocationType(locTypeTO, layoutTO);
      if (validModelComponent(locTypeModel, systemModel, collectedErrorMessages)) {
        addLocationTypeToModel(locTypeModel, systemModel);
      }
    }
  }

  private void importVehicles(PlantModelCreationTO model, VisualLayoutCreationTO layoutTO,
                              SystemModel systemModel, Set<String> collectedErrorMessages) {
    for (VehicleCreationTO vehicleTO : model.getVehicles()) {
      VehicleModel vehicleModel = elementConverter.importVehicle(vehicleTO, layoutTO);
      if (validModelComponent(vehicleModel, systemModel, collectedErrorMessages)) {
        addVehicleToModel(vehicleModel, systemModel);
      }
    }
  }

  private void importPaths(PlantModelCreationTO model, VisualLayoutCreationTO layoutTO,
                           SystemModel systemModel, Set<String> collectedErrorMessages) {
    for (PathCreationTO pathTO : model.getPaths()) {
      PathModel pathModel = elementConverter.importPath(pathTO, layoutTO);
      if (validModelComponent(pathModel, systemModel, collectedErrorMessages)) {
        addPathToModel(pathModel, systemModel);
      }
    }
  }

  private void importPoints(PlantModelCreationTO model,
                            VisualLayoutCreationTO layoutTO,
                            SystemModel systemModel,
                            Set<String> collectedErrorMessages) {
    for (PointCreationTO pointTO : model.getPoints()) {
      PointModel pointModel = elementConverter.importPoint(pointTO, layoutTO);
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
      String deserializationError = ResourceBundleUtil.getBundle()
          .getFormatted("UnifiedModelReader.deserialization.error",
                        modelComponent.getName(),
                        validator.getErrors());
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

  private void addStaticRouteToModel(StaticRouteModel staticRoute, SystemModel systemModel) {
    systemModel.getMainFolder(SystemModel.FolderKey.STATIC_ROUTES).add(staticRoute);
  }

  private void addGroupToModel(GroupModel group, SystemModel systemModel) {
    systemModel.getMainFolder(SystemModel.FolderKey.GROUPS).add(group);
  }

  private void updateLayoutInModel(LayoutModel layout, SystemModel systemModel) {
    // SystemModel already contains a LayoutModel, just copy the properties
    ModelComponent layoutComponent = systemModel.getMainFolder(SystemModel.FolderKey.LAYOUT);
    for (Map.Entry<String, Property> property : layout.getProperties().entrySet()) {
      layoutComponent.setProperty(property.getKey(), property.getValue());
    }
  }

}
