// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.AcceptableOrderTypeTO;

/**
 * Tests for {@link AcceptableOrderTypeConverter}.
 */
public class AcceptableOrderTypeConverterTest {
  private AcceptableOrderTypeConverter acceptableOrderTypeConverter;

  @BeforeEach
  void setUp() {
    acceptableOrderTypeConverter = new AcceptableOrderTypeConverter();
  }

  @Test
  void checkToAcceptableOrderTypeTOs() {
    Set<AcceptableOrderType> acceptableOrderTypes = Set.of(
        new AcceptableOrderType("order-type1", 1),
        new AcceptableOrderType("order-type2", 0),
        new AcceptableOrderType("order-type3", 1)
    );
    List<AcceptableOrderTypeTO> result = acceptableOrderTypeConverter.toAcceptableOrderTypeTOs(
        acceptableOrderTypes
    );

    assertThat(result, hasSize(3));
    assertThat(result.get(0), samePropertyValuesAs(new AcceptableOrderTypeTO("order-type2", 0)));
    assertThat(result.get(1), samePropertyValuesAs(new AcceptableOrderTypeTO("order-type1", 1)));
    assertThat(result.get(2), samePropertyValuesAs(new AcceptableOrderTypeTO("order-type3", 1)));
  }

  @Test
  void checkToAcceptableOrderTypes() {
    List<AcceptableOrderTypeTO> acceptableOrderTypeTOs = List.of(
        new AcceptableOrderTypeTO("order-type", 2)
    );
    Set<AcceptableOrderType> result = acceptableOrderTypeConverter.toAcceptableOrderTypes(
        acceptableOrderTypeTOs
    );

    assertThat(result, hasSize(1));
    assertThat(result, hasItem(new AcceptableOrderType("order-type", 2)));
  }
}
