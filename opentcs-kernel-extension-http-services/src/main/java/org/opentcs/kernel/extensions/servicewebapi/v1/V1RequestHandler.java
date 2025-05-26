// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.opentcs.access.KernelRuntimeException;
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
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Service;

/**
 * Handles requests and produces responses for version 1 of the web API.
 */
public class V1RequestHandler
    implements
      RequestHandler {

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
      VehicleAttachmentInformationConverter vehicleAttachmentInformationConverter
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
  public void addRoutes(Service service) {
    requireNonNull(service, "service");

    service.get(
        "/events",
        this::handleGetEvents
    );
    service.post(
        "/vehicles/dispatcher/trigger",
        this::handlePostDispatcherTrigger
    );
    service.post(
        "/vehicles/:NAME/routeComputationQuery",
        this::handleGetVehicleRoutes
    );
    service.put(
        "/vehicles/:NAME/commAdapter/attachment",
        this::handlePutVehicleCommAdapterAttachment
    );
    service.get(
        "/vehicles/:NAME/commAdapter/attachmentInformation",
        this::handleGetVehicleCommAdapterAttachmentInfo
    );
    service.put(
        "/vehicles/:NAME/commAdapter/enabled",
        this::handlePutVehicleCommAdapterEnabled
    );
    service.post(
        "/vehicles/:NAME/commAdapter/message",
        this::handlePostVehicleCommAdapterMessage
    );
    service.put(
        "/vehicles/:NAME/paused",
        this::handlePutVehiclePaused
    );
    service.put(
        "/vehicles/:NAME/integrationLevel",
        this::handlePutVehicleIntegrationLevel
    );
    service.post(
        "/vehicles/:NAME/withdrawal",
        this::handlePostWithdrawalByVehicle
    );
    service.post(
        "/vehicles/:NAME/rerouteRequest",
        this::handlePostVehicleRerouteRequest
    );
    service.put(
        "/vehicles/:NAME/allowedOrderTypes",
        this::handlePutVehicleAllowedOrderTypes
    );
    service.put(
        "/vehicles/:NAME/acceptableOrderTypes",
        this::handlePutVehicleAcceptableOrderTypes
    );
    service.put(
        "/vehicles/:NAME/energyLevelThresholdSet",
        this::handlePutVehicleEnergyLevelThresholdSet
    );
    service.put(
        "/vehicles/:NAME/envelopeKey",
        this::handlePutVehicleEnvelopeKey
    );
    service.get(
        "/vehicles/:NAME",
        this::handleGetVehicleByName
    );
    service.get(
        "/vehicles",
        this::handleGetVehicles
    );
    service.post(
        "/transportOrders/dispatcher/trigger",
        this::handlePostDispatcherTrigger
    );
    service.post(
        "/transportOrders/:NAME/immediateAssignment",
        this::handlePostImmediateAssignment
    );
    service.post(
        "/transportOrders/:NAME/withdrawal",
        this::handlePostWithdrawalByOrder
    );
    service.post(
        "/transportOrders/:NAME",
        this::handlePostTransportOrder
    );
    service.put(
        "/transportOrders/:NAME/intendedVehicle",
        this::handlePutTransportOrderIntendedVehicle
    );
    service.get(
        "/transportOrders/:NAME",
        this::handleGetTransportOrderByName
    );
    service.get(
        "/transportOrders",
        this::handleGetTransportOrders
    );
    service.post(
        "/orderSequences/:NAME",
        this::handlePostOrderSequence
    );
    service.get(
        "/orderSequences",
        this::handleGetOrderSequences
    );
    service.get(
        "/orderSequences/:NAME",
        this::handleGetOrderSequenceByName
    );
    service.put(
        "/orderSequences/:NAME/complete",
        this::handlePutOrderSequenceComplete
    );
    service.put(
        "/plantModel",
        this::handlePutPlantModel
    );
    service.get(
        "/plantModel",
        this::handleGetPlantModel
    );
    service.post(
        "/plantModel/topologyUpdateRequest",
        this::handlePostUpdateTopology
    );
    service.put(
        "/paths/:NAME/locked",
        this::handlePutPathLocked
    );
    service.put(
        "/locations/:NAME/locked",
        this::handlePutLocationLocked
    );
    service.post(
        "/dispatcher/trigger",
        this::handlePostDispatcherTrigger
    );
    service.post(
        "/peripherals/dispatcher/trigger",
        this::handlePostPeripheralJobsDispatchTrigger
    );
    service.post(
        "/peripherals/:NAME/withdrawal",
        this::handlePostPeripheralWithdrawal
    );
    service.put(
        "/peripherals/:NAME/commAdapter/enabled",
        this::handlePutPeripheralCommAdapterEnabled
    );
    service.get(
        "/peripherals/:NAME/commAdapter/attachmentInformation",
        this::handleGetPeripheralCommAdapterAttachmentInfo
    );
    service.put(
        "/peripherals/:NAME/commAdapter/attachment",
        this::handlePutPeripheralCommAdapterAttachment
    );
    service.get(
        "/peripheralJobs",
        this::handleGetPeripheralJobs
    );
    service.get(
        "/peripheralJobs/:NAME",
        this::handleGetPeripheralJobsByName
    );
    service.post(
        "/peripheralJobs/:NAME",
        this::handlePostPeripheralJobsByName
    );
    service.post(
        "/peripheralJobs/:NAME/withdrawal",
        this::handlePostPeripheralJobWithdrawal
    );
    service.post(
        "/peripheralJobs/dispatcher/trigger",
        this::handlePostPeripheralJobsDispatchTrigger
    );
  }

  private Object handlePostDispatcherTrigger(Request request, Response response)
      throws KernelRuntimeException {
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    orderDispatcherHandler.triggerDispatcher();
    return "";
  }

  private Object handleGetEvents(Request request, Response response)
      throws IllegalArgumentException,
        IllegalStateException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        statusEventDispatcher.fetchEvents(
            minSequenceNo(request),
            maxSequenceNo(request),
            timeout(request)
        )
    );
  }

  private Object handlePutVehicleCommAdapterEnabled(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleCommAdapterEnabled(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePostVehicleCommAdapterMessage(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.postVehicleCommAdapterMessage(
        request.params(":NAME"),
        jsonBinder.fromJson(request.body(), PostVehicleCommAdapterMessageRequestTO.class)
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handleGetVehicleCommAdapterAttachmentInfo(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        vehicleAttachmentInformationConverter.toGetVehicleAttachmentInfoResponseTO(
            vehicleHandler.getVehicleCommAdapterAttachmentInformation(
                request.params(":NAME")
            )
        )
    );
  }

  private Object handleGetVehicleRoutes(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        PostVehicleRoutesResponseTO.fromMap(
            vehicleHandler.getVehicleRoutes(
                request.params(":NAME"),
                maxRoutePerDestinationPoint(request),
                jsonBinder.fromJson(request.body(), PostVehicleRoutesRequestTO.class)
            )
        )
    );
  }

  private Object handlePutVehicleCommAdapterAttachment(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleCommAdapter(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePostTransportOrder(Request request, Response response)
      throws ObjectUnknownException,
        ObjectExistsException,
        IllegalArgumentException,
        IllegalStateException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        transportOrderConverter.toGetTransportOrderResponse(
            transportOrderHandler.createOrder(
                request.params(":NAME"),
                jsonBinder.fromJson(request.body(), PostTransportOrderRequestTO.class)
            )
        )
    );
  }

  private Object handlePutTransportOrderIntendedVehicle(Request request, Response response)
      throws ObjectUnknownException {
    transportOrderHandler.updateTransportOrderIntendedVehicle(
        request.params(":NAME"),
        request.queryParamOrDefault("vehicle", null)
    );
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return "";
  }

  private Object handlePostOrderSequence(Request request, Response response)
      throws ObjectUnknownException,
        ObjectExistsException,
        IllegalArgumentException,
        IllegalStateException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        orderSequenceConverter.toGetOrderSequenceResponseTO(
            transportOrderHandler.createOrderSequence(
                request.params(":NAME"),
                jsonBinder.fromJson(request.body(), PostOrderSequenceRequestTO.class)
            )
        )
    );
  }

  private Object handleGetOrderSequences(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        transportOrderHandler.getOrderSequences(
            valueIfKeyPresent(request.queryMap(), "intendedVehicle")
        )
    );
  }

  private Object handleGetOrderSequenceByName(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        transportOrderHandler.getOrderSequenceByName(request.params(":NAME"))
    );
  }

  private Object handlePutOrderSequenceComplete(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException,
        InterruptedException,
        ExecutionException {
    transportOrderHandler.putOrderSequenceComplete(request.params(":NAME"));
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePostImmediateAssignment(Request request, Response response)
      throws ObjectUnknownException {
    orderDispatcherHandler.tryImmediateAssignment(request.params(":NAME"));
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePostWithdrawalByOrder(Request request, Response response)
      throws ObjectUnknownException {
    orderDispatcherHandler.withdrawByTransportOrder(
        request.params(":NAME"),
        immediate(request),
        disableVehicle(request)
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePostWithdrawalByVehicle(Request request, Response response)
      throws ObjectUnknownException {
    orderDispatcherHandler.withdrawByVehicle(
        request.params(":NAME"),
        immediate(request),
        disableVehicle(request)
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePostPeripheralJobWithdrawal(Request request, Response response)
      throws KernelRuntimeException {
    jobDispatcherHandler.withdrawPeripheralJob(request.params(":NAME"));
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePostVehicleRerouteRequest(Request request, Response response)
      throws ObjectUnknownException {
    orderDispatcherHandler.reroute(request.params(":NAME"), forced(request));
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handleGetTransportOrders(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        transportOrderHandler.getTransportOrders(
            valueIfKeyPresent(request.queryMap(), "intendedVehicle")
        )
    );
  }

  private Object handlePutPlantModel(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    plantModelHandler.putPlantModel(jsonBinder.fromJson(request.body(), PlantModelTO.class));
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handleGetPlantModel(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(plantModelHandler.getPlantModel());
  }

  private Object handlePostUpdateTopology(Request request, Response response)
      throws ObjectUnknownException,
        KernelRuntimeException {
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    if (request.body().isBlank()) {
      plantModelHandler.requestTopologyUpdate(new PostTopologyUpdateRequestTO(List.of()));
    }
    else {
      plantModelHandler
          .requestTopologyUpdate(
              jsonBinder.fromJson(request.body(), PostTopologyUpdateRequestTO.class)
          );
    }
    return "";
  }

  private Object handlePutPathLocked(Request request, Response response) {
    pathHandler.updatePathLock(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePutLocationLocked(Request request, Response response) {
    locationHandler.updateLocationLock(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handleGetTransportOrderByName(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        transportOrderHandler.getTransportOrderByName(request.params(":NAME"))
    );
  }

  private Object handleGetVehicles(Request request, Response response)
      throws IllegalArgumentException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        vehicleHandler.getVehiclesState(
            valueIfKeyPresent(
                request.queryMap(),
                "procState"
            )
        )
    );
  }

  private Object handleGetVehicleByName(Request request, Response response)
      throws ObjectUnknownException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        vehicleHandler.getVehicleStateByName(request.params(":NAME"))
    );
  }

  private Object handlePutVehicleIntegrationLevel(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleIntegrationLevel(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePutVehiclePaused(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehiclePaused(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  @Deprecated
  private Object handlePutVehicleAllowedOrderTypes(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleAllowedOrderTypes(
        request.params(":NAME"),
        jsonBinder.fromJson(request.body(), PutVehicleAllowedOrderTypesTO.class)
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePutVehicleAcceptableOrderTypes(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleAcceptableOrderTypes(
        request.params(":NAME"),
        jsonBinder.fromJson(request.body(), PutVehicleAcceptableOrderTypesTO.class)
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePutVehicleEnergyLevelThresholdSet(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleEnergyLevelThresholdSet(
        request.params(":NAME"),
        jsonBinder.fromJson(request.body(), PutVehicleEnergyLevelThresholdSetTO.class)
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePutVehicleEnvelopeKey(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    vehicleHandler.putVehicleEnvelopeKey(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePostPeripheralWithdrawal(Request request, Response response)
      throws KernelRuntimeException {
    jobDispatcherHandler.withdrawPeripheralJobByLocation(request.params(":NAME"));
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePutPeripheralCommAdapterEnabled(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    peripheralHandler.putPeripheralCommAdapterEnabled(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handleGetPeripheralCommAdapterAttachmentInfo(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        peripheralAttachmentInformationConverter.toGetPeripheralAttachmentInfoResponseTO(
            peripheralHandler.getPeripheralCommAdapterAttachmentInformation(
                request.params(":NAME")
            )
        )
    );
  }

  private Object handleGetPeripheralJobs(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        peripheralJobHandler.getPeripheralJobs(
            valueIfKeyPresent(request.queryMap(), "relatedVehicle"),
            valueIfKeyPresent(request.queryMap(), "relatedTransportOrder")
        )
    );
  }

  private Object handlePutPeripheralCommAdapterAttachment(Request request, Response response)
      throws ObjectUnknownException,
        IllegalArgumentException {
    peripheralHandler.putPeripheralCommAdapter(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handleGetPeripheralJobsByName(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        peripheralJobHandler.getPeripheralJobByName(request.params(":NAME"))
    );
  }

  private Object handlePostPeripheralJobsByName(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        peripheralJobConverter.toGetPeripheralJobResponseTO(
            peripheralJobHandler.createPeripheralJob(
                request.params(":NAME"),
                jsonBinder.fromJson(request.body(), PostPeripheralJobRequestTO.class)
            )
        )
    );
  }

  private Object handlePostPeripheralJobsDispatchTrigger(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    jobDispatcherHandler.triggerJobDispatcher();
    return "";
  }

  private String valueIfKeyPresent(QueryParamsMap queryParams, String key) {
    if (queryParams.hasKey(key)) {
      return queryParams.value(key);
    }
    else {
      return null;
    }
  }

  private int maxRoutePerDestinationPoint(Request request)
      throws IllegalArgumentException {
    String param
        = request.queryParamOrDefault("maxRoutesPerDestinationPoint", "1");
    try {
      return Integer.parseInt(param);
    }
    catch (NumberFormatException exc) {
      throw new IllegalArgumentException("Malformed maxRoutesPerDestinationPoint: " + param);
    }
  }

  private long minSequenceNo(Request request)
      throws IllegalArgumentException {
    String param = request.queryParamOrDefault("minSequenceNo", "0");
    try {
      return Long.parseLong(param);
    }
    catch (NumberFormatException exc) {
      throw new IllegalArgumentException("Malformed minSequenceNo: " + param);
    }
  }

  private long maxSequenceNo(Request request)
      throws IllegalArgumentException {
    String param = request.queryParamOrDefault("maxSequenceNo", String.valueOf(Long.MAX_VALUE));
    try {
      return Long.parseLong(param);
    }
    catch (NumberFormatException exc) {
      throw new IllegalArgumentException("Malformed minSequenceNo: " + param);
    }
  }

  private long timeout(Request request)
      throws IllegalArgumentException {
    String param = request.queryParamOrDefault("timeout", "1000");
    try {
      // Allow a maximum timeout of 10 seconds so server threads are only bound for a limited time.
      return Math.min(10000, Long.parseLong(param));
    }
    catch (NumberFormatException exc) {
      throw new IllegalArgumentException("Malformed timeout: " + param);
    }
  }

  private boolean immediate(Request request) {
    return Boolean.parseBoolean(request.queryParamOrDefault("immediate", "false"));
  }

  private boolean disableVehicle(Request request) {
    return Boolean.parseBoolean(request.queryParamOrDefault("disableVehicle", "false"));
  }

  private boolean forced(Request request) {
    return Boolean.parseBoolean(request.queryParamOrDefault("forced", "false"));
  }

}
