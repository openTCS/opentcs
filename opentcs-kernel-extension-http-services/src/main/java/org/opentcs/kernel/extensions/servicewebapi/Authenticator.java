// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import io.javalin.http.Context;
import jakarta.inject.Inject;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticates incoming requests.
 */
public class Authenticator {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Authenticator.class);
  /**
   * Defines the required access rules.
   */
  private final ServiceWebApiConfiguration configuration;

  /**
   * Creates a new instance.
   *
   * @param configuration Defines the required access rules.
   */
  @Inject
  public Authenticator(ServiceWebApiConfiguration configuration) {
    this.configuration = requireNonNull(configuration, "configuration");
  }

  /**
   * Checks whether authentication is required and the given request is authenticated.
   *
   * @param context The context to be checked.
   * @return <code>true</code> if, and only if, authentication is required and the given request is
   * authenticated.
   */
  public boolean isAuthenticated(Context context) {
    requireNonNull(context, "context");

    String requestAccessKey = context.header(HttpConstants.HEADER_NAME_ACCESS_KEY);
    LOG.debug(
        "Provided access key in header is '{}', required value is '{}'",
        requestAccessKey,
        configuration.accessKey()
    );

    // Any empty access key indicates authentication is not required.
    if (Strings.isNullOrEmpty(configuration.accessKey())) {
      LOG.debug("No access key, authentication not required.");
      return true;
    }

    return Objects.equals(requestAccessKey, configuration.accessKey());
  }

}
