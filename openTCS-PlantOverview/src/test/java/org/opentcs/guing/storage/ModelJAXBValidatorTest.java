/**
 * Copyright (c) 2016 Fraunhofer IML
 */
package org.opentcs.guing.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.*;
import org.mockito.Matchers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.properties.type.AbstractProperty;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LocationThemeProperty;
import org.opentcs.guing.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.components.properties.type.VehicleThemeProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;

/**
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
@SuppressWarnings("unchecked")
public class ModelJAXBValidatorTest {

  private static final String LAYOUT_NAME = "VLayout-001";

  private static final String LOCATION_THEME_NAME = "Loc-Theme-001";

  private static final String VEHICLE_THEME_NAME = "Vehicle-Theme-001";

  private static final String POINT_NAME = "Point-001";

  private static final String POINT_NAME_2 = "Point-002";

  private static final String PATH_NAME = "Point-001 --- Point-002";

  private static final String LOCATION_TYPE_NAME = "Ladestation";

  private static final String LOCATION_NAME = "Location-001";

  /**
   * The system model for this test.
   */
  private SystemModel model;

  /**
   * The validator for this test.
   */
  private ModelJAXBValidator validator;

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
    validator = new ModelJAXBValidator();
    objectPropertiesMap = new HashMap<>();
    components = new HashMap<>();

    when(model.getAll()).thenAnswer((InvocationOnMock invocation) -> {
      List<ModelComponent> result = new LinkedList<>();
      components.values().stream().forEach(result::add);
      return result;
    });
    when(model.getLocationTypeModels()).thenAnswer((InvocationOnMock invocation) -> {
      return components.values().stream().filter(o -> o instanceof LocationTypeModel).collect(Collectors.toList());
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
    components.put(POINT_NAME, createComponentWithName(ModelComponent.class, POINT_NAME));

    Assert.assertFalse("Validator said valid for duplicate names in the components list",
                       validator.isValidWith(model, createComponentWithName(ModelComponent.class, POINT_NAME)));
  }

  @Test
  public void testLayoutEmpty() {
    LayoutModel layoutModel = createComponentWithName(LayoutModel.class, LAYOUT_NAME);
    Assert.assertFalse("Validator said valid for empty layout model properties.",
                       validator.isValidWith(model, layoutModel));
  }

  @Test
  public void testLayoutMissingScaleX() {
    LayoutModel layoutModel = createLayoutModel(LAYOUT_NAME);
    removeProperty(layoutModel, LayoutModel.SCALE_X);
    Assert.assertFalse("Validator said valid for missing scale x.",
                       validator.isValidWith(model, layoutModel));
  }

  @Test
  public void testLayoutInvalidScaleX() {
    LayoutModel layoutModel = createLayoutModel(LAYOUT_NAME);
    addPropperty(layoutModel, LengthProperty.class, LayoutModel.SCALE_X, "abc");
    Assert.assertFalse("Validator said valid for corrupt scale x.",
                       validator.isValidWith(model, layoutModel));
  }

  @Test
  public void testLayoutMissingScaleY() {
    LayoutModel layoutModel = createLayoutModel(LAYOUT_NAME);
    removeProperty(layoutModel, LayoutModel.SCALE_Y);
    Assert.assertFalse("Validator said valid for missing scale y.",
                       validator.isValidWith(model, layoutModel));
  }

  @Test
  public void testLayoutInvalidScaleY() {
    LayoutModel layoutModel = createLayoutModel(LAYOUT_NAME);
    addPropperty(layoutModel, LengthProperty.class, LayoutModel.SCALE_Y, "abc");
    Assert.assertFalse("Validator said valid for corrupt scale y.",
                       validator.isValidWith(model, layoutModel));
  }

  @Test
  public void testLayoutMissingLocationTheme() {
    LayoutModel layoutModel = createLayoutModel(LAYOUT_NAME);
    removeProperty(layoutModel, LayoutModel.LOCATION_THEME);
    Assert.assertFalse("Validator said valid for missing location theme.",
                       validator.isValidWith(model, layoutModel));
  }

  @Test
  public void testLayoutMissingVehicleTheme() {
    LayoutModel layoutModel = createLayoutModel(LAYOUT_NAME);
    removeProperty(layoutModel, LayoutModel.VEHICLE_THEME);
    Assert.assertFalse("Validator said valid for missing vehicle theme.",
                       validator.isValidWith(model, layoutModel));
  }

  @Test
  public void testLayoutValid() {
    LayoutModel layoutModel = createLayoutModel(LAYOUT_NAME);
    Assert.assertTrue("Validator said invalid for valid layout model.",
                      validator.isValidWith(model, layoutModel));
  }

  @Test
  public void testPointEmpty() {
    PointModel point = createComponentWithName(PointModel.class, POINT_NAME);
    Assert.assertFalse("Validator said valid for empty point model properties.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointMissingOrientationAngle() {
    PointModel point = createPointModel(POINT_NAME);
    removeProperty(point, PointModel.VEHICLE_ORIENTATION_ANGLE);
    Assert.assertFalse("Validator said valid for corrupt orientation angle.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointInvalidOrientationAngle() {
    PointModel point = createPointModel(POINT_NAME);
    addPropperty(point, AngleProperty.class, PointModel.VEHICLE_ORIENTATION_ANGLE, "abc");
    Assert.assertFalse("Validator said valid for corrupt orientation angle.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointNegativeOrientationAngle() {
    PointModel point = createPointModel(POINT_NAME);
    addPropperty(point, AngleProperty.class, PointModel.VEHICLE_ORIENTATION_ANGLE, "-5");
    Assert.assertFalse("Validator said valid for invalid orientation angle.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointMissingType() {
    PointModel point = createPointModel(POINT_NAME);
    removeProperty(point, PointModel.TYPE);
    Assert.assertFalse("Validator said valid for missing point type.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointInvalidType() {
    PointModel point = createPointModel(POINT_NAME);
    addPropperty(point, SelectionProperty.class, PointModel.TYPE, "abc");
    Assert.assertFalse("Validator said valid for corrupt point type.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointMissingModelXPosition() {
    PointModel point = createPointModel(POINT_NAME);
    removeProperty(point, PointModel.MODEL_X_POSITION);
    Assert.assertFalse("Validator said valid for missing model x position.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointInvalidModelXPosition() {
    PointModel point = createPointModel(POINT_NAME);
    addPropperty(point, CoordinateProperty.class, PointModel.MODEL_X_POSITION, "");
    Assert.assertFalse("Validator said valid for corrupt model x position.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointMissingModelYPosition() {
    PointModel point = createPointModel(POINT_NAME);
    removeProperty(point, PointModel.MODEL_Y_POSITION);
    Assert.assertFalse("Validator said valid for missing model y position.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointInvalidModelYPosition() {
    PointModel point = createPointModel(POINT_NAME);
    addPropperty(point, CoordinateProperty.class, PointModel.MODEL_Y_POSITION, "");
    Assert.assertFalse("Validator said valid for corrupt model y position.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointMissingXPosition() {
    PointModel point = createPointModel(POINT_NAME);
    removeProperty(point, ElementPropKeys.POINT_POS_X);
    Assert.assertFalse("Validator said valid for missing point x position.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointInvalidXPosition() {
    PointModel point = createPointModel(POINT_NAME);
    addPropperty(point, StringProperty.class, ElementPropKeys.POINT_POS_X, "");
    Assert.assertFalse("Validator said valid for corrupt point x position.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointMissingYPosition() {
    PointModel point = createPointModel(POINT_NAME);
    removeProperty(point, ElementPropKeys.POINT_POS_Y);
    Assert.assertFalse("Validator said valid for missing point y position.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointInvalidYPosition() {
    PointModel point = createPointModel(POINT_NAME);
    addPropperty(point, StringProperty.class, ElementPropKeys.POINT_POS_Y, "");
    Assert.assertFalse("Validator said valid for corrupt point y position.",
                       validator.isValidWith(model, point));
  }

  @Test
  public void testPointValid() {
    PointModel point = createPointModel(POINT_NAME);
    Assert.assertTrue("Validator said invalid for valid point model.",
                      validator.isValidWith(model, point));
  }

  @Test
  public void testPathMissingLength() {
    PathModel path = createPathModel(PATH_NAME);
    removeProperty(path, PathModel.LENGTH);
    Assert.assertFalse("Validator said valid for missing path length.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathInvalidLength() {
    PathModel path = createPathModel(PATH_NAME);
    addPropperty(path, LengthProperty.class, PathModel.LENGTH, "abc");
    Assert.assertFalse("Validator said valid for invalid path length.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathNegativeLength() {
    PathModel path = createPathModel(PATH_NAME);
    addPropperty(path, LengthProperty.class, PathModel.LENGTH, "-5");
    Assert.assertFalse("Validator said valid for negative path length.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathMissingRoutingCost() {
    PathModel path = createPathModel(PATH_NAME);
    removeProperty(path, PathModel.ROUTING_COST);
    Assert.assertFalse("Validator said valid for missing route cost.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathInvalidRoutingCost() {
    PathModel path = createPathModel(PATH_NAME);
    addPropperty(path, IntegerProperty.class, PathModel.ROUTING_COST, "abc");
    Assert.assertFalse("Validator said valid for invalid route cost.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathMissingMaxVelocity() {
    PathModel path = createPathModel(PATH_NAME);
    removeProperty(path, PathModel.MAX_VELOCITY);
    Assert.assertFalse("Validator said valid for missing max velocity.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathInvalidMaxVelocity() {
    PathModel path = createPathModel(PATH_NAME);
    addPropperty(path, SpeedProperty.class, PathModel.MAX_VELOCITY, "abc");
    Assert.assertFalse("Validator said valid for invalid max velocity.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathMissingMaxReverseVelocity() {
    PathModel path = createPathModel(PATH_NAME);
    removeProperty(path, PathModel.MAX_REVERSE_VELOCITY);
    Assert.assertFalse("Validator said valid for missing max reverse velocity.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathInvalidMaxReverseVelocity() {
    PathModel path = createPathModel(PATH_NAME);
    addPropperty(path, SpeedProperty.class, PathModel.MAX_REVERSE_VELOCITY, "abc");
    Assert.assertFalse("Validator said valid for invalid max reverse velocity.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathMissingConnType() {
    PathModel path = createPathModel(PATH_NAME);
    removeProperty(path, ElementPropKeys.PATH_CONN_TYPE);
    Assert.assertFalse("Validator said valid for missing connection type.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathInvalidConnType() {
    PathModel path = createPathModel(PATH_NAME);
    addPropperty(path, SelectionProperty.class, ElementPropKeys.PATH_CONN_TYPE, "abc");
    Assert.assertFalse("Validator said valid for invalid connection type.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathInvalidControlPoints() {
    PathModel path = createPathModel(PATH_NAME);
    addPropperty(path, SelectionProperty.class, ElementPropKeys.PATH_CONN_TYPE, PathModel.LinerType.BEZIER);
    Assert.assertFalse("Validator said valid for invalid end point.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathMissingControlPoints() {
    PathModel path = createPathModel(PATH_NAME);
    removeProperty(path, ElementPropKeys.PATH_CONTROL_POINTS);
    Assert.assertFalse("Validator said valid for missing path control points.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathMissingEndPoint() {
    PathModel path = createPathModel(PATH_NAME);
    removeProperty(path, PathModel.END_COMPONENT);
    Assert.assertFalse("Validator said valid for missing end point.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathMissingLocked() {
    PathModel path = createPathModel(PATH_NAME);
    removeProperty(path, PathModel.LOCKED);
    Assert.assertFalse("Validator said valid for missing locked property.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathInvalidStartPoint() {
    PathModel path = createPathModel(PATH_NAME);
    components.remove(POINT_NAME);
    Assert.assertFalse("Validator said valid for invalid start point.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathInvalidEndPoint() {
    PathModel path = createPathModel(PATH_NAME);
    components.remove(POINT_NAME_2);
    Assert.assertFalse("Validator said valid for invalid end point.",
                       validator.isValidWith(model, path));
  }

  @Test
  public void testPathValid() {
    PathModel path = createPathModel(PATH_NAME);
    Assert.assertTrue("Validator said invalid for valid path model.",
                      validator.isValidWith(model, path));
  }

  @Test
  public void testLocationTypeMissingOperations() {
    LocationTypeModel locationType = createLocationType(LOCATION_TYPE_NAME);
    removeProperty(locationType, LocationTypeModel.ALLOWED_OPERATIONS);
    Assert.assertFalse("Validator said valid for missing allowed operations.",
                       validator.isValidWith(model, locationType));
  }

  @Test
  public void testLocationTypeValid() {
    LocationTypeModel locationType = createLocationType(LOCATION_TYPE_NAME);
    Assert.assertTrue("Validator said invalid for valid location type model.",
                      validator.isValidWith(model, locationType));
  }

  @Test
  public void testLocationMissingModelPositionX() {
    LocationModel location = createLocation(LOCATION_NAME);
    removeProperty(location, LocationModel.MODEL_X_POSITION);
    Assert.assertFalse("Validator said valid for missing model position x.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationInvalidModelPositionX() {
    LocationModel location = createLocation(LOCATION_NAME);
    addPropperty(location, CoordinateProperty.class, LocationModel.MODEL_X_POSITION, "abc");
    Assert.assertFalse("Validator said valid for invalid model position x.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationMissingModelPositionY() {
    LocationModel location = createLocation(LOCATION_NAME);
    removeProperty(location, LocationModel.MODEL_Y_POSITION);
    Assert.assertFalse("Validator said valid for missing model position x.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationInvalidModelPositionY() {
    LocationModel location = createLocation(LOCATION_NAME);
    addPropperty(location, CoordinateProperty.class, LocationModel.MODEL_Y_POSITION, "abc");
    Assert.assertFalse("Validator said valid for invalid model position x.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationMissingPositionX() {
    LocationModel location = createLocation(LOCATION_NAME);
    removeProperty(location, ElementPropKeys.LOC_POS_X);
    Assert.assertFalse("Validator said valid for missing location position x.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationInvalidPositionX() {
    LocationModel location = createLocation(LOCATION_NAME);
    addPropperty(location, StringProperty.class, ElementPropKeys.LOC_POS_X, "abc");
    Assert.assertFalse("Validator said valid for invalid location position x.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationMissingPositionY() {
    LocationModel location = createLocation(LOCATION_NAME);
    removeProperty(location, ElementPropKeys.LOC_POS_Y);
    Assert.assertFalse("Validator said valid for missing location position y.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationInvalidPositionY() {
    LocationModel location = createLocation(LOCATION_NAME);
    addPropperty(location, StringProperty.class, ElementPropKeys.LOC_POS_Y, "abc");
    Assert.assertFalse("Validator said valid for invalid location position y.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationMissingLabelOffsetX() {
    LocationModel location = createLocation(LOCATION_NAME);
    removeProperty(location, ElementPropKeys.LOC_LABEL_OFFSET_X);
    Assert.assertFalse("Validator said valid for missing label offset x.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationMissingLabelOffsetY() {
    LocationModel location = createLocation(LOCATION_NAME);
    removeProperty(location, ElementPropKeys.LOC_LABEL_OFFSET_Y);
    Assert.assertFalse("Validator said valid for missing label offset y.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationMissingOrientationAngle() {
    LocationModel location = createLocation(LOCATION_NAME);
    removeProperty(location, ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);
    Assert.assertFalse("Validator said valid for missing label orientation angle.",
                       validator.isValidWith(model, location));
  }

  @Test
  public void testLocationMissingType() {
    LocationModel location = createLocation(LOCATION_NAME);
    removeProperty(location, LocationModel.TYPE);
    Assert.assertFalse("Validator said valid for missing location type.",
                       validator.isValidWith(model, location));
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
    when(comp.getProperty(Matchers.anyString())).thenAnswer((InvocationOnMock invocation) -> {
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
    addPropperty(layoutModel, LengthProperty.class, LayoutModel.SCALE_X, 0d);
    addPropperty(layoutModel, LengthProperty.class, LayoutModel.SCALE_Y, 0d);
    addPropperty(layoutModel, LocationThemeProperty.class, LayoutModel.LOCATION_THEME, LOCATION_THEME_NAME);
    addPropperty(layoutModel, VehicleThemeProperty.class, LayoutModel.VEHICLE_THEME, VEHICLE_THEME_NAME);
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
    addPropperty(point, AngleProperty.class, PointModel.VEHICLE_ORIENTATION_ANGLE, "5");
    addPropperty(point, SelectionProperty.class, PointModel.TYPE, PointModel.PointType.HALT);
    addPropperty(point, CoordinateProperty.class, PointModel.MODEL_X_POSITION, "0");
    addPropperty(point, CoordinateProperty.class, PointModel.MODEL_Y_POSITION, "0");
    addPropperty(point, StringProperty.class, ElementPropKeys.POINT_POS_X, "0");
    addPropperty(point, StringProperty.class, ElementPropKeys.POINT_POS_Y, "0");
    return point;
  }

  /**
   * Creates a path model with all properties set.
   *
   * @param name the name of the path model
   * @return the path model
   */
  private PathModel createPathModel(String name) {
    PathModel path = createComponentWithName(PathModel.class, name);
    addPropperty(path, LengthProperty.class, PathModel.LENGTH, 0d);
    addPropperty(path, IntegerProperty.class, PathModel.ROUTING_COST, 0);
    addPropperty(path, SpeedProperty.class, PathModel.MAX_VELOCITY, 0d);
    addPropperty(path, SpeedProperty.class, PathModel.MAX_REVERSE_VELOCITY, 0d);
    addPropperty(path, SelectionProperty.class, ElementPropKeys.PATH_CONN_TYPE, PathModel.LinerType.DIRECT);
    addPropperty(path, StringProperty.class, ElementPropKeys.PATH_CONTROL_POINTS, "");
    addPropperty(path, StringProperty.class, PathModel.START_COMPONENT, POINT_NAME);
    addPropperty(path, StringProperty.class, PathModel.END_COMPONENT, POINT_NAME_2);
    addPropperty(path, BooleanProperty.class, PathModel.LOCKED, false);
    components.put(POINT_NAME, createPointModel(POINT_NAME));
    components.put(POINT_NAME_2, createPointModel(POINT_NAME_2));
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
    addPropperty(locationType, StringSetProperty.class, LocationTypeModel.ALLOWED_OPERATIONS, new HashSet<>());
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
    addPropperty(location, CoordinateProperty.class, LocationModel.MODEL_X_POSITION, "0");
    addPropperty(location, CoordinateProperty.class, LocationModel.MODEL_Y_POSITION, "0");
    addPropperty(location, StringProperty.class, ElementPropKeys.LOC_POS_X, "0");
    addPropperty(location, StringProperty.class, ElementPropKeys.LOC_POS_Y, "0");
    addPropperty(location, LocationTypeProperty.class, LocationModel.TYPE, LOCATION_TYPE_NAME);
    addPropperty(location, StringProperty.class, ElementPropKeys.LOC_LABEL_OFFSET_X, "0");
    addPropperty(location, StringProperty.class, ElementPropKeys.LOC_LABEL_OFFSET_Y, "0");
    addPropperty(location, StringProperty.class, ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE, "");
    components.put(LOCATION_TYPE_NAME, createLocationType(LOCATION_TYPE_NAME));
    return location;
  }

  /**
   *
   * @param name
   * @return
   */
  private LinkModel createLink(String name) {
    LinkModel link = createComponentWithName(LinkModel.class, name);
    addPropperty(link, StringProperty.class, LinkModel.START_COMPONENT, POINT_NAME);
    addPropperty(link, StringProperty.class, LinkModel.END_COMPONENT, LOCATION_NAME);
    components.put(POINT_NAME, createPointModel(POINT_NAME));
    components.put(LOCATION_NAME, createLocation(LOCATION_NAME));

    return link;
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
  private <T extends AbstractProperty> void addPropperty(ModelComponent component,
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
