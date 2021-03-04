/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter;

import org.junit.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.common.PortalManager;
import org.opentcs.kernelcontrolcenter.exchange.KernelEventFetcher;
import org.opentcs.kernelcontrolcenter.util.KernelControlCenterConfiguration;
import org.opentcs.util.event.EventBus;

/**
 * Test cases for the {@link KernelControlCenterApplication}.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class KernelControlCenterApplicationTest {

  /**
   * The class to test.
   */
  private KernelControlCenterApplication application;

  /**
   * The event bus for online events.
   */
  private EventBus eventBus;

  /**
   * The portal manager to establish kernel connections.
   */
  private PortalManager portalManager;

  /**
   * The control center gui.
   */
  private KernelControlCenter controlCenter;

  /**
   * The configuration of the control center.
   */
  private KernelControlCenterConfiguration configuration;

  @Before
  public void setUp() {
    eventBus = mock(EventBus.class);
    portalManager = mock(PortalManager.class);
    configuration = mock(KernelControlCenterConfiguration.class);
    controlCenter = mock(KernelControlCenter.class);
    application = spy(new KernelControlCenterApplication(mock(KernelEventFetcher.class),
                                                         controlCenter,
                                                         portalManager,
                                                         eventBus,
                                                         configuration));
  }

  @Test
  public void onlyInitializeOnce() {
    when(configuration.connectAutomaticallyOnStartup()).thenReturn(true);
    application.initialize();
    application.initialize();

    Assert.assertTrue("Control center is not initialized", application.isInitialized());
    verify(controlCenter, times(1)).initialize();
    verify(portalManager, times(1)).connect(any());
    verify(application, times(1)).online(anyBoolean());
  }

  @Test
  public void onlyTerminateOnce() {
    when(configuration.connectAutomaticallyOnStartup()).thenReturn(false);
    application.initialize();
    application.terminate();
    application.terminate();

    Assert.assertFalse("Control center is initialized", application.isInitialized());
    verify(controlCenter, times(1)).terminate();
    verify(application, times(1)).offline();
  }

  @Test
  public void shouldOnlyConnectOnce() {
    //When trying to connect, return the value that indicates a successful connection
    when(portalManager.connect(any())).thenReturn(true);
    application.initialize();
    application.online(true);
    application.online(true);

    Assert.assertTrue("Control center is not initialized", application.isInitialized());
    Assert.assertTrue("Control center is not online", application.isOnline());
    verify(controlCenter, times(1)).initialize();
    verify(portalManager, times(1)).connect(any());
  }
}
