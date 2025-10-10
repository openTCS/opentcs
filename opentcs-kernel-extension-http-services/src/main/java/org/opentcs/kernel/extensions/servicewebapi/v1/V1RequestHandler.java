// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;
import static java.util.Objects.requireNonNull;

import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.LocalKernel;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.kernel.extensions.servicewebapi.HttpConstants;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.RequestHandler;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PlantModelTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostOrderSequenceRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostPeripheralJobRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostTopologyUpdateRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostTransportOrderRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostVehicleCommAdapterMessageRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostVehicleRoutesRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostVehicleRoutesResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PutVehicleAcceptableOrderTypesTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PutVehicleAllowedOrderTypesTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PutVehicleEnergyLevelThresholdSetTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.OrderSequenceConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.PeripheralAttachmentInformationConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.PeripheralJobConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.TransportOrderConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.VehicleAttachmentInformationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles requests and produces responses for version 1 of the web API.
 */
public class V1RequestHandler
    implements
      RequestHandler {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(V1RequestHandler.class);
  private final JsonBinder jsonBinder;
  private final StatusEventDispatcher statusEventDispatcher;
  private final TransportOrderDispatcherHandler orderDispatcherHandler;
  private final TransportOrderHandler transportOrderHandler;
  private final PeripheralJobHandler peripheralJobHandler;
  private final PeripheralJobDispatcherHandler jobDispatcherHandler;
  private final PlantModelHandler plantModelHandler;
  private final VehicleHandler vehicleHandler;
  private final PathHandler pathHandler;
  private final LocationHandler locationHandler;
  private final PeripheralHandler peripheralHandler;
  private final OrderSequenceConverter orderSequenceConverter;
  private final PeripheralJobConverter peripheralJobConverter;
  private final TransportOrderConverter transportOrderConverter;
  private final PeripheralAttachmentInformationConverter peripheralAttachmentInformationConverter;
  private final VehicleAttachmentInformationConverter vehicleAttachmentInformationConverter;
  private final LocalKernel kernel;
  private final ScheduledExecutorService kernelExecutor;

  private boolean initialized;

  @Inject
  public V1RequestHandler(
      JsonBinder jsonBinder,
      StatusEventDispatcher statusEventDispatcher,
      TransportOrderDispatcherHandler orderDispatcherHandler,
      TransportOrderHandler transportOrderHandler,
      PeripheralJobHandler peripheralJobHandler,
      PeripheralJobDispatcherHandler jobDispatcherHandler,
      PlantModelHandler plantModelHandler,
      VehicleHandler vehicleHandler,
      PathHandler pathHandler,
      LocationHandler locationHandler,
      PeripheralHandler peripheralHandler,
      OrderSequenceConverter orderSequenceConverter,
      PeripheralJobConverter peripheralJobConverter,
      TransportOrderConverter transportOrderConverter,
      PeripheralAttachmentInformationConverter peripheralAttachmentInformationConverter,
      VehicleAttachmentInformationConverter vehicleAttachmentInformationConverter,
      LocalKernel kernel,
      @KernelExecutor
      ScheduledExecutorService kernelExecutor
  ) {
    this.jsonBinder = requireNonNull(jsonBinder, "jsonBinder");
    this.statusEventDispatcher = requireNonNull(statusEventDispatcher, "statusEventDispatcher");
    this.orderDispatcherHandler = requireNonNull(orderDispatcherHandler, "orderDispatcherHandler");
    this.transportOrderHandler = requireNonNull(transportOrderHandler, "transportOrderHandler");
    this.peripheralJobHandler = requireNonNull(peripheralJobHandler, "peripheralJobHandler");
    this.jobDispatcherHandler = requireNonNull(jobDispatcherHandler, "jobDispatcherHandler");
    this.plantModelHandler = requireNonNull(plantModelHandler, "plantModelHandler");
    this.vehicleHandler = requireNonNull(vehicleHandler, "vehicleHandler");
    this.pathHandler = requireNonNull(pathHandler, "pathHandler");
    this.locationHandler = requireNonNull(locationHandler, "locationHandler");
    this.peripheralHandler = requireNonNull(peripheralHandler, "peripheralHandler");
    this.orderSequenceConverter = requireNonNull(orderSequenceConverter, "orderSequenceConverter");
    this.peripheralJobConverter = requireNonNull(peripheralJobConverter, "peripheralJobConverter");
    this.transportOrderConverter = requireNonNull(
        transportOrderConverter, "transportOrderConverter"
    );
    this.peripheralAttachmentInformationConverter = requireNonNull(
        peripheralAttachmentInformationConverter, "peripheralAttachmentInformationConverter"
    );
    this.vehicleAttachmentInformationConverter = requireNonNull(
        vehicleAttachmentInformationConverter, "vehicleAttachmentInformationConverter"
    );
    this.kernel = requireNonNull(kernel, "kernel");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    statusEventDispatcher.initialize();

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

    statusEventDispatcher.terminate();

    initialized = false;
  }

  @Override
  public EndpointGroup createRoutes() {
    return () -> path(
        "/v1", () -> {
          get("/kernel/version", this::handleGetVersion);
          delete("/kernel", this::handleDeleteKernel);
          get("/events", this::handleGetEvents);
          post("/vehicles/dispatcher/trigger", this::handlePostDispatcherTrigger);
          post("/vehicles/{NAME}/routeComputationQuery", this::handleGetVehicleRoutes);
          put(
              "/vehicles/{NAME}/commAdapter/attachment",
              this::handlePutVehicleCommAdapterAttachment
          );
          get(
              "/vehicles/{NAME}/commAdapter/attachmentInformation",
              this::handleGetVehicleCommAdapterAttachmentInfo
          );
          put("/vehicles/{NAME}/commAdapter/enabled", this::handlePutVehicleCommAdapterEnabled);
          post("/vehicles/{NAME}/commAdapter/message", this::handlePostVehicleCommAdapterMessage);
          put("/vehicles/{NAME}/paused", this::handlePutVehiclePaused);
          put("/vehicles/{NAME}/integrationLevel", this::handlePutVehicleIntegrationLevel);
          post("/vehicles/{NAME}/withdrawal", this::handlePostWithdrawalByVehicle);
          post("/vehicles/{NAME}/rerouteRequest", this::handlePostVehicleRerouteRequest);
          put("/vehicles/{NAME}/allowedOrderTypes", this::handlePutVehicleAllowedOrderTypes);
          put(
              "/vehicles/{NAME}/acceptableOrderTypes",
              this::handlePutVehicleAcceptableOrderTypes
          );
          put(
              "/vehicles/{NAME}/energyLevelThresholdSet",
              this::handlePutVehicleEnergyLevelThresholdSet
          );
          put("/vehicles/{NAME}/envelopeKey", this::handlePutVehicleEnvelopeKey);
          get("/vehicles/{NAME}", this::handleGetVehicleByName);
          get("/vehicles", this::handleGetVehicles);
          post("/transportOrders/dispatcher/trigger", this::handlePostDispatcherTrigger);
          post(
              "/transportOrders/{NAME}/immediateAssignment",
              this::handlePostImmediateAssignment
          );
          post("/transportOrders/{NAME}/withdrawal", this::handlePostWithdrawalByOrder);
          post("/transportOrders/{NAME}", this::handlePostTransportOrder);
          put(
              "/transportOrders/{NAME}/intendedVehicle",
              this::handlePutTransportOrderIntendedVehicle
          );
          get("/transportOrders/{NAME}", this::handleGetTransportOrderByName);
          get("/transportOrders", this::handleGetTransportOrders);
          post("/orderSequences/{NAME}", this::handlePostOrderSequence);
          get("/orderSequences", this::handleGetOrderSequences);
          get("/orderSequences/{NAME}", this::handleGetOrderSequenceByName);
          put("/orderSequences/{NAME}/complete", this::handlePutOrderSequenceComplete);
          put("/plantModel", this::handlePutPlantModel);
          get("/plantModel", this::handleGetPlantModel);
          post("/plantModel/topologyUpdateRequest", this::handlePostUpdateTopology);
          put("/paths/{NAME}/locked", this::handlePutPathLocked);
          put("/locations/{NAME}/locked", this::handlePutLocationLocked);
          post("/dispatcher/trigger", this::handlePostDispatcherTrigger);
          post("/peripherals/dispatcher/trigger", this::handlePostPeripheralJobsDispatchTrigger);
          post("/peripherals/{NAME}/withdrawal", this::handlePostPeripheralWithdrawal);
          put(
              "/peripherals/{NAME}/commAdapter/enabled",
              this::handlePutPeripheralCommAdapterEnabled
          );
          get(
              "/peripherals/{NAME}/commAdapter/attachmentInformation",
              this::handleGetPeripheralCommAdapterAttachmentInfo
          );
          put(
              "/peripherals/{NAME}/commAdapter/attachment",
              this::handlePutPeripheralCommAdapterAttachment
          );
          get("/peripheralJobs", this::handleGetPeripheralJobs);
          get("/peripheralJobs/{NAME}", this::handleGetPeripheralJobsByName);
          post("/peripheralJobs/{NAME}", this::handlePostPeripheralJobsByName);
          post("/peripheralJobs/{NAME}/withdrawal", this::handlePostPeripheralJobWithdrawal);
          post(
              "/peripheralJobs/dispatcher/trigger",
              this::handlePostPeripheralJobsDispatchTrigger
          );
        }
    );
  }

  public void handleGetVersion(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(jsonBinder.toJson(new Version()));
  }

  public void handleDeleteKernel(Context ctx) {
    LOG.info("Initiating kernel shutdown as requested from {}...", ctx.ip());
    kernelExecutor.schedule(() -> kernel.setState(Kernel.State.SHUTDOWN), 1, TimeUnit.SECONDS);
    ctx.result("");
  }

  private void handlePostDispatcherTrigger(Context ctx)
      throws KernelRuntimeException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    orderDispatcherHandler.triggerDispatcher();
    ctx.result("");
  }

  public void handleGetEvents(Context ctx)
      throws IllegalArgumentException,
        IllegalStateException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            statusEventDispatcher.fetchEvents(minSequenceNo(ctx), maxSequenceNo(ctx), timeout(ctx))
        )
    );
  }

  private void handlePutVehicleCommAdapterEnabled(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleCommAdapterEnabled(ctx.pathParam("NAME"), ctx.queryParam("newValue"));
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePostVehicleCommAdapterMessage(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.postVehicleCommAdapterMessage(
        ctx.pathParam("NAME"),
        jsonBinder.fromJson(ctx.body(), PostVehicleCommAdapterMessageRequestTO.class)
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handleGetVehicleCommAdapterAttachmentInfo(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            vehicleAttachmentInformationConverter.toGetVehicleAttachmentInfoResponseTO(
                vehicleHandler.getVehicleCommAdapterAttachmentInformation(ctx.pathParam("NAME"))
            )
        )
    );
  }

  private void handleGetVehicleRoutes(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            PostVehicleRoutesResponseTO.fromMap(
                vehicleHandler.getVehicleRoutes(
                    ctx.pathParam("NAME"),
                    maxRoutePerDestinationPoint(ctx),
                    jsonBinder.fromJson(ctx.body(), PostVehicleRoutesRequestTO.class)
                )
            )
        )
    );
  }

  private void handlePutVehicleCommAdapterAttachment(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleCommAdapter(ctx.pathParam("NAME"), ctx.queryParam("newValue"));
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePostTransportOrder(Context ctx)
      throws ObjectUnknownException,
        ObjectExistsException,
        IllegalArgumentException,
        IllegalStateException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            transportOrderConverter.toGetTransportOrderResponse(
                transportOrderHandler.createOrder(
                    ctx.pathParam("NAME"),
                    jsonBinder.fromJson(ctx.body(), PostTransportOrderRequestTO.class)
                )
            )
        )
    );
  }

  private void handlePutTransportOrderIntendedVehicle(Context ctx)
      throws ObjectUnknownException {
    transportOrderHandler.updateTransportOrderIntendedVehicle(
        ctx.pathParam("NAME"),
        ctx.queryParam("vehicle")
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePostOrderSequence(Context ctx)
      throws ObjectUnknownException,
        ObjectExistsException,
        IllegalArgumentException,
        IllegalStateException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            orderSequenceConverter.toGetOrderSequenceResponseTO(
                transportOrderHandler.createOrderSequence(
                    ctx.pathParam("NAME"),
                    jsonBinder.fromJson(ctx.body(), PostOrderSequenceRequestTO.class)
                )
            )
        )
    );
  }

  private void handleGetOrderSequences(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            transportOrderHandler.getOrderSequences(
                ctx.queryParam("intendedVehicle")
            )
        )
    );
  }

  private void handleGetOrderSequenceByName(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(transportOrderHandler.getOrderSequenceByName(ctx.pathParam("NAME")))
    );
  }

  private void handlePutOrderSequenceComplete(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException,
        InterruptedException,
        ExecutionException {
    transportOrderHandler.putOrderSequenceComplete(ctx.pathParam("NAME"));
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePostImmediateAssignment(Context ctx)
      throws ObjectUnknownException {
    orderDispatcherHandler.tryImmediateAssignment(ctx.pathParam("NAME"));
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePostWithdrawalByOrder(Context ctx)
      throws ObjectUnknownException {
    orderDispatcherHandler.withdrawByTransportOrder(
        ctx.pathParam("NAME"),
        immediate(ctx),
        disableVehicle(ctx)
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePostWithdrawalByVehicle(Context ctx)
      throws ObjectUnknownException {
    orderDispatcherHandler.withdrawByVehicle(
        ctx.pathParam("NAME"), immediate(ctx),
        disableVehicle(ctx)
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePostPeripheralJobWithdrawal(Context ctx)
      throws KernelRuntimeException {
    jobDispatcherHandler.withdrawPeripheralJob(ctx.pathParam("NAME"));
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePostVehicleRerouteRequest(Context ctx)
      throws ObjectUnknownException {
    orderDispatcherHandler.reroute(ctx.pathParam("NAME"), forced(ctx));
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handleGetTransportOrders(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            transportOrderHandler.getTransportOrders(
                ctx.queryParam("intendedVehicle")
            )
        )
    );
  }

  private void handlePutPlantModel(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    plantModelHandler.putPlantModel(jsonBinder.fromJson(ctx.body(), PlantModelTO.class));
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handleGetPlantModel(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(jsonBinder.toJson(plantModelHandler.getPlantModel()));
  }

  private void handlePostUpdateTopology(Context ctx)
      throws ObjectUnknownException,
        KernelRuntimeException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    if (ctx.body().isBlank()) {
      plantModelHandler.requestTopologyUpdate(new PostTopologyUpdateRequestTO(List.of()));
    }
    else {
      plantModelHandler.requestTopologyUpdate(
          jsonBinder.fromJson(ctx.body(), PostTopologyUpdateRequestTO.class)
      );
    }
    ctx.result("");
  }

  private void handlePutPathLocked(Context ctx) {
    pathHandler.updatePathLock(
        ctx.pathParam("NAME"),
        ctx.queryParam("newValue")
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePutLocationLocked(Context ctx) {
    locationHandler.updateLocationLock(
        ctx.pathParam("NAME"),
        ctx.queryParam("newValue")
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handleGetTransportOrderByName(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(transportOrderHandler.getTransportOrderByName(ctx.pathParam("NAME")))
    );
  }

  private void handleGetVehicles(Context ctx)
      throws IllegalArgumentException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            vehicleHandler.getVehiclesState(ctx.queryParam("procState"))
        )
    );
  }

  private void handleGetVehicleByName(Context ctx)
      throws ObjectUnknownException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(jsonBinder.toJson(vehicleHandler.getVehicleStateByName(ctx.pathParam("NAME"))));
  }

  private void handlePutVehicleIntegrationLevel(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleIntegrationLevel(
        ctx.pathParam("NAME"),
        ctx.queryParam("newValue")
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePutVehiclePaused(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehiclePaused(
        ctx.pathParam("NAME"),
        ctx.queryParam("newValue")
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  @Deprecated
  private void handlePutVehicleAllowedOrderTypes(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleAllowedOrderTypes(
        ctx.pathParam("NAME"),
        jsonBinder.fromJson(ctx.body(), PutVehicleAllowedOrderTypesTO.class)
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePutVehicleAcceptableOrderTypes(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleAcceptableOrderTypes(
        ctx.pathParam("NAME"),
        jsonBinder.fromJson(ctx.body(), PutVehicleAcceptableOrderTypesTO.class)
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePutVehicleEnergyLevelThresholdSet(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleEnergyLevelThresholdSet(
        ctx.pathParam("NAME"),
        jsonBinder.fromJson(ctx.body(), PutVehicleEnergyLevelThresholdSetTO.class)
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePutVehicleEnvelopeKey(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleEnvelopeKey(
        ctx.pathParam("NAME"),
        ctx.queryParam("newValue")
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePostPeripheralWithdrawal(Context ctx)
      throws KernelRuntimeException {
    jobDispatcherHandler.withdrawPeripheralJobByLocation(ctx.pathParam("NAME"));
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePutPeripheralCommAdapterEnabled(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    peripheralHandler.putPeripheralCommAdapterEnabled(
        ctx.pathParam("NAME"),
        ctx.queryParam("newValue")
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handleGetPeripheralCommAdapterAttachmentInfo(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            peripheralAttachmentInformationConverter.toGetPeripheralAttachmentInfoResponseTO(
                peripheralHandler.getPeripheralCommAdapterAttachmentInformation(
                    ctx.pathParam("NAME")
                )
            )
        )
    );
  }

  private void handleGetPeripheralJobs(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            peripheralJobHandler.getPeripheralJobs(
                ctx.queryParam("relatedVehicle"),
                ctx.queryParam("relatedTransportOrder")
            )
        )
    );
  }

  private void handlePutPeripheralCommAdapterAttachment(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    peripheralHandler.putPeripheralCommAdapter(
        ctx.pathParam("NAME"),
        ctx.queryParam("newValue")
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handleGetPeripheralJobsByName(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            peripheralJobHandler.getPeripheralJobByName(ctx.pathParam("NAME"))
        )
    );
  }

  private void handlePostPeripheralJobsByName(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            peripheralJobConverter.toGetPeripheralJobResponseTO(
                peripheralJobHandler.createPeripheralJob(
                    ctx.pathParam("NAME"),
                    jsonBinder.fromJson(ctx.body(), PostPeripheralJobRequestTO.class)
                )
            )
        )
    );
  }

  private void handlePostPeripheralJobsDispatchTrigger(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    jobDispatcherHandler.triggerJobDispatcher();
    ctx.result("");
  }

  private int maxRoutePerDestinationPoint(Context ctx)
      throws IllegalArgumentException {
    String param = ctx.queryParamAsClass("maxRoutesPerDestinationPoint", String.class)
        .getOrDefault("1");
    try {
      return Integer.parseInt(param);
    }
    catch (NumberFormatException exc) {
      throw new IllegalArgumentException("Malformed maxRoutesPerDestinationPoint: " + param);
    }
  }

  private long minSequenceNo(Context ctx)
      throws IllegalArgumentException {
    String param = ctx.queryParamAsClass("minSequenceNo", String.class).getOrDefault("0");
    try {
      return Long.parseLong(param);
    }
    catch (NumberFormatException exc) {
      throw new IllegalArgumentException("Malformed minSequenceNo: " + param);
    }
  }

  private long maxSequenceNo(Context ctx)
      throws IllegalArgumentException {
    String param = ctx.queryParamAsClass("maxSequenceNo", String.class)
        .getOrDefault(String.valueOf(Long.MAX_VALUE));
    try {
      return Long.parseLong(param);
    }
    catch (NumberFormatException exc) {
      throw new IllegalArgumentException("Malformed minSequenceNo: " + param);
    }
  }

  private long timeout(Context ctx)
      throws IllegalArgumentException {
    String param = ctx.queryParamAsClass("timeout", String.class).getOrDefault("1000");
    try {
      // Allow a maximum timeout of 10 seconds so server threads are only bound for a limited time.
      return Math.min(10000, Long.parseLong(param));
    }
    catch (NumberFormatException exc) {
      throw new IllegalArgumentException("Malformed timeout: " + param);
    }
  }

  private boolean immediate(Context ctx) {
    return Boolean.parseBoolean(ctx.queryParam("immediate"));
  }

  private boolean disableVehicle(Context ctx) {
    return Boolean.parseBoolean(ctx.queryParam("disableVehicle"));
  }

  private boolean forced(Context ctx) {
    return Boolean.parseBoolean(ctx.queryParam("forced"));
  }
}
