/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;

/**
 * Tests for proper serialization and deserialization for TransportOrder and OrderSequence
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class OrderSerializationTest {

  public OrderSerializationTest() {
  }

  @Test
  public void shouldSerializeAndDeserializeTransportOrder()
      throws Exception {
    TransportOrder originalObject = createTransportOrder();
    TransportOrder deserializedObject = (TransportOrder) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
    assertEquals(originalObject.getProperties(), deserializedObject.getProperties());
    assertTrue(originalObject.getAllDriveOrders().get(0).getDestination()
        .equals(deserializedObject.getAllDriveOrders().get(0).getDestination()));
  }

  @Test
  public void shouldSerializeAndDeserializeOrderSequence()
      throws Exception {
    OrderSequence originalObject = createOrderSequence();
    OrderSequence deserializedObject = (OrderSequence) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
    assertTrue(originalObject.getOrders().get(0)
        .equals(deserializedObject.getOrders().get(0)));
  }

  private TransportOrder createTransportOrder() {
    List<DriveOrder> driveOrders = new ArrayList<>();
    @SuppressWarnings("unchecked")
    Location location1 = new Location("Location1", mock(TCSObjectReference.class));
    @SuppressWarnings("unchecked")
    Location location2 = new Location("Location2", mock(TCSObjectReference.class));
    driveOrders.add(new DriveOrder(new DriveOrder.Destination(location1.getReference())
        .withOperation("someOperation1")));
    driveOrders.add(new DriveOrder(new DriveOrder.Destination(location2.getReference())
        .withOperation("someOperation2")));
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
      throws IOException, ClassNotFoundException {
    TCSObject<?> deserializedObject;
    try (ByteArrayInputStream is = new ByteArrayInputStream(serializedObject);
         ObjectInputStream ois = new ObjectInputStream(is)) {
      deserializedObject = (TCSObject) ois.readObject();
    }
    return deserializedObject;
  }
}
