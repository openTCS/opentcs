/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost.status.binding;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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

  public TCSStatusMessageSetTest() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void shouldMapToOrderStatusMessage()
      throws IOException {
    List<DriveOrder> driveOrders = new LinkedList<>();
    LocationType locType = new LocationType("testLocType");
    Location loc1 = new Location("Storage 01", locType.getReference());
    Location loc2 = new Location("Storage 02", locType.getReference());
    DriveOrder.Destination dest1 = new DriveOrder.Destination(loc1.getReference())
        .withOperation("Load cargo");
    DriveOrder.Destination dest2 = new DriveOrder.Destination(loc2.getReference())
        .withOperation("Unload cargo");
    driveOrders.add(new DriveOrder(dest1));
    driveOrders.add(new DriveOrder(dest2));
    TransportOrder order = new TransportOrder("TOrder-0001", driveOrders)
        .withProperty("waitBefore", "Unload")
        .withState(TransportOrder.State.ACTIVE);

    OrderStatusMessage message = OrderStatusMessage.fromTransportOrder(order);
    TCSStatusMessageSet messageSet = new TCSStatusMessageSet();
    messageSet.getStatusMessages().add(message);

    StringWriter writer = new StringWriter();
    messageSet.toXml(writer);
    String xmlOutput = writer.toString();

    LOG.info(xmlOutput);

    StringReader reader = new StringReader(xmlOutput);
    TCSStatusMessageSet parsedMessageSet = TCSStatusMessageSet.fromXml(reader);
    assertTrue("parsed message set should have exactly one message",
               parsedMessageSet.getStatusMessages().size() == 1);
  }

}
