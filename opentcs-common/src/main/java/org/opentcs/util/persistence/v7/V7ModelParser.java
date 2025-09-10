// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v7;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.util.persistence.v6.V6ModelParser;
import org.opentcs.util.persistence.v6.V6PlantModelTO;
import org.semver4j.Semver;
import org.semver4j.SemverException;

/**
 * The parser for V7 models.
 */
public class V7ModelParser {

  /**
   * The maximum supported schema version for model files.
   */
  private static final Semver V7_SUPPORTED_VERSION = new Semver(V7PlantModelTO.VERSION_STRING);

  /**
   * Creates a new instance.
   */
  public V7ModelParser() {
  }

  /**
   * Reads a model with the given reader and parses it to a {@link PlantModelCreationTO} instance.
   *
   * @param reader The reader to use.
   * @param modelVersion The model version.
   * @return The parsed {@link PlantModelCreationTO}.
   * @throws IOException If there was an error reading the model.
   */
  public PlantModelCreationTO read(Reader reader, String modelVersion)
      throws IOException {
    return new V7TOMapper().map(readRaw(reader, modelVersion));
  }

  /**
   * Reads a model with the given reader and parses it to a {@link V7PlantModelTO} instance.
   *
   * @param reader The reader to use.
   * @param modelVersion The model version.
   * @return The parsed {@link V7PlantModelTO}.
   * @throws IOException If there was an error reading the model.
   */
  public V7PlantModelTO readRaw(
      @Nonnull
      Reader reader,
      @Nonnull
      String modelVersion
  )
      throws IOException {
    requireNonNull(reader, "reader");
    requireNonNull(modelVersion, "modelVersion");

    Semver fileVersionNumber;
    try {
      fileVersionNumber = new Semver(modelVersion);
    }
    catch (SemverException e) {
      throw new IOException(e);
    }

    if (fileVersionNumber.getMajor() == V7_SUPPORTED_VERSION.getMajor()
        && fileVersionNumber.isLowerThanOrEqualTo(V7_SUPPORTED_VERSION)) {
      return V7PlantModelTO.fromXml(reader);
    }
    else {
      return convert(new V6ModelParser().readRaw(reader, modelVersion));
    }
  }

  private V7PlantModelTO convert(V6PlantModelTO to) {
    return new V7PlantModelTO()
        .setName(to.getName())
        .setPoints(convertPoints(to))
        .setPaths(convertPaths(to))
        .setVehicles(convertVehicles(to))
        .setLocationTypes(convertLocationTypes(to))
        .setLocations(convertLocations(to))
        .setBlocks(convertBlocks(to))
        .setVisualLayout(convertVisualLayout(to))
        .setProperties(convertProperties(to.getProperties()));
  }

  private List<PropertyTO> convertProperties(
      List<org.opentcs.util.persistence.v6.PropertyTO> tos
  ) {
    return tos.stream()
        .map(property -> new PropertyTO().setName(property.getName()).setValue(property.getValue()))
        .toList();
  }

  private List<PointTO> convertPoints(V6PlantModelTO to) {
    return to.getPoints().stream()
        .map(point -> {
          PointTO result = new PointTO();
          result.setName(point.getName())
              .setProperties(convertProperties(point.getProperties()));
          result.setPositionX(point.getPositionX())
              .setPositionY(point.getPositionY())
              .setPositionZ(point.getPositionZ())
              .setVehicleOrientationAngle(point.getVehicleOrientationAngle())
              .setType(convertPointTOType(point.getType()))
              .setVehicleEnvelopes(convertVehicleEnvelopes(point.getVehicleEnvelopes()))
              .setOutgoingPaths(convertOutgoingPaths(point))
              .setMaxVehicleBoundingBox(convertBoundingBox(point.getMaxVehicleBoundingBox()))
              .setPointLayout(
                  new PointTO.PointLayout()
                      .setLabelOffsetX(point.getPointLayout().getLabelOffsetX())
                      .setLabelOffsetY(point.getPointLayout().getLabelOffsetY())
                      .setLayerId(point.getPointLayout().getLayerId())
              );
          return result;
        })
        .toList();
  }

  private List<VehicleEnvelopeTO> convertVehicleEnvelopes(
      List<org.opentcs.util.persistence.v6.VehicleEnvelopeTO> tos
  ) {
    return tos.stream()
        .map(
            vehicleEnvelope -> new VehicleEnvelopeTO()
                .setKey(vehicleEnvelope.getKey())
                .setVertices(
                    vehicleEnvelope.getVertices().stream()
                        .map(
                            couple -> new CoupleTO()
                                .setX(couple.getX())
                                .setY(couple.getY())
                        )
                        .toList()
                )
        )
        .toList();
  }

  private List<PointTO.OutgoingPath> convertOutgoingPaths(
      org.opentcs.util.persistence.v6.PointTO to
  ) {
    return to.getOutgoingPaths().stream()
        .map(path -> new PointTO.OutgoingPath().setName(path.getName()))
        .toList();
  }

  private List<PathTO> convertPaths(V6PlantModelTO to) {
    return to.getPaths().stream()
        .map(path -> {
          PathTO result = new PathTO();
          result.setName(path.getName())
              .setProperties(convertProperties(path.getProperties()));
          result.setSourcePoint(path.getSourcePoint())
              .setDestinationPoint(path.getDestinationPoint())
              .setLength(path.getLength())
              .setMaxVelocity(path.getMaxVelocity())
              .setMaxReverseVelocity(path.getMaxReverseVelocity())
              .setPeripheralOperations(convertPeripheralOperations(path.getPeripheralOperations()))
              .setLocked(path.isLocked())
              .setVehicleEnvelopes(convertVehicleEnvelopes(path.getVehicleEnvelopes()))
              .setPathLayout(
                  new PathTO.PathLayout()
                      .setConnectionType(
                          convertConnectionType(path.getPathLayout().getConnectionType())
                      )
                      .setControlPoints(
                          path.getPathLayout().getControlPoints().stream()
                              .map(
                                  controlPoint -> new PathTO.ControlPoint()
                                      .setX(controlPoint.getX())
                                      .setY(controlPoint.getY())
                              )
                              .toList()
                      )
                      .setLayerId(path.getPathLayout().getLayerId())
              );
          return result;
        })
        .toList();
  }

  private BoundingBoxTO convertBoundingBox(org.opentcs.util.persistence.v6.BoundingBoxTO to) {
    return new BoundingBoxTO()
        .setLength(to.getLength())
        .setWidth(to.getWidth())
        .setHeight(to.getHeight())
        .setReferenceOffsetX(to.getReferenceOffsetX())
        .setReferenceOffsetY(to.getReferenceOffsetY());
  }

  private List<PeripheralOperationTO> convertPeripheralOperations(
      List<org.opentcs.util.persistence.v6.PeripheralOperationTO> tos
  ) {
    return tos.stream()
        .map(
            peripheralOperation -> {
              PeripheralOperationTO result = new PeripheralOperationTO();
              result.setName(peripheralOperation.getName())
                  .setProperties(convertProperties(peripheralOperation.getProperties()));
              result.setLocationName(peripheralOperation.getLocationName())
                  .setExecutionTrigger(
                      convertExecutionTrigger(peripheralOperation.getExecutionTrigger())
                  )
                  .setCompletionRequired(peripheralOperation.isCompletionRequired());
              return result;
            }
        )
        .toList();
  }

  private List<VehicleTO> convertVehicles(V6PlantModelTO to) {
    return to.getVehicles().stream()
        .map(vehicle -> {
          VehicleTO result = new VehicleTO();
          result.setName(vehicle.getName())
              .setProperties(convertProperties(vehicle.getProperties()));
          result.setEnergyLevelCritical(vehicle.getEnergyLevelCritical())
              .setEnergyLevelGood(vehicle.getEnergyLevelGood())
              .setEnergyLevelFullyRecharged(vehicle.getEnergyLevelFullyRecharged())
              .setEnergyLevelSufficientlyRecharged(vehicle.getEnergyLevelSufficientlyRecharged())
              .setMaxVelocity(vehicle.getMaxVelocity())
              .setMaxReverseVelocity(vehicle.getMaxReverseVelocity())
              .setEnvelopeKey(vehicle.getEnvelopeKey())
              .setBoundingBox(convertBoundingBox(vehicle.getBoundingBox()))
              .setVehicleLayout(
                  new VehicleTO.VehicleLayout()
                      .setColor(vehicle.getVehicleLayout().getColor())
              );
          return result;
        })
        .toList();
  }

  private List<LocationTypeTO> convertLocationTypes(V6PlantModelTO to) {
    return to.getLocationTypes().stream()
        .map(locationType -> {
          LocationTypeTO result = new LocationTypeTO();
          result.setName(locationType.getName())
              .setProperties(convertProperties(locationType.getProperties()));
          result.setAllowedOperations(convertAllowedOperations(locationType.getAllowedOperations()))
              .setAllowedPeripheralOperations(
                  convertAllowedPeripheralOperations(locationType.getAllowedPeripheralOperations())
              )
              .setLocationTypeLayout(
                  new LocationTypeTO.LocationTypeLayout()
                      .setLocationRepresentation(
                          convertLocationRepresentation(
                              locationType.getLocationTypeLayout().getLocationRepresentation()
                          )
                      )
              );
          return result;
        })
        .toList();
  }

  private List<AllowedOperationTO> convertAllowedOperations(
      List<org.opentcs.util.persistence.v6.AllowedOperationTO> tos
  ) {
    return tos.stream()
        .map(allowedOperation -> {
          AllowedOperationTO result = new AllowedOperationTO();
          result.setName(allowedOperation.getName());
          result.setProperties(convertProperties(allowedOperation.getProperties()));
          return result;
        })
        .toList();
  }

  private List<AllowedPeripheralOperationTO> convertAllowedPeripheralOperations(
      List<org.opentcs.util.persistence.v6.AllowedPeripheralOperationTO> tos
  ) {
    return tos.stream()
        .map(
            allowedPeripheralOperation -> {
              AllowedPeripheralOperationTO result = new AllowedPeripheralOperationTO();
              result.setName(allowedPeripheralOperation.getName());
              result.setProperties(convertProperties(allowedPeripheralOperation.getProperties()));
              return result;
            }
        )
        .toList();
  }

  private List<LocationTO> convertLocations(V6PlantModelTO to) {
    return to.getLocations().stream()
        .map(location -> {
          LocationTO result = new LocationTO();
          result.setName(location.getName())
              .setProperties(convertProperties(location.getProperties()));
          result.setPositionX(location.getPositionX())
              .setPositionY(location.getPositionY())
              .setPositionZ(location.getPositionZ())
              .setType(location.getType())
              .setLinks(convertLinks(location))
              .setLocked(location.isLocked())
              .setLocationLayout(
                  new LocationTO.LocationLayout()
                      .setLabelOffsetX(location.getLocationLayout().getLabelOffsetX())
                      .setLabelOffsetY(location.getLocationLayout().getLabelOffsetY())
                      .setLocationRepresentation(
                          convertLocationRepresentation(
                              location.getLocationLayout().getLocationRepresentation()
                          )
                      )
                      .setLayerId(location.getLocationLayout().getLayerId())
              );
          return result;
        })
        .toList();
  }

  private List<LocationTO.Link> convertLinks(org.opentcs.util.persistence.v6.LocationTO to) {
    return to.getLinks().stream()
        .map(link -> {
          return new LocationTO.Link()
              .setPoint(link.getPoint())
              .setAllowedOperations(convertAllowedOperations(link.getAllowedOperations()));
        })
        .toList();
  }

  private List<BlockTO> convertBlocks(V6PlantModelTO to) {
    return to.getBlocks().stream()
        .map(block -> {
          BlockTO result = new BlockTO();
          result.setName(block.getName())
              .setProperties(convertProperties(block.getProperties()));
          result.setType(convertBlockTOType(block.getType()))
              .setMembers(convertMembers(block.getMembers()))
              .setBlockLayout(
                  new BlockTO.BlockLayout()
                      .setColor(block.getBlockLayout().getColor())
              );
          return result;
        })
        .toList();
  }

  private List<MemberTO> convertMembers(List<org.opentcs.util.persistence.v6.MemberTO> tos) {
    return tos.stream()
        .map(member -> {
          MemberTO result = new MemberTO();
          result.setName(member.getName())
              .setProperties(convertProperties(member.getProperties()));
          return result;
        })
        .toList();
  }

  private VisualLayoutTO convertVisualLayout(V6PlantModelTO to) {
    VisualLayoutTO result = new VisualLayoutTO()
        .setScaleX(to.getVisualLayout().getScaleX())
        .setScaleY(to.getVisualLayout().getScaleY())
        .setLayers(
            to.getVisualLayout().getLayers().stream()
                .map(
                    layer -> new VisualLayoutTO.Layer()
                        .setId(layer.getId())
                        .setOrdinal(layer.getOrdinal())
                        .setVisible(layer.isVisible())
                        .setName(layer.getName())
                        .setGroupId(layer.getGroupId())
                )
                .toList()
        )
        .setLayerGroups(
            to.getVisualLayout().getLayerGroups().stream()
                .map(
                    layerGroup -> new VisualLayoutTO.LayerGroup()
                        .setId(layerGroup.getId())
                        .setName(layerGroup.getName())
                        .setVisible(layerGroup.isVisible())
                )
                .toList()
        );
    result
        .setProperties(convertProperties(to.getVisualLayout().getProperties()))
        .setName(to.getVisualLayout().getName());

    return result;
  }

  private PointTO.Type convertPointTOType(org.opentcs.util.persistence.v6.PointTO.Type type) {
    return switch (type) {
      case HALT_POSITION -> PointTO.Type.HALT_POSITION;
      case PARK_POSITION -> PointTO.Type.PARK_POSITION;
    };
  }

  private PathTO.PathLayout.ConnectionType convertConnectionType(
      org.opentcs.util.persistence.v6.PathTO.PathLayout.ConnectionType connectionType
  ) {
    return switch (connectionType) {
      case BEZIER -> PathTO.PathLayout.ConnectionType.BEZIER;
      case BEZIER_3 -> PathTO.PathLayout.ConnectionType.BEZIER_3;
      case DIRECT -> PathTO.PathLayout.ConnectionType.DIRECT;
      case ELBOW -> PathTO.PathLayout.ConnectionType.ELBOW;
      case POLYPATH -> PathTO.PathLayout.ConnectionType.POLYPATH;
      case SLANTED -> PathTO.PathLayout.ConnectionType.SLANTED;
    };
  }

  private PeripheralOperationTO.ExecutionTrigger convertExecutionTrigger(
      org.opentcs.util.persistence.v6.PeripheralOperationTO.ExecutionTrigger executionTrigger
  ) {
    return switch (executionTrigger) {
      case AFTER_ALLOCATION -> PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION;
      case AFTER_MOVEMENT -> PeripheralOperationTO.ExecutionTrigger.AFTER_MOVEMENT;
    };
  }

  private LocationRepresentation convertLocationRepresentation(
      org.opentcs.util.persistence.v6.LocationRepresentation locRepresentation
  ) {
    return switch (locRepresentation) {
      case DEFAULT -> LocationRepresentation.DEFAULT;
      case LOAD_TRANSFER_ALT_1 -> LocationRepresentation.LOAD_TRANSFER_ALT_1;
      case LOAD_TRANSFER_ALT_2 -> LocationRepresentation.LOAD_TRANSFER_ALT_2;
      case LOAD_TRANSFER_ALT_3 -> LocationRepresentation.LOAD_TRANSFER_ALT_3;
      case LOAD_TRANSFER_ALT_4 -> LocationRepresentation.LOAD_TRANSFER_ALT_4;
      case LOAD_TRANSFER_ALT_5 -> LocationRepresentation.LOAD_TRANSFER_ALT_5;
      case LOAD_TRANSFER_GENERIC -> LocationRepresentation.LOAD_TRANSFER_GENERIC;
      case NONE -> LocationRepresentation.NONE;
      case RECHARGE_ALT_1 -> LocationRepresentation.RECHARGE_ALT_1;
      case RECHARGE_ALT_2 -> LocationRepresentation.RECHARGE_ALT_2;
      case RECHARGE_GENERIC -> LocationRepresentation.RECHARGE_GENERIC;
      case WORKING_ALT_1 -> LocationRepresentation.WORKING_ALT_1;
      case WORKING_ALT_2 -> LocationRepresentation.WORKING_ALT_2;
      case WORKING_GENERIC -> LocationRepresentation.WORKING_GENERIC;
    };
  }

  private BlockTO.Type convertBlockTOType(org.opentcs.util.persistence.v6.BlockTO.Type type) {
    return switch (type) {
      case SAME_DIRECTION_ONLY -> BlockTO.Type.SAME_DIRECTION_ONLY;
      case SINGLE_VEHICLE_ONLY -> BlockTO.Type.SINGLE_VEHICLE_ONLY;
    };
  }
}
