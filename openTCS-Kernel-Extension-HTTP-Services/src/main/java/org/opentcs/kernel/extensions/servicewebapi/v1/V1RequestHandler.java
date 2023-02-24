/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.kernel.extensions.servicewebapi.HttpConstants;
import org.opentcs.kernel.extensions.servicewebapi.RequestHandler;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetPeripheralJobResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetTransportOrderResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetVehicleAttachmentInfoResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostPeripheralJobRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostTransportOrderRequestTO;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Service;

/**
 * Handles requests and produces responses for version 1 of the web API.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class V1RequestHandler
    implements RequestHandler {

  /**
   * Binds JSON data to objects and vice versa.
   */
  private final JsonBinder jsonBinder;
  /**
   * Collects interesting events and provides them for client requests.
   */
  private final StatusEventDispatcher statusEventDispatcher;
  /**
   * Creates transport orders.
   */
  private final OrderHandler orderHandler;

  private final RequestStatusHandler statusInformationProvider;
  /**
   * Whether this instance is initialized.
   */
  private boolean initialized;

  @Inject
  public V1RequestHandler(JsonBinder jsonBinder,
                          StatusEventDispatcher statusEventDispatcher,
                          OrderHandler orderHandler,
                          RequestStatusHandler requestHandler) {
    this.jsonBinder = requireNonNull(jsonBinder, "jsonBinder");
    this.statusEventDispatcher = requireNonNull(statusEventDispatcher, "statusEventDispatcher");
    this.orderHandler = requireNonNull(orderHandler, "orderHandler");
    this.statusInformationProvider = requireNonNull(requestHandler, "requestHandler");
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

    service.get("/events",
                this::handleGetEvents);
    service.post("/vehicles/dispatcher/trigger",
                 this::handlePostDispatcherTrigger);
    service.put("/vehicles/:NAME/commAdapter/attachment",
                this::handlePutVehicleCommAdapterAttachment);
    service.get("/vehicles/:NAME/commAdapter/attachmentInformation",
                this::handleGetVehicleCommAdapterAttachmentInfo);
    service.put("/vehicles/:NAME/commAdapter/enabled",
                this::handlePutVehicleCommAdapterEnabled);
    service.put("/vehicles/:NAME/paused",
                this::handlePutVehiclePaused);
    service.put("/vehicles/:NAME/integrationLevel",
                this::handlePutVehicleIntegrationLevel);
    service.post("/vehicles/:NAME/withdrawal",
                 this::handlePostWithdrawalByVehicle);
    service.post("/vehicles/:NAME/rerouteRequest",
                 this::handlePostVehicleRerouteRequest);
    service.get("/vehicles/:NAME",
                this::handleGetVehicleByName);
    service.get("/vehicles",
                this::handleGetVehicles);
    service.post("/transportOrders/dispatcher/trigger",
                 this::handlePostDispatcherTrigger);
    service.post("/transportOrders/:NAME/withdrawal",
                 this::handlePostWithdrawalByOrder);
    service.post("/transportOrders/:NAME",
                 this::handlePostTransportOrder);
    service.get("/transportOrders/:NAME",
                this::handleGetTransportOrderByName);
    service.get("/transportOrders",
                this::handleGetTransportOrders);
    service.post("/dispatcher/trigger",
                 this::handlePostDispatcherTrigger);
    service.get("/peripheralJobs",
                this::handleGetPeripheralJobs);
    service.get("/peripheralJobs/:NAME",
                this::handleGetPeripheralJobsByName);
    service.post("/peripheralJobs/:NAME",
                 this::handlePostPeripheralJobsByName);
    service.post("/peripheralJobs/:NAME/withdrawal",
                 this::handlePostPeripheralJobWithdrawal);
    service.post("/peripheralJobs/dispatcher/trigger",
                 this::handlePostPeripheralJobsDispatchTrigger);
  }

  private Object handlePostDispatcherTrigger(Request request, Response response)
      throws KernelRuntimeException {
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    orderHandler.triggerDispatcher();
    return "";
  }

  private Object handleGetEvents(Request request, Response response)
      throws IllegalArgumentException, IllegalStateException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(statusEventDispatcher.fetchEvents(minSequenceNo(request),
                                                               maxSequenceNo(request),
                                                               timeout(request)));
  }

  private Object handlePutVehicleCommAdapterEnabled(Request request, Response response)
      throws ObjectUnknownException, IllegalArgumentException {
    statusInformationProvider.putVehicleCommAdapterEnabled(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handleGetVehicleCommAdapterAttachmentInfo(Request request, Response response)
      throws ObjectUnknownException, IllegalArgumentException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(GetVehicleAttachmentInfoResponseTO.fromAttachmentInformation(
        statusInformationProvider.getVehicleCommAdapterAttachmentInformation(
            request.params(":NAME")
        )
    )
    );
  }

  private Object handlePutVehicleCommAdapterAttachment(Request request, Response response)
      throws ObjectUnknownException, IllegalArgumentException {
    statusInformationProvider.putVehicleCommAdapter(
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
        GetTransportOrderResponseTO.fromTransportOrder(
            orderHandler.createOrder(
                request.params(":NAME"),
                jsonBinder.fromJson(request.body(), PostTransportOrderRequestTO.class)
            )
        )
    );
  }

  private Object handlePostWithdrawalByOrder(Request request, Response response)
      throws ObjectUnknownException {
    orderHandler.withdrawByTransportOrder(request.params(":NAME"),
                                          immediate(request),
                                          disableVehicle(request));
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePostWithdrawalByVehicle(Request request, Response response)
      throws ObjectUnknownException {
    orderHandler.withdrawByVehicle(request.params(":NAME"),
                                   immediate(request),
                                   disableVehicle(request));
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePostPeripheralJobWithdrawal(Request request, Response response)
      throws KernelRuntimeException {
    orderHandler.withdrawPeripheralJob(request.params(":NAME"));
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePostVehicleRerouteRequest(Request request, Response response)
      throws ObjectUnknownException {
    orderHandler.reroute(request.params(":NAME"), forced(request));
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handleGetTransportOrders(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        statusInformationProvider.getTransportOrdersState(
            valueIfKeyPresent(request.queryMap(), "intendedVehicle")
        )
    );
  }

  private Object handleGetTransportOrderByName(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        statusInformationProvider.getTransportOrderByName(request.params(":NAME"))
    );
  }

  private Object handleGetVehicles(Request request, Response response)
      throws IllegalArgumentException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        statusInformationProvider.getVehiclesState(valueIfKeyPresent(request.queryMap(),
                                                                     "procState"))
    );
  }

  private Object handleGetVehicleByName(Request request, Response response)
      throws ObjectUnknownException {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        statusInformationProvider.getVehicleStateByName(request.params(":NAME"))
    );
  }

  private Object handlePutVehicleIntegrationLevel(Request request, Response response)
      throws ObjectUnknownException, IllegalArgumentException {
    statusInformationProvider.putVehicleIntegrationLevel(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handlePutVehiclePaused(Request request, Response response)
      throws ObjectUnknownException, IllegalArgumentException {
    statusInformationProvider.putVehiclePaused(
        request.params(":NAME"),
        valueIfKeyPresent(request.queryMap(), "newValue")
    );
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object handleGetPeripheralJobs(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        statusInformationProvider.getPeripheralJobs(
            valueIfKeyPresent(request.queryMap(), "relatedVehicle"),
            valueIfKeyPresent(request.queryMap(), "relatedTransportOrder")
        )
    );
  }

  private Object handleGetPeripheralJobsByName(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        statusInformationProvider.getPeripheralJobByName(request.params(":NAME"))
    );
  }

  private Object handlePostPeripheralJobsByName(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
    return jsonBinder.toJson(
        GetPeripheralJobResponseTO.fromPeripheralJob(
            orderHandler.createPeripheralJob(
                request.params(":NAME"),
                jsonBinder.fromJson(request.body(), PostPeripheralJobRequestTO.class)
            )
        )
    );
  }

  private Object handlePostPeripheralJobsDispatchTrigger(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    orderHandler.triggerJobDispatcher();
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
