/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status;

import java.util.ArrayList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.ServiceWebApiConfiguration;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.StatusMessageList;
import org.opentcs.util.event.EventSource;
import org.opentcs.util.event.SimpleEventBus;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatusEventDispatcherTest {

  private ServiceWebApiConfiguration configuration;

  private EventSource eventSource;

  private StatusEventDispatcher statusEventDispatcher;

  @Before
  public void setUp() {
    configuration = mock(ServiceWebApiConfiguration.class);
    eventSource = new SimpleEventBus();
    statusEventDispatcher = new StatusEventDispatcher(configuration, eventSource);
  }

  @Test
  public void returnEmptyListIfThereWereNoEvents() {
    statusEventDispatcher.initialize();

    StatusMessageList list = statusEventDispatcher.fetchEvents(0, Long.MAX_VALUE, 1);
    assertThat(list.getStatusMessages(), is(empty()));
  }

  @Test
  public void respectConfiguredCapacity() {
    final int CAPACITY = 10;
    final int EVENT_COUNT = CAPACITY * 2;
    when(configuration.statusEventsCapacity()).thenReturn(CAPACITY);
    statusEventDispatcher.initialize();

    TransportOrder order = new TransportOrder("SomeOrder", new ArrayList<>());
    for (int i = 0; i < EVENT_COUNT; i++) {
      statusEventDispatcher.onEvent(
          new TCSObjectEvent(order, order, TCSObjectEvent.Type.OBJECT_MODIFIED)
      );
    }

    StatusMessageList list = statusEventDispatcher.fetchEvents(0, Long.MAX_VALUE, 1);
    assertThat(list.getStatusMessages().size(), is(CAPACITY));
    assertThat(list.getStatusMessages().get(CAPACITY - 1).getSequenceNumber(),
               is((long) EVENT_COUNT - 1));
  }

  @Test
  public void keepAllEventsIfLessThanCapacity() {
    final int CAPACITY = 10;
    final int EVENT_COUNT = CAPACITY / 2;
    when(configuration.statusEventsCapacity()).thenReturn(CAPACITY);
    statusEventDispatcher.initialize();

    TransportOrder order = new TransportOrder("SomeOrder", new ArrayList<>());
    for (int i = 0; i < 5; i++) {
      statusEventDispatcher.onEvent(
          new TCSObjectEvent(order, order, TCSObjectEvent.Type.OBJECT_MODIFIED)
      );
    }

    StatusMessageList list = statusEventDispatcher.fetchEvents(0, Long.MAX_VALUE, 1);
    assertThat(list.getStatusMessages().size(), is(EVENT_COUNT));
    assertThat(list.getStatusMessages().get(EVENT_COUNT - 1).getSequenceNumber(),
               is((long) EVENT_COUNT - 1));
  }
}
