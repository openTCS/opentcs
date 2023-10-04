/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model.elements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.components.properties.type.SpeedProperty;

/**
 * Unit tests for {@link PathModel}.
 */
public class PathModelTest {

  private PathModel pathModel;

  @BeforeEach
  public void setUp() {
    pathModel = new PathModel();
  }

  @Test
  public void setLength() {
    pathModel.getPropertyLength().setValueAndUnit(4321.0, LengthProperty.Unit.MM);

    assertThat(
        pathModel.getPropertyLength().getValueByUnit(LengthProperty.Unit.MM),
        is(4321.0)
    );
  }

  @Test
  public void setMaxVelocity() {
    pathModel.getPropertyMaxVelocity().setValueAndUnit(900.0, SpeedProperty.Unit.MM_S);

    assertThat(
        pathModel.getPropertyMaxVelocity().getValueByUnit(SpeedProperty.Unit.MM_S),
        is(900.0)
    );
  }

  @Test
  public void setMaxReverseVelocity() {
    pathModel.getPropertyMaxReverseVelocity().setValueAndUnit(900.0, SpeedProperty.Unit.MM_S);

    assertThat(
        pathModel.getPropertyMaxReverseVelocity().getValueByUnit(SpeedProperty.Unit.MM_S),
        is(900.0)
    );
  }

  @Test
  public void manageVehicleModels() {
    assertThat(pathModel.getVehicleModels(), is(empty()));

    VehicleModel vehicleModel1 = new VehicleModel();
    vehicleModel1.setName("vehicle-1");
    VehicleModel vehicleModel2 = new VehicleModel();
    vehicleModel2.setName("vehicle-2");

    pathModel.addVehicleModel(vehicleModel1);
    pathModel.addVehicleModel(vehicleModel2);

    assertThat(pathModel.getVehicleModels(), hasSize(2));
    assertThat(pathModel.getVehicleModels(), containsInAnyOrder(vehicleModel1, vehicleModel2));

    pathModel.removeVehicleModel(vehicleModel1);

    assertThat(pathModel.getVehicleModels(), hasSize(1));
    assertThat(pathModel.getVehicleModels(), containsInAnyOrder(vehicleModel2));
  }

  @Test
  public void manageBlockModels() {
    assertThat(pathModel.getVehicleModels(), is(empty()));

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
