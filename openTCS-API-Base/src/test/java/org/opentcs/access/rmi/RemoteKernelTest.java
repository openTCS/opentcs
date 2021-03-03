/*
 */
package org.opentcs.access.rmi;

import java.lang.reflect.Method;
import org.junit.*;
import static org.junit.Assert.assertNotNull;
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
  private static final Logger log
      = LoggerFactory.getLogger(RemoteKernelTest.class);

  @Before
  public void setUp() {
    // Do nada.
  }

  @After
  public void tearDown() {
    // Do nada.
  }

  /**
   * Verify that RemoteKernel has corresponding methods for all methods declared
   * in the Kernel interface.
   */
  @Test
  public void testEquivalenceToKernelInterface() {
    for (Method kernelMethod : Kernel.class.getDeclaredMethods()) {
      try {
        Method neededMethod = RemoteMethods.getRemoteKernelMethod(kernelMethod);
        log.debug("Found " + neededMethod + " corresponding to " + kernelMethod);
      }
      catch (NoSuchMethodException exc) {
        fail("Did not find corresponding method for: " + kernelMethod);
      }
    }
  }

  @Test
  public void testPermissionAnnotations() {
    for (Method method : RemoteKernel.class.getDeclaredMethods()) {
      CallPermissions perms = method.getAnnotation(CallPermissions.class);
      assertNotNull("No permissions annotation at method " + method, perms);
    }
  }
}
