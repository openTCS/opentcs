// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;
import static java.util.Objects.requireNonNull;

import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.kernel.extensions.servicewebapi.common.HttpConstants;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.auth.AccessControl;
import org.opentcs.kernel.extensions.servicewebapi.v8.auth.AuthenticationException;
import org.opentcs.kernel.extensions.servicewebapi.v8.auth.UserPermission;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostEnvironmentalEntityRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostLoginRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostOrderSequenceRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostPeripheralJobRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostTopologyUpdateRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostTransportOrderRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostVehicleCommAdapterMessageRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostVehicleRouteComputationQueryRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PutEnvironmentalEntityEnvelopeRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PutEnvironmentalEntityPoseRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PutPlantModelRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PutVehicleAcceptableOrderTypesTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PutVehicleEnergyLevelThresholdSetTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.GetVersionResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.LoginResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.EnvironmentalEntityConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.OrderSequenceConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.PeripheralAttachmentInformationConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.PeripheralJobConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.TransportOrderConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.UserNotificationConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.VehicleAttachmentInformationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles requests and produces responses for version 8 of the web API.
 */
public class RequestHandler
    implements
      Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RequestHandler.class);
  private final JsonBinder jsonBinder;
  private final TransportOrderDispatcherHandler orderDispatcherHandler;
  private final TransportOrderHandler transportOrderHandler;
  private final PeripheralJobHandler peripheralJobHandler;
  private final PeripheralJobDispatcherHandler jobDispatcherHandler;
  private final PlantModelHandler plantModelHandler;
  private final VehicleHandler vehicleHandler;
  private final PointHandler pointHandler;
  private final PathHandler pathHandler;
  private final LocationHandler locationHandler;
  private final LocationTypeHandler locationTypeHandler;
  private final BlockHandler blockHandler;
  private final VisualLayoutHandler visualLayoutHandler;
  private final PeripheralHandler peripheralHandler;
  private final EnvironmentalEntityHandler environmentalEntityHandler;
  private final OrderSequenceConverter orderSequenceConverter;
  private final PeripheralJobConverter peripheralJobConverter;
  private final TransportOrderConverter transportOrderConverter;
  private final EnvironmentalEntityConverter environmentalEntityConverter;
  private final PeripheralAttachmentInformationConverter peripheralAttachmentInformationConverter;
  private final VehicleAttachmentInformationConverter vehicleAttachmentInformationConverter;
  private final UserNotificationConverter userNotificationConverter;
  private final NotificationService notificationService;
  private final LocalKernel kernel;
  private final ScheduledExecutorService kernelExecutor;
  private final AccessControl accessControl;

  private boolean initialized;

  @Inject
  public RequestHandler(
      JsonBinder jsonBinder,
      TransportOrderDispatcherHandler orderDispatcherHandler,
      TransportOrderHandler transportOrderHandler,
      PeripheralJobHandler peripheralJobHandler,
      PeripheralJobDispatcherHandler jobDispatcherHandler,
      PlantModelHandler plantModelHandler,
      VehicleHandler vehicleHandler,
      PointHandler pointHandler,
      PathHandler pathHandler,
      LocationHandler locationHandler,
      LocationTypeHandler locationTypeHandler,
      BlockHandler blockHandler,
      VisualLayoutHandler visualLayoutHandler,
      PeripheralHandler peripheralHandler,
      EnvironmentalEntityHandler environmentalEntityHandler,
      OrderSequenceConverter orderSequenceConverter,
      PeripheralJobConverter peripheralJobConverter,
      TransportOrderConverter transportOrderConverter,
      EnvironmentalEntityConverter environmentalEntityConverter,
      PeripheralAttachmentInformationConverter peripheralAttachmentInformationConverter,
      VehicleAttachmentInformationConverter vehicleAttachmentInformationConverter,
      UserNotificationConverter userNotificationConverter,
      NotificationService notificationService,
      LocalKernel kernel,
      @KernelExecutor
      ScheduledExecutorService kernelExecutor,
      AccessControl accessControl
  ) {
    this.jsonBinder = requireNonNull(jsonBinder, "jsonBinder");
    this.orderDispatcherHandler = requireNonNull(orderDispatcherHandler, "orderDispatcherHandler");
    this.transportOrderHandler = requireNonNull(transportOrderHandler, "transportOrderHandler");
    this.peripheralJobHandler = requireNonNull(peripheralJobHandler, "peripheralJobHandler");
    this.jobDispatcherHandler = requireNonNull(jobDispatcherHandler, "jobDispatcherHandler");
    this.plantModelHandler = requireNonNull(plantModelHandler, "plantModelHandler");
    this.vehicleHandler = requireNonNull(vehicleHandler, "vehicleHandler");
    this.pointHandler = requireNonNull(pointHandler, "pointHandler");
    this.pathHandler = requireNonNull(pathHandler, "pathHandler");
    this.locationHandler = requireNonNull(locationHandler, "locationHandler");
    this.locationTypeHandler = requireNonNull(locationTypeHandler, "locationTypeHandler");
    this.blockHandler = requireNonNull(blockHandler, "blockHandler");
    this.visualLayoutHandler = requireNonNull(visualLayoutHandler, "visualLayoutHandler");
    this.peripheralHandler = requireNonNull(peripheralHandler, "peripheralHandler");
    this.environmentalEntityHandler
        = requireNonNull(environmentalEntityHandler, "environmentalEntityHandler");
    this.orderSequenceConverter = requireNonNull(orderSequenceConverter, "orderSequenceConverter");
    this.peripheralJobConverter = requireNonNull(peripheralJobConverter, "peripheralJobConverter");
    this.transportOrderConverter
        = requireNonNull(transportOrderConverter, "transportOrderConverter");
    this.environmentalEntityConverter
        = requireNonNull(environmentalEntityConverter, "environmentalEntityConverter");
    this.peripheralAttachmentInformationConverter = requireNonNull(
        peripheralAttachmentInformationConverter, "peripheralAttachmentInformationConverter"
    );
    this.vehicleAttachmentInformationConverter = requireNonNull(
        vehicleAttachmentInformationConverter,
        "vehicleAttachmentInformationConverter"
    );
    this.userNotificationConverter
        = requireNonNull(userNotificationConverter, "userNotificationConverter");
    this.notificationService = requireNonNull(notificationService, "notificationService");
    this.kernel = requireNonNull(kernel, "kernel");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.accessControl = requireNonNull(accessControl, "accessControl");
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

  public EndpointGroup createRoutes() {
    return () -> path(
        "/v8", () -> {
          post("/login", this::handlePostLogin);
          post("/logout", this::handlePostLogout);
          get("/session", this::handleGetSession);
          get("/kernel/version", this::handleGetVersion, UserPermission.READ_DATA);
          delete("/kernel", this::handleDeleteKernel, UserPermission.SHUTDOWN_KERNEL);
          post(
              "/vehicles/dispatcher/trigger",
              this::handlePostDispatcherTrigger,
              UserPermission.MODIFY_VEHICLE
          );
          post(
              "/vehicles/{NAME}/routeComputationQuery",
              this::handleGetVehicleRoutes,
              UserPermission.READ_DATA
          );
          put(
              "/vehicles/{NAME}/commAdapter/attachment",
              this::handlePutVehicleCommAdapterAttachment,
              UserPermission.MODIFY_VEHICLE
          );
          get(
              "/vehicles/{NAME}/commAdapter/attachmentInformation",
              this::handleGetVehicleCommAdapterAttachmentInfo,
              UserPermission.READ_DATA
          );
          put(
              "/vehicles/{NAME}/commAdapter/enabled",
              this::handlePutVehicleCommAdapterEnabled,
              UserPermission.MODIFY_VEHICLE
          );
          post(
              "/vehicles/{NAME}/commAdapter/message",
              this::handlePostVehicleCommAdapterMessage,
              UserPermission.MODIFY_VEHICLE
          );
          put(
              "/vehicles/{NAME}/paused",
              this::handlePutVehiclePaused,
              UserPermission.MODIFY_VEHICLE
          );
          put(
              "/vehicles/{NAME}/integrationLevel",
              this::handlePutVehicleIntegrationLevel,
              UserPermission.MODIFY_VEHICLE
          );
          post(
              "/vehicles/{NAME}/withdrawal",
              this::handlePostWithdrawalByVehicle,
              UserPermission.MODIFY_ORDER
          );
          post(
              "/vehicles/{NAME}/rerouteRequest",
              this::handlePostVehicleRerouteRequest,
              UserPermission.MODIFY_VEHICLE
          );
          put(
              "/vehicles/{NAME}/acceptableOrderTypes",
              this::handlePutVehicleAcceptableOrderTypes,
              UserPermission.MODIFY_VEHICLE
          );
          put(
              "/vehicles/{NAME}/energyLevelThresholdSet",
              this::handlePutVehicleEnergyLevelThresholdSet,
              UserPermission.MODIFY_VEHICLE
          );
          put(
              "/vehicles/{NAME}/envelopeKey",
              this::handlePutVehicleEnvelopeKey,
              UserPermission.MODIFY_VEHICLE
          );
          get("/vehicles/{NAME}", this::handleGetVehicleByName, UserPermission.READ_DATA);
          get("/vehicles", this::handleGetVehicles, UserPermission.READ_DATA);
          post(
              "/transportOrders/dispatcher/trigger",
              this::handlePostDispatcherTrigger,
              UserPermission.MODIFY_ORDER
          );
          post(
              "/transportOrders/{NAME}/immediateAssignment",
              this::handlePostImmediateAssignment,
              UserPermission.MODIFY_ORDER
          );
          post(
              "/transportOrders/{NAME}/withdrawal",
              this::handlePostWithdrawalByOrder,
              UserPermission.MODIFY_ORDER
          );
          post(
              "/transportOrders/{NAME}",
              this::handlePostTransportOrder,
              UserPermission.MODIFY_ORDER
          );
          put(
              "/transportOrders/{NAME}/intendedVehicle",
              this::handlePutTransportOrderIntendedVehicle,
              UserPermission.MODIFY_ORDER
          );
          get(
              "/transportOrders/{NAME}",
              this::handleGetTransportOrderByName,
              UserPermission.READ_DATA
          );
          get("/transportOrders", this::handleGetTransportOrders, UserPermission.READ_DATA);
          post(
              "/orderSequences/{NAME}",
              this::handlePostOrderSequence,
              UserPermission.MODIFY_ORDER_SEQUENCE
          );
          get("/orderSequences", this::handleGetOrderSequences, UserPermission.READ_DATA);
          get(
              "/orderSequences/{NAME}",
              this::handleGetOrderSequenceByName,
              UserPermission.READ_DATA
          );
          put(
              "/orderSequences/{NAME}/complete",
              this::handlePutOrderSequenceComplete,
              UserPermission.MODIFY_ORDER_SEQUENCE
          );
          put("/plantModel", this::handlePutPlantModel, UserPermission.LOAD_MODEL);
          get("/plantModel", this::handleGetPlantModel, UserPermission.READ_DATA);
          post(
              "/plantModel/topologyUpdateRequest",
              this::handlePostUpdateTopology,
              UserPermission.UPDATE_ROUTING_TOPOLOGY
          );
          get("/points", this::handleGetPoints, UserPermission.READ_DATA);
          get("/points/{NAME}", this::handleGetPointByName, UserPermission.READ_DATA);
          get("/paths", this::handleGetPaths, UserPermission.READ_DATA);
          get("/paths/{NAME}", this::handleGetPathByName, UserPermission.READ_DATA);
          put("/paths/{NAME}/locked", this::handlePutPathLocked, UserPermission.LOCK_RESOURCE);
          get("/locations", this::handleGetLocations, UserPermission.READ_DATA);
          get("/locations/{NAME}", this::handleGetLocationByName, UserPermission.READ_DATA);
          put(
              "/locations/{NAME}/locked",
              this::handlePutLocationLocked,
              UserPermission.LOCK_RESOURCE
          );
          get("/locationTypes", this::handleGetLocationTypes, UserPermission.READ_DATA);
          get(
              "/locationTypes/{NAME}",
              this::handleGetLocationTypeByName,
              UserPermission.READ_DATA
          );
          get("/blocks", this::handleGetBlocks, UserPermission.READ_DATA);
          get("/blocks/{NAME}", this::handleGetBlockByName, UserPermission.READ_DATA);
          get("/visualLayout", this::handleGetVisualLayout, UserPermission.READ_DATA);
          post(
              "/peripherals/dispatcher/trigger",
              this::handlePostPeripheralJobsDispatchTrigger,
              UserPermission.MODIFY_PERIPHERAL
          );
          post(
              "/peripherals/{NAME}/withdrawal",
              this::handlePostPeripheralWithdrawal,
              UserPermission.MODIFY_PERIPHERAL
          );
          put(
              "/peripherals/{NAME}/commAdapter/enabled",
              this::handlePutPeripheralCommAdapterEnabled,
              UserPermission.MODIFY_PERIPHERAL
          );
          get(
              "/peripherals/{NAME}/commAdapter/attachmentInformation",
              this::handleGetPeripheralCommAdapterAttachmentInfo,
              UserPermission.READ_DATA
          );
          put(
              "/peripherals/{NAME}/commAdapter/attachment",
              this::handlePutPeripheralCommAdapterAttachment,
              UserPermission.MODIFY_PERIPHERAL
          );
          get("/peripheralJobs", this::handleGetPeripheralJobs, UserPermission.READ_DATA);
          get(
              "/peripheralJobs/{NAME}",
              this::handleGetPeripheralJobsByName,
              UserPermission.READ_DATA
          );
          post(
              "/peripheralJobs/{NAME}",
              this::handlePostPeripheralJobsByName,
              UserPermission.MODIFY_PERIPHERAL_JOB
          );
          post(
              "/peripheralJobs/{NAME}/withdrawal",
              this::handlePostPeripheralJobWithdrawal,
              UserPermission.MODIFY_PERIPHERAL_JOB
          );
          post(
              "/peripheralJobs/dispatcher/trigger",
              this::handlePostPeripheralJobsDispatchTrigger,
              UserPermission.MODIFY_PERIPHERAL_JOB
          );
          get(
              "/environmentalEntities",
              this::handleGetEnvironmentalEntities,
              UserPermission.READ_DATA
          );
          get(
              "/environmentalEntities/{NAME}",
              this::handleGetEnvironmentalEntityByName,
              UserPermission.READ_DATA
          );
          post(
              "/environmentalEntities/{NAME}",
              this::handlePostEnvironmentalEntity,
              UserPermission.MODIFY_ENVIRONMENTAL_ENTITY
          );
          put(
              "/environmentalEntities/{NAME}/envelope",
              this::handlePutEnvironmentalEntityEnvelope,
              UserPermission.MODIFY_ENVIRONMENTAL_ENTITY
          );
          put(
              "/environmentalEntities/{NAME}/pose",
              this::handlePutEnvironmentalEntityPose,
              UserPermission.MODIFY_ENVIRONMENTAL_ENTITY
          );
          put(
              "/environmentalEntities/{NAME}/integrationLevel",
              this::handlePutEnvironmentalEntityIntegrationLevel,
              UserPermission.MODIFY_ENVIRONMENTAL_ENTITY
          );
          put(
              "/environmentalEntities/{NAME}/retired",
              this::handlePutEnvironmentalEntityRetired,
              UserPermission.MODIFY_ENVIRONMENTAL_ENTITY
          );
          get("/userNotifications", this::handleGetUserNotifications, UserPermission.READ_DATA);
        }
    );
  }

  private void handlePostLogin(Context ctx)
      throws AuthenticationException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    PostLoginRequestTO request = jsonBinder.fromJson(ctx.body(), PostLoginRequestTO.class);
    ctx.result(
        jsonBinder.toJson(
            accessControl.login(request.getUsername(), request.getPassword())
        )
    );
  }

  private void handleGetSession(Context ctx) {
    Optional<LoginResponseTO> loginResponse = accessControl.getLoginInformation();
    if (loginResponse.isEmpty()) {
      ctx.status(HttpStatus.UNAUTHORIZED);
      return;
    }

    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(jsonBinder.toJson(loginResponse.get()));
  }

  private void handlePostLogout(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    if (accessControl.logout()) {
      ctx.result("Log out successful.");
    }
    else {
      ctx.result("Log out failed.");
    }
  }

  private void handleGetVersion(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(jsonBinder.toJson(new GetVersionResponseTO()));
  }

  private void handleDeleteKernel(Context ctx) {
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
            vehicleHandler.getVehicleRoutes(
                ctx.pathParam("NAME"),
                maxRoutePerDestinationPoint(ctx),
                jsonBinder.fromJson(ctx.body(), PostVehicleRouteComputationQueryRequestTO.class)
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
            transportOrderConverter.convert(
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
            orderSequenceConverter.convert(
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
    orderDispatcherHandler.withdrawByTransportOrder(ctx.pathParam("NAME"), immediate(ctx));
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePostWithdrawalByVehicle(Context ctx)
      throws ObjectUnknownException {
    orderDispatcherHandler.withdrawByVehicle(ctx.pathParam("NAME"), immediate(ctx));
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
    plantModelHandler.putPlantModel(jsonBinder.fromJson(ctx.body(), PutPlantModelRequestTO.class));
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

  private void handleGetPoints(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(jsonBinder.toJson(pointHandler.getPoints(ctx.queryParams("names"))));
  }

  private void handleGetPointByName(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(pointHandler.getPointByName(ctx.pathParam("NAME")))
    );
  }

  private void handleGetPaths(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(jsonBinder.toJson(pathHandler.getPaths(ctx.queryParams("names"))));
  }

  private void handleGetPathByName(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(pathHandler.getPathByName(ctx.pathParam("NAME")))
    );
  }

  private void handlePutPathLocked(Context ctx) {
    pathHandler.updatePathLock(
        ctx.pathParam("NAME"),
        ctx.queryParam("newValue")
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handleGetLocations(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(jsonBinder.toJson(locationHandler.getLocations(ctx.queryParams("names"))));
  }

  private void handleGetLocationByName(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(locationHandler.getLocationByName(ctx.pathParam("NAME")))
    );
  }

  private void handlePutLocationLocked(Context ctx) {
    locationHandler.updateLocationLock(
        ctx.pathParam("NAME"),
        ctx.queryParam("newValue")
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handleGetLocationTypes(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(jsonBinder.toJson(locationTypeHandler.getLocationTypes(ctx.queryParams("names"))));
  }

  private void handleGetLocationTypeByName(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(locationTypeHandler.getLocationTypeByName(ctx.pathParam("NAME")))
    );
  }

  private void handleGetBlocks(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(jsonBinder.toJson(blockHandler.getBlocks(ctx.queryParams("names"))));
  }

  private void handleGetBlockByName(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(blockHandler.getBlockByName(ctx.pathParam("NAME")))
    );
  }

  private void handleGetVisualLayout(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(visualLayoutHandler.getVisualLayout())
    );
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
            peripheralJobConverter.convert(
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

  private void handleGetEnvironmentalEntities(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            environmentalEntityHandler.getEnvironmentalEntitiesState()
        )
    );
  }

  private void handleGetEnvironmentalEntityByName(Context ctx)
      throws ObjectUnknownException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            environmentalEntityHandler.getEnvironmentalEntityStateByName(ctx.pathParam("NAME"))
        )
    );
  }

  public void handleGetUserNotifications(Context ctx) {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            notificationService
                .fetchUserNotifications(
                    Filters.userNotificationCreatedAfter(since(ctx))
                )
                .stream()
                .map(userNotificationConverter::convert)
                .toList()
        )
    );
  }

  private void handlePostEnvironmentalEntity(Context ctx)
      throws ObjectExistsException,
        IllegalArgumentException,
        IllegalStateException {
    ctx.contentType(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    ctx.result(
        jsonBinder.toJson(
            environmentalEntityConverter.convert(
                environmentalEntityHandler.createEnvironmentalEntity(
                    ctx.pathParam("NAME"),
                    jsonBinder.fromJson(ctx.body(), PostEnvironmentalEntityRequestTO.class)
                )
            )
        )
    );
  }

  private void handlePutEnvironmentalEntityEnvelope(Context ctx)
      throws ObjectUnknownException {
    environmentalEntityHandler.putEnvironmentalEntityEnvelope(
        ctx.pathParam("NAME"),
        jsonBinder.fromJson(ctx.body(), PutEnvironmentalEntityEnvelopeRequestTO.class)
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePutEnvironmentalEntityPose(Context ctx)
      throws ObjectExistsException {
    environmentalEntityHandler.putEnvironmentalEntityPose(
        ctx.pathParam("NAME"),
        jsonBinder.fromJson(ctx.body(), PutEnvironmentalEntityPoseRequestTO.class)
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePutEnvironmentalEntityIntegrationLevel(Context ctx)
      throws ObjectUnknownException,
        IllegalArgumentException {
    environmentalEntityHandler.putEnvironmentalEntityIntegrationLevel(
        ctx.pathParam("NAME"),
        ctx.queryParam("newValue")
    );
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private void handlePutEnvironmentalEntityRetired(Context ctx)
      throws ObjectUnknownException {
    environmentalEntityHandler.putEnvironmentalEntityRetired(ctx.pathParam("NAME"));
    ctx.contentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    ctx.result("");
  }

  private Instant since(Context ctx) {
    String param = ctx.queryParamAsClass("since", String.class).getOrDefault(null);
    if (param != null) {
      try {
        return Instant.parse(param);
      }
      catch (DateTimeParseException exc) {
        throw new IllegalArgumentException("Malformed since: " + param);
      }
    }
    return Instant.EPOCH;
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
