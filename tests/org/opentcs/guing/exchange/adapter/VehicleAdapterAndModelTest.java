package org.opentcs.guing.exchange.adapter;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.LoadHandlingDevice;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.TripleProperty;
import org.opentcs.guing.exchange.OpenTCSEventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * A test for <code>VehicleAdapter</code> and <code>VehicleModel</code>.
 *
 * @author pseifert
 */
public class VehicleAdapterAndModelTest {

  /**
   * The (mocked) kernel.
   */
  private LocalKernel kernel;
  private VehicleAdapter adapter;
  private VehicleModel model;
  private Vehicle vehicle;
  OpenTCSEventDispatcher dispatcher;

  @Before
  public void setUp() throws Exception {
    kernel = mock(LocalKernel.class);
    vehicle = new Vehicle(1, "Vehicle");
    when(kernel.createVehicle()).thenReturn(vehicle);
    when(kernel.getTCSObject(Matchers.<Class<Vehicle>>any(),
                             Matchers.<TCSObjectReference<Vehicle>>any())).
        thenReturn(vehicle);

    dispatcher = new OpenTCSEventDispatcher(new ProcessAdapterFactory());
    dispatcher.setKernel(kernel);

    model = new VehicleModel();
    adapter = new VehicleAdapterT();
    adapter.setModel(model);
    dispatcher.addProcessAdapter(adapter);
    adapter.createProcessObject();
  }

  @After
  public void tearDown() {
    kernel = null;
    adapter = null;
    vehicle = null;
    model = null;
    dispatcher = null;
  }

  @Test
  public void testCreateProcessObject() {
    verify(kernel).createVehicle();
  }

  @Test
  public void testReleaseProcessObject() {
    TCSObjectReference<Vehicle> ref = adapter.getProcessObject();
    adapter.releaseProcessObject();
    verify(kernel).removeTCSObject(ref);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessName() {
    model.setName("TestVehicle");
    model.getProperty(ModelComponent.NAME).markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).renameTCSObject(adapter.getProcessObject(), "TestVehicle");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessLength() {
    ((LengthProperty) model.getProperty(VehicleModel.LENGTH)).setValue(100);
    model.getProperty(VehicleModel.LENGTH).markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).setVehicleLength(adapter.getProcessObject(), 100);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessEnergyLevelCritical() {
    ((PercentProperty) model.getProperty(VehicleModel.ENERGY_LEVEL_CRITICAL)).setValue(40);
    model.getProperty(VehicleModel.ENERGY_LEVEL_CRITICAL).markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).setVehicleEnergyLevelCritical(adapter.getProcessObject(), 40);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessEnergyLevelGood() {
    ((PercentProperty) model.getProperty(VehicleModel.ENERGY_LEVEL_GOOD)).setValue(50);
    model.getProperty(VehicleModel.ENERGY_LEVEL_GOOD).markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).setVehicleEnergyLevelGood(adapter.getProcessObject(), 50);
  }

  @Test
  public void testUpdateModelName() {
    vehicle.setName("TestName");
    adapter.updateModelProperties();
    assertEquals("TestName",
                 ((StringProperty) model.getProperty(ModelComponent.NAME)).getText());
  }

  @Test
  public void testUpdateModelLength() {
    vehicle.setLength(100);
    adapter.updateModelProperties();
    assertEquals(100.0,
                 ((LengthProperty) model.getProperty(VehicleModel.LENGTH)).getValue());
  }

  @Test
  public void testUpdateModelEnergyLevelCritical() {
    vehicle.setEnergyLevelCritical(30);
    adapter.updateModelProperties();
    assertEquals(30,
                 ((PercentProperty) model.getProperty(VehicleModel.ENERGY_LEVEL_CRITICAL)).getValue());
  }

  @Test
  public void testUpdateModelEnergyLevelGood() {
    vehicle.setEnergyLevelGood(50);
    adapter.updateModelProperties();
    assertEquals(50,
                 ((PercentProperty) model.getProperty(VehicleModel.ENERGY_LEVEL_GOOD)).getValue());
  }

  @Test
  public void testUpdateModelEnergyLevel() {
    vehicle.setEnergyLevel(70);
    adapter.updateModelProperties();
    assertEquals(70,
                 ((PercentProperty) model.getProperty(VehicleModel.ENERGY_LEVEL)).getValue());
  }

  @Test
  public void testUpdateModelEnergyState() {
    List<LoadHandlingDevice> l = new ArrayList<>();
    l.add(new LoadHandlingDevice("x", true));
    vehicle.setLoadHandlingDevices(l);
    adapter.updateModelProperties();
    assertTrue((boolean) ((BooleanProperty) model.getProperty(VehicleModel.LOADED)).getValue());
  }

  @Test
  public void testUpdateModelState() {
    vehicle.setState(Vehicle.State.CHARGING);
    adapter.updateModelProperties();
    assertEquals(Vehicle.State.CHARGING,
                 ((SelectionProperty) model.getProperty(VehicleModel.STATE)).getValue());
  }

  @Test
  public void testUpdateModelProcState() {
    vehicle.setProcState(Vehicle.ProcState.AWAITING_ORDER);
    adapter.updateModelProperties();
    assertEquals(Vehicle.ProcState.AWAITING_ORDER,
                 ((SelectionProperty) model.getProperty(VehicleModel.PROC_STATE)).getValue());
  }

  @Test
  public void testUpdateModelCurrentPoint() {
    Point p = new Point(2, "CurPoint");
    vehicle.setCurrentPosition(p.getReference());
    PointAdapter curPointAdapter = mock(PointAdapter.class);
    PointModel curPointModel = mock(PointModel.class);
    when(curPointAdapter.getProcessObject()).thenReturn(p.getReference());
    when(curPointAdapter.getModel()).thenReturn(curPointModel);
    dispatcher.addProcessAdapter(curPointAdapter);
    adapter.updateModelProperties();
    assertEquals(curPointModel, model.getPoint());
  }

  @Test
  public void testUpdateModelNextPoint() {
    Point p = new Point(2, "NextPoint");
    vehicle.setNextPosition(p.getReference());
    PointAdapter nextPointAdapter = mock(PointAdapter.class);
    PointModel nextPointModel = mock(PointModel.class);
    when(nextPointAdapter.getProcessObject()).thenReturn(p.getReference());
    when(nextPointAdapter.getModel()).thenReturn(nextPointModel);
    dispatcher.addProcessAdapter(nextPointAdapter);
    adapter.updateModelProperties();
    assertEquals(nextPointModel, model.getNextPoint());
  }

  @Test
  public void testUpdateModelPrecisePosition() {
    vehicle.setPrecisePosition(new Triple(5, 5, 0));
    adapter.updateModelProperties();
    assertEquals("5,5,0",
                 ((TripleProperty) model.getProperty(VehicleModel.PRECISE_POSITION)).getComparableValue());
  }

  @Test
  public void testUpdateModelAngle() {
    vehicle.setOrientationAngle(10.0);
    adapter.updateModelProperties();
    assertEquals(10.0,
                 ((AngleProperty) model.getProperty(VehicleModel.ORIENTATION_ANGLE)).getValue());
  }

  private class VehicleAdapterT
      extends VehicleAdapter {

    @Override
    public boolean hasModelingState() {
      return true;
    }
  }
}
