package org.opentcs.kernel.extensions.HeliWeb;

import com.alibaba.fastjson.JSONObject;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.*;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.OrderHandler;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostTransportOrderRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.posttransportorder.Destination;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

/**
 * @PACKAGE_NAME: org.opentcs.kernel.extensions.HeliWeb
 * @NAME: HeLiHandler
 * @USER: FSY
 * @DATE: 2023/6/26 0026
 * @TIME: 14:17
 * @YEAR: 2023
 * @MONTH_NAME_SHORT: 6月
 * @DAY_NAME_SHORT: 周一
 * @PROJECT_NAME: openTCS
 * @Description:
 */
public class HeLiHandler {

  /**
   * The service we use to create transport orders.
   */
  private final TransportOrderService orderService;
  /**
   * The service we use to update vehicle states.
   */
  private final VehicleService vehicleService;
  /**
   * The service we use to withdraw transport orders.
   */
  private final DispatcherService dispatcherService;
  /**
   * The service we use to create peripheral jobs.
   */
  private final PeripheralJobService jobService;
  /**
   * The service we use to dispatch peripheral jobs.
   */
  private final PeripheralDispatcherService jobDispatcherService;
  /**
   * Executes calls via the kernel executor and waits for the outcome.
   */
  private final KernelExecutorWrapper executorWrapper;

  private final ExecutorService kernelExecutor;

  private final OrderHandler orderHandler;

  /**
   * Creates a new instance.
   *
   * @param orderService         Used to create transport orders.
   * @param vehicleService       Used to update vehicle state.
   * @param dispatcherService    Used to withdraw transport orders.
   * @param jobService           Used to create peripheral jobs.
   * @param jobDispatcherService Used to dispatch peripheral jobs.
   * @param executorWrapper      Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public HeLiHandler(TransportOrderService orderService,
                     VehicleService vehicleService,
                     DispatcherService dispatcherService,
                     PeripheralJobService jobService,
                     PeripheralDispatcherService jobDispatcherService,
                     KernelExecutorWrapper executorWrapper, @KernelExecutor ExecutorService kernelExecutor
                      , OrderHandler orderHandler) {
    this.orderService = requireNonNull(orderService, "orderService");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.dispatcherService = requireNonNull(dispatcherService, "dispatcherService");
    this.jobService = requireNonNull(jobService, "jobService");
    this.jobDispatcherService = requireNonNull(jobDispatcherService, "jobDispatcherService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.orderHandler = requireNonNull(orderHandler, "orderHandler");
  }


  public void testInit(Integer size) {
    Set<Vehicle> vehicles = orderService.fetchObjects(Vehicle.class);
    Set<Point> points = orderService.fetchObjects(Point.class, point -> point.getType().equals(Point.Type.PARK_POSITION));
    if (size < 0)
      size = vehicles.size();
    int i = 1;
    for (Vehicle vehicle : vehicles) {
      if (i > size) {
        break;
      }
      if(i>points.size())
      {
        System.out.println("park 点位少于初始化的车辆个数");
        break;
      }
      vehicleService.enableCommAdapter(vehicle.getReference());
      vehicleService.updateVehicleIntegrationLevel(vehicle.getReference(), Vehicle.IntegrationLevel.TO_BE_UTILIZED);
      Point point = points.stream().skip(i - 1).findFirst().get();
      vehicleService.setVehicleCurrentPosition(vehicle.getReference(), point.getReference());
      i++;
    }
  }

  public void createMoveOrderForRemote(String body) {
    JSONObject jsonObject = JSONObject.parseObject(body);
    String vehicleName = jsonObject.getString("vehicleName");
    String pointName = jsonObject.getString("pointName");
    TransportOrderCreationTO transportOrderCreationTO = new TransportOrderCreationTO(
        "Move-",
        Collections.singletonList(new DestinationCreationTO(pointName,
            DriveOrder.Destination.OP_MOVE)))
        .withIncompleteName(true)
        .withDeadline(Instant.now())
        .withIntendedVehicleName(vehicleName);

    orderService.createTransportOrder(transportOrderCreationTO);

  }

  public void createMoveOrderForLocal(Integer size) {
    Set<Vehicle> vehicles = orderService.fetchObjects(Vehicle.class, vehicle -> vehicle.getIntegrationLevel().equals(Vehicle.IntegrationLevel.TO_BE_UTILIZED));
    Set<Point> points = orderService.fetchObjects(Point.class, point -> point.getType().equals(Point.Type.HALT_POSITION));
    Random random = new Random();
    int j = 0;
    while (j < size) {
      int i = random.nextInt(vehicles.size()) + 1;
      String vehicleName = vehicles.stream().skip(i - 1).findFirst().get().getName();
//      String vehicleName = "Vehicle-"+formatNum(i,4);
      int i1 = random.nextInt(points.size()) + 1;
      String pointName = "Point-" + formatNum(i1, 4);
      TransportOrderCreationTO transportOrderCreationTO = new TransportOrderCreationTO(
          "Move-",
          Collections.singletonList(new DestinationCreationTO(pointName,
              DriveOrder.Destination.OP_MOVE)))
          .withIncompleteName(true)
          .withDeadline(Instant.now())
          .withIntendedVehicleName(vehicleName);
      orderService.createTransportOrder(transportOrderCreationTO);
      j++;
    }
  }


  public void triggerDispatcher() {
    kernelExecutor.submit(() -> dispatcherService.dispatch());
  }


  /**
   * 创建取放货订单
   * @param orderNum
   */
  public void createOrderForLocal(Integer orderNum) {
    Set<Location> locations = orderService.fetchObjects(Location.class);
    String orderPrefixName = "-fsy-"+UUID.randomUUID();
    Random random = new Random();
    for (Integer i = 0; i < orderNum; i++) {
      int startLocationNum = random.nextInt(locations.size()) + 1;
      int endLocationNum = random.nextInt(locations.size()) + 1;
      String startLocationName = locations.stream().skip(startLocationNum - 1).findFirst().get().getName();
      String endLocationName = locations.stream().skip(endLocationNum - 1).findFirst().get().getName();
      PostTransportOrderRequestTO postTransportOrderRequestTO = createTestTransportOrderRequestTO(startLocationName,endLocationName);
      orderHandler.createOrder(i+orderPrefixName,postTransportOrderRequestTO);
    }
  }

  private PostTransportOrderRequestTO createTestTransportOrderRequestTO(String startLocationName, String endLocationName)
  {
    PostTransportOrderRequestTO postTransportOrderRequestTO = new PostTransportOrderRequestTO();
    Destination startDestination = new Destination();
    startDestination.setLocationName(startLocationName);
    startDestination.setOperation("load");
    Destination endDestination = new Destination();
    endDestination.setLocationName(startLocationName);
    endDestination.setOperation("unload");
    postTransportOrderRequestTO.setDestinations(Arrays.asList(startDestination,endDestination));
    return postTransportOrderRequestTO;
  }

  private String formatNum(int i, int len) {
    int lenDiff = len - String.valueOf(i).length();
    StringBuilder stringBuilder = new StringBuilder();
    for (int i1 = 0; i1 < lenDiff; i1++) {
      stringBuilder.append("0");
    }
    return (stringBuilder.toString() + i);
  }
}

