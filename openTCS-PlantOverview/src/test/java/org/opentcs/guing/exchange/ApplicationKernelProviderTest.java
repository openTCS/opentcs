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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.guing.util.PlantOverviewApplicationConfiguration;

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
   * A (mocked) configuration.
   */
  private PlantOverviewApplicationConfiguration appConfig;
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
    appConfig = mock(PlantOverviewApplicationConfiguration.class);
    kernelProvider = new ApplicationKernelProvider(kernelProxyManager, dialogProvider, appConfig);
  }

  @Test
  public void shouldConnectOnClientRegistration() {
    when(kernelProxyManager.isConnected()).thenReturn(false, false, true);
    when(kernelProxyManager.kernel()).thenReturn(mock(Kernel.class));
    when(dialogProvider.get()).thenReturn(dialog);

    kernelProvider.register();

    verify(dialog).setVisible(true);
  }

  @Test
  public void shouldNotConnectIfAlreadyConnected() {
    when(kernelProxyManager.isConnected()).thenReturn(true);
    when(kernelProxyManager.kernel()).thenReturn(mock(Kernel.class));

    kernelProvider.register();

    verify(dialog, never()).setVisible(anyBoolean());
  }
}
