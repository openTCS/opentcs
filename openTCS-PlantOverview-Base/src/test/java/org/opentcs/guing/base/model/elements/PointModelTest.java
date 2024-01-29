/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model.elements;

import static java.util.Map.entry;
import org.assertj.core.api.Assertions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.guing.base.AllocationState;
import org.opentcs.guing.base.components.properties.type.LengthProperty;

/**
 * Unit tests for {@link PointModel}.
 */
class PointModelTest {

  private PointModel pointModel;

  @BeforeEach
  void setUp() {
    pointModel = new PointModel();
  }

  @Test
  void setModelPositionX() {
    pointModel.getPropertyModelPositionX().setValueAndUnit(1234.0, LengthProperty.Unit.MM);

    assertThat(
        pointModel.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM),
        is(1234.0)
    );
  }

  @Test
  void setModelPositionY() {
    pointModel.getPropertyModelPositionY().setValueAndUnit(1234.0, LengthProperty.Unit.MM);

    assertThat(
        pointModel.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM),
        is(1234.0)
    );
  }

  @Test
  void manageVehicleModels() {
    assertThat(pointModel.getAllocationStates(), is(anEmptyMap()));

    VehicleModel vehicleModel1 = new VehicleModel();
    vehicleModel1.setName("vehicle-1");
    VehicleModel vehicleModel2 = new VehicleModel();
    vehicleModel2.setName("vehicle-2");

    pointModel.updateAllocationState(vehicleModel1, AllocationState.ALLOCATED);
    pointModel.updateAllocationState(vehicleModel2, AllocationState.CLAIMED);

    assertThat(pointModel.getAllocationStates(), is(aMapWithSize(2)));
    Assertions.assertThat(pointModel.getAllocationStates())
        .contains(entry(vehicleModel1, AllocationState.ALLOCATED))
        .contains(entry(vehicleModel2, AllocationState.CLAIMED));

    pointModel.clearAllocationState(vehicleModel1);

    assertThat(pointModel.getAllocationStates(), is(aMapWithSize(1)));
    Assertions.assertThat(pointModel.getAllocationStates())
        .contains(entry(vehicleModel2, AllocationState.CLAIMED));
  }

  @Test
  void manageBlockModels() {
    assertThat(pointModel.getBlockModels(), is(empty()));

    BlockModel blockModel1 = new BlockModel();
    blockModel1.setName("block-1");
    BlockModel blockModel2 = new BlockModel();
    blockModel2.setName("block-2");

    pointModel.addBlockModel(blockModel1);
    pointModel.addBlockModel(blockModel2);

    assertThat(pointModel.getBlockModels(), hasSize(2));
    assertThat(pointModel.getBlockModels(), containsInAnyOrder(blockModel1, blockModel2));

    pointModel.removeBlockModel(blockModel1);

    assertThat(pointModel.getBlockModels(), hasSize(1));
    assertThat(pointModel.getBlockModels(), containsInAnyOrder(blockModel2));
  }
}
