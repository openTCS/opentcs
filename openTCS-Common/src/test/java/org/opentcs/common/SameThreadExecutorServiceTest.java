/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SameThreadExecutorService}.
 */
class SameThreadExecutorServiceTest {

  private ExecutorService executor;

  @BeforeEach
  void setup() {
    executor = new SameThreadExecutorService();
  }

  @Test
  void shouldShutdown() {
    assertThat(executor.isShutdown(), is(false));
    executor.shutdown();
    assertThat(executor.isShutdown(), is(true));
  }

  @Test
  void shouldShutdownNow() {
    // shutdownNow should shutdown the executor and always return an empty list of tasks
    // since all task are executed to completion when submited.
    executor.submit(() -> {
    });

    assertThat(executor.shutdownNow(), empty());
    assertThat(executor.isShutdown(), is(true));
  }

  @Test
  void awaitTerminationShouldReturnIfShutdown()
      throws InterruptedException {
    executor.shutdown();
    assertThat(executor.awaitTermination(10, TimeUnit.SECONDS), is(true));
  }

  @Test
  void awaitTerminationShouldThrowIfNotShutdown()
      throws InterruptedException {
    assertThrows(
        IllegalStateException.class,
        () -> {
          executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    );
  }

  @Test
  void shouldRunSubmittedRunnable() {
    Runnable task = mock(Runnable.class);
    executor.submit(task);
    verify(task).run();
  }

  @Test
  void shouldRunSubmittedCallable()
      throws Exception {
    @SuppressWarnings("unchecked")
    Callable<Integer> task = (Callable<Integer>) mock(Callable.class);
    executor.submit(task);
    verify(task).call();
  }

}
