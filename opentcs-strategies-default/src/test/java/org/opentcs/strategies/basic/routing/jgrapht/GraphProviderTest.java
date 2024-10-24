// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.GroupMapper;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.routing.jgrapht.GraphProvider.GraphResult;

/**
 * Tests for {@link GraphProvider}.
 */
class GraphProviderTest {

  private DefaultModelGraphMapper defaultModelGraphMapper;
  private GroupMapper groupMapper;
  private GraphMutator graphMutator;
  private GraphProvider graphProvider;

  @BeforeEach
  void setUp() {
    defaultModelGraphMapper = mock();
    groupMapper = mock();
    graphMutator = mock();
    graphProvider = new GraphProvider(
        mock(TCSObjectService.class),
        mock(GeneralModelGraphMapper.class),
        defaultModelGraphMapper,
        groupMapper,
        graphMutator
    );
  }

  @Test
  void deriveGraphWithSameKeyOnlyOnce() {
    Vehicle vehicle = new Vehicle("some-vehicle");
    when(groupMapper.apply(vehicle)).thenReturn("some-group");
    when(defaultModelGraphMapper.translateModel(anyCollection(), anyCollection(), eq(vehicle)))
        .thenReturn(new DirectedWeightedMultigraph<>(Edge.class));
    when(graphMutator.deriveGraph(anySet(), anySet(), any(GraphResult.class)))
        .thenReturn(
            new GraphResult(
                vehicle,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                new DirectedWeightedMultigraph<>(Edge.class)
            )
        );

    graphProvider.getDerivedGraphResult(vehicle, Set.of(), Set.of());
    verify(graphMutator).deriveGraph(anySet(), anySet(), any(GraphResult.class));

    graphProvider.getDerivedGraphResult(vehicle, Set.of(), Set.of());
    verifyNoMoreInteractions(graphMutator);
  }

  @Test
  void deriveGraphWithSameKeyAgainAfterInvalidation() {
    Vehicle vehicle = new Vehicle("some-vehicle");
    when(groupMapper.apply(vehicle)).thenReturn("some-group");
    when(defaultModelGraphMapper.translateModel(anyCollection(), anyCollection(), eq(vehicle)))
        .thenReturn(new DirectedWeightedMultigraph<>(Edge.class));
    when(graphMutator.deriveGraph(anySet(), anySet(), any(GraphResult.class)))
        .thenReturn(
            new GraphResult(
                vehicle,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                new DirectedWeightedMultigraph<>(Edge.class)
            )
        );

    graphProvider.getDerivedGraphResult(vehicle, Set.of(), Set.of());
    graphProvider.invalidate();
    graphProvider.getDerivedGraphResult(vehicle, Set.of(), Set.of());
    verify(graphMutator, times(2)).deriveGraph(anySet(), anySet(), any(GraphResult.class));
  }
}
