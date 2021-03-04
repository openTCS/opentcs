/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import com.google.common.base.Strings;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

/**
 * Authenticates incoming requests.
 *
 * @author Stefan Walter (Fraunhofer IML)
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
   * @param request The request to be checked.
   * @return <code>true</code> if, and only if, authentication is required and the given request is
   * authenticated.
   */
  public boolean isAuthenticated(Request request) {
    requireNonNull(request, "request");

    String requestAccessKey = request.headers(HttpConstants.HEADER_NAME_ACCESS_KEY);
    LOG.debug("Provided access key in header is '{}', required value is '{}'",
              requestAccessKey,
              configuration.accessKey());

    // Any empty access key indicates authentication is not required.
    if (Strings.isNullOrEmpty(configuration.accessKey())) {
      LOG.debug("No access key, authentication not required.");
      return true;
    }

    return Objects.equals(requestAccessKey, configuration.accessKey());
  }

}
