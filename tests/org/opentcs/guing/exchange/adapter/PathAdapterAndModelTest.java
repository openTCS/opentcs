package org.opentcs.guing.exchange.adapter;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.event.ConnectionChangeEvent;
import org.opentcs.guing.exchange.OpenTCSEventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;

/**
 * A test for <code>PathAdapter</code> and <code>PathModel</code>.
 *
 * @author pseifert
 */
public class PathAdapterAndModelTest {

  /**
   * The (mocked) kernel.
   */
  private LocalKernel kernel;
  private PathAdapter adapter;
  private PathModel model;
  private Path path;
  OpenTCSEventDispatcher dispatcher;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    kernel = mock(LocalKernel.class);
    Point srcPoint = new Point(2, "srcPoint");
    Point destPoint = new Point(3, "destPoint");
    path = new Path(1, "Path", srcPoint.getReference(), destPoint.getReference());
    when(kernel.createPath(srcPoint.getReference(), destPoint.getReference())).thenReturn(path);
    when(kernel.getTCSObject(Path.class, path.getReference())).
        thenReturn(path);

    PointAdapter srcAdapter = mock(PointAdapter.class);
    PointModel srcModel = mock(PointModel.class);
    when(srcAdapter.getModel()).thenReturn(srcModel);
    when(srcAdapter.getProcessObject()).thenReturn(srcPoint.getReference());
    PointAdapter destAdapter = mock(PointAdapter.class);
    PointModel destModel = mock(PointModel.class);
    when(destAdapter.getModel()).thenReturn(destModel);
    when(destAdapter.getProcessObject()).thenReturn(destPoint.getReference());

    dispatcher = new OpenTCSEventDispatcher(new ProcessAdapterFactory());
    dispatcher.setKernel(kernel);

    model = new PathModel();
    model.setConnectedComponents(srcModel, destModel);
    adapter = new PathAdapterT();
    adapter.setModel(model);
    dispatcher.addProcessAdapter(adapter);
    dispatcher.addProcessAdapter(srcAdapter);
    dispatcher.addProcessAdapter(destAdapter);
    adapter.createProcessObject();
  }

  @After
  public void tearDown() {
    kernel = null;
    adapter = null;
    path = null;
    model = null;
    dispatcher = null;
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testConnectionChanged() {
    assertNull(adapter.getProcessObject());
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    verify(kernel).createPath(Matchers.<TCSObjectReference<Point>>any(), 
                              Matchers.<TCSObjectReference<Point>>any());
    assertNotNull(adapter.getProcessObject());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testReleaseProcessObject() {
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    TCSObjectReference<Path> ref = adapter.getProcessObject();
    adapter.releaseProcessObject();
    verify(kernel).removeTCSObject(ref);
    assertNull(adapter.getProcessObject());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessName() {
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    ((StringProperty) model.getProperty(ModelComponent.NAME)).setText("TestPath");
    model.getProperty(ModelComponent.NAME).markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).renameTCSObject(adapter.getProcessObject(), "TestPath");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessLength() {
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    ((LengthProperty) model.getProperty(PathModel.LENGTH)).setValue(100);
    model.getProperty(PathModel.LENGTH).markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).setPathLength(adapter.getProcessObject(), 100);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessRoutingCost() {
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    ((IntegerProperty) model.getProperty(PathModel.ROUTING_COST)).setValue(100);
    model.getProperty(PathModel.ROUTING_COST).markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).setPathRoutingCost(adapter.getProcessObject(), 100);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessMaxVelocity() throws Exception{
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    ((SpeedProperty) model.getProperty(PathModel.MAX_VELOCITY)).setValueAndUnit(100, SpeedProperty.Unit.MM_S);
    model.getProperty(PathModel.MAX_VELOCITY).markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).setPathMaxVelocity(adapter.getProcessObject(), 100);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessMaxReverseVelocity() throws Exception{
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    ((SpeedProperty) model.getProperty(PathModel.MAX_REVERSE_VELOCITY)).setValueAndUnit(100, SpeedProperty.Unit.MM_S);
    model.getProperty(PathModel.MAX_REVERSE_VELOCITY).markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).setPathMaxReverseVelocity(adapter.getProcessObject(), 100);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessLocked() {
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    // XXX Next line is neccessary because OpenTCSView can't be mocked.
    // This property is operating editable and would try to call a method of the
    // OpenTCSView
    ((BooleanProperty) model.getProperty(PathModel.LOCKED)).setOperatingEditable(false);
    ((BooleanProperty) model.getProperty(PathModel.LOCKED)).setValue(true);
    model.getProperty(PathModel.LOCKED).markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).setPathLocked(adapter.getProcessObject(), true);
  }

  @Test
  public void testUpdateModelName() {
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    path.setName("TestPath");
    adapter.updateModelProperties();
    assertEquals("TestPath",
                 ((StringProperty) model.getProperty(ModelComponent.NAME)).getText());
  }

  @Test
  public void testUpdateModelLength() {
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    path.setLength(100);
    adapter.updateModelProperties();
    assertEquals(100.0,
                 ((LengthProperty) model.getProperty(PathModel.LENGTH)).getValue());
  }

  @Test
  public void testUpdateModelRoutingCost() {
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    path.setRoutingCost(100L);
    adapter.updateModelProperties();
    assertEquals(100,
                 ((IntegerProperty) model.getProperty(PathModel.ROUTING_COST)).getValue());
  }

  @Test
  public void testUpdateModelMaxVelocity() {
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    path.setMaxVelocity(100);
    adapter.updateModelProperties();
    // Property saves it as m/s
    assertEquals(100 * 0.001,
                 ((SpeedProperty) model.getProperty(PathModel.MAX_VELOCITY)).getValue());
  }

  @Test
  public void testUpdateModelMaxReverseVelocity() {
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    path.setMaxReverseVelocity(100);
    adapter.updateModelProperties();
    // Property saves it as m/s
    assertEquals(100 * 0.001,
                 ((SpeedProperty) model.getProperty(PathModel.MAX_REVERSE_VELOCITY)).getValue());
  }

  @Test
  public void testUpdateModelLocked() {
    adapter.connectionChanged(new ConnectionChangeEvent(model));
    path.setLocked(true);
    adapter.updateModelProperties();
    assertEquals(true,
                 ((BooleanProperty) model.getProperty(PathModel.LOCKED)).getValue());
  }

  private class PathAdapterT
      extends PathAdapter {

    @Override
    public boolean hasModelingState() {
      return true;
    }
  }
}
