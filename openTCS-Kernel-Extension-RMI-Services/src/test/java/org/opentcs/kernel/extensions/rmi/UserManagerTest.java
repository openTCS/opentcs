/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import java.io.File;
import java.util.EnumSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Set;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import org.opentcs.util.event.EventSource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.AfterEach;
import org.opentcs.access.rmi.ClientID;

/**
 * Unit tests for {@link UserManager}.
 */
public class UserManagerTest {

  private File homedirectory;
  private EventSource eventSource;
  private ScheduledExecutorService kernelExecutor;
  private RmiKernelInterfaceConfiguration configuration;
  private UserAccountProvider userAccountProvider;
  private UserAccount account1;
  private UserManager.ClientEntry client1;
  private ClientID id1;
  private UserManager manager;

  @BeforeEach
  public void setUp() {
    homedirectory = mock();
    eventSource = mock();
    kernelExecutor = Executors.newSingleThreadScheduledExecutor();
    configuration = mock();
    userAccountProvider = mock();

    Set<UserPermission> permissions = EnumSet.of(UserPermission.READ_DATA);

    account1 = new UserAccount("peter", "123", permissions);
    Set<UserAccount> userAccounts = Set.of(account1);

    client1 = new UserManager.ClientEntry("auto", permissions);
    id1 = new ClientID("auto");

    given(userAccountProvider.getUserAccounts())
        .willReturn(userAccounts);

    given(configuration.clientSweepInterval())
        .willReturn(1000L);

    manager = new UserManager(
        homedirectory,
        eventSource,
        kernelExecutor,
        configuration,
        userAccountProvider);
    manager.initialize();
  }

  @AfterEach
  public void tearDown() {
    kernelExecutor.shutdown();
  }

  @Test
  public void checkRegisterClient() {
    manager.registerClient(id1, client1);
    assertThat(manager.getKnownClients(), is(aMapWithSize(1)));
    assertThat(manager.getKnownClients(), hasEntry(id1, client1));
  }

  @Test
  public void checkUnregisterClient() {
    manager.registerClient(id1, client1);
    manager.unregisterClient(id1);
    assertThat(manager.getKnownClients(), is(anEmptyMap()));
  }
}
