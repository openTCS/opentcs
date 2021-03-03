/*
 *
 * Created on June 26, 2006, 2:02 PM
 */

package org.opentcs.kernel.persistence;

import java.util.ArrayList;
import java.util.List;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.junit.*;
import static org.junit.Assert.assertTrue;
import org.opentcs.TestEnvironment;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.opentcs.kernel.workingset.TransportOrderPool;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class XMLFileOrderPersisterTest{
  /**
   * A flag indicating whether the tests in this test case should be ignored.
   * Useful for temporarily disabling this whole test case.
   */
  private static final boolean ignoreTestCase = false;
  /**
   * The number of transport orders created in the test pool.
   */
  private static final int poolSize = 500;
  /**
   * The order persister that is to be tested here.
   */
  private XMLFileOrderPersister persister;
  /**
   * The object pool backing the order pool.
   */
  private TCSObjectPool globalObjectPool;
  /**
   * The pool containing all transport orders for testing.
   */
  private TransportOrderPool orderPool;
  
  /** {@inheritDoc} */
  @Before
  public void setUp() {
    if (ignoreTestCase) {
      return;
    }
    // Create a pool of many transport orders.
    globalObjectPool
        = new TCSObjectPool(new MBassador<>(BusConfiguration.Default()));
    orderPool = new TransportOrderPool(globalObjectPool);
    for (int i = 0; i < poolSize; i++) {
      int driveOrderCount = 1 + i % 5;
      List<DriveOrder> driveOrders = new ArrayList<>(driveOrderCount);
      for (int j = 0; j < driveOrderCount; j++) {
        int pointCount = 10 + i % 11;
        List<TCSObjectReference<Point>> route = new ArrayList<>(pointCount);
        for (int k = 0; k < pointCount; k++) {
          route.add((new Point(globalObjectPool.getUniqueObjectId(),
                "P-" + i + "-" + j + "-"+ k)).getReference());
        }
        // XXX Only NOPs?
        // XXX Commented out, because the DriveOrder constructor has changed
        //driveOrders.add(new DriveOrder(route, VehicleOperation.NOP));
      }
//      TransportOrder transportOrder =
//            orderPool.createTransportOrder(driveOrders);
//      transportOrder.setDeadline(System.currentTimeMillis() + 3600000);
//      transportOrder.setState(TransportOrder.State.PRISTINE);
    }
    persister = new XMLFileOrderPersister(TestEnvironment.getKernelHomeDirectory());
  }
  
  /** {@inheritDoc} */
  @After
  public void tearDown() {
    persister = null;
    orderPool = null;
    globalObjectPool = null;
  }
  
  /**
   * A dummy test that prevents this test case from failing because no tests
   * are defined.
   */
  @Test
  public void testNothing() {
    assertTrue(true);
  }

// Temporarily disable these until there's time to do them right.
//  /**
//   * Test archiving of lots of single transport orders.
//   */
//  public void testSingleOrderArchiving()
//  throws IOException {
//    if (ignoreTestCase) {
//      return;
//    }
//    int i = 0;
//    for (TransportOrder curOrder : orderPool.getTransportOrders(null)) {
//      // Pick every fifth order.
//      if (i % 3 == 0) {
//        curOrder.setState(TransportOrder.State.ACTIVE);
//        // Finish every DriveOrder, then the TransportOrder itself.
//        DriveOrder curDriveOrder = curOrder.getCurrentDriveOrder();
//        while (curDriveOrder != null) {
//          curDriveOrder.setState(DriveOrder.State.TRAVELLING);
//          TCSObjectReference<Point> curPoint = curDriveOrder.getNextPoint();
//          while (curPoint != null) {
//            curDriveOrder.finishNextPoint();
//            curPoint = curDriveOrder.getNextPoint();
//          }
//          curDriveOrder.setState(DriveOrder.State.OPERATING);
//          curOrder.finishCurrentDriveOrder();
//          curDriveOrder = curOrder.getCurrentDriveOrder();
//        }
//        curOrder.setState(TransportOrder.State.FINISHED);
//      }
//      i++;
//    }
//    Set<TransportOrder> finishedOrders =
//          orderPool.removeFinishedTransportOrders();
//    int writeCount = finishedOrders.size();
//    long startTime;
//    long endTime;
//    startTime = System.currentTimeMillis();
//    for (TransportOrder curOrder : finishedOrders) {
//      persister.archiveTransportOrder(curOrder);
//    }
//    endTime = System.currentTimeMillis();
//    float writingTime = (float) (endTime - startTime) / 1000;
//    System.out.printf("Writing %d orders one by one took %.2f seconds",
//          writeCount, writingTime);
//    System.out.println();
//  }
//  
//  /**
//   * Test archiving of lots of single transport orders in a list.
//   */
//  public void testOrderListArchiving()
//  throws IOException {
//    if (ignoreTestCase) {
//      return;
//    }
//    int i = 0;
//    for (TransportOrder curOrder : orderPool.getTransportOrders(null)) {
//      // Pick every fifth order.
//      if (i % 3 == 0) {
//        curOrder.setState(TransportOrder.State.ACTIVE);
//        // Finish every DriveOrder, then the TransportOrder itself.
//        DriveOrder curDriveOrder = curOrder.getCurrentDriveOrder();
//        while (curDriveOrder != null) {
//          curDriveOrder.setState(DriveOrder.State.TRAVELLING);
//          TCSObjectReference<Point> curPoint = curDriveOrder.getNextPoint();
//          while (curPoint != null) {
//            curDriveOrder.finishNextPoint();
//            curPoint = curDriveOrder.getNextPoint();
//          }
//          curDriveOrder.setState(DriveOrder.State.OPERATING);
//          curOrder.finishCurrentDriveOrder();
//          curDriveOrder = curOrder.getCurrentDriveOrder();
//        }
//        curOrder.setState(TransportOrder.State.FINISHED);
//      }
//      i++;
//    }
//    Set<TransportOrder> finishedOrders =
//          orderPool.removeFinishedTransportOrders();
//    int writeCount = finishedOrders.size();
//    long startTime;
//    long endTime;
//    startTime = System.currentTimeMillis();
//    persister.archiveTransportOrders(finishedOrders);
//    endTime = System.currentTimeMillis();
//    float writingTime = (float) (endTime - startTime) / 1000;
//    System.out.printf("Writing %d orders in a list took %.2f seconds",
//          writeCount, writingTime);
//    System.out.println();
//  }
}
