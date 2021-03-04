/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import spark.Request;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AuthenticatorTest {

  private ServiceWebApiConfiguration configuration;

  private Authenticator authenticator;

  @Before
  public void setUp() {
    configuration = mock(ServiceWebApiConfiguration.class);
    authenticator = new Authenticator(configuration);
  }

  @Test
  public void allowRequestsIfNoAccessKeyConfigured() {
    when(configuration.accessKey()).thenReturn("");

    assertTrue(authenticator.isAuthenticated(aRequestWithAccessKey(null)));
    assertTrue(authenticator.isAuthenticated(aRequestWithAccessKey("")));
    assertTrue(authenticator.isAuthenticated(aRequestWithAccessKey("some random value")));
  }

  @Test
  public void allowRequestIfCorrectAccessKeyGiven() {
    when(configuration.accessKey()).thenReturn("my access key");

    assertTrue(authenticator.isAuthenticated(aRequestWithAccessKey("my access key")));
  }

  @Test
  public void disallowRequestIfWrongAccessKeyGiven() {
    when(configuration.accessKey()).thenReturn("my access key");

    assertFalse(authenticator.isAuthenticated(aRequestWithAccessKey(null)));
    assertFalse(authenticator.isAuthenticated(aRequestWithAccessKey("")));
    assertFalse(authenticator.isAuthenticated(aRequestWithAccessKey("some random value")));
  }

  private Request aRequestWithAccessKey(String accessKey) {
    Request request = mock(Request.class);

    when(request.headers(HttpConstants.HEADER_NAME_ACCESS_KEY)).thenReturn(accessKey);

    return request;
  }

}
