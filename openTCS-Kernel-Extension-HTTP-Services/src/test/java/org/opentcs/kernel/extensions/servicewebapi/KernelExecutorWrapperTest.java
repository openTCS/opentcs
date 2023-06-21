/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;

/**
 * Tests for {@link KernelExecutorWrapper}.
 */
public class KernelExecutorWrapperTest {

  private ExecutorService executorService;
  private KernelExecutorWrapper executorWrapper;

  @BeforeEach
  public void setUp() {
    this.executorService = Executors.newSingleThreadExecutor();
    this.executorWrapper = new KernelExecutorWrapper(executorService);
  }

  @AfterEach
  public void tearDown() {
    executorService.shutdown();
  }

  @Test
  public void returnValueReturnedInCallable() {
    assertThat(
        executorWrapper.callAndWait(() -> "my result"),
        is("my result")
    );
  }

  @Test
  public void forwardCausingExceptionIfRuntimeException() {
    assertThrows(ObjectUnknownException.class,
                 () -> {
                   executorWrapper.callAndWait(() -> {
                     throw new ObjectUnknownException("some exception");
                   });
                 }
    );

    assertThrows(ObjectExistsException.class,
                 () -> {
                   executorWrapper.callAndWait(() -> {
                     throw new ObjectExistsException("some exception");
                   });
                 }
    );
  }

  @Test
  public void wrapUnhandledExceptionInKernelRuntimeException() {
    assertThrows(KernelRuntimeException.class,
                 () -> {
                   executorWrapper.callAndWait(() -> {
                     throw new Exception("some exception");
                   });
                 }
    );
    
  }
}
