/*
 *
 * Created on May 19, 2006, 11:48 AM
 */
package org.opentcs.kernel.workingset;

import java.util.List;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import org.opentcs.data.notification.UserNotification;

/**
 * A test class for NotificationBuffer.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class NotificationBufferTest {

  /**
   * A constant capacity for buffers to be tested here.
   */
  private static final int capacity = 100;
  /**
   * The buffer to be tested here.
   */
  private NotificationBuffer testBuffer;

  @Before
  public void setUp() {
    testBuffer = new NotificationBuffer();
    testBuffer.setCapacity(capacity);
  }

  @After
  public void tearDown() {
    testBuffer = null;
  }

  /**
   * Verifies that getMessageCount() returns the correct number.
   */
  @Test
  public void testMessageCountValidity() {
    int cutBackCount = capacity / 2;
    testBuffer.setCutBackCount(cutBackCount);
    // Fill the buffer to its capacity.
    for (int i = 1; i <= capacity; i++) {
      testBuffer.addNotification(new UserNotification("message text",
                                                      UserNotification.Level.INFORMATIONAL));
      assertEquals(i, testBuffer.getMessageCount());
      List<UserNotification> messages = testBuffer.getNotifications();
      assertEquals(i, messages.size());
    }
    // Add one more message to exceed the capacity.
    testBuffer.addNotification(new UserNotification("message text", UserNotification.Level.INFORMATIONAL));
    assertEquals(cutBackCount, testBuffer.getMessageCount());
    List<UserNotification> messages = testBuffer.getNotifications();
    assertEquals(cutBackCount, messages.size());
  }

  /**
   * Verify the buffer's capacity and its cut back count.
   */
  @Test
  public void testCapacityAndCutBackCount() {
    for (int cutBackCount = 0; cutBackCount < capacity; cutBackCount++) {
      testBuffer.setCutBackCount(cutBackCount);
      // Add one more message than the buffer can hold.
      for (int i = 0; i < (capacity + 1); i++) {
        testBuffer.addNotification(new UserNotification("message text",
                                                        UserNotification.Level.INFORMATIONAL));
      }
      // The number of messages should now be equal to the buffer's capacity.
      assertEquals(cutBackCount, testBuffer.getMessageCount());
      testBuffer.clear();
    }
  }
}
