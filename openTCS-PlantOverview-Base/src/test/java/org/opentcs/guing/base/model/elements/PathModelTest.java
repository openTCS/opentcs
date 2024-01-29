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
import org.opentcs.guing.base.components.properties.type.SpeedProperty;

/**
 * Unit tests for {@link PathModel}.
 */
class PathModelTest {

  private PathModel pathModel;

  @BeforeEach
  void setUp() {
    pathModel = new PathModel();
  }

  @Test
  void setLength() {
    pathModel.getPropertyLength().setValueAndUnit(4321.0, LengthProperty.Unit.MM);

    assertThat(
        pathModel.getPropertyLength().getValueByUnit(LengthProperty.Unit.MM),
        is(4321.0)
    );
  }

  @Test
  void setMaxVelocity() {
    pathModel.getPropertyMaxVelocity().setValueAndUnit(900.0, SpeedProperty.Unit.MM_S);

    assertThat(
        pathModel.getPropertyMaxVelocity().getValueByUnit(SpeedProperty.Unit.MM_S),
        is(900.0)
    );
  }

  @Test
  void setMaxReverseVelocity() {
    pathModel.getPropertyMaxReverseVelocity().setValueAndUnit(900.0, SpeedProperty.Unit.MM_S);

    assertThat(
        pathModel.getPropertyMaxReverseVelocity().getValueByUnit(SpeedProperty.Unit.MM_S),
        is(900.0)
    );
  }

  @Test
  void manageVehicleModels() {
    assertThat(pathModel.getAllocationStates(), is(anEmptyMap()));

    VehicleModel vehicleModel1 = new VehicleModel();
    vehicleModel1.setName("vehicle-1");
    VehicleModel vehicleModel2 = new VehicleModel();
    vehicleModel2.setName("vehicle-2");

    pathModel.updateAllocationState(vehicleModel1, AllocationState.ALLOCATED);
    pathModel.updateAllocationState(vehicleModel2, AllocationState.CLAIMED);

    assertThat(pathModel.getAllocationStates(), is(aMapWithSize(2)));
    Assertions.assertThat(pathModel.getAllocationStates())
        .contains(entry(vehicleModel1, AllocationState.ALLOCATED))
        .contains(entry(vehicleModel2, AllocationState.CLAIMED));

    pathModel.clearAllocationState(vehicleModel1);

    assertThat(pathModel.getAllocationStates(), is(aMapWithSize(1)));
    Assertions.assertThat(pathModel.getAllocationStates())
        .contains(entry(vehicleModel2, AllocationState.CLAIMED));
  }

  @Test
  void manageBlockModels() {
    assertThat(pathModel.getBlockModels(), is(empty()));

    BlockModel blockModel1 = new BlockModel();
    blockModel1.setName("block-1");
    BlockModel blockModel2 = new BlockModel();
    blockModel2.setName("block-2");

    pathModel.addBlockModel(blockModel1);
    pathModel.addBlockModel(blockModel2);

    assertThat(pathModel.getBlockModels(), hasSize(2));
    assertThat(pathModel.getBlockModels(), containsInAnyOrder(blockModel1, blockModel2));

    pathModel.removeBlockModel(blockModel1);

    assertThat(pathModel.getBlockModels(), hasSize(1));
    assertThat(pathModel.getBlockModels(), containsInAnyOrder(blockModel2));
  }
}
