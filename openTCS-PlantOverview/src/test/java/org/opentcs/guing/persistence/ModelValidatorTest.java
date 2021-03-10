/**
 * Copyright (c) 2016 Fraunhofer IML
 */
package org.opentcs.guing.persistence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.properties.type.AbstractProperty;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.components.properties.type.TripleProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
@SuppressWarnings("unchecked")
public class ModelValidatorTest {

  private static final String LAYOUT_NAME = "VLayout-001";

  private static final String LOCATION_THEME_NAME = "Loc-Theme-001";

  private static final String VEHICLE_THEME_NAME = "Vehicle-Theme-001";

  private static final String POINT_NAME = "Point-001";

  private static final String POINT_NAME_2 = "Point-002";

  private static final String PATH_NAME = "Point-001 --- Point-002";

  private static final String LOCATION_TYPE_NAME = "Ladestation";

  private static final String LOCATION_NAME = "Location-001";

  private static final String LINK_NAME = POINT_NAME + " --- " + LOCATION_NAME;

  private static final String VEHICLE_NAME = "Vehicle-001";

  /**
   * The system model for this test.
   */
  private SystemModel model;

  /**
   * The validator for this test.
   */
  private ModelValidator validator;

  /**
   * Stores the properties for every model component.
   */
  private Map<ModelComponent, Map<String, Object>> objectPropertiesMap;

  /**
   * Stores all model components returned by {@link SystemModel#getAll()}.
   */
  private Map<String, ModelComponent> components;

  @Before
  public void setUp() {
    model = mock(SystemModel.class);
    validator = new ModelValidator();
    objectPropertiesMap = new HashMap<>();
    components = new HashMap<>();

    when(model.getAll()).thenAnswer((InvocationOnMock invocation) -> {
      List<ModelComponent> result = new LinkedList<>();
      components.values().stream().forEach(result::add);
      return result;
    });
    when(model.getLocationTypeModels()).thenAnswer((InvocationOnMock invocation) -> {
      return components.values().stream()
          .filter(o -> o instanceof LocationTypeModel)
          .collect(Collectors.toList());
    });
  }

  @After
  public void tearDown() {

  }

  @Test
  public void shouldInvalidateIfNull() {
    Assert.assertFalse("Validator said valid for null input as model object.",
                       validator.isValidWith(model, null));

    Assert.assertFalse("Validator said valid for null input as system model.",
                       validator.isValidWith(null, mock(ModelComponent.class)));
  }

  @Test
  public void shouldInvalidateWhenEmptyName() {
    Assert.assertFalse("Validator said valid for empty component name",
                       validator.isValidWith(model, createComponentWithName(ModelComponent.class, "")));
  }

  @Test
  public void shouldInvalidateIfExists() {
    ModelComponent component = createComponentWithName(ModelComponent.class, POINT_NAME);
    components.put(POINT_NAME, component);
    when(model.getModelComponent(POINT_NAME)).thenReturn(component);

    Assert.assertFalse("Validator said valid for duplicate names in the components list",
                       validator.isValidWith(model, createComponentWithName(ModelComponent.class, POINT_NAME)));
  }

  @Test
  public void testPointNegativeOrientationAngle() {
    PointModel point = createPointModel(POINT_NAME);
    addProperty(point, AngleProperty.class, PointModel.VEHICLE_ORIENTATION_ANGLE, -5d);
    Assert.assertTrue("Validator said invalid for negative orientation angle.",
                      validator.isValidWith(model, point));
  }

  @Test
  public void testPathNegativeLength() {
    PathModel path = createPathModel(PATH_NAME, POINT_NAME, POINT_NAME_2);
    addProperty(path, LengthProperty.class, PathModel.LENGTH, -5d);
    Assert.assertFalse("Validator said valid for negative path length.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathInvalidStartPoint() {
    PathModel path = createPathModel(PATH_NAME, POINT_NAME, POINT_NAME_2);
    when(model.getModelComponent(POINT_NAME_2)).thenReturn(components.get(POINT_NAME_2));
    components.remove(POINT_NAME);
    Assert.assertFalse("Validator said valid for invalid start point.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathInvalidEndPoint() {
    PathModel path = createPathModel(PATH_NAME, POINT_NAME, POINT_NAME_2);
    when(model.getModelComponent(POINT_NAME)).thenReturn(components.get(POINT_NAME));
    components.remove(POINT_NAME_2);
    Assert.assertFalse("Validator said valid for invalid end point.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathValid() {
    PathModel path = createPathModel(PATH_NAME, POINT_NAME, POINT_NAME_2);
    when(model.getModelComponent(POINT_NAME)).thenReturn(components.get(POINT_NAME));
    when(model.getModelComponent(POINT_NAME_2)).thenReturn(components.get(POINT_NAME_2));
    Assert.assertTrue("Validator said invalid for valid path model.",
                      validator.isValidWith(model, path));
  }

  @Test
  public void testLocationInvalidType() {
    LocationModel location = createLocation(LOCATION_NAME);
    components.remove(LOCATION_TYPE_NAME);
    Assert.assertFalse("Validator said valid for invalid location type.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationValid() {
    LocationModel location = createLocation(LOCATION_NAME);
    Assert.assertTrue("Validator said invalid for valid location model.",
                      validator.isValidWith(model, location));
  }

  @Test
  public void testLinkInvalidEndPoint() {
    LinkModel link = createLink(LINK_NAME);
    when(model.getModelComponent(POINT_NAME)).thenReturn(components.get(POINT_NAME));
    Assert.assertFalse("Validator said valid for missing end point.",
                       validator.isValidWith(model, link));
  }

  @Test
  public void testLinkInvalidStartPoint() {
    LinkModel link = createLink(LINK_NAME);
    when(model.getModelComponent(LOCATION_NAME)).thenReturn(components.get(LOCATION_NAME));
    Assert.assertFalse("Validator said valid for missing start point.",
                       validator.isValidWith(model, link));
  }

  @Test
  public void testLinkValid() {
    LinkModel link = createLink(LINK_NAME);
    when(model.getModelComponent(POINT_NAME)).thenReturn(components.get(POINT_NAME));
    when(model.getModelComponent(LOCATION_NAME)).thenReturn(components.get(LOCATION_NAME));
    Assert.assertTrue("Validator said invalid for valid link model.",
                      validator.isValidWith(model, link));
  }

  @Test
  public void testVehicleInvalidNextPosition() {
    VehicleModel vehicle = createVehicle(VEHICLE_NAME);
    when(model.getModelComponent(POINT_NAME)).thenReturn(components.get(POINT_NAME));
    Assert.assertFalse("Validator said valid for invalid vehicle model.",
                       validator.isValidWith(model, vehicle));
  }

  @Test
  public void testVehicleValidWithNullPoints() {
    VehicleModel vehicle = createVehicle(VEHICLE_NAME);
    addProperty(vehicle, StringProperty.class, VehicleModel.POINT, "null");
    addProperty(vehicle, StringProperty.class, VehicleModel.NEXT_POINT, "null");
    Assert.assertTrue("Validator said invalid for valid vehicle model.",
                      validator.isValidWith(model, vehicle));
  }

  @Test
  public void testVehicleValid() {
    VehicleModel vehicle = createVehicle(VEHICLE_NAME);
    when(model.getModelComponent(POINT_NAME)).thenReturn(components.get(POINT_NAME));
    when(model.getModelComponent(POINT_NAME_2)).thenReturn(components.get(POINT_NAME_2));
    Assert.assertTrue("Validator said invalid for valid vehicle model.",
                      validator.isValidWith(model, vehicle));
  }

  /**
   * Creates a mock for a given class with the given name and registers Mockito Stubs on it
   * to return from the properties map defined at the beginning.
   *
   * @param <T> the type of the model component
   * @param clazz the class of the model component
   * @param name the name of the model component
   * @return the mocked model component
   */
  private <T extends ModelComponent> T createComponentWithName(Class<T> clazz, String name) {
    T comp = mock(clazz);
    when(comp.getName()).thenReturn(name);
    when(comp.getProperty(anyString())).thenAnswer((InvocationOnMock invocation) -> {
      String propName = invocation.getArguments()[0].toString();
      objectPropertiesMap.putIfAbsent(comp, new HashMap<>());
      return objectPropertiesMap.get(comp).get(propName);
    });
    return comp;
  }

  /**
   * Creates a layout model with all properties set.
   *
   * @param name the name of the layout model
   * @return the layout model
   */
  private LayoutModel createLayoutModel(String name) {
    LayoutModel layoutModel = createComponentWithName(LayoutModel.class, name);
    addProperty(layoutModel, LengthProperty.class, LayoutModel.SCALE_X, 0d);
    addProperty(layoutModel, LengthProperty.class, LayoutModel.SCALE_Y, 0d);
    return layoutModel;
  }

  /**
   * Creates a point model with all properties set.
   *
   * @param name the name of the point model
   * @return the point model
   */
  private PointModel createPointModel(String name) {
    PointModel point = createComponentWithName(PointModel.class, name);
    addProperty(point, AngleProperty.class, PointModel.VEHICLE_ORIENTATION_ANGLE, "5");
    addProperty(point, SelectionProperty.class, PointModel.TYPE, PointModel.Type.HALT);
    addProperty(point, CoordinateProperty.class, PointModel.MODEL_X_POSITION, "0");
    addProperty(point, CoordinateProperty.class, PointModel.MODEL_Y_POSITION, "0");
    addProperty(point, StringProperty.class, ElementPropKeys.POINT_POS_X, "0");
    addProperty(point, StringProperty.class, ElementPropKeys.POINT_POS_Y, "0");
    return point;
  }

  /**
   * Creates a path model with all properties set.
   *
   * @param name the name of the path model
   * @return the path model
   */
  private PathModel createPathModel(String name, String pointName1, String pointName2) {
    PathModel path = createComponentWithName(PathModel.class, name);
    addProperty(path, LengthProperty.class, PathModel.LENGTH, 0d);
    addProperty(path, IntegerProperty.class, PathModel.ROUTING_COST, 0);
    addProperty(path, SpeedProperty.class, PathModel.MAX_VELOCITY, 0d);
    addProperty(path, SpeedProperty.class, PathModel.MAX_REVERSE_VELOCITY, 0d);
    addProperty(path, SelectionProperty.class, ElementPropKeys.PATH_CONN_TYPE, PathModel.Type.DIRECT);
    addProperty(path, StringProperty.class, ElementPropKeys.PATH_CONTROL_POINTS, "");
    addProperty(path, StringProperty.class, PathModel.START_COMPONENT, pointName1);
    addProperty(path, StringProperty.class, PathModel.END_COMPONENT, pointName2);
    addProperty(path, BooleanProperty.class, PathModel.LOCKED, false);
    components.put(pointName1, createPointModel(pointName1));
    components.put(pointName2, createPointModel(pointName2));
    return path;
  }

  /**
   * Creates a location type model with all properties set.
   *
   * @param name the name of the location type model
   * @return the location type model
   */
  private LocationTypeModel createLocationType(String name) {
    LocationTypeModel locationType = createComponentWithName(LocationTypeModel.class, name);
    addProperty(locationType, StringSetProperty.class, LocationTypeModel.ALLOWED_OPERATIONS, new HashSet<>());
    return locationType;
  }

  /**
   * Creates a location model with all properties set.
   *
   * @param name the name of the location model
   * @return the location model
   */
  private LocationModel createLocation(String name) {
    LocationModel location = createComponentWithName(LocationModel.class, name);
    addProperty(location, CoordinateProperty.class, LocationModel.MODEL_X_POSITION, "0");
    addProperty(location, CoordinateProperty.class, LocationModel.MODEL_Y_POSITION, "0");
    addProperty(location, StringProperty.class, ElementPropKeys.LOC_POS_X, "0");
    addProperty(location, StringProperty.class, ElementPropKeys.LOC_POS_Y, "0");
    addProperty(location, LocationTypeProperty.class, LocationModel.TYPE, LOCATION_TYPE_NAME);
    addProperty(location, StringProperty.class, ElementPropKeys.LOC_LABEL_OFFSET_X, "0");
    addProperty(location, StringProperty.class, ElementPropKeys.LOC_LABEL_OFFSET_Y, "0");
    addProperty(location, StringProperty.class, ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE, "");
    components.put(LOCATION_TYPE_NAME, createLocationType(LOCATION_TYPE_NAME));
    return location;
  }

  /**
   * Creates a link model with all properties set.
   *
   * @param name The name of the link
   * @return The link model
   */
  private LinkModel createLink(String name) {
    LinkModel link = createComponentWithName(LinkModel.class, name);
    addProperty(link, StringProperty.class, LinkModel.START_COMPONENT, POINT_NAME);
    addProperty(link, StringProperty.class, LinkModel.END_COMPONENT, LOCATION_NAME);
    components.put(POINT_NAME, createPointModel(POINT_NAME));
    components.put(LOCATION_NAME, createLocation(LOCATION_NAME));

    return link;
  }

  /**
   * Creates a vehicle model with all properties set.
   *
   * @param name The name of the vehicle
   * @return The vehicle model
   */
  private VehicleModel createVehicle(String name) {
    VehicleModel vehicle = createComponentWithName(VehicleModel.class, name);
    addProperty(vehicle, LengthProperty.class, VehicleModel.LENGTH, 1.0);
    addProperty(vehicle, PercentProperty.class, VehicleModel.ENERGY_LEVEL_CRITICAL, 30);
    addProperty(vehicle, PercentProperty.class, VehicleModel.ENERGY_LEVEL_GOOD, 80);
    addProperty(vehicle, PercentProperty.class, VehicleModel.ENERGY_LEVEL, 60);
    addProperty(vehicle, SelectionProperty.class, VehicleModel.PROC_STATE, Vehicle.ProcState.IDLE);
    addProperty(vehicle, SelectionProperty.class, VehicleModel.STATE, Vehicle.State.IDLE);
    addProperty(vehicle, SelectionProperty.class,
                VehicleModel.INTEGRATION_LEVEL,
                Vehicle.IntegrationLevel.TO_BE_RESPECTED);
    addProperty(vehicle, BooleanProperty.class, VehicleModel.LOADED, Boolean.FALSE);
    addProperty(vehicle, StringProperty.class, VehicleModel.POINT, POINT_NAME);
    addProperty(vehicle, StringProperty.class, VehicleModel.NEXT_POINT, POINT_NAME_2);
    addProperty(vehicle, TripleProperty.class, VehicleModel.PRECISE_POSITION, new Triple(0, 0, 0));
    addProperty(vehicle, AngleProperty.class, VehicleModel.ORIENTATION_ANGLE, 0.0);

    components.put(POINT_NAME, createPointModel(POINT_NAME));
    components.put(POINT_NAME_2, createPointModel(POINT_NAME_2));
    return vehicle;
  }

  /**
   * Adds a property to the given model component.
   *
   * @param <T> the type of the property
   * @param component the model component
   * @param clazz the class of the property
   * @param propName the property key
   * @param propValue the property value
   */
  private <T extends AbstractProperty> void addProperty(ModelComponent component,
                                                        Class<T> clazz,
                                                        String propName,
                                                        Object propValue) {
    objectPropertiesMap.putIfAbsent(component, new HashMap<>());
    T property = mock(clazz);
    when(property.getValue()).thenReturn(propValue);
    when(property.getComparableValue()).thenReturn(propValue);
    if (clazz.isAssignableFrom(StringProperty.class)) {
      when(((StringProperty) property).getText()).thenReturn(propValue.toString());
    }
    objectPropertiesMap.get(component).put(propName, property);
  }

  /**
   * Removes a property from a model component.
   *
   * @param component the model component
   * @param propName the property key
   */
  private void removeProperty(ModelComponent component, String propName) {
    objectPropertiesMap.putIfAbsent(component, new HashMap<>());
    objectPropertiesMap.get(component).remove(propName);
  }
}
