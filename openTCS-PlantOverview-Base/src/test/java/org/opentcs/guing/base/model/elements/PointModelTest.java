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

/**
 * Unit tests for {@link PointModel}.
 */
public class PointModelTest {

  private PointModel pointModel;

  @BeforeEach
  public void setUp() {
    pointModel = new PointModel();
  }

  @Test
  public void setModelPositionX() {
    pointModel.getPropertyModelPositionX().setValueAndUnit(1234.0, LengthProperty.Unit.MM);

    assertThat(
        pointModel.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM),
        is(1234.0)
    );
  }

  @Test
  public void setModelPositionY() {
    pointModel.getPropertyModelPositionY().setValueAndUnit(1234.0, LengthProperty.Unit.MM);

    assertThat(
        pointModel.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM),
        is(1234.0)
    );
  }

  @Test
  public void manageVehicleModels() {
    assertThat(pointModel.getVehicleModels(), is(empty()));

    VehicleModel vehicleModel1 = new VehicleModel();
    vehicleModel1.setName("vehicle-1");
    VehicleModel vehicleModel2 = new VehicleModel();
    vehicleModel2.setName("vehicle-2");

    pointModel.addVehicleModel(vehicleModel1);
    pointModel.addVehicleModel(vehicleModel2);

    assertThat(pointModel.getVehicleModels(), hasSize(2));
    assertThat(pointModel.getVehicleModels(), containsInAnyOrder(vehicleModel1, vehicleModel2));

    pointModel.removeVehicleModel(vehicleModel1);

    assertThat(pointModel.getVehicleModels(), hasSize(1));
    assertThat(pointModel.getVehicleModels(), containsInAnyOrder(vehicleModel2));
  }

  @Test
  public void manageBlockModels() {
    assertThat(pointModel.getVehicleModels(), is(empty()));

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
