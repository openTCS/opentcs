// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 */
class AuthenticatorTest {

  private ServiceWebApiConfiguration configuration;

  private Authenticator authenticator;

  @BeforeEach
  void setUp() {
    configuration = mock(ServiceWebApiConfiguration.class);
    authenticator = new Authenticator(configuration);
  }

  @Test
  void allowRequestsIfNoAccessKeyConfigured() {
    when(configuration.accessKey()).thenReturn("");

    assertTrue(authenticator.isAuthenticated(aRequestWithAccessKey(null)));
    assertTrue(authenticator.isAuthenticated(aRequestWithAccessKey("")));
    assertTrue(authenticator.isAuthenticated(aRequestWithAccessKey("some random value")));
  }

  @Test
  void allowRequestIfCorrectAccessKeyGiven() {
    when(configuration.accessKey()).thenReturn("my access key");

    assertTrue(authenticator.isAuthenticated(aRequestWithAccessKey("my access key")));
  }

  @Test
  void disallowRequestIfWrongAccessKeyGiven() {
    when(configuration.accessKey()).thenReturn("my access key");

    assertFalse(authenticator.isAuthenticated(aRequestWithAccessKey(null)));
    assertFalse(authenticator.isAuthenticated(aRequestWithAccessKey("")));
    assertFalse(authenticator.isAuthenticated(aRequestWithAccessKey("some random value")));
  }

  private Context aRequestWithAccessKey(String accessKey) {
    Context context = mock(Context.class);

    when(context.header(HttpConstants.HEADER_NAME_ACCESS_KEY)).thenReturn(accessKey);

    return context;
  }

}
