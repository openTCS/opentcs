/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import javax.inject.Provider;
import org.junit.*;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.access.SharedKernelProvider;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ApplicationKernelProviderTest {

  /**
   * A (mocked) proxy manager used by the kernel provider.
   */
  private KernelProxyManager kernelProxyManager;
  /**
   * A (mocked) provider for connection dialogs.
   */
  private Provider<ConnectToServerDialog> dialogProvider;
  /**
   * A (mocked) connection dialog.
   */
  private ConnectToServerDialog dialog;
  /**
   * The kernel provider to be tested.
   */
  private SharedKernelProvider kernelProvider;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    kernelProxyManager = mock(KernelProxyManager.class);
    dialogProvider = (Provider<ConnectToServerDialog>) mock(Provider.class);
    dialog = mock(ConnectToServerDialog.class);
    kernelProvider = new ApplicationKernelProvider(kernelProxyManager,
                                                   dialogProvider);
  }

  @Test
  public void shouldConnectOnClientRegistration() {
    final Object client1 = new Object();
    
    when(kernelProxyManager.isConnected()).thenReturn(false);
    when(dialogProvider.get()).thenReturn(dialog);
    
    kernelProvider.register(client1);
    
    verify(dialog).setVisible(true);
  }

  @Test
  public void shouldNotConnectIfAlreadyConnected() {
    final Object client1 = new Object();
    
    when(kernelProxyManager.isConnected()).thenReturn(true);
    when(dialogProvider.get()).thenReturn(dialog);
    
    kernelProvider.register(client1);
    
    verify(dialog, never()).setVisible(anyBoolean());
  }
}
