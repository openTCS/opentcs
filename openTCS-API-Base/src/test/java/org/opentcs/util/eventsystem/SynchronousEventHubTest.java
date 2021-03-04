/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import org.junit.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * A test case for class SynchronousEventHub.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@SuppressWarnings("deprecation")
public class SynchronousEventHubTest {

  private SynchronousEventHub<TCSEvent> hub = new SynchronousEventHub<>();

  @Test
  public void shouldDeliverToRegisteredListener() {
    @SuppressWarnings("unchecked")
    EventListener<TCSEvent> listener = mock(EventListener.class);

    hub.addEventListener(listener);

    TCSEvent event = new TCSEvent() {
    };

    hub.processEvent(event);
    
    verify(listener).processEvent(event);
  }
  
  @Test
  public void shouldNotDeliverToUnregisteredListener() {
    @SuppressWarnings("unchecked")
    EventListener<TCSEvent> listener = mock(EventListener.class);

    hub.addEventListener(listener);
    hub.removeEventListener(listener);

    TCSEvent event = new TCSEvent() {
    };

    hub.processEvent(event);
    
    verify(listener, never()).processEvent(any(TCSEvent.class));
  }

  @Test
  @SuppressWarnings({"unchecked", "deprecation"})
  public void testAcceptingListener() {
    EventListener<TCSEvent> acceptingListener = mock(EventListener.class);
    EventFilter<TCSEvent> acceptingFilter = mock(EventFilter.class);
    TCSEvent event = mock(TCSEvent.class);

    doReturn(true).when(acceptingFilter).accept(any(TCSEvent.class));

    hub.addEventListener(acceptingListener, acceptingFilter);

    hub.processEvent(event);

    verify(acceptingFilter, times(1)).accept(any(TCSEvent.class));
    verify(acceptingListener, times(1)).processEvent(event);
  }

  @Test
  @SuppressWarnings({"unchecked", "deprecation"})
  public void testRefusingListener() {
    EventListener<TCSEvent> refusingListener = mock(EventListener.class);
    EventFilter<TCSEvent> refusingFilter = mock(EventFilter.class);
    TCSEvent event = mock(TCSEvent.class);

    doReturn(false).when(refusingFilter).accept(any(TCSEvent.class));

    hub.addEventListener(refusingListener, refusingFilter);

    hub.processEvent(event);

    verify(refusingFilter, times(1)).accept(any(TCSEvent.class));
    verify(refusingListener, never()).processEvent(event);
  }

}
