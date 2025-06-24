// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.adminwebapi;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static java.util.Objects.requireNonNull;

import io.javalin.Javalin;
import jakarta.inject.Inject;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.kernel.extensions.adminwebapi.v1.V1RequestHandler;
import org.opentcs.kernel.extensions.servicewebapi.HttpConstants;

/**
 * Provides an HTTP interface for basic administration needs.
 */
public class AdminWebApi
    implements
      KernelExtension {

  /**
   * The interface configuration.
   */
  private final AdminWebApiConfiguration configuration;
  /**
   * Handles requests for API version 1.
   */
  private final V1RequestHandler v1RequestHandler;
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
   * @param v1RequestHandler Handles requests for API version 1.
   */
  @Inject
  public AdminWebApi(
      AdminWebApiConfiguration configuration,
      V1RequestHandler v1RequestHandler
  ) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.v1RequestHandler = requireNonNull(v1RequestHandler, "v1RequestHandler");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    app = Javalin.create(
        config -> {
          config.showJavalinBanner = false;
          config.router.apiBuilder(
              () -> path(
                  "/v1", () -> {
                    get("/version", v1RequestHandler::handleGetVersion);
                    get("/status", v1RequestHandler::handleGetStatus);
                    delete("/kernel", v1RequestHandler::handleDeleteKernel);
                  }
              )
          );
        }
    ).start(configuration.bindAddress(), configuration.bindPort());

    app.exception(
        IllegalArgumentException.class, (exception, ctx) -> {
          ctx.status(400);
          ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
          ctx.result(exception.getMessage());
        }
    );

    app.exception(
        IllegalStateException.class, (exception, ctx) -> {
          ctx.status(500);
          ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
          ctx.result(exception.getMessage());
        }
    );

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    app.stop();

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

}
