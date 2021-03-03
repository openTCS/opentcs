/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import org.junit.*;
import static org.mockito.Matchers.any;
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
public class SynchronousEventHubTest {

  private SynchronousEventHub<TCSEvent> hub;

  @Before
  public void setUp() {
    hub = new SynchronousEventHub<>();
  }

  @After
  public void tearDown() {
    hub = null;
  }

  @Test
  public void testAcceptingListener() {
    @SuppressWarnings("unchecked")
    EventListener<TCSEvent> acceptingListener = mock(EventListener.class);
    @SuppressWarnings("unchecked")
    EventFilter<TCSEvent> acceptingFilter = mock(EventFilter.class);
    TCSEvent event = mock(TCSEvent.class);
    
    doReturn(true).when(acceptingFilter).accept(any(TCSEvent.class));

    hub.addEventListener(acceptingListener, acceptingFilter);

    hub.processEvent(event);
    
    verify(acceptingFilter, times(1)).accept(any(TCSEvent.class));
    verify(acceptingListener, times(1)).processEvent(event);
  }

  @Test
  public void testRefusingListener() {
    @SuppressWarnings("unchecked")
    EventListener<TCSEvent> refusingListener = mock(EventListener.class);
    @SuppressWarnings("unchecked")
    EventFilter<TCSEvent> refusingFilter = mock(EventFilter.class);
    TCSEvent event = mock(TCSEvent.class);
    
    doReturn(false).when(refusingFilter).accept(any(TCSEvent.class));

    hub.addEventListener(refusingListener, refusingFilter);

    hub.processEvent(event);
    
    verify(refusingFilter, times(1)).accept(any(TCSEvent.class));
    verify(refusingListener, never()).processEvent(event);
  }

}
