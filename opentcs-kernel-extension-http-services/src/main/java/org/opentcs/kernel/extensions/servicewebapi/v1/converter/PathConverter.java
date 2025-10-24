// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Path;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PathTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;

/**
 * Includes the conversion methods for all Path classes.
 */
public class PathConverter {

  private final PropertyConverter pConverter;
  private final PeripheralOperationConverter pOConverter;
  private final EnvelopeConverter envelopeConverter;

  @Inject
  public PathConverter(
      PropertyConverter pConverter, PeripheralOperationConverter pOConverter,
      EnvelopeConverter envelopeConverter
  ) {
    this.pConverter = requireNonNull(pConverter, "pConverter");
    this.pOConverter = requireNonNull(pOConverter, "pOConverter");
    this.envelopeConverter = requireNonNull(envelopeConverter, "envelopeConverter");
  }

  public List<PathTO> toPathTOs(Set<Path> paths) {
    return paths.stream()
        .map(
            path -> new PathTO(
                path.getName(),
                path.getSourcePoint().getName(),
                path.getDestinationPoint().getName()
            )
                .setLength(path.getLength())
                .setLocked(path.isLocked())
                .setMaxReverseVelocity(path.getMaxReverseVelocity())
                .setMaxVelocity(path.getMaxVelocity())
                .setVehicleEnvelopes(envelopeConverter.toEnvelopeTOs(path.getVehicleEnvelopes()))
                .setProperties(pConverter.toPropertyTOs(path.getProperties()))
                .setPeripheralOperations(
                    pOConverter.toPeripheralOperationsTOs(path.getPeripheralOperations())
                )
                .setLayout(
                    new PathTO.Layout()
                        .setLayerId(path.getLayout().getLayerId())
                        .setConnectionType(
                            toPathTOConnectionType(path.getLayout().getConnectionType())
                        )
                        .setControlPoints(toCoupleTOs(path.getLayout().getControlPoints()))
                )
        )
        .sorted(Comparator.comparing(PathTO::getName))
        .collect(Collectors.toList());
  }

  public List<PathCreationTO> toPathCreationTOs(List<PathTO> paths) {
    return paths.stream()
        .map(
            path -> new PathCreationTO(
                path.getName(),
                path.getSrcPointName(),
                path.getDestPointName()
            )
                .withName(path.getName())
                .withProperties(pConverter.toPropertyMap(path.getProperties()))
                .withLength(path.getLength())
                .withMaxVelocity(path.getMaxVelocity())
                .withMaxReverseVelocity(path.getMaxReverseVelocity())
                .withLocked(path.isLocked())
                .withLayout(toPathCreationTOLayout(path.getLayout()))
                .withVehicleEnvelopes(
                    envelopeConverter
                        .toVehicleEnvelopeMap(path.getVehicleEnvelopes())
                )
                .withPeripheralOperations(
                    pOConverter.toPeripheralOperationCreationTOs(path.getPeripheralOperations())
                )
        )
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private PathCreationTO.Layout toPathCreationTOLayout(PathTO.Layout layout) {
    return new PathCreationTO.Layout(
        toPathConnectionType(layout.getConnectionType()),
        layout.getControlPoints()
            .stream()
            .map(cp -> new CoupleCreationTO(cp.getX(), cp.getY()))
            .collect(Collectors.toList()),
        layout.getLayerId()
    );
  }

  private List<CoupleTO> toCoupleTOs(List<Couple> controlPoints) {
    return controlPoints.stream()
        .map(cp -> new CoupleTO(cp.getX(), cp.getY()))
        .collect(Collectors.toList());
  }

  private PathCreationTO.Layout.ConnectionType toPathConnectionType(
      PathTO.Layout.ConnectionType connectionType
  ) {
    return switch (connectionType) {
      case BEZIER -> PathCreationTO.Layout.ConnectionType.BEZIER;
      case BEZIER_3 -> PathCreationTO.Layout.ConnectionType.BEZIER_3;
      case DIRECT -> PathCreationTO.Layout.ConnectionType.DIRECT;
      case ELBOW -> PathCreationTO.Layout.ConnectionType.ELBOW;
      case POLYPATH -> PathCreationTO.Layout.ConnectionType.POLYPATH;
      case SLANTED -> PathCreationTO.Layout.ConnectionType.SLANTED;
    };
  }

  private PathTO.Layout.ConnectionType toPathTOConnectionType(
      Path.Layout.ConnectionType connectionType
  ) {
    return switch (connectionType) {
      case BEZIER -> PathTO.Layout.ConnectionType.BEZIER;
      case BEZIER_3 -> PathTO.Layout.ConnectionType.BEZIER_3;
      case DIRECT -> PathTO.Layout.ConnectionType.DIRECT;
      case ELBOW -> PathTO.Layout.ConnectionType.ELBOW;
      case POLYPATH -> PathTO.Layout.ConnectionType.POLYPATH;
      case SLANTED -> PathTO.Layout.ConnectionType.SLANTED;
    };
  }
}
