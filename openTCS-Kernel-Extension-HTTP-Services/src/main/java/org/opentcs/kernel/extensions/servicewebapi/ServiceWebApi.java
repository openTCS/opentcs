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
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SslParameterSet;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.kernel.extensions.servicewebapi.v1.V1RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

/**
 * Provides an HTTP interface for basic administration needs.
 */
public class ServiceWebApi
    implements KernelExtension {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ServiceWebApi.class);
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
   * Binds JSON data to objects and vice versa.
   */
  private final JsonBinder jsonBinder;
  /**
   * The connection encryption configuration.
   */
  private final SslParameterSet sslParamSet;
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
   * @param sslParamSet The SSL parameter set.
   * @param authenticator Authenticates incoming requests.
   * @param jsonBinder Binds JSON data to objects and vice versa.
   * @param v1RequestHandler Handles requests for API version 1.
   */
  @Inject
  public ServiceWebApi(ServiceWebApiConfiguration configuration,
                       SslParameterSet sslParamSet,
                       Authenticator authenticator,
                       JsonBinder jsonBinder,
                       V1RequestHandler v1RequestHandler) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.sslParamSet = requireNonNull(sslParamSet, "sslParamSet");
    this.authenticator = requireNonNull(authenticator, "authenticator");
    this.jsonBinder = requireNonNull(jsonBinder, "jsonBinder");
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

    if (configuration.useSsl()) {
      service.secure(sslParamSet.getKeystoreFile().getAbsolutePath(),
                     sslParamSet.getKeystorePassword(),
                     null,
                     null);
    }
    else {
      LOG.warn("Encryption disabled, connections will not be secured!");
    }

    service.before((request, response) -> {
      if (!authenticator.isAuthenticated(request)) {
        // Delay the response a bit to slow down brute force attacks.
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        service.halt(403, "Not authenticated.");
      }

      // Add a CORS header to allow cross-origin requests from all hosts.
      // This also makes using the "try it out" buttons in the Swagger UI documentation possible.
      response.header("Access-Control-Allow-Origin", "*");
    });

    // Reflect that we allow cross-origin requests for any headers and methods.
    service.options(
        "/*",
        (request, response) -> {
          String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
          if (accessControlRequestHeaders != null) {
            response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
          }

          String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
          if (accessControlRequestMethod != null) {
            response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
          }

          return "OK";
        });

    // Register routes for API versions here.
    service.path("/v1", () -> v1RequestHandler.addRoutes(service));

    service.exception(IllegalArgumentException.class, (exception, request, response) -> {
                    response.status(400);
                    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
                    response.body(jsonBinder.toJson(exception));
                  });
    service.exception(ObjectUnknownException.class, (exception, request, response) -> {
                    response.status(404);
                    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
                    response.body(jsonBinder.toJson(exception));
                  });
    service.exception(ObjectExistsException.class, (exception, request, response) -> {
                    response.status(409);
                    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
                    response.body(jsonBinder.toJson(exception));
                  });
    service.exception(KernelRuntimeException.class, (exception, request, response) -> {
                    response.status(500);
                    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
                    response.body(jsonBinder.toJson(exception));
                  });
    service.exception(IllegalStateException.class, (exception, request, response) -> {
                    response.status(500);
                    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
                    response.body(jsonBinder.toJson(exception));
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
