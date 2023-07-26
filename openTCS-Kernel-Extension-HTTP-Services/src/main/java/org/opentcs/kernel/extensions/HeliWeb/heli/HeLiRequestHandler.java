package org.opentcs.kernel.extensions.HeliWeb.heli;

import org.opentcs.access.to.model.*;
import org.opentcs.data.model.Triple;
import org.opentcs.kernel.extensions.HeliWeb.HeLiHandler;
import org.opentcs.kernel.extensions.servicewebapi.HttpConstants;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.RequestHandler;
import org.opentcs.kernel.extensions.servicewebapi.v1.RequestStatusHandler;
import org.opentcs.kernel.extensions.servicewebapi.v1.StatusEventDispatcher;
import spark.Request;
import spark.Response;
import spark.Service;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

/**
 * @PACKAGE_NAME: org.opentcs.kernel.extensions.HeliWeb.heli
 * @NAME: HeLiRequestHandler
 * @USER: FSY
 * @DATE: 2023/6/26 0026
 * @TIME: 13:30
 * @YEAR: 2023
 * @MONTH_NAME_SHORT: 6月
 * @DAY_NAME_SHORT: 周一
 * @PROJECT_NAME: openTCS
 * @Description:
 */
public class HeLiRequestHandler implements RequestHandler {

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
  private final HeLiHandler orderHandler;


  private final RequestStatusHandler statusInformationProvider;


  /**
   * Whether this instance is initialized.
   */
  private boolean initialized;

  @Inject
  public HeLiRequestHandler(JsonBinder jsonBinder,
                            StatusEventDispatcher statusEventDispatcher,
                            HeLiHandler orderHandler,
                            RequestStatusHandler requestHandler) {
    this.jsonBinder = requireNonNull(jsonBinder, "jsonBinder");
    this.statusEventDispatcher = requireNonNull(statusEventDispatcher, "statusEventDispatcher");
    this.orderHandler = requireNonNull(orderHandler, "orderHandler");
    this.statusInformationProvider = requireNonNull(requestHandler, "requestHandler");
  }

  @Override
  public void addRoutes(Service service) {
//    service.get("/test/:param", this::test);

    service.post("/test/createMove",this::testCreateMove);
    service.get("/test/createMoveForLocal/:NAME",this::testCreateMoveForLocal);
    service.get("/test/createOrderForLocal/:NAME",this::testCreateOrderForLocal);
    service.get(("/test/init/:NAME"),this::testInit);
    service.get(("/test/dispatcher"),this::testDispatcher);
  }

  private Object testCreateOrderForLocal(Request request, Response response) {
    String params = request.params(":NAME");
    if(params==null)
      return "failed! not have order num params";
    else
      orderHandler.createOrderForLocal(Integer.valueOf(params));
    return "ok";
  }


  private Object testCreateMoveForLocal(Request request, Response response) {
    String params = request.params(":NAME");
    if(params==null)
    orderHandler.createMoveOrderForLocal(1);
    else
      orderHandler.createMoveOrderForLocal(Integer.valueOf(params));
    return "ok";
  }

  private Object testDispatcher(Request request, Response response) {
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    orderHandler.triggerDispatcher();
    return "ok";
  }

  private Object testInit(Request request, Response response) {
    String params = request.params(":NAME");
    if(params==null)
    orderHandler.testInit(-1);
    else
      orderHandler.testInit(Integer.valueOf(params));
    return "ok";
  }

  private Object testCreateMove(Request request, Response response) {
    String body = request.body();
    orderHandler.createMoveOrderForRemote(body);
    return "ok";
  }




  private Object operate(Request request, Response response) {
    //todo:操作模式
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }

  private Object editor(Request request, Response response) {
    //todo:编辑模式
    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
    return "";
  }



  private Object test(Request request, Response response) {
    PlantModelCreationTO test = new PlantModelCreationTO("test");

    PointCreationTO pointCreationTO = new PointCreationTO("123");
    PathCreationTO pathCreationTO = new PathCreationTO("123","source","des");
    BlockCreationTO blockCreationTO = new BlockCreationTO("123");
    LocationCreationTO locationCreationTO = new LocationCreationTO("123","load",new Triple(1,2,0));
    VisualLayoutCreationTO visualLayoutCreationTO = new VisualLayoutCreationTO("123");
    LocationTypeCreationTO locationTypeCreationTO = new LocationTypeCreationTO("123");
    VehicleCreationTO vehicleCreationTO = new VehicleCreationTO("123");
    PlantModelCreationTO plantModelCreationTO = test.withVisualLayout(visualLayoutCreationTO).withPath(pathCreationTO).withLocationType(locationTypeCreationTO).withBlock(blockCreationTO)
        .withPoint(pointCreationTO).withBlock(blockCreationTO);

    return jsonBinder.toJson(plantModelCreationTO);
  }

  private Object testPut(Request request, Response response) {
    PointCreationTO pointCreationTO = new PointCreationTO("123");
    return jsonBinder.toJson(pointCreationTO);
  }

  @Override
  public void initialize() {

  }

  @Override
  public boolean isInitialized() {
    return false;
  }

  @Override
  public void terminate() {

  }
}
