// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Comparator;
import org.opentcs.data.model.PlantModel;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.GetPlantModelResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.BlockTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PathTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PointTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.VehicleTO;

/**
 * Provides methods to convert {@link PlantModel}s to their web API representation.
 */
public class PlantModelConverter {

  private final PointConverter pointConverter;
  private final PathConverter pathConverter;
  private final LocationTypeConverter locationTypeConverter;
  private final LocationConverter locationConverter;
  private final BlockConverter blockConverter;
  private final VehicleConverter vehicleConverter;
  private final VisualLayoutConverter visualLayoutConverter;

  @Inject
  public PlantModelConverter(
      PointConverter pointConverter,
      PathConverter pathConverter,
      LocationTypeConverter locationTypeConverter,
      LocationConverter locationConverter,
      BlockConverter blockConverter,
      VehicleConverter vehicleConverter,
      VisualLayoutConverter visualLayoutConverter
  ) {
    this.pointConverter = requireNonNull(pointConverter, "pointConverter");
    this.pathConverter = requireNonNull(pathConverter, "pathConverter");
    this.locationTypeConverter = requireNonNull(locationTypeConverter, "locationTypeConverter");
    this.locationConverter = requireNonNull(locationConverter, "locationConverter");
    this.blockConverter = requireNonNull(blockConverter, "blockConverter");
    this.vehicleConverter = requireNonNull(vehicleConverter, "vehicleConverter");
    this.visualLayoutConverter = requireNonNull(visualLayoutConverter, "visualLayoutConverter");
  }

  /**
   * Converts a {@link PlantModel} to its web API representation.
   *
   * @param plantModel The plant model to convert.
   * @return The converted plant model.
   */
  public GetPlantModelResponseTO convert(PlantModel plantModel) {
    return new GetPlantModelResponseTO()
        .setName(plantModel.getName())
        .setProperties(plantModel.getProperties())
        .setPoints(
            plantModel.getPoints().stream()
                .map(pointConverter::convert)
                .sorted(Comparator.comparing(PointTO::getName))
                .toList()
        )
        .setPaths(
            plantModel.getPaths().stream()
                .map(pathConverter::convert)
                .sorted(Comparator.comparing(PathTO::getName))
                .toList()
        )
        .setLocationTypes(
            plantModel.getLocationTypes().stream()
                .map(locationTypeConverter::convert)
                .sorted(Comparator.comparing(LocationTypeTO::getName))
                .toList()
        )
        .setLocations(
            plantModel.getLocations().stream()
                .map(locationConverter::convert)
                .sorted(Comparator.comparing(LocationTO::getName))
                .toList()
        )
        .setBlocks(
            plantModel.getBlocks().stream()
                .map(blockConverter::convert)
                .sorted(Comparator.comparing(BlockTO::getName))
                .toList()
        )
        .setVehicles(
            plantModel.getVehicles().stream()
                .map(vehicleConverter::convert)
                .sorted(Comparator.comparing(VehicleTO::getName))
                .toList()
        )
        .setVisualLayout(
            visualLayoutConverter.convert(plantModel.getVisualLayout())
        );
  }
}
