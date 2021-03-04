/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
 *
 * @author Stefan Walter (Fraunhofer IML)
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
   * Maps between objects and their JSON representations.
   */
  private final ObjectMapper objectMapper
      = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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
   * @param v1RequestHandler Handles requests for API version 1.
   */
  @Inject
  public ServiceWebApi(ServiceWebApiConfiguration configuration,
                       SslParameterSet sslParamSet,
                       Authenticator authenticator,
                       V1RequestHandler v1RequestHandler) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.authenticator = requireNonNull(authenticator, "authenticator");
    this.v1RequestHandler = requireNonNull(v1RequestHandler, "v1RequestHandler");
    this.sslParamSet = requireNonNull(sslParamSet, "sslParamSet");
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
    });

    // Register routes for API versions here.
    service.path("/v1", () -> v1RequestHandler.addRoutes(service));

    service.exception(IllegalArgumentException.class, (exception, request, response) -> {
                    response.status(400);
                    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
                    response.body(toJson(exception.getMessage()));
                  });
    service.exception(ObjectUnknownException.class, (exception, request, response) -> {
                    response.status(404);
                    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
                    response.body(toJson(exception.getMessage()));
                  });
    service.exception(ObjectExistsException.class, (exception, request, response) -> {
                    response.status(409);
                    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
                    response.body(toJson(exception.getMessage()));
                  });
    service.exception(KernelRuntimeException.class, (exception, request, response) -> {
                    response.status(500);
                    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
                    response.body(toJson(exception.getMessage()));
                  });
    service.exception(IllegalStateException.class, (exception, request, response) -> {
                    response.status(500);
                    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
                    response.body(toJson(exception.getMessage()));
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

  private String toJson(String exceptionMessage)
      throws IllegalStateException {
    try {
      return objectMapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(objectMapper.createArrayNode().add(exceptionMessage));
    }
    catch (JsonProcessingException exc) {
      throw new IllegalStateException("Could not produce JSON output", exc);
    }
  }
}
