// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentcs.data.order.TransportOrder;

/**
 * Test for {@link ListSearchUtil}.
 */
public class ListSearchUtilTest {

  @Test
  public void binarySearchFindElementInListWithAllDistinctFunctionResults() {
    List<TransportOrder> list = List.of(
        new TransportOrder("t1", List.of())
            .withCreationTime(Instant.parse("2007-12-03T10:15:30.00Z")),
        new TransportOrder("t2", List.of())
            .withCreationTime(Instant.parse("2008-12-03T10:15:30.00Z")),
        new TransportOrder("t3", List.of())
            .withCreationTime(Instant.parse("2009-12-03T10:15:30.00Z")),
        new TransportOrder("t4", List.of())
            .withCreationTime(Instant.parse("2010-12-03T10:15:30.00Z")),
        new TransportOrder("t5", List.of())
            .withCreationTime(Instant.parse("2011-12-03T10:15:30.00Z")),
        new TransportOrder("t6", List.of())
            .withCreationTime(Instant.parse("2012-12-03T10:15:30.00Z")),
        new TransportOrder("t7", List.of())
            .withCreationTime(Instant.parse("2013-12-03T10:15:30.00Z"))
    );

    assertThat(
        ListSearchUtil.binarySearch(
            list,
            new TransportOrder("t3", List.of())
                .withCreationTime(Instant.parse("2009-12-03T10:15:30.00Z")),
            TransportOrder::getCreationTime
        ),
        is(2)
    );
  }

  @Test
  public void binarySearchFailForListNotContainingGivenElement() {
    List<TransportOrder> list = List.of(
        new TransportOrder("t1", List.of())
            .withCreationTime(Instant.parse("2007-12-03T10:15:30.00Z")),
        new TransportOrder("t2", List.of())
            .withCreationTime(Instant.parse("2008-12-03T10:15:30.00Z")),
        new TransportOrder("t3", List.of())
            .withCreationTime(Instant.parse("2009-12-03T10:15:30.00Z")),
        new TransportOrder("t4", List.of())
            .withCreationTime(Instant.parse("2010-12-03T10:15:30.00Z")),
        new TransportOrder("t5", List.of())
            .withCreationTime(Instant.parse("2011-12-03T10:15:30.00Z")),
        new TransportOrder("t6", List.of())
            .withCreationTime(Instant.parse("2012-12-03T10:15:30.00Z")),
        new TransportOrder("t7", List.of())
            .withCreationTime(Instant.parse("2013-12-03T10:15:30.00Z"))
    );

    assertThat(
        ListSearchUtil.binarySearch(
            list,
            new TransportOrder("t9", List.of())
                .withCreationTime(Instant.parse("2009-12-03T10:15:30.00Z")),
            TransportOrder::getCreationTime
        ),
        is(-1)
    );
  }

  @Test
  public void binarySearchFindElementInListWithIndistinctFunctionResult() {
    List<TransportOrder> list = List.of(
        new TransportOrder("t1", List.of())
            .withCreationTime(Instant.parse("2007-12-03T10:15:30.00Z")),
        new TransportOrder("t2", List.of())
            .withCreationTime(Instant.parse("2009-12-03T10:15:30.00Z")),
        new TransportOrder("t3", List.of())
            .withCreationTime(Instant.parse("2009-12-03T10:15:30.00Z")),
        new TransportOrder("t4", List.of())
            .withCreationTime(Instant.parse("2009-12-03T10:15:30.00Z")),
        new TransportOrder("t5", List.of())
            .withCreationTime(Instant.parse("2009-12-03T10:15:30.00Z")),
        new TransportOrder("t6", List.of())
            .withCreationTime(Instant.parse("2012-12-03T10:15:30.00Z")),
        new TransportOrder("t7", List.of())
            .withCreationTime(Instant.parse("2013-12-03T10:15:30.00Z"))
    );

    assertThat(
        ListSearchUtil.binarySearch(
            list,
            new TransportOrder("t5", List.of())
                .withCreationTime(Instant.parse("2009-12-03T10:15:30.00Z")),
            TransportOrder::getCreationTime
        ),
        is(4)
    );
  }

}
