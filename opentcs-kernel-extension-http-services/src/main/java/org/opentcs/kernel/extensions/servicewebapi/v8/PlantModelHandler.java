// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.kernel.extensions.servicewebapi.common.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostTopologyUpdateRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PutPlantModelRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter.BlockConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter.LocationConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter.LocationTypeConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter.PathConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter.PointConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter.PropertyConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter.VehicleConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter.VisualLayoutConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.GetPlantModelResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.PlantModelConverter;

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

  private final PlantModelConverter plantModelConverter;
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
   * @param plantModelConverter Converts plant model instances.
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
  public PlantModelHandler(
      PlantModelService plantModelService,
      KernelExecutorWrapper executorWrapper,
      PlantModelConverter plantModelConverter,
      PointConverter pointConverter,
      PathConverter pathConverter,
      LocationTypeConverter locationTypeConverter,
      LocationConverter locationConverter,
      BlockConverter blockConverter,
      VehicleConverter vehicleConverter,
      VisualLayoutConverter visualLayoutConverter,
      PropertyConverter propertyConverter,
      RouterService routerService
  ) {
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
    this.plantModelConverter = requireNonNull(plantModelConverter, "plantModelConverter");
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

  public void putPlantModel(PutPlantModelRequestTO putPlantModel)
      throws ObjectUnknownException,
        IllegalArgumentException {
    requireNonNull(putPlantModel, "putPlantModel");

    PlantModelCreationTO plantModelCreationTO = new PlantModelCreationTO(putPlantModel.getName())
        .withPoints(pointConverter.toPointCreationTOs(putPlantModel.getPoints()))
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

  public GetPlantModelResponseTO
      getPlantModel() {
    return plantModelConverter.convert(plantModelService.getPlantModel());
  }

  public void requestTopologyUpdate(PostTopologyUpdateRequestTO request)
      throws ObjectUnknownException {
    executorWrapper.callAndWait(
        () -> routerService.updateRoutingTopology(toResourceReferences(request.getPaths()))
    );
  }

  private Set<TCSObjectReference<Path>> toResourceReferences(List<String> paths) {
    Set<TCSObjectReference<Path>> pathsToUpdate = new HashSet<>();

    for (String name : paths) {
      Path path = plantModelService.fetch(Path.class, name)
          .orElseThrow(() -> new ObjectUnknownException("Unknown path: " + name));
      pathsToUpdate.add(path.getReference());
    }

    return pathsToUpdate;
  }
}
