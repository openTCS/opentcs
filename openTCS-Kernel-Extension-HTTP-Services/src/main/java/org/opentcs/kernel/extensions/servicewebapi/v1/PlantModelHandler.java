/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.PlantModel;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PlantModelTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.BlockConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.LocationConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.LocationTypeConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.PathConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.PointConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.PropertyConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.VehicleConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.VisualLayoutConverter;

/**
 * Handles requests related to plant models.
 */
public class PlantModelHandler {

  /**
   * Used to set or retrieve plant models.
   */
  private final PlantModelService plantModelService;
  /**
   * Executes calls via the kernel executor and waits for the outcome.
   */
  private final KernelExecutorWrapper executorWrapper;

  private final PointConverter pointConverter;
  private final PathConverter pathConverter;
  private final LocationTypeConverter locationTypeConverter;
  private final LocationConverter locationConverter;
  private final BlockConverter blockConverter;
  private final VehicleConverter vehicleConverter;
  private final VisualLayoutConverter visualLayoutConverter;
  private final PropertyConverter propertyConverter;
  private final RouterService routerService;

  /**
   * Creates a new instance.
   *
   * @param plantModelService Used to set or retrieve plant models.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   * @param pointConverter Converts point instances.
   * @param pathConverter Converts path instances.
   * @param locationTypeConverter Converts location type instances.
   * @param locationConverter Converts location instances.
   * @param blockConverter Converts block instances.
   * @param vehicleConverter Converts vehicle instances.
   * @param visualLayoutConverter Converts visual layout instances.
   * @param propertyConverter Converts property instances.
   * @param routerService Provides methods concerning the router.
   */
  @Inject
  public PlantModelHandler(PlantModelService plantModelService,
                           KernelExecutorWrapper executorWrapper,
                           PointConverter pointConverter,
                           PathConverter pathConverter,
                           LocationTypeConverter locationTypeConverter,
                           LocationConverter locationConverter,
                           BlockConverter blockConverter,
                           VehicleConverter vehicleConverter,
                           VisualLayoutConverter visualLayoutConverter,
                           PropertyConverter propertyConverter,
                           RouterService routerService) {
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
    this.pointConverter = requireNonNull(pointConverter, "pointConverter");
    this.pathConverter = requireNonNull(pathConverter, "pathConverter");
    this.locationTypeConverter = requireNonNull(locationTypeConverter, "locationTypeConverter");
    this.locationConverter = requireNonNull(locationConverter, "locationConverter");
    this.blockConverter = requireNonNull(blockConverter, "blockConverter");
    this.vehicleConverter = requireNonNull(vehicleConverter, "vehicleConverter");
    this.visualLayoutConverter = requireNonNull(visualLayoutConverter, "visualLayoutConverter");
    this.propertyConverter = requireNonNull(propertyConverter, "propertyConverter");
    this.routerService = requireNonNull(routerService, "routerService");
  }

  public void putPlantModel(PlantModelTO putPlantModel)
      throws ObjectUnknownException,
             IllegalArgumentException {
    requireNonNull(putPlantModel, "putPlantModel");

    PlantModelCreationTO plantModelCreationTO = new PlantModelCreationTO(putPlantModel.getName())
        .withPoints(pointConverter.toPointCreationTOs(putPlantModel.getPoints()))
        .withPaths(pathConverter.toPathCreationTOs(putPlantModel.getPaths()))
        .withLocationTypes(
            locationTypeConverter.toLocationTypeCreationTOs(putPlantModel.getLocationTypes())
        )
        .withLocations(locationConverter.toLocationCreationTOs(putPlantModel.getLocations()))
        .withBlocks(blockConverter.toBlockCreationTOs(putPlantModel.getBlocks()))
        .withVehicles(vehicleConverter.toVehicleCreationTOs(putPlantModel.getVehicles()))
        .withVisualLayout(
            visualLayoutConverter.toVisualLayoutCreationTO(putPlantModel.getVisualLayout())
        )
        .withProperties(propertyConverter.toPropertyMap(putPlantModel.getProperties()));

    executorWrapper.callAndWait(() -> plantModelService.createPlantModel(plantModelCreationTO));
  }

  public PlantModelTO getPlantModel() {
    PlantModel plantModel = plantModelService.getPlantModel();
    return new PlantModelTO(plantModel.getName())
        .setPoints(pointConverter.toPointTOs(plantModel.getPoints()))
        .setPaths(pathConverter.toPathTOs(plantModel.getPaths()))
        .setLocationTypes(locationTypeConverter.toLocationTypeTOs(plantModel.getLocationTypes()))
        .setLocations(locationConverter.toLocationTOs(plantModel.getLocations()))
        .setBlocks(blockConverter.toBlockTOs(plantModel.getBlocks()))
        .setVehicles(vehicleConverter.toVehicleTOs(plantModel.getVehicles()))
        .setVisualLayout(visualLayoutConverter.toVisualLayoutTO(plantModel.getVisualLayouts()))
        .setProperties(propertyConverter.toPropertyTOs(plantModel.getProperties()));
  }

  public void requestTopologyUpdate() {
    executorWrapper.callAndWait(() -> routerService.updateRoutingTopology(Set.of()));
  }
}
