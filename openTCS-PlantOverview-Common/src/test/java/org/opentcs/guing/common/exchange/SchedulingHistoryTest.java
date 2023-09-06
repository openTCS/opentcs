/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange;

import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.guing.base.model.FigureDecorationDetails;
import org.opentcs.guing.base.model.elements.PointModel;

/**
 * Unit tests for {@link SchedulingHistory}.
 */
public class SchedulingHistoryTest {

  private SchedulingHistory schedulingHistory;

  @BeforeEach
  public void setUp() {
    schedulingHistory = new SchedulingHistory();
  }

  @Test
  public void returnEmptySetInitially() {
    assertThat(
        schedulingHistory.updateAllocatedAndClaimedComponents("some-vehicle", Set.of()),
        is(empty())
    );
  }

  @Test
  public void returnEmptySetOnExtension() {
    PointModel point1 = new PointModel();
    PointModel point2 = new PointModel();

    schedulingHistory.updateAllocatedAndClaimedComponents(
        "some-vehicle",
        Set.of(point1, point2)
    );

    Set<FigureDecorationDetails> result
        = schedulingHistory.updateAllocatedAndClaimedComponents(
            "some-vehicle",
            Set.of(point1, point2, new PointModel())
        );

    assertThat(result, is(empty()));
  }

  @Test
  public void returnDifferenceOnReduction() {
    PointModel point1 = new PointModel();
    PointModel point2 = new PointModel();

    schedulingHistory.updateAllocatedAndClaimedComponents(
        "some-vehicle",
        Set.of(point1, point2)
    );

    Set<FigureDecorationDetails> result
        = schedulingHistory.updateAllocatedAndClaimedComponents("some-vehicle", Set.of(point2));

    assertThat(result, hasSize(1));
    assertThat(result, contains(point1));
  }

  @Test
  public void returnDifferenceOnExchange() {
    PointModel point1 = new PointModel();
    PointModel point2 = new PointModel();

    schedulingHistory.updateAllocatedAndClaimedComponents(
        "some-vehicle",
        Set.of(point1, point2)
    );

    Set<FigureDecorationDetails> result
        = schedulingHistory.updateAllocatedAndClaimedComponents(
            "some-vehicle",
            Set.of(new PointModel())
        );

    assertThat(result, hasSize(2));
    assertThat(result, containsInAnyOrder(point1, point2));
  }

}
