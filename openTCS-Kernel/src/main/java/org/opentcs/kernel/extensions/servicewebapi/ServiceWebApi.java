/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import com.google.common.util.concurrent.Uninterruptibles;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.kernel.extensions.servicewebapi.v1.V1RequestHandler;
import spark.Service;

/**
 * Provides an HTTP interface for basic administration needs.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ServiceWebApi
    implements KernelExtension {

  /**
   * The interface configuration.
   */
  private final ServiceWebApiConfiguration configuration;
  /**
   * Authenticates incoming requests.
   */
  private final Authenticator authenticator;
  /**
   * Handles requests for API version 1.
   */
  private final V1RequestHandler v1RequestHandler;
  /**
   * The actual HTTP service.
   */
  private Service service;
  /**
   * Whether this kernel extension is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param configuration The interface configuration.
   * @param authenticator Authenticates incoming requests.
   * @param v1RequestHandler Handles requests for API version 1.
   */
  @Inject
  public ServiceWebApi(ServiceWebApiConfiguration configuration,
                       Authenticator authenticator,
                       V1RequestHandler v1RequestHandler) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.authenticator = requireNonNull(authenticator, "authenticator");
    this.v1RequestHandler = requireNonNull(v1RequestHandler, "v1RequestHandler");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    v1RequestHandler.initialize();

    service = Service.ignite()
        .ipAddress(configuration.bindAddress())
        .port(configuration.bindPort());

    service.before((request, response) -> {
      if (!authenticator.isAuthenticated(request)) {
        // Delay the response a bit to slow down brute force attacks.
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        service.halt(403, "Not authenticated.");
      }
    });
    service.path("/v1", () -> {
               service.get("/events",
                           v1RequestHandler::handleGetEvents);
               service.post("/transportOrders/:NAME",
                            v1RequestHandler::handlePostTransportOrder);
               service.post("/transportOrders/:NAME/withdrawal",
                            v1RequestHandler::handlePostWithdrawalByOrder);
               service.post("/vehicles/:NAME/withdrawal",
                            v1RequestHandler::handlePostWithdrawalByVehicle);
             }
    );
    service.exception(IllegalArgumentException.class, (exception, request, response) -> {
                    response.status(400);
                    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    response.body(exception.getMessage());
                  });
    service.exception(ObjectUnknownException.class, (exception, request, response) -> {
                    response.status(404);
                    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    response.body(exception.getMessage());
                  });
    service.exception(IllegalStateException.class, (exception, request, response) -> {
                    response.status(500);
                    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    response.body(exception.getMessage());
                  });

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    v1RequestHandler.terminate();
    service.stop();

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }
}
