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
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.then;
import org.opentcs.util.event.EventSource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.CredentialsException;

/**
 * Unit tests for {@link UserManager}.
 */
class UserManagerTest {

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
  void setUp() {
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
  void tearDown() {
    kernelExecutor.shutdown();
  }

  @Test
  void testIfUserManagerIsInitialized() {
    assertThat(manager.isInitialized(), is(true));
  }

  @Test
  void testIfCleanerTaskIsTerminated() {
    manager.terminate();

    assertThat(manager.isInitialized(), is(false));
    then(eventSource).should().unsubscribe(manager);
  }

  @Test
  void checkGetKnownUsers() {
    assertThat(manager.getKnownUsers(), is(aMapWithSize(1)));
    assertThat(manager.getKnownUsers(), hasEntry("peter", account1));
  }

  @Test
  void checkGetUserShouldReturnRightUser() {
    assertThat(manager.getUser("peter"), is(account1));
  }

  @Test
  void checkGetUserShouldReturnNullForUnknownUser() {
    assertNull(manager.getUser("marie"));
  }

  @Test
  void checkGetClientShouldReturnRightClient() {
    manager.registerClient(id1, client1);

    assertThat(manager.getClient(id1), is(client1));
  }

  @Test
  void checkGetClientShouldReturnNull() {
    manager.registerClient(id1, client1);

    assertNull(manager.getClient(new ClientID("jet")));
  }

  @Test
  void checkIfPollEventsReturnsTheCorrectEventList() {
    manager.registerClient(id1, client1);

    client1.getEventBuffer().setEventFilter(event -> true);

    Object event1 = new Object();
    manager.onEvent(event1);

    List<Object> eventList = manager.pollEvents(id1, 0);

    assertThat(eventList, hasSize(1));
    assertThat(eventList, contains(event1));
  }

  @Test
  void checkVerifyCredentialsShouldThrowExceptionIfClientHasNoPermission() {
    manager.registerClient(id1, client1);
    assertThrows(CredentialsException.class,
                 () -> manager.verifyCredentials(id1, UserPermission.SAVE_MODEL));
  }

  @Test
  void checkVerifyCredentialsShouldThrowExceptionIfClientDoesNotExist() {
    assertThrows(CredentialsException.class,
                 () -> manager.verifyCredentials(new ClientID("unknown-client"),
                                                 UserPermission.SAVE_MODEL));
  }

  @Test
  void checkVerifyCredentialsShouldThrowNoException() {
    manager.registerClient(id1, client1);
    assertDoesNotThrow(
        () -> manager.verifyCredentials(id1, UserPermission.READ_DATA));
  }

  @Test
  void checkRegisterClient() {
    manager.registerClient(id1, client1);
    assertThat(manager.getKnownClients(), is(aMapWithSize(1)));
    assertThat(manager.getKnownClients(), hasEntry(id1, client1));
  }

  @Test
  void checkUnregisterClient() {
    manager.registerClient(id1, client1);
    manager.unregisterClient(id1);
    assertThat(manager.getKnownClients(), is(anEmptyMap()));
  }
}
