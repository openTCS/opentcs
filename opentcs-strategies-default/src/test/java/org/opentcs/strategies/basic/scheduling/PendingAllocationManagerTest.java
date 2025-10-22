// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT

package org.opentcs.strategies.basic.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.Scheduler;

/**
 * Tests for {@link PendingAllocationManager}.
 */
class PendingAllocationManagerTest {

  private PendingAllocationManager pendingAllocationManager;

  private Scheduler.Client client;

  private AllocatorCommand.Allocate allocateCommand;

  @BeforeEach
  void setUp() {
    pendingAllocationManager = new PendingAllocationManager();

    client = mock(Scheduler.Client.class);

    allocateCommand = new AllocatorCommand.Allocate(client, Set.of());
  }

  @Test
  void shouldInitializeProperly() {
    pendingAllocationManager.initialize();
    assertThat(pendingAllocationManager.isInitialized()).isTrue();
    assertThat(pendingAllocationManager.drainDeferredAllocations())
        .isEmpty();
    assertThat(pendingAllocationManager.countPendingAllocationFutures())
        .isEmpty();
  }

  @Test
  void shouldTerminateProperly() {
    pendingAllocationManager.initialize();
    pendingAllocationManager.terminate();

    assertThat(pendingAllocationManager.isInitialized())
        .isFalse();
    assertThat(pendingAllocationManager.drainDeferredAllocations())
        .isEmpty();
    assertThat(pendingAllocationManager.countPendingAllocationFutures())
        .isEmpty();
  }


  @Test
  void shouldAddDeferredAllocation() {
    pendingAllocationManager.addDeferredAllocation(allocateCommand);

    assertThat(pendingAllocationManager.drainDeferredAllocations())
        .hasSize(1)
        .contains(allocateCommand);
  }

  @Test
  void shouldClearPendingAllocationsForExistingClient() {
    pendingAllocationManager.addDeferredAllocation(allocateCommand);

    pendingAllocationManager.clearPendingAllocations(client);

    assertThat(pendingAllocationManager.drainDeferredAllocations())
        .isEmpty();
  }

  @Test
  void shouldNotClearPendingAllocationsForUnknownClient() {
    pendingAllocationManager.addDeferredAllocation(allocateCommand);

    Scheduler.Client unknownClient = mock(Scheduler.Client.class);

    pendingAllocationManager.clearPendingAllocations(unknownClient);
    assertThat(pendingAllocationManager.drainDeferredAllocations())
        .hasSize(1)
        .contains(allocateCommand);
  }

  @Test
  void shouldAddAllocationFuture() {
    Future<?> future = mock(Future.class);

    Map<Scheduler.Client, Integer> counts
        = pendingAllocationManager.countPendingAllocationFutures();
    assertThat(counts.getOrDefault(client, 0))
        .isEqualTo(0);

    pendingAllocationManager.addAllocationFuture(client, future);

    assertThat(pendingAllocationManager.countPendingAllocationFutures().get(client))
        .isEqualTo(1);
  }

  @Test
  void shouldRemoveCompletedAllocationsWhenAddingNewFuture() {
    Future<?> future1 = mock(Future.class);
    pendingAllocationManager.addAllocationFuture(client, future1);

    when(future1.isDone()).thenReturn(true);

    Future<?> future2 = mock(Future.class);
    pendingAllocationManager.addAllocationFuture(client, future2);
    Map<Scheduler.Client, Integer> counts
        = pendingAllocationManager.countPendingAllocationFutures();
    assertThat(counts.getOrDefault(client, 0))
        .isEqualTo(1);
  }

  @Test
  void shouldClearDeferredAndCancelPendingFuturesForClient() {
    pendingAllocationManager.addDeferredAllocation(allocateCommand);

    Future<?> future1 = mock(Future.class);
    Future<?> future2 = mock(Future.class);

    when(future1.isDone()).thenReturn(false);
    when(future2.isDone()).thenReturn(true);

    pendingAllocationManager.addAllocationFuture(client, future1);
    pendingAllocationManager.addAllocationFuture(client, future2);

    pendingAllocationManager.clearPendingAllocations(client);
    Map<Scheduler.Client, Integer> counts
        = pendingAllocationManager.countPendingAllocationFutures();

    assertThat(pendingAllocationManager.drainDeferredAllocations())
        .isEmpty();
    assertThat(counts.getOrDefault(client, 0))
        .isEqualTo(1);

    verify(future1, times(1))
        .cancel(false);
    verify(future2, times(0))
        .cancel(false);

  }


}
