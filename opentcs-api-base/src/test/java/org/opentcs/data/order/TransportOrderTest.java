// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Point;

/**
 * Unit tests for {@link TransportOrder}.
 */
class TransportOrderTest {

  @Test
  void progressInDriveOrderViewsWithIndex() {
    TransportOrder transportOrder
        = new TransportOrder(
            "some-order",
            List.of(
                new DriveOrder(
                    "driveOrder1",
                    new DriveOrder.Destination(new Point("point1").getReference())
                ),
                new DriveOrder(
                    "driveOrder2",
                    new DriveOrder.Destination(new Point("point2").getReference())
                ),
                new DriveOrder(
                    "driveOrder3",
                    new DriveOrder.Destination(new Point("point3").getReference())
                )
            )
        );

    assertThat(transportOrder.getAllDriveOrders()).hasSize(3);
    assertThat(transportOrder.getPastDriveOrders()).hasSize(0);
    assertThat(transportOrder.getCurrentDriveOrder()).isNull();
    assertThat(transportOrder.getFutureDriveOrders()).hasSize(3);

    transportOrder = transportOrder.withCurrentDriveOrderIndex(0);

    assertThat(transportOrder.getAllDriveOrders()).hasSize(3);
    assertThat(transportOrder.getPastDriveOrders()).hasSize(0);
    assertThat(transportOrder.getCurrentDriveOrder()).isNotNull();
    assertThat(transportOrder.getFutureDriveOrders()).hasSize(2);

    transportOrder = transportOrder.withCurrentDriveOrderIndex(1);

    assertThat(transportOrder.getAllDriveOrders()).hasSize(3);
    assertThat(transportOrder.getPastDriveOrders()).hasSize(1);
    assertThat(transportOrder.getCurrentDriveOrder()).isNotNull();
    assertThat(transportOrder.getFutureDriveOrders()).hasSize(1);

    transportOrder = transportOrder.withCurrentDriveOrderIndex(2);

    assertThat(transportOrder.getAllDriveOrders()).hasSize(3);
    assertThat(transportOrder.getPastDriveOrders()).hasSize(2);
    assertThat(transportOrder.getCurrentDriveOrder()).isNotNull();
    assertThat(transportOrder.getFutureDriveOrders()).hasSize(0);

    transportOrder = transportOrder.withCurrentDriveOrderIndex(3);

    assertThat(transportOrder.getAllDriveOrders()).hasSize(3);
    assertThat(transportOrder.getPastDriveOrders()).hasSize(3);
    assertThat(transportOrder.getCurrentDriveOrder()).isNull();
    assertThat(transportOrder.getFutureDriveOrders()).hasSize(0);
  }
}
