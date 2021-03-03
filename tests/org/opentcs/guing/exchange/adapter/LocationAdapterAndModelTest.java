package org.opentcs.guing.exchange.adapter;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.OpenTCSEventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PointModel;

/**
 * A test for <code>LocationAdapter</code> and <code>LocationModel</code>.
 *
 * @author pseifert
 */
public class LocationAdapterAndModelTest {

  /**
   * The (mocked) kernel.
   */
  private LocalKernel kernel;
  private Location location;
  private LocationAdapter adapter;
  private LocationModel model;
  private OpenTCSEventDispatcher dispatcher;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    kernel = mock(LocalKernel.class);
    location = new Location(1, "Location", new LocationType(2, "LocationType").getReference());
    when(kernel.createLocation(any(TCSObjectReference.class))).thenReturn(location);

    dispatcher = new OpenTCSEventDispatcher(new ProcessAdapterFactory());
    dispatcher.setKernel(kernel);

    model = new LocationModel();
    adapter = new LocationAdapterT();
    adapter.setModel(model);
    dispatcher.addProcessAdapter(adapter);

    LocationTypeAdapter locTypeAdapter = addAnotherLocationType(3, "BaseType");
    dispatcher.addProcessAdapter(locTypeAdapter);

    model.setLocationType(locTypeAdapter.getModel());
    adapter.createProcessObject();
    when(kernel.getTCSObject(Location.class, adapter.getProcessObject()))
        .thenReturn(location);
  }

  @After
  public void tearDown() {
    kernel = null;
    adapter = null;
    model = null;
    location = null;
    dispatcher = null;
  }

  @SuppressWarnings("unchecked")
  private LocationTypeAdapter addAnotherLocationType(int i, String name) {
    LocationTypeAdapter locTypeAdapter = mock(LocationTypeAdapter.class);
    LocationTypeModel locTypeModel = mock(LocationTypeModel.class);
    locTypeAdapter.setModel(locTypeModel);
    TCSObjectReference<LocationType> locTypeRef = new LocationType(i, name).getReference();
    when(locTypeAdapter.getProcessObject()).thenReturn(locTypeRef);
    when(locTypeAdapter.getModel()).thenReturn(locTypeModel);
    when(locTypeAdapter.getProcessObject()).thenReturn(locTypeRef);
    when(locTypeModel.getName()).thenReturn(name);

    dispatcher.addProcessAdapter(locTypeAdapter);

    return locTypeAdapter;
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testCreateProcessObject() {
    LocationTypeAdapter locTypeAdapter
        = (LocationTypeAdapter) dispatcher.findProcessAdapter(model.getLocationType());
    verify(kernel).createLocation(locTypeAdapter.getProcessObject());
  }

  @Test
  public void testReleaseProcessObject() {
    TCSObjectReference<Location> ref = adapter.getProcessObject();
    adapter.releaseProcessObject();
    verify(kernel).removeTCSObject(ref);
  }

  @Test
  public void testUpdateProcessName() {
    StringProperty pName = (StringProperty) model.getProperty(ModelComponent.NAME);
    pName.setText("NewLocName");
    pName.markChanged();
    adapter.updateProcessProperties(false);
    verify(kernel).renameTCSObject(adapter.getProcessObject(), "NewLocName");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateProcessLocationType() {
    LocationTypeAdapter locTypeAdapter = addAnotherLocationType(4, "newLocType");
    model.setLocationType(locTypeAdapter.getModel());

    adapter.updateProcessProperties(false);
    verify(kernel).setLocationType(adapter.getProcessObject(),
                                   locTypeAdapter.getProcessObject());
  }

  @Test
  public void testUpdateModelName() {
    location.setName("newLocName");
    adapter.updateModelProperties();
    assertEquals("newLocName",
                 ((StringProperty) model.getProperty(ModelComponent.NAME)).getText());
  }

  @Test
  public void testUpdateModelType() {
    TCSObjectReference<LocationType> newLocType = new LocationType(4, "newLocType").getReference();
    location.setType(newLocType);

    LocationTypeAdapter locTypeAdapter = addAnotherLocationType(5, "newLocType");
    List<LocationTypeModel> types = new ArrayList<>();
    types.add(model.getLocationType());
    types.add(locTypeAdapter.getModel());
    model.updateTypeProperty(types);

    adapter.updateModelProperties();
    assertEquals("newLocType",
                 ((SelectionProperty) model.getProperty(LocationModel.TYPE)).getValue());
  }

  @Test
  public void testAddRemoveLink() {
    LinkModel linkModel = new LinkModel();
    PointModel other = mock(PointModel.class);
    assertFalse(model.hasConnectionTo(other));
    linkModel.setConnectedComponents(model, other);
    assertTrue(model.hasConnectionTo(other));
    linkModel.removingConnection();
    assertFalse(model.hasConnectionTo(other));
  }

  private class LocationAdapterT
      extends LocationAdapter {

    @Override
    public boolean hasModelingState() {
      return true;
    }
  }
}
