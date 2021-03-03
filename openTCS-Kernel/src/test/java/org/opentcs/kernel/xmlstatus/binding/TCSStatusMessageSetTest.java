/*
 * openTCS copyright information:
 * Copyright (c) 2016 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlstatus.binding;

import java.util.LinkedList;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.assertTrue;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TCSStatusMessageSetTest {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TCSStatusMessageSetTest.class);
  /**
   * A counter for unique object IDs.
   */
  private int objectIdCounter;

  public TCSStatusMessageSetTest() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void shouldMapToOrderStatusMessage() {
    List<DriveOrder.Destination> destinations = new LinkedList<>();
    LocationType locType = new LocationType(objectIdCounter++, "testLocType");
    Location loc1 = new Location(objectIdCounter++, "Storage 01", locType.getReference());
    Location loc2 = new Location(objectIdCounter++, "Storage 02", locType.getReference());
    DriveOrder.Destination dest1 = new DriveOrder.Destination(loc1.getReference(), "Load cargo");
    DriveOrder.Destination dest2 = new DriveOrder.Destination(loc2.getReference(), "Unload cargo");
    destinations.add(dest1);
    destinations.add(dest2);
    TransportOrder order = new TransportOrder(0, "TOrder-0001", destinations, System.currentTimeMillis());
    order.setProperty("waitBefore", "Unload");
    order.setState(TransportOrder.State.ACTIVE);

    OrderStatusMessage message = OrderStatusMessage.fromTransportOrder(order);
    TCSStatusMessageSet messageSet = new TCSStatusMessageSet();
    messageSet.getStatusMessages().add(message);

    String xmlOutput = messageSet.toXml();

    LOG.info(xmlOutput);

    TCSStatusMessageSet parsedMessageSet = TCSStatusMessageSet.fromXml(xmlOutput);
    assertTrue("parsed message set should have exactly one message",
               parsedMessageSet.getStatusMessages().size() == 1);
  }

}
