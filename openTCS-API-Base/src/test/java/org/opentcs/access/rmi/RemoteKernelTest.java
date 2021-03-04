/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
/*
 */
package org.opentcs.access.rmi;

import java.lang.reflect.Method;
import org.junit.*;
import static org.junit.Assert.fail;
import org.opentcs.access.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the RemoteKernel interface.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RemoteKernelTest {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RemoteKernelTest.class);

  @Test
  @SuppressWarnings("deprecation")
  public void shouldMapAllMethodsInKernelInterface() {
    for (Method kernelMethod : Kernel.class.getDeclaredMethods()) {
      try {
        if (shouldIgnoreMethod(kernelMethod)) {
          continue;
        }

        Method neededMethod = RemoteMethods.getRemoteKernelMethod(kernelMethod);
        LOG.debug("Found {} corresponding to {}", neededMethod, kernelMethod);
      }
      catch (NoSuchMethodException exc) {
        fail("Did not find corresponding method for: " + kernelMethod);
      }
    }
  }

  @SuppressWarnings("deprecation")
  private boolean shouldIgnoreMethod(Method method) {
    boolean ignore = false;

    // Ignore overriden methods from EventSoruce 
    for (Method eventSourceMethod : org.opentcs.util.eventsystem.EventSource.class.getDeclaredMethods()) {
      if (method.getName().equals(eventSourceMethod.getName())) {
        ignore = true;
      }
    }

    return ignore;
  }
}
