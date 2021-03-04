/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;

/**
 * Tests for proper serialization and deserialization of classes derived by TCSObject
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ModelSerializationTest {

  public ModelSerializationTest() {
  }

  @Test
  public void shouldSerializeAndDeserializeBlock()
      throws Exception {
    Block originalObject = new Block("Block1");
    Block deserializedObject = (Block) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
  }

  @Test
  public void shouldSerializeAndDeserializeGroup()
      throws Exception {
    Group originalObject = new Group("Group1");
    Group deserializedObject = (Group) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
  }

  @Test
  public void shouldSerializeAndDeserializeLocation()
      throws Exception {
    @SuppressWarnings("unchecked")
    Location originalObject = new Location("Location1", mock(TCSObjectReference.class));
    Location deserializedObject = (Location) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
  }

  @Test
  public void shouldSerializeAndDeserializeLocationType()
      throws Exception {
    LocationType originalObject = new LocationType("LocationType1");
    LocationType deserializedObject = (LocationType) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
  }

  @Test
  public void shouldSerializeAndDeserializePath()
      throws Exception {
    @SuppressWarnings("unchecked")
    Path originalObject = new Path("Path1", mock(TCSObjectReference.class), mock(TCSObjectReference.class));
    Path deserializedObject = (Path) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
  }

  @Test
  public void shouldSerializeAndDeserializePoint()
      throws Exception {
    Point originalObject = new Point("Point1");
    Point deserializedObject = (Point) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
  }

  @Test
  @Deprecated
  public void shouldSerializeAndDeserializeStaticRoute()
      throws Exception {
    StaticRoute originalObject = new StaticRoute("StaticRoute1");
    StaticRoute deserializedObject = (StaticRoute) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
  }

  @Test
  public void shouldSerializeAndDeserializeVehicle()
      throws Exception {
    Vehicle originalObject = new Vehicle("Vehicle1");
    Vehicle deserializedObject = (Vehicle) deserializeTCSObject(serializeTCSObject(originalObject));

    assertEquals(originalObject, deserializedObject);
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
