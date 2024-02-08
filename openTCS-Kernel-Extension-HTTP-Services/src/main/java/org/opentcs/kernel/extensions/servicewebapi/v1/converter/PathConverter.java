/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
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
  public PathConverter(PropertyConverter pConverter, PeripheralOperationConverter pOConverter,
                       EnvelopeConverter envelopeConverter) {
    this.pConverter = requireNonNull(pConverter, "pConverter");
    this.pOConverter = requireNonNull(pOConverter, "pOConverter");
    this.envelopeConverter = requireNonNull(envelopeConverter, "envelopeConverter");
  }

  public List<PathTO> toPathTOs(Set<Path> paths) {
    return paths.stream()
        .map(path -> new PathTO(path.getName(),
                                path.getSourcePoint().getName(),
                                path.getDestinationPoint().getName())
        .setLength(path.getLength())
        .setLocked(path.isLocked())
        .setMaxReverseVelocity(path.getMaxReverseVelocity())
        .setMaxVelocity(path.getMaxVelocity())
        .setVehicleEnvelopes(envelopeConverter.toEnvelopeTOs(path.getVehicleEnvelopes()))
        .setProperties(pConverter.toPropertyTOs(path.getProperties()))
        .setPeripheralOperations(
            pOConverter.toPeripheralOperationsTOs(path.getPeripheralOperations()))
        .setLayout(new PathTO.Layout()
            .setLayerId(path.getLayout().getLayerId())
            .setConnectionType(path.getLayout().getConnectionType().name())
            .setControlPoints(toCoupleTOs(path.getLayout().getControlPoints()))))
        .sorted(Comparator.comparing(PathTO::getName))
        .collect(Collectors.toList());
  }

  public List<PathCreationTO> toPathCreationTOs(List<PathTO> paths) {
    return paths.stream()
        .map(
            path -> new PathCreationTO(path.getName(),
                                       path.getSrcPointName(),
                                       path.getDestPointName())
                .withName(path.getName())
                .withProperties(pConverter.toPropertyMap(path.getProperties()))
                .withLength(path.getLength())
                .withMaxVelocity(path.getMaxVelocity())
                .withMaxReverseVelocity(path.getMaxReverseVelocity())
                .withLocked(path.isLocked())
                .withLayout(toPathCreationTOLayout(path.getLayout()))
                .withVehicleEnvelopes(envelopeConverter
                    .toVehicleEnvelopeMap(path.getVehicleEnvelopes()))
                .withPeripheralOperations(
                    pOConverter.toPeripheralOperationCreationTOs(path.getPeripheralOperations())))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private PathCreationTO.Layout toPathCreationTOLayout(PathTO.Layout layout) {
    return new PathCreationTO.Layout(
        Path.Layout.ConnectionType.valueOf(layout.getConnectionType()),
        layout.getControlPoints()
            .stream()
            .map(cp -> new Couple(cp.getX(), cp.getY()))
            .collect(Collectors.toList()),
        layout.getLayerId()
    );
  }

  private List<CoupleTO> toCoupleTOs(List<Couple> controlPoints) {
    return controlPoints.stream()
        .map(cp -> new CoupleTO(cp.getX(), cp.getY()))
        .collect(Collectors.toList());
  }
}
