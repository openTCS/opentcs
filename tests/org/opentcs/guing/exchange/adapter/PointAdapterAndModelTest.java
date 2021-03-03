package org.opentcs.guing.exchange.adapter;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.OpenTCSEventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import static org.opentcs.guing.model.elements.PointModel.PointType.PARK;

/**
 * A test for <code>PointAdapter</code> and <code>PointModel</code>.
 *
 * @author pseifert
 */
public class PointAdapterAndModelTest {

  /**
   * The (mocked) kernel.
   */
  private LocalKernel kernel;
  private Point point;
  private PointAdapter adapter;
  private PointModel pointModel;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    kernel = mock(LocalKernel.class);
    point = new Point(1, "Point");
    point.setVehicleOrientationAngle(0);
    when(kernel.createPoint()).thenReturn(point);

    OpenTCSEventDispatcher dispatcher
        = new OpenTCSEventDispatcher(new ProcessAdapterFactory());
    dispatcher.setKernel(kernel);

    pointModel = new PointModel();
    adapter = new PointAdapterT();
    adapter.setModel(pointModel);
    dispatcher.addProcessAdapter(adapter);
    adapter.createProcessObject();
    when(kernel.getTCSObject(Point.class, adapter.getProcessObject()))
        .thenReturn(point);
  }

  @After
  public void tearDown() {
    kernel = null;
    adapter = null;
    pointModel = null;
    point = null;
  }

  @Test
  public void testCreateProcessObject() {
    verify(kernel).createPoint();
  }

  @Test
  public void testReleaseProcessObject() {
    TCSObjectReference<Point> ref = adapter.getProcessObject();
    adapter.releaseProcessObject();
    verify(kernel).removeTCSObject(ref);
  }

  @Test
  public void testUpdateProcessName() {
    pointModel.setName("TestPoint");
    pointModel.getProperty(ModelComponent.NAME).markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).renameTCSObject(adapter.getProcessObject(), "TestPoint");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessPositions() {
    LengthProperty lpx = (LengthProperty) pointModel.getProperty(PointModel.MODEL_X_POSITION);
    LengthProperty lpy = (LengthProperty) pointModel.getProperty(PointModel.MODEL_Y_POSITION);
    lpx.setValue(5.0);
    lpx.markChanged();
    lpy.setValue(5.0);
    lpy.markChanged();

    double posX = lpx.getValueByUnit(LengthProperty.Unit.MM);
    double posY = lpy.getValueByUnit(LengthProperty.Unit.MM);
    Triple position = new Triple();
    position.setX((int) posX);
    position.setY((int) posY);

    adapter.updateProcessProperties(false);
    verify(kernel).setPointPosition(adapter.getProcessObject(), position);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessAngle() {
    AngleProperty pAngle = (AngleProperty) pointModel.getProperty(PointModel.VEHICLE_ORIENTATION_ANGLE);
    pAngle.setValue(5.0);
    pAngle.markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).setPointVehicleOrientationAngle(adapter.getProcessObject(),
                                                   5.0);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessType() {
    SelectionProperty pType = (SelectionProperty) pointModel.getProperty(PointModel.TYPE);
    pType.setValue(PARK);
    pType.markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).setPointType(adapter.getProcessObject(),
                                Point.Type.PARK_POSITION);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateModelName() {
    point.setName("TestName");
    adapter.updateModelProperties();
    assertEquals("TestName",
                 ((StringProperty) pointModel.getProperty(ModelComponent.NAME)).getText());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateModelPositions() {
    point.setPosition(new Triple(5, 5, 0));
    adapter.updateModelProperties();
    assertEquals(5.0,
                 ((LengthProperty) pointModel.getProperty(PointModel.MODEL_X_POSITION)).getValue());
    assertEquals(5.0,
                 ((LengthProperty) pointModel.getProperty(PointModel.MODEL_Y_POSITION)).getValue());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateModelAngle() {
    point.setVehicleOrientationAngle(5.0);
    adapter.updateModelProperties();
    assertEquals(5.0,
                 ((AngleProperty) pointModel.getProperty(PointModel.VEHICLE_ORIENTATION_ANGLE)).getValue());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateModelType() {
    point.setType(Point.Type.PARK_POSITION);
    adapter.updateModelProperties();
    assertEquals(PARK,
                 ((SelectionProperty) pointModel.getProperty(PointModel.TYPE)).getValue());
  }

  @Test
  public void testAddRemoveConnection() {
    PointModel other = new PointModel();
    PathModel path = new PathModel();
    assertFalse(pointModel.hasConnectionTo(other));
    path.setConnectedComponents(pointModel, other);
    assertTrue(pointModel.hasConnectionTo(other));
    path.removingConnection();
    assertFalse(pointModel.hasConnectionTo(other));
  }

  private class PointAdapterT
      extends PointAdapter {

    @Override
    public boolean hasModelingState() {
      return true;
    }
  }
}
