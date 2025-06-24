// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.adminwebapi.v1;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.http.Context;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.kernel.extensions.servicewebapi.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles requests and produces responses for version 1 of the admin web API.
 */
public class V1RequestHandler
    implements
      Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(V1RequestHandler.class);
  /**
   * Maps between objects and their JSON representations.
   */
  private final ObjectMapper objectMapper
      = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  /**
   * The local kernel.
   */
  private final LocalKernel kernel;
  /**
   * Used to schedule kernel shutdowns.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * Whether this instance is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param kernel The local kernel.
   * @param kernelExecutor Use to schedule kernel shutdowns.
   */
  @Inject
  public V1RequestHandler(
      LocalKernel kernel,
      @KernelExecutor
      ScheduledExecutorService kernelExecutor
  ) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    initialized = false;
  }

  public void handleGetVersion(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(toJson(new Version()));
  }

  public void handleGetStatus(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(toJson(new Status()));
  }

  public void handleDeleteKernel(Context ctx) {
    LOG.info("Initiating kernel shutdown as requested from {}...", ctx.ip());
    kernelExecutor.schedule(() -> kernel.setState(Kernel.State.SHUTDOWN), 1, TimeUnit.SECONDS);
    ctx.result("");
  }

  private <T> T fromJson(String jsonString, Class<T> clazz)
      throws IllegalArgumentException {
    try {
      return objectMapper.readValue(jsonString, clazz);
    }
    catch (IOException exc) {
      throw new IllegalArgumentException("Could not parse JSON input", exc);
    }
  }

  private String toJson(Object object)
      throws IllegalStateException {
    try {
      return objectMapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(object);
    }
    catch (JsonProcessingException exc) {
      throw new IllegalStateException("Could not produce JSON output", exc);
    }
  }

}
