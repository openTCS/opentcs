/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.util.event.SimpleEventBus;

/**
 * A test class for NotificationBuffer.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class NotificationBufferTest {

  /**
   * A constant capacity for buffers to be tested here.
   */
  private static final int CAPACITY = 100;
  /**
   * The buffer to be tested here.
   */
  private NotificationBuffer testBuffer;

  @BeforeEach
  public void setUp() {
    testBuffer = new NotificationBuffer(new SimpleEventBus());
    testBuffer.setCapacity(CAPACITY);
  }

  /**
   * Verifies that getMessageCount() returns the correct number.
   */
  @Test
  public void testMessageCountValidity() {
    int cutBackCount = CAPACITY / 2;
    testBuffer.setCutBackCount(cutBackCount);
    // Fill the buffer to its capacity.
    for (int i = 1; i <= CAPACITY; i++) {
      testBuffer.addNotification(new UserNotification("message text",
                                                      UserNotification.Level.INFORMATIONAL));
      assertEquals(i, testBuffer.getMessageCount());
      List<UserNotification> messages = testBuffer.getNotifications();
      assertEquals(i, messages.size());
    }
    // Add one more message to exceed the capacity.
    testBuffer.addNotification(
        new UserNotification("message text", UserNotification.Level.INFORMATIONAL)
    );
    assertEquals(cutBackCount, testBuffer.getMessageCount());
    List<UserNotification> messages = testBuffer.getNotifications();
    assertEquals(cutBackCount, messages.size());
  }

  /**
   * Verify the buffer's capacity and its cut back count.
   */
  @Test
  public void testCapacityAndCutBackCount() {
    for (int cutBackCount = 0; cutBackCount < CAPACITY; cutBackCount++) {
      testBuffer.setCutBackCount(cutBackCount);
      // Add one more message than the buffer can hold.
      for (int i = 0; i < (CAPACITY + 1); i++) {
        testBuffer.addNotification(new UserNotification("message text",
                                                        UserNotification.Level.INFORMATIONAL));
      }
      // The number of messages should now be equal to the buffer's capacity.
      assertEquals(cutBackCount, testBuffer.getMessageCount());
      testBuffer.clear();
    }
  }
}
