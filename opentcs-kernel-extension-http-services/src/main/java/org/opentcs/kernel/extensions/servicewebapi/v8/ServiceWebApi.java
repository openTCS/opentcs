// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Uninterruptibles;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Header;
import io.javalin.http.HttpResponseException;
import jakarta.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.eclipse.jetty.http.HttpMethod;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SslParameterSet;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.auth.AccessControl;
import org.opentcs.kernel.extensions.servicewebapi.v8.auth.AuthenticationException;
import org.opentcs.kernel.extensions.servicewebapi.v8.auth.AuthenticationServletContextListener;
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
   * Handles requests for API version 8.
   */
  private final RequestHandler requestHandler;
  /**
   * Handles connections to the Server-Sent Events API version 8.
   */
  private final SseHandler sseHandler;
  /**
   * Binds JSON data to objects and vice versa.
   */
  private final JsonBinder jsonBinder;
  /**
   * The connection encryption configuration.
   */
  private final SslParameterSet sslParamSet;
  /**
   * The access control.
   */
  private final AccessControl accessControl;
  /**
   * The servlet context listener for authentication and authorization.
   */
  private final AuthenticationServletContextListener authenticationServletContextListener;
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
   * @param jsonBinder Binds JSON data to objects and vice versa.
   * @param requestHandler Handles requests for API version 8.
   * @param sseHandler Handles connections to the Server-Sent Events API version 8.
   * @param accessControl The access control.
   * @param authenticationServletContextListener A servlet context listener for authentication and
   * authorization.
   */
  @Inject
  public ServiceWebApi(
      ServiceWebApiConfiguration configuration,
      SslParameterSet sslParamSet,
      JsonBinder jsonBinder,
      RequestHandler requestHandler,
      SseHandler sseHandler,
      AccessControl accessControl,
      AuthenticationServletContextListener authenticationServletContextListener
  ) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.sslParamSet = requireNonNull(sslParamSet, "sslParamSet");
    this.jsonBinder = requireNonNull(jsonBinder, "jsonBinder");
    this.requestHandler = requireNonNull(requestHandler, "requestHandler");
    this.sseHandler = requireNonNull(sseHandler, "sseHandler");
    this.accessControl = requireNonNull(accessControl, "accessControl");
    this.authenticationServletContextListener = requireNonNull(
        authenticationServletContextListener,
        "authenticationServletContextListener"
    );
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    requestHandler.initialize();
    sseHandler.initialize();

    Consumer<JavalinConfig> config = cfg -> {
      cfg.startup.showJavalinBanner = false;
      cfg.routes.apiBuilder(requestHandler.createRoutes());
      if (configuration.maxRequestBodySize() <= 0) {
        LOG.warn(
            "Maximum request body size must be at least 1 MB. Using default size of {} bytes.",
            cfg.http.maxRequestSize
        );
      }
      else {
        cfg.http.maxRequestSize = configuration.maxRequestBodySize() * 1024L * 1024L;
      }

      cfg.bundledPlugins.enableCors(cors -> {
        cors.addRule(it -> {
          // Add a CORS header to allow cross-origin requests from all hosts. Instead of simply
          // using the special star/wildcard origin ("*"), explicitly reflect the client's origin
          // in response headers. This is necessary since we also need to set the
          // Access-Control-Allow-Credentials response header to allow credentials to be included
          // in requests (in our case via a cookie header).
          it.reflectClientOrigin = true;
          // Allow credentials to be included in cross-origin HTTP requests.
          it.allowCredentials = true;
        });
      });

      cfg.jetty.modifyServletContextHandler(
          handler -> handler.addEventListener(authenticationServletContextListener)
      );

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
        cfg.jetty.host = configuration.bindAddress();
        cfg.jetty.port = configuration.bindPort();

        LOG.warn("Encryption disabled, connections will not be secured!");
      }

      configureRequestLogging(cfg);

      cfg.routes.sse("/v8/sse", sseHandler::handleSseConnection);

      cfg.routes.beforeMatched(ctx -> {
        if (HttpMethod.OPTIONS.is(ctx.req().getMethod())) {
          // According to the W3C specification, CORS preflight requests (which use the OPTIONS
          // method) never include credentials. Therefore, preflight requests should not require
          // authentication or authorization.
          return;
        }
        if (ctx.path().equals("/v8/login")) {
          return;
        }
        if (!accessControl.isLoggedIn()) {
          // Delay the response a bit to slow down brute force attacks.
          Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
          throw new HttpResponseException(401, "Not authenticated.");
        }
        if (!accessControl.isAuthorized(ctx.routeRoles())) {
          // Delay the response a bit to slow down brute force attacks.
          Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
          throw new HttpResponseException(403, "Not authorized.");
        }
      }
      );

      // Reflect that we allow cross-origin requests for any headers and methods.
      cfg.routes.options("/*", ctx -> {
        String requestHeaders = ctx.header(Header.ACCESS_CONTROL_REQUEST_HEADERS);
        if (requestHeaders != null) {
          ctx.header(Header.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders);
        }

        String requestMethod = ctx.header(Header.ACCESS_CONTROL_REQUEST_METHOD);
        if (requestMethod != null) {
          ctx.header(Header.ACCESS_CONTROL_ALLOW_METHODS, requestMethod);
        }

        ctx.result("OK");
      });

      cfg.routes.exception(IllegalArgumentException.class, (e, ctx) -> {
        ctx.status(400);
        ctx.result(jsonBinder.toJson(e));
      });

      cfg.routes.exception(
          AuthenticationException.class, (e, ctx) -> {
            ctx.status(401);
            ctx.result(jsonBinder.toJson(e));
          }
      );

      cfg.routes.exception(
          IllegalStateException.class, (e, ctx) -> {
            ctx.status(500);
            ctx.result(jsonBinder.toJson(e));
          }
      );

      cfg.routes.exception(ObjectUnknownException.class, (e, ctx) -> {
        ctx.status(404);
        ctx.result(jsonBinder.toJson(e));
      });

      cfg.routes.exception(ObjectExistsException.class, (e, ctx) -> {
        ctx.status(409);
        ctx.result(jsonBinder.toJson(e));
      });

      cfg.routes.exception(KernelRuntimeException.class, (e, ctx) -> {
        ctx.status(500);
        ctx.result(jsonBinder.toJson(e));
      });
    };

    app = Javalin.create(config).start();

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    sseHandler.terminate();
    requestHandler.terminate();
    app.stop();

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  private void configureRequestLogging(JavalinConfig cfg) {
    cfg.routes.beforeMatched(
        ctx -> LOG.debug(
            "Incoming request to '{} {}' by '{}'. (Request ID: {})",
            ctx.method(),
            ctx.fullUrl(),
            ctx.host(),
            ctx.req().getRequestId()
        )
    );

    cfg.requestLogger.http(
        (ctx, ms) -> LOG.debug(
            "Request to '{} {}' by '{}' took {} ms. (Request ID: {})",
            ctx.method(),
            ctx.fullUrl(),
            ctx.host(),
            ms.longValue(),
            ctx.req().getRequestId()
        )
    );
  }
}
