// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;

/**
 * Tests for proper serialization and deserialization for TransportOrder and OrderSequence.
 */
class OrderSerializationTest {

  OrderSerializationTest() {
  }

  @Test
  void shouldSerializeAndDeserializeTransportOrder()
      throws Exception {
    TransportOrder originalObject = createTransportOrder();
    TransportOrder deserializedObject
        = (TransportOrder) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
    assertEquals(originalObject.getProperties(), deserializedObject.getProperties());
    assertTrue(
        originalObject.getAllDriveOrders().get(0).getDestination()
            .equals(deserializedObject.getAllDriveOrders().get(0).getDestination())
    );
  }

  @Test
  void shouldSerializeAndDeserializeOrderSequence()
      throws Exception {
    OrderSequence originalObject = createOrderSequence();
    OrderSequence deserializedObject
        = (OrderSequence) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
    assertTrue(
        originalObject.getOrders().get(0)
            .equals(deserializedObject.getOrders().get(0))
    );
  }

  private TransportOrder createTransportOrder() {
    List<DriveOrder> driveOrders = new ArrayList<>();
    @SuppressWarnings("unchecked")
    Location location1 = new Location("Location1", mock(TCSObjectReference.class));
    @SuppressWarnings("unchecked")
    Location location2 = new Location("Location2", mock(TCSObjectReference.class));
    driveOrders.add(
        new DriveOrder(
            "driveOrder1",
            new DriveOrder.Destination(location1.getReference())
                .withOperation("someOperation1")
        )
    );
    driveOrders.add(
        new DriveOrder(
            "driveOrder2",
            new DriveOrder.Destination(location2.getReference())
                .withOperation("someOperation2")
        )
    );
    TransportOrder transportOrder = new TransportOrder("TransportOrder", driveOrders)
        .withProperty("someKey", "someValue");
    return transportOrder;
  }

  private OrderSequence createOrderSequence() {
    return new OrderSequence("OrderSequence")
        .withOrder(createTransportOrder().getReference());
  }

  private byte[] serializeTCSObject(TCSObject<?> tcsObject)
      throws IOException {
    byte[] serializedObject;
    try (ByteArrayOutputStream os = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(os)) {
      oos.writeObject(tcsObject);
      serializedObject = os.toByteArray();
    }

    return serializedObject;
  }

  private TCSObject<?> deserializeTCSObject(byte[] serializedObject)
      throws IOException,
        ClassNotFoundException {
    TCSObject<?> deserializedObject;
    try (ByteArrayInputStream is = new ByteArrayInputStream(serializedObject);
         ObjectInputStream ois = new ObjectInputStream(is)) {
      deserializedObject = (TCSObject) ois.readObject();
    }
    return deserializedObject;
  }
}
