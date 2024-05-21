/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.OrderPoolConfiguration;

class WorkingSetCleanupTaskTest {

  private WorkingSetCleanupTask cleanupTask;
  private TCSObjectRepository objectRepository;

  private OrderPoolConfiguration configuration;

  @BeforeEach
  void setup() {
    configuration = mock();
    objectRepository = new TCSObjectRepository();
    CreationTimeThreshold creationTimeThreshold = new CreationTimeThreshold();

    PeripheralJobPoolManager peripheralJobPoolManager
        = new PeripheralJobPoolManager(objectRepository,
                                       mock(),
                                       new PrefixedUlidObjectNameProvider());
    TransportOrderPoolManager orderPoolManager
        = new TransportOrderPoolManager(objectRepository,
                                        mock(),
                                        new PrefixedUlidObjectNameProvider());
    DefaultPeripheralJobCleanupApproval peripheralJobCleanupApproval
        = new DefaultPeripheralJobCleanupApproval(creationTimeThreshold);
    DefaultTransportOrderCleanupApproval orderCleanupApproval
        = new DefaultTransportOrderCleanupApproval(peripheralJobPoolManager,
                                                   peripheralJobCleanupApproval,
                                                   creationTimeThreshold);
    DefaultOrderSequenceCleanupApproval orderSequenceCleanupApproval
        = new DefaultOrderSequenceCleanupApproval(orderPoolManager, orderCleanupApproval);
    cleanupTask = new WorkingSetCleanupTask(new Object(),
                                            orderPoolManager,
                                            peripheralJobPoolManager,
                                            configuration,
                                            new CompositeOrderSequenceCleanupApproval(
                                                Set.of(),
                                                orderSequenceCleanupApproval
                                            ),
                                            new CompositeTransportOrderCleanupApproval(
                                                Set.of(),
                                                orderCleanupApproval
                                            ),
                                            new CompositePeripheralJobCleanupApproval(
                                                Set.of(),
                                                peripheralJobCleanupApproval
                                            ),
                                            creationTimeThreshold);
  }

  @Test
  void cleanExpiredTransportOrders() {
    when(configuration.sweepAge()).thenReturn(60000);

    objectRepository.addObject(
        new TransportOrder("Order-1", List.of())
            .withCreationTime(Instant.now().minusMillis(70000))
            .withState(TransportOrder.State.FINISHED)
    );

    objectRepository.addObject(
        new TransportOrder("Order-2", List.of())
            .withCreationTime(Instant.now().minusMillis(50000))
            .withState(TransportOrder.State.FINISHED)
    );
    assertEquals(2, objectRepository.getObjects(TransportOrder.class).size());
    cleanupTask.run();
    assertEquals(1, objectRepository.getObjects(TransportOrder.class).size());
  }

  @Test
  void cleanExpiredOrderSequences() {
    when(configuration.sweepAge()).thenReturn(60000);

    OrderSequence orderSequence = new OrderSequence("seq-1");
    objectRepository.addObject(orderSequence);

    TransportOrder order = new TransportOrder("Order-1", List.of())
        .withCreationTime(Instant.now().minusMillis(70000))
        .withState(TransportOrder.State.FINISHED);
    objectRepository.addObject(order);

    objectRepository.replaceObject(order.withWrappingSequence(orderSequence.getReference()));
    objectRepository.replaceObject(
        orderSequence.withOrder(order.getReference())
            .withComplete(true)
            .withFinished(true)
    );

    assertEquals(1, objectRepository.getObjects(TransportOrder.class).size());
    assertEquals(1, objectRepository.getObjects(OrderSequence.class).size());
    cleanupTask.run();
    assertEquals(0, objectRepository.getObjects(TransportOrder.class).size());
    assertEquals(0, objectRepository.getObjects(OrderSequence.class).size());
  }

  @Test
  void cleanRelatedPeripheralJob() {
    when(configuration.sweepAge()).thenReturn(60000);

    TransportOrder order = new TransportOrder("Order-1", List.of())
        .withCreationTime(Instant.now().minusMillis(70000))
        .withState(TransportOrder.State.FINISHED);
    objectRepository.addObject(order);

    objectRepository.addObject(
        new PeripheralJob("Job-1", "Vehicle-1", mock())
            .withCreationTime(Instant.now().minusMillis(65000))
            .withState(PeripheralJob.State.FINISHED)
            .withRelatedTransportOrder(order.getReference())
    );

    assertEquals(1, objectRepository.getObjects(TransportOrder.class).size());
    assertEquals(1, objectRepository.getObjects(PeripheralJob.class).size());
    cleanupTask.run();
    assertEquals(0, objectRepository.getObjects(PeripheralJob.class).size());
    assertEquals(0, objectRepository.getObjects(TransportOrder.class).size());
  }

}
