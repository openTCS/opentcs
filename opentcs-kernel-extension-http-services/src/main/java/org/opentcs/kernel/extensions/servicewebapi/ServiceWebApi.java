// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Uninterruptibles;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.HttpResponseException;
import jakarta.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SslParameterSet;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.kernel.extensions.servicewebapi.v1.V1RequestHandler;
import org.opentcs.kernel.extensions.servicewebapi.v1.V1SseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an HTTP interface for basic administration needs.
 */
public class ServiceWebApi
    implements
      KernelExtension {

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
   * Handles connections to the Server-Sent Events API version 1.
   */
  private final V1SseHandler v1SseHandler;
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
  private Javalin app;
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
   * @param v1SseHandler Handles connections to the Server-Sent Events API version 1.
   */
  @Inject
  public ServiceWebApi(
      ServiceWebApiConfiguration configuration,
      SslParameterSet sslParamSet,
      Authenticator authenticator,
      JsonBinder jsonBinder,
      V1RequestHandler v1RequestHandler,
      V1SseHandler v1SseHandler
  ) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.sslParamSet = requireNonNull(sslParamSet, "sslParamSet");
    this.authenticator = requireNonNull(authenticator, "authenticator");
    this.jsonBinder = requireNonNull(jsonBinder, "jsonBinder");
    this.v1RequestHandler = requireNonNull(v1RequestHandler, "v1RequestHandler");
    this.v1SseHandler = requireNonNull(v1SseHandler, "sseHandler");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    v1RequestHandler.initialize();
    v1SseHandler.initialize();

    Consumer<JavalinConfig> config = cfg -> {
      cfg.showJavalinBanner = false;
      cfg.router.apiBuilder(v1RequestHandler.createRoutes());

      if (configuration.useSsl()) {
        cfg.registerPlugin(
            new SslPlugin(ssl -> {
              ssl.keystoreFromPath(
                  sslParamSet.getKeystoreFile().getAbsolutePath(),
                  sslParamSet.getKeystorePassword()
              );
              // Disable the default (insecure) HTTP connector.
              ssl.insecure = false;
              // Configure host and port for the (secure) SSL connector.
              ssl.host = configuration.bindAddress();
              ssl.securePort = configuration.bindPort();
            })
        );
      }
      else {
        cfg.jetty.defaultHost = configuration.bindAddress();
        cfg.jetty.defaultPort = configuration.bindPort();

        LOG.warn("Encryption disabled, connections will not be secured!");
      }
    };

    app = Javalin.create(config).start();

    app.sse("/v1/sse", v1SseHandler::handleSseConnection);

    app.beforeMatched(ctx -> {
      if (!authenticator.isAuthenticated(ctx)) {
        // Delay the response a bit to slow down brute force attacks.
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        throw new HttpResponseException(403, "Not authenticated.");
      }
      // Add a CORS header to allow cross-origin requests from all hosts.
      // This also makes using the "try it out" buttons in the Swagger UI documentation possible.
      ctx.header("Access-Control-Allow-Origin", "*");
    }
    );

    // Reflect that we allow cross-origin requests for any headers and methods.
    app.options("/*", ctx -> {
      String requestHeaders = ctx.header("Access-Control-Request-Headers");
      if (requestHeaders != null) {
        ctx.header("Access-Control-Allow-Headers", requestHeaders);
      }

      String requestMethod = ctx.header("Access-Control-Request-Method");
      if (requestMethod != null) {
        ctx.header("Access-Control-Allow-Methods", requestMethod);
      }

      ctx.result("OK");
    });

    app.exception(IllegalArgumentException.class, (e, ctx) -> {
      ctx.status(400);
      ctx.result(jsonBinder.toJson(e));
    });

    app.exception(ObjectUnknownException.class, (e, ctx) -> {
      ctx.status(404);
      ctx.result(jsonBinder.toJson(e));
    });

    app.exception(ObjectExistsException.class, (e, ctx) -> {
      ctx.status(409);
      ctx.result(jsonBinder.toJson(e));
    });

    app.exception(KernelRuntimeException.class, (e, ctx) -> {
      ctx.status(500);
      ctx.result(jsonBinder.toJson(e));
    });

    app.exception(KernelRuntimeException.class, (e, ctx) -> {
      ctx.status(400);
      ctx.result(jsonBinder.toJson(e));
    });

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    v1SseHandler.terminate();
    v1RequestHandler.terminate();
    app.stop();

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }
}
