/**
 * Copyright (c) 2016 Fraunhofer IML
 */
package org.opentcs.guing.storage;

import com.google.common.base.Strings;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.CoursePointProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LocationThemeProperty;
import org.opentcs.guing.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.components.properties.type.TripleProperty;
import org.opentcs.guing.components.properties.type.VehicleThemeProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * Validator for a {@link SystemModel} and its {@link ModelComponent}s.
 * Validates if the model component can safely be added to a system model.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class ModelValidator {

  /**
   * The collection of errors which happened after the last reset.
   */
  private final List<String> errors = new LinkedList<>();

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ModelValidator.class);

  /**
   * Creates a new instance.
   */
  public ModelValidator() {
  }

  /**
   * Returns all errors which happened after the last reset.
   *
   * @return the collection of errors as string
   */
  public final List<String> getErrors() {
    return new LinkedList<>(errors);
  }

  /**
   * Clears all error messages.
   */
  public void resetErrors() {
    errors.clear();
  }

  /**
   * Checks whether the given model will be valid if the component would be added to it.
   *
   * @param model the system model
   * @param component the model component
   * @return true if the model will be valid after adding the component, false otherwise
   */
  public boolean isValidWith(SystemModel model, ModelComponent component) {
    if (model == null) {
      errorOccurred(model, "The system model is null.");
      return false;
    }
    if (component == null) {
      errorOccurred(component, "The model component to validate is null.");
      return false;
    }
    //Validate the name of the component
    if (Strings.isNullOrEmpty(component.getName())) {
      errorOccurred(component, "Invalid name \"{}\".", component.getName());
      return false;
    }
    if (nameExists(model, component)) {
      errorOccurred(component, "Component name \"{}\" used multiple times.", component.getName());
      return false;
    }
    //Validate the miscellaneous property of the component
    //TODO: Seems to be optional in some models?!
    KeyValueSetProperty miscellaneous = (KeyValueSetProperty) component.getProperty(ModelComponent.MISCELLANEOUS);
    /*if (miscellaneous == null) {
     errorOccurred(component, "Miscellaneous key-value-set does not exist.");
     return false;
     }*/
    boolean valid = true;
    if (component instanceof LayoutModel) {
      valid = validateLayoutModel(model, (LayoutModel) component);
    }
    else if (component instanceof PointModel) {
      valid = validatePoint(model, (PointModel) component);
    }
    else if (component instanceof PathModel) {
      valid = validatePath(model, (PathModel) component);
    }
    else if (component instanceof LocationTypeModel) {
      valid = validateLocationType(model, (LocationTypeModel) component);
    }
    else if (component instanceof LocationModel) {
      valid = validateLocation(model, (LocationModel) component);
    }
    else if (component instanceof LinkModel) {
      valid = validateLink(model, (LinkModel) component);
    }
    else if (component instanceof BlockModel) {
      valid = validateBlock(model, (BlockModel) component);
    }
    else if (component instanceof GroupModel) {
      valid = validateGroup(model, (GroupModel) component);
    }
    else if (component instanceof StaticRouteModel) {
      valid = validateStaticRoute(model, (StaticRouteModel) component);
    }
    else if (component instanceof VehicleModel) {
      valid = validateVehicle(model, (VehicleModel) component);
    }
    else {
      LOG.warn("Unknown model component {} - skipping validation.", component.getClass());
    }
    return valid;
  }

  /**
   * Handles all occurred errors while validating the system model.
   *
   * @param component the component where the error occurred
   * @param format the format string
   * @param args a list of arguments
   */
  private void errorOccurred(ModelComponent component, String format, Object... args) {
    String name = component == null ? "null" : component.getName();
    Object[] arguments = new Object[args.length + 1];
    arguments[0] = name;
    for (int x = 0; x < args.length; x++) {
      arguments[x + 1] = args[x];
    }

    String message = MessageFormatter.arrayFormat(format, arguments).getMessage();
    LOG.info(message);
    errors.add(message);
  }

  /**
   * Validates the properties of a layout model.
   *
   * @param model the systemo model to validate against
   * @param layoutModel the layout model to validate
   * @return true if the layout model is valid, false otherwise
   */
  private boolean validateLayoutModel(SystemModel model, LayoutModel layoutModel) {
    boolean valid = true;
    //Validate the x-scale in the model view
    LengthProperty scaleXProperty = (LengthProperty) layoutModel.getProperty(LayoutModel.SCALE_X);
    if (scaleXProperty == null || scaleXProperty.getValue() == null) {
      errorOccurred(layoutModel, "ScaleX property does not exist.");
      return false; //Return because the next if would be a NPE
    }
    double scaleX = 0;
    try {
      scaleX = Double.parseDouble(scaleXProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(layoutModel, "ScaleX property is not a valid double: {}.", scaleXProperty.getValue());
      valid = false;
    }
    if (scaleX < 0) {
      errorOccurred(layoutModel, "ScaleX property {} is < 0.", scaleXProperty.getValue());
      valid = false;
    }

    //validate the y-scale in the model view
    LengthProperty scaleYProperty = (LengthProperty) layoutModel.getProperty(LayoutModel.SCALE_Y);
    if (scaleYProperty == null || scaleYProperty.getValue() == null) {
      errorOccurred(layoutModel, "ScaleY property does not exist.");
      return false; //Return because the next if would be a NPE
    }
    double scaleY = 0;
    try {
      scaleY = Double.parseDouble(scaleYProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(layoutModel, "ScaleY property is not a valid double: {}.", scaleYProperty.getValue());
      valid = false;
    }
    if (scaleY < 0) {
      errorOccurred(layoutModel, "ScaleY property {} is < 0.", scaleYProperty.getValue());
      valid = false;
    }

    //Validate the location theme in the model view
    LocationThemeProperty locThemeProperty = (LocationThemeProperty) layoutModel.getProperty(LayoutModel.LOCATION_THEME);
    if (locThemeProperty == null) {
      errorOccurred(layoutModel, "Location theme does not exist.");
      valid = false;
    }

    //Validate the vehicle theme in the model view
    VehicleThemeProperty vehicleThemeProperty = (VehicleThemeProperty) layoutModel.getProperty(LayoutModel.VEHICLE_THEME);
    if (vehicleThemeProperty == null) {
      errorOccurred(layoutModel, "Vehicle theme does not exist.");
      valid = false;
    }
    return valid;
  }

  /**
   * Validates the properties of a point model.
   *
   * @param model the system model to validate against
   * @param point the point model to validate
   * @return true if the point model is valid, false otherwise
   */
  private boolean validatePoint(SystemModel model, PointModel point) {
    boolean valid = true;
    //Validate the vehicle orientation angle
    AngleProperty orientationProperty = (AngleProperty) point.getProperty(PointModel.VEHICLE_ORIENTATION_ANGLE);
    if (orientationProperty == null || orientationProperty.getValue() == null) {
      errorOccurred(point, "Vehicle orientation angle does not exist or null.");
      return false; //Return because the next if would be a NPE
    }
    double angle = 0;
    try {
      angle = Double.parseDouble(orientationProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(point, "Orientation angle property is not a valid double: {}.", orientationProperty.getValue());
      valid = false;
    }
    if (angle < 0) {
      errorOccurred(point, "Negative vehicle orientation angle {} not allowed. Transformed to "
                    + "positive.", angle);
      angle = 360 + angle % 360;
      orientationProperty.setValue(angle);
    }
    //Validate the point type
    SelectionProperty<PointModel.PointType> typeProperty
        = (SelectionProperty<PointModel.PointType>) point.getProperty(PointModel.TYPE);
    if (typeProperty == null || typeProperty.getComparableValue() == null) {
      errorOccurred(point, "Point type property does not exist.");
      return false; //Return because the next if would be a NPE
    }

    boolean found = false;
    for (PointModel.PointType type : PointModel.PointType.values()) {
      if (typeProperty.getValue().equals(type)) {
        found = true;
      }
    }
    if (!found) {
      errorOccurred(point, "Invalid type of point \"{}\".", typeProperty.getComparableValue());
      valid = false;
    }

    //Validate the x position in the model view
    CoordinateProperty xCoordProperty = (CoordinateProperty) point.getProperty(PointModel.MODEL_X_POSITION);
    if (xCoordProperty == null || xCoordProperty.getValue().toString().equals("")) {
      errorOccurred(point, "The x value of the model position does not exist.");
      valid = false;
    }

    //Validate the y position in the model view
    CoordinateProperty yCoordProperty = (CoordinateProperty) point.getProperty(PointModel.MODEL_Y_POSITION);
    if (yCoordProperty == null
        || yCoordProperty.getValue() == null
        || yCoordProperty.getValue().toString().equals("")) {
      errorOccurred(point, "The y value of the model position does not exist.");
      valid = false;
    }

    //Validate the x position in the course model
    StringProperty xPosProperty = (StringProperty) point.getProperty(ElementPropKeys.POINT_POS_X);
    if (xPosProperty == null || xPosProperty.getText() == null || xPosProperty.getText().equals("")) {
      errorOccurred(point, "The x value of the point position does not exist.");
      valid = false;
    }

    //Validate the y position in the course model
    StringProperty yPosProperty = (StringProperty) point.getProperty(ElementPropKeys.POINT_POS_Y);
    if (yPosProperty == null || yPosProperty.getText() == null || yPosProperty.getText().equals("")) {
      errorOccurred(point, "The y value of the point position does not exist.");
      valid = false;
    }

    return valid;
  }

  /**
   * Validates the properties of a path model.
   *
   * @param model the system model to validate against
   * @param path the path model to validate
   * @return true if the path model is valid, false otherwise
   */
  private boolean validatePath(SystemModel model, PathModel path) {
    boolean valid = true;
    //Validate the length of the path
    LengthProperty lengthProperty = (LengthProperty) path.getProperty(PathModel.LENGTH);
    if (lengthProperty == null || lengthProperty.getValue() == null) {
      errorOccurred(path, "Path length does not exist.");
      return false; //Return because the next if would be a NPE
    }
    double length = 0;
    try {
      length = Double.parseDouble(lengthProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(path, "Path length property is not a valid double: {}.", lengthProperty.getValue());
      valid = false;
    }
    if (length < 0) {
      errorOccurred(path, "Invalid path length {}.", length);
      valid = false;
    }

    //Validate the routing costs
    IntegerProperty costProperty = (IntegerProperty) path.getProperty(PathModel.ROUTING_COST);
    if (costProperty == null || costProperty.getValue() == null) {
      errorOccurred(path, "Path routing cost does not exist.");
      return false; //Return because the next if would be a NPE
    }
    int costs = 0;
    try {
      costs = Integer.parseInt(costProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(path, "Routing costs property is not a valid double: {}.", lengthProperty.getValue());
      valid = false;
    }
    if (costs < 0) {
      errorOccurred(path, "Invalid routing costs {}.", costs);
      valid = false;
    }

    //Validate the max velocity
    SpeedProperty maxVelocityProperty = (SpeedProperty) path.getProperty(PathModel.MAX_VELOCITY);
    if (maxVelocityProperty == null || maxVelocityProperty.getValue() == null) {
      errorOccurred(path, "Path max velocity does not exist.");
      return false; //Return because the next if would be a NPE
    }
    double maxVelocity = 0;
    try {
      maxVelocity = Double.parseDouble(maxVelocityProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(path, "Max velocity property is not a valid double: {}.", maxVelocityProperty.getValue());
      valid = false;
    }
    if (maxVelocity < 0) {
      errorOccurred(path, "Invalid max velocity {}.", maxVelocity);
      valid = false;
    }

    //Validate the max reverse velocity
    SpeedProperty maxRevVelocityProperty = (SpeedProperty) path.getProperty(PathModel.MAX_REVERSE_VELOCITY);
    if (maxRevVelocityProperty == null || maxRevVelocityProperty.getValue() == null) {
      errorOccurred(path, "Path max velocity does not exist.");
      return false; //Return because the next if would be a NPE
    }

    double maxRevVelocity = 0;
    try {
      maxRevVelocity = Double.parseDouble(maxRevVelocityProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(path, "Max velocity property is not a valid double: {}.", maxRevVelocityProperty.getValue());
      valid = false;
    }
    if (maxRevVelocity < 0) {
      errorOccurred(path, "Invalid max reverse velocity {}.", maxRevVelocity);
      valid = false;
    }

    //Validate the connection type of this path
    SelectionProperty typeProperty = (SelectionProperty) path.getProperty(ElementPropKeys.PATH_CONN_TYPE);
    if (typeProperty == null) {
      errorOccurred(path, "Path connection type does not exist.");
      return false; //Return because the next if would be a NPE
    }
    boolean found = false;
    for (PathModel.LinerType type : PathModel.LinerType.values()) {
      if (type.equals(typeProperty.getValue())) {
        found = true;
      }
    }
    //Record error if property value is not present in the enum
    if (!found) {
      errorOccurred(path, "Invalid type of path \"{}\".", typeProperty.getComparableValue());
      valid = false;
    }

    //Validate the control points of the path
    StringProperty controlPointsProperty = (StringProperty) path.getProperty(ElementPropKeys.PATH_CONTROL_POINTS);
    if (controlPointsProperty == null) {
      errorOccurred(path, "Path control points do not exist.");
      return false; //Return because the next if would be a NPE
    }

    if ((typeProperty.getValue().equals(PathModel.LinerType.BEZIER)
         || typeProperty.getValue().equals(PathModel.LinerType.BEZIER_3))
        && (controlPointsProperty.getText() == null || controlPointsProperty.getText().equals(""))) {
      errorOccurred(path, "Invalid path control points for bezier curve \"{}\".", controlPointsProperty.getText());
      valid = false;
    }

    //Validate the start component of this path
    StringProperty startProperty = (StringProperty) path.getProperty(PathModel.START_COMPONENT);
    if (startProperty == null) {
      errorOccurred(path, "Start component does not exist.");
      return false; //Return because the next if would be a NPE
    }

    if (startProperty.getText() == null
        || startProperty.getText().equals("")) {
      errorOccurred(path, "Invalid start component name \"{}\".", startProperty.getText());
      valid = false;
    }

    if (!nameExists(model, startProperty.getText())) {
      errorOccurred(model, "Start component \"{}\" does not exist.", startProperty.getText());
      valid = false;
    }

    //Validate the end component of this path
    StringProperty endProperty = (StringProperty) path.getProperty(PathModel.END_COMPONENT);
    if (endProperty == null) {
      errorOccurred(path, "End component does not exist.");
      return false; //Return because the next if would be a NPE
    }

    if (endProperty.getText() == null
        || endProperty.getText().equals("")) {
      errorOccurred(path, "Invalid end component name \"{}\".", endProperty.getText());
      valid = false;
    }

    if (!nameExists(model, endProperty.getText())) {
      errorOccurred(model, "End component \"{}\" does not exist.", endProperty.getText());
      valid = false;
    }

    //Validate the existence of the locked property
    BooleanProperty lockedProperty = (BooleanProperty) path.getProperty(PathModel.LOCKED);
    if (lockedProperty == null) {
      errorOccurred(path, "Locked property does not exist.");
      valid = false; //Return because the next if would be a NPE
    }
    return valid;
  }

  /**
   * Validates the properties of a location type model
   *
   * @param model the system model to validate against
   * @param locationType the location type model to validate
   * @return true if the location type model is valid, false otherwise
   */
  private boolean validateLocationType(SystemModel model, LocationTypeModel locationType) {
    boolean valid = true;

    //Validate the allowed operations for the location type
    StringSetProperty allowedOperationsProperty
        = (StringSetProperty) locationType.getProperty(LocationTypeModel.ALLOWED_OPERATIONS);

    if (allowedOperationsProperty == null) {
      errorOccurred(locationType, "Allowed operations does not exist.");
      valid = false; //Return because the next if would be a NPE
    }

    return valid;
  }

  /**
   * Validates the properties of a location model
   *
   * @param model the system model to validate against
   * @param location the location model to validate
   * @return true if the location model is valid, false otherwise
   */
  private boolean validateLocation(SystemModel model, LocationModel location) {
    boolean valid = true;

    //Validate the x position in the model view
    CoordinateProperty xCoordProperty = (CoordinateProperty) location.getProperty(LocationModel.MODEL_X_POSITION);
    if (xCoordProperty == null) {
      errorOccurred(location, "The x value of the model position does not exist.");
      return false;
    }
    try {
      Double.parseDouble(xCoordProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(location, "The x value of the model position is not an integer.");
      valid = false;
    }

    //Validate the y position in the model view
    CoordinateProperty yCoordProperty = (CoordinateProperty) location.getProperty(LocationModel.MODEL_Y_POSITION);
    if (yCoordProperty == null) {
      errorOccurred(location, "The y value of the model position does not exist.");
      return false;
    }
    try {
      Double.parseDouble(yCoordProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(location, "The y value of the model position is not an integer.");
      valid = false;
    }

    //Validate the x position in the course model
    StringProperty xPosProperty = (StringProperty) location.getProperty(ElementPropKeys.LOC_POS_X);
    if (xPosProperty == null || xPosProperty.getText() == null) {
      errorOccurred(location, "The x value of the location position does not exist.");
      return false;
    }
    try {
      Integer.parseInt(xPosProperty.getText().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(location, "The x value of the location position is not an integer.");
      valid = false;
    }

    //Validate the y position in the course model
    StringProperty yPosProperty = (StringProperty) location.getProperty(ElementPropKeys.LOC_POS_Y);
    if (yPosProperty == null || yPosProperty.getText() == null) {
      errorOccurred(location, "The y value of the location position does not exist.");
      return false;
    }
    try {
      Integer.parseInt(yPosProperty.getText().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(location, "The y value of the location position is not an integer.");
      valid = false;
    }

    //Validate the location type
    boolean found = false;
    LocationTypeProperty locTypeProperty = (LocationTypeProperty) location.getProperty(LocationModel.TYPE);
    if (locTypeProperty == null) {
      errorOccurred(location, "The location type does not exist.");
      return false; //Return because the next if would be a NPE
    }
    for (LocationTypeModel type : model.getLocationTypeModels()) {
      if (locTypeProperty.getValue().equals(type.getName())) {
        found = true;
        break; // Skip searching all elements if we already found the correct one
      }
    }

    //Record error if location type is not present in the system model
    if (!found) {
      errorOccurred(location, "Invalid type of location \"{}\" - not found.", locTypeProperty.getValue());
      valid = false;
    }

    //Validate the labels offset x-coordinate for the model view
    StringProperty labelOffsetXProperty
        = (StringProperty) location.getProperty(ElementPropKeys.LOC_LABEL_OFFSET_X);
    if (labelOffsetXProperty == null) {
      errorOccurred(location, "The x value of the location label offset does not exist.");
      return false;
    }

    //Validate the labels offset y-coordinate for the model view
    StringProperty labelOffsetYProperty
        = (StringProperty) location.getProperty(ElementPropKeys.LOC_LABEL_OFFSET_Y);
    if (labelOffsetYProperty == null) {
      errorOccurred(location, "The y value of the location label offset does not exist.");
      return false;
    }

    //Validate the label orientation angle for the model view
    StringProperty labelOrientationAngleProperty
        = (StringProperty) location.getProperty(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);
    if (labelOrientationAngleProperty == null) {
      errorOccurred(location, "The orientation angle for the label does not exist.");
      valid = false;
    }

    return valid;
  }

  /**
   * Validates the properties of a link model.
   *
   * @param model the system model to validate against
   * @param link the link model to validate
   * @return true if the link model is valid, false otherwise
   */
  private boolean validateLink(SystemModel model, LinkModel link) {
    boolean valid = true;

    //Validate the start component of the link
    StringProperty startProperty = (StringProperty) link.getProperty(LinkModel.START_COMPONENT);
    if (startProperty == null) {
      errorOccurred(link, "Start component does not exist.");
      return false; //Return because the next if would be a NPE
    }
    if (startProperty.getText() == null
        || startProperty.getText().equals("")) {
      errorOccurred(link, "Invalid start component name \"{}\".", startProperty.getText());
      valid = false;
    }

    //Validate the end component of the link
    StringProperty endProperty = (StringProperty) link.getProperty(LinkModel.END_COMPONENT);
    if (endProperty == null) {
      errorOccurred(link, "End component does not exist.");
      return false; //Return because the next if would be a NPE
    }
    if (endProperty.getText() == null
        || endProperty.getText().equals("")) {
      errorOccurred(link, "Invalid end component name \"{}\".", endProperty.getText());
      valid = false;
    }
    if (!valid) {
      return valid;
    }
    //Validate whether the start component exists
    if (!nameExists(model, startProperty.getText())) {
      errorOccurred(link, "Start component \"{}\" does not exist.", startProperty.getModel().getName());
      valid = false;
    }
    //Validate whether the point exists
    if (!nameExists(model, endProperty.getText())) {
      errorOccurred(link, "End component \"{}\" does not exist.", endProperty.getModel().getName());
      valid = false;
    }
    return valid;
  }

  /**
   * Validates the properties of a block model.
   *
   * @param model the system model to validate against
   * @param block the block model to validate
   * @return true if the block model is valid, false otherwise
   */
  private boolean validateBlock(SystemModel model, BlockModel block) {
    boolean valid = true;

    //Validate that all members of the block exists
    StringSetProperty elementsProperty = (StringSetProperty) block.getProperty(BlockModel.ELEMENTS);
    if (elementsProperty == null) {
      errorOccurred(model, "Elements property for the block does not exist.");
      return false; //Return because the next if would be a NPE
    }

    Set<String> elements = new HashSet<>();
    for (String element : elementsProperty.getItems()) {
      if (elements.contains(element)) {
        errorOccurred(model, "Block element \"{}\" is listed multiple times.", element);
        valid = false;
      }
      elements.add(element);
      if (!nameExists(model, element)) {
        errorOccurred(model, "Block element \"{}\" does not exist.", element);
        valid = false;
      }
    }

    return valid;
  }

  /**
   * Validates the properties of a group model.
   *
   * @param model the system model to validate against
   * @param group the group model to validate
   * @return true if the group model is valid, false otherwise
   */
  private boolean validateGroup(SystemModel model, GroupModel group) {
    boolean valid = true;

    //Validate that all elements of the group exists
    StringSetProperty elementsProperty = (StringSetProperty) group.getProperty(GroupModel.ELEMENTS);
    if (elementsProperty == null) {
      errorOccurred(model, "Elements property for the group does not exist.");
      return false; //Return because the next if would be a NPE
    }
    Set<String> elements = new HashSet<>();
    for (String element : elementsProperty.getItems()) {
      if (elements.contains(element)) {
        errorOccurred(model, "Group element \"{}\" is listed multiple times.", element);
        valid = false;
      }
      elements.add(element);
      if (!nameExists(model, element)) {
        errorOccurred(model, "Group element \"{}\" does not exist.", element);
        valid = false;
      }
    }

    return valid;
  }

  /**
   * Validates the properties of a static route model.
   *
   * @param model the system model to validate against
   * @param staticRoute the static route model to validate
   * @return true if the static route model is valid, false otherwise
   */
  private boolean validateStaticRoute(SystemModel model, StaticRouteModel staticRoute) {
    boolean valid = true;

    //Validate that all elements of the static route exists
    StringSetProperty elementsProperty = (StringSetProperty) staticRoute.getProperty(StaticRouteModel.ELEMENTS);
    if (elementsProperty == null) {
      errorOccurred(model, "Elements property for the static route does not exist.");
      return false; //Return because the next if would be a NPE
    }
    Set<String> elements = new HashSet<>();
    for (String element : elementsProperty.getItems()) {
      if (elements.contains(element)) {
        errorOccurred(model, "Static route element \"{}\" is listed multiple times.", element);
        valid = false;
      }
      elements.add(element);
      if (!nameExists(model, element)) {
        errorOccurred(model, "Static route element \"{}\" does not exist.", element);
        valid = false;
      }
    }

    return valid;
  }

  /**
   * Validates the properties of a vehicle model.
   *
   * @param model the system model to validate against
   * @param vehicle the vehicle model to validate
   * @return true if the vehicle model is valid, false otherwise
   */
  private boolean validateVehicle(SystemModel model, VehicleModel vehicle) {
    boolean valid = true;

    //Validate that all properties needed exist
    LengthProperty lengthProperty = (LengthProperty) vehicle.getProperty(VehicleModel.LENGTH);
    if (lengthProperty == null) {
      errorOccurred(model, "Length property does not exist.");
      valid = false;
    }

    PercentProperty energyCriticalProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL_CRITICAL);
    if (energyCriticalProperty == null) {
      errorOccurred(model, "Energy level critical property does not exist.");
      valid = false;
    }

    PercentProperty energyGoodProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL_GOOD);
    if (energyGoodProperty == null) {
      errorOccurred(model, "Energy level good property does not exist.");
      valid = false;
    }

    CoursePointProperty initialPositionProperty
        = (CoursePointProperty) vehicle.getProperty(VehicleModel.INITIAL_POSITION);
    if (initialPositionProperty == null) {
      errorOccurred(model, "Initial position property does not exist.");
      valid = false;
    }

    PercentProperty energyLevelProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL);
    if (energyLevelProperty == null) {
      errorOccurred(model, "Energy level property does not exist.");
      valid = false;
    }

    SelectionProperty<VehicleModel.EnergyState> energyStateProperty
        = (SelectionProperty<VehicleModel.EnergyState>) vehicle.getProperty(VehicleModel.ENERGY_STATE);
    if (energyStateProperty == null) {
      errorOccurred(model, "Energy state property does not exist.");
      valid = false;
    }

    BooleanProperty loadedProperty = (BooleanProperty) vehicle.getProperty(VehicleModel.LOADED);
    if (loadedProperty == null) {
      errorOccurred(model, "Loaded property does not exist.");
      valid = false;
    }

    SelectionProperty<Vehicle.State> stateProperty
        = (SelectionProperty<Vehicle.State>) vehicle.getProperty(VehicleModel.STATE);
    if (stateProperty == null) {
      errorOccurred(model, "State property does not exist.");
      valid = false;
    }

    SelectionProperty<Vehicle.ProcState> procStateproperty
        = (SelectionProperty<Vehicle.ProcState>) vehicle.getProperty(VehicleModel.PROC_STATE);
    if (procStateproperty == null) {
      errorOccurred(model, "Processing state property does not exist.");
      valid = false;
    }

    StringProperty currentPointProperty = (StringProperty) vehicle.getProperty(VehicleModel.POINT);
    if (currentPointProperty == null) {
      errorOccurred(model, "Current point property does not exist.");
      valid = false;
    }

    StringProperty nextPointProperty = (StringProperty) vehicle.getProperty(VehicleModel.NEXT_POINT);
    if (nextPointProperty == null) {
      errorOccurred(model, "Next point property does not exist.");
      valid = false;
    }

    TripleProperty precisePositionProperty = (TripleProperty) vehicle.getProperty(VehicleModel.PRECISE_POSITION);
    if (precisePositionProperty == null) {
      errorOccurred(model, "Precise position property does not exist.");
      valid = false;
    }

    AngleProperty orientationProperty = (AngleProperty) vehicle.getProperty(VehicleModel.ORIENTATION_ANGLE);
    if (orientationProperty == null) {
      errorOccurred(model, "Orientation angle property does not exist.");
      valid = false;
    }

    //If some required properties do not exist, return to avoid NPE for semantic validation
    if (!valid) {
      return valid;
    }

    //Validate the length
    if (((Double) lengthProperty.getValue()) < 0) {
      errorOccurred(model, "Length property {} has to be >= 0.", lengthProperty.getValue());
      valid = false;
    }

    //Validate the critical energy level
    if (((int) energyCriticalProperty.getValue()) < 0
        || ((int) energyCriticalProperty.getValue()) > 100) {
      errorOccurred(model, "Energy level critical has to be in range of [0..100] - currently {}",
                    energyCriticalProperty.getValue());
      valid = false;
    }

    //Validate the good energy level
    if (((int) energyGoodProperty.getValue()) < 0
        || ((int) energyGoodProperty.getValue()) > 100) {
      errorOccurred(model, "Energy level good has to be in range of [0..100] - currently {}",
                    energyGoodProperty.getValue());
      valid = false;
    }

    //Validate that the good energy level is greater equals than the critical energy level
    if (((int) energyGoodProperty.getValue()) < ((int) energyCriticalProperty.getValue())) {
      errorOccurred(model, "Energy level good(\"{}\") has to be >= energy level critical(\"{}\").",
                    energyGoodProperty.getValue(),
                    energyCriticalProperty.getValue());
      valid = false;
    }

    //Validate the current energy level
    if (((int) energyLevelProperty.getValue()) < 0
        || ((int) energyLevelProperty.getValue()) > 100) {
      errorOccurred(model, "Energy level has to be in range of [0..100] - currently {}",
                    energyLevelProperty.getValue());
      valid = false;
    }

    //Validate that the energy state is a valid/known value
    boolean found = false;
    for (VehicleModel.EnergyState energyState : VehicleModel.EnergyState.values()) {
      if (energyState.equals(energyStateProperty.getValue())) {
        found = true;
        break;
      }
    }
    if (!found) {
      errorOccurred(model, "Unknown energy state {}.", energyStateProperty.getValue());
      valid = false;
    }

    //Validate that the processing state is a valid/known value
    found = false;
    for (Vehicle.ProcState procState : Vehicle.ProcState.values()) {
      if (procState.equals(procStateproperty.getValue())) {
        found = true;
        break;
      }
    }
    if (!found) {
      errorOccurred(model, "Unknown processing state {}.", procStateproperty.getValue());
      valid = false;
    }

    //Validate the precise position happens in the property converter
    if (((Double) orientationProperty.getValue()) < 0) {
      errorOccurred(model, "Orientation angle has to be >= 0 - currently {}.",
                    orientationProperty.getValue());
      valid = false;
    }
    return valid;
  }

  /**
   * Checks whether the given property exists in the model component.
   * //TODO: Use this method to validate all required properties
   *
   * @param propertyName the name of the property
   * @param component the model component
   * @return true if the property exists, false otherwise
   */
  private boolean checkPropertyExists(String propertyName, ModelComponent component) {
    Property property = component.getProperty(propertyName);
    if (property == null) {
      errorOccurred(component, "{} property does not exist.", propertyName);
      return false;
    }
    else {
      return true;
    }
  }

  /**
   * Checks whether the name of the component is already present in the system model and the object
   * is not equals to the component to check.
   *
   * @param model the system model
   * @param component the component
   * @return true if the name is present, false otherwise
   */
  private boolean nameExists(SystemModel model, ModelComponent component) {
    if (Strings.isNullOrEmpty(component.getName())) {
      return false;
    }
    ModelComponent foundComponent = model.getModelComponent(component.getName());
    return foundComponent != null && foundComponent != component;
  }

  /**
   * Checks whether the name of the component is already present in the system model.
   *
   * @param model the system model
   * @param name the component name
   * @return true if the name is present, false otherwise
   */
  private boolean nameExists(SystemModel model, String name) {
    if (Strings.isNullOrEmpty(name)) {
      return false;
    }
    return model.getModelComponent(name) != null;
  }
}
