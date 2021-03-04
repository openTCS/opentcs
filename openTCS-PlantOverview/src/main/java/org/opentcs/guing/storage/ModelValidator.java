/**
 * Copyright (c) 2016 Fraunhofer IML
 */
package org.opentcs.guing.storage;

import com.google.common.base.Strings;
import static com.google.common.base.Strings.isNullOrEmpty;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.properties.type.AbstractProperty;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.components.properties.type.TripleProperty;
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
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator for a {@link SystemModel} and its {@link ModelComponent}s.
 * Validates if the model component can safely be added to a system model.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class ModelValidator {

  /**
   * This class' resource bundle.
   */
  private final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

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
  @Inject
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
      errorOccurred(model, "ModelValidator.error.nullModel");
      return false;
    }
    if (component == null) {
      errorOccurred(component, "ModelValidator.error.nullComponent");
      return false;
    }
    //Validate the name of the component
    if (Strings.isNullOrEmpty(component.getName())) {
      errorOccurred(component, "ModelValidator.error.invalidComponentName", component.getName());
      return false;
    }
    if (nameExists(model, component)) {
      errorOccurred(component, "ModelValidator.error.componentNameExists", component.getName());
      return false;
    }
    //Validate the miscellaneous property of the component
    //TODO: Seems to be optional in some models?!
    KeyValueSetProperty miscellaneous
        = (KeyValueSetProperty) component.getProperty(ModelComponent.MISCELLANEOUS);
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
   * @param bundleKey the bundle key with the error description
   * @param args a list of arguments for the error description
   */
  private void errorOccurred(ModelComponent component, String bundleKey, Object... args) {
    String componentName = component == null ? "null" : component.getName();
    String message = componentName + ": " + bundle.getFormatted(bundleKey, args);
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
      errorOccurred(layoutModel, "ModelValidator.error.layout.scaleXMissing");
      return false; //Return because the next if would be a NPE
    }
    double scaleX = 0;
    try {
      scaleX = Double.parseDouble(scaleXProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(layoutModel, "ModelValidator.error.layout.scaleXInvalid",
                    scaleXProperty.getValue());
      valid = false;
    }
    if (scaleX < 0) {
      errorOccurred(layoutModel, "ModelValidator.error.layout.scaleXNegative",
                    scaleXProperty.getValue());
      valid = false;
    }

    //validate the y-scale in the model view
    LengthProperty scaleYProperty = (LengthProperty) layoutModel.getProperty(LayoutModel.SCALE_Y);
    if (scaleYProperty == null || scaleYProperty.getValue() == null) {
      errorOccurred(layoutModel, "ModelValidator.error.layout.scaleYMissing");
      return false; //Return because the next if would be a NPE
    }
    double scaleY = 0;
    try {
      scaleY = Double.parseDouble(scaleYProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(layoutModel, "ModelValidator.error.layout.scaleYInvalid",
                    scaleYProperty.getValue());
      valid = false;
    }
    if (scaleY < 0) {
      errorOccurred(layoutModel, "ModelValidator.error.layout.scaleYNegative",
                    scaleYProperty.getValue());
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
    AngleProperty orientationProperty
        = (AngleProperty) point.getProperty(PointModel.VEHICLE_ORIENTATION_ANGLE);
    if (orientationProperty == null || orientationProperty.getValue() == null) {
      errorOccurred(point, "ModelValidator.error.point.nullVehicleOrientation");
      return false; //Return because the next if would be a NPE
    }

    if (!(orientationProperty.getValue() instanceof Double)) {
      LOG.warn("{}: Orientation angle property ('{}') is not a number. Setting it to 0.",
               point.getName(),
               orientationProperty.getValue());
      orientationProperty.setValueAndUnit(0, AngleProperty.Unit.DEG);
    }
    else {
      Double angle = (Double) orientationProperty.getValue();
      if (angle < 0) {
        LOG.warn("{}: Orientation angle property is {} but has to be > 0."
            + " Transforming to positive angle.",
                 point.getName(),
                 orientationProperty.getValue());
        orientationProperty.setValueAndUnit(360 + angle % 360, AngleProperty.Unit.DEG);
      }
    }
    point.setProperty(PointModel.VEHICLE_ORIENTATION_ANGLE, orientationProperty);

    //Validate the point type
    AbstractProperty typeProperty = (AbstractProperty) point.getProperty(PointModel.TYPE);
    if (typeProperty == null || typeProperty.getComparableValue() == null) {
      errorOccurred(point, "ModelValidator.error.point.nullType");
      return false; //Return because the next if would be a NPE
    }

    boolean found = false;
    for (PointModel.PointType type : PointModel.PointType.values()) {
      if (typeProperty.getValue().equals(type)) {
        found = true;
      }
    }
    if (!found) {
      errorOccurred(point, "ModelValidator.error.point.invalidType",
                    typeProperty.getComparableValue());
      valid = false;
    }

    //Validate the x position in the model view
    CoordinateProperty xCoordProperty
        = (CoordinateProperty) point.getProperty(PointModel.MODEL_X_POSITION);
    if (xCoordProperty == null || xCoordProperty.getValue().toString().equals("")) {
      errorOccurred(point, "ModelValidator.error.point.missingModelX");
      valid = false;
    }

    //Validate the y position in the model view
    CoordinateProperty yCoordProperty
        = (CoordinateProperty) point.getProperty(PointModel.MODEL_Y_POSITION);
    if (yCoordProperty == null
        || yCoordProperty.getValue() == null
        || yCoordProperty.getValue().toString().equals("")) {
      errorOccurred(point, "ModelValidator.error.point.missingModelY");
      valid = false;
    }

    //Validate the x position in the course model
    StringProperty xPosProperty = (StringProperty) point.getProperty(ElementPropKeys.POINT_POS_X);
    if (xPosProperty == null
        || xPosProperty.getText() == null
        || xPosProperty.getText().equals("")) {
      errorOccurred(point, "ModelValidator.error.point.missingPositionX");
      valid = false;
    }

    //Validate the y position in the course model
    StringProperty yPosProperty = (StringProperty) point.getProperty(ElementPropKeys.POINT_POS_Y);
    if (yPosProperty == null || isNullOrEmpty(yPosProperty.getText())) {
      errorOccurred(point, "ModelValidator.error.point.missingPositionY");
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
      errorOccurred(path, "ModelValidator.error.path.nullLength");
      return false; //Return because the next if would be a NPE
    }

    //Validate the routing costs
    IntegerProperty costProperty = (IntegerProperty) path.getProperty(PathModel.ROUTING_COST);
    if (costProperty == null || costProperty.getValue() == null) {
      errorOccurred(path, "ModelValidator.error.path.nullRoutingCost");
      return false; //Return because the next if would be a NPE
    }

    //Validate the max velocity
    SpeedProperty maxVelocityProperty = (SpeedProperty) path.getProperty(PathModel.MAX_VELOCITY);
    if (maxVelocityProperty == null || maxVelocityProperty.getValue() == null) {
      errorOccurred(path, "ModelValidator.error.path.nullMaxVelocity");
      return false; //Return because the next if would be a NPE
    }

    //Validate the max reverse velocity
    SpeedProperty maxRevVelocityProperty
        = (SpeedProperty) path.getProperty(PathModel.MAX_REVERSE_VELOCITY);
    if (maxRevVelocityProperty == null || maxRevVelocityProperty.getValue() == null) {
      errorOccurred(path, "ModelValidator.error.path.nullMaxReverseVelocity");
      return false; //Return because the next if would be a NPE
    }

    //Validate the connection type of this path
    AbstractProperty typeProperty
        = (AbstractProperty) path.getProperty(ElementPropKeys.PATH_CONN_TYPE);
    if (typeProperty == null) {
      errorOccurred(path, "ModelValidator.error.path.nullConnectionType");
      return false; //Return because the next if would be a NPE
    }

    //Validate the control points of the path
    StringProperty controlPointsProperty
        = (StringProperty) path.getProperty(ElementPropKeys.PATH_CONTROL_POINTS);
    if (controlPointsProperty == null) {
      errorOccurred(path, "ModelValidator.error.path.nullControlPoints");
      return false; //Return because the next if would be a NPE
    }

    if ((typeProperty.getValue().equals(PathModel.LinerType.BEZIER)
         || typeProperty.getValue().equals(PathModel.LinerType.BEZIER_3))
        && (isNullOrEmpty(controlPointsProperty.getText()))) {
      errorOccurred(path, "ModelValidator.error.path.invalidControlPoints",
                    controlPointsProperty.getText());
      valid = false;
    }

    //Validate the start component of this path
    StringProperty startProperty = (StringProperty) path.getProperty(PathModel.START_COMPONENT);
    if (startProperty == null) {
      errorOccurred(path, "ModelValidator.error.path.missingStartProperty");
      return false; //Return because the next if would be a NPE
    }

    if (isNullOrEmpty(startProperty.getText())) {
      errorOccurred(path, "ModelValidator.error.path.invalidStart", startProperty.getText());
      valid = false;
    }

    if (!nameExists(model, startProperty.getText())) {
      errorOccurred(model, "ModelValidator.error.path.notExistingStart", startProperty.getText());
      valid = false;
    }

    //Validate the end component of this path
    StringProperty endProperty = (StringProperty) path.getProperty(PathModel.END_COMPONENT);
    if (endProperty == null) {
      errorOccurred(path, "ModelValidator.error.path.missingEndProperty");
      return false; //Return because the next if would be a NPE
    }

    if (endProperty.getText() == null
        || endProperty.getText().equals("")) {
      errorOccurred(path, "ModelValidator.error.path.invalidEnd", endProperty.getText());
      valid = false;
    }

    if (!nameExists(model, endProperty.getText())) {
      errorOccurred(model, "ModelValidator.error.path.notExistingEnd", endProperty.getText());
      valid = false;
    }

    //Validate the existence of the locked property
    BooleanProperty lockedProperty = (BooleanProperty) path.getProperty(PathModel.LOCKED);
    if (lockedProperty == null) {
      errorOccurred(path, "ModelValidator.error.path.missingLockedProperty");
      valid = false; //Return because the next if would be a NPE
    }

    //If some required properties do not exist, return to avoid NPE for semantic validation
    if (!valid) {
      return valid;
    }

    //Validate the length
    if (((Double) lengthProperty.getValue()) < 1) {
      LOG.warn("{}: Length property is {} but has to be > 0. Setting it to 1.",
               path.getName(),
               lengthProperty.getValue());
      lengthProperty.setValueAndUnit(1, LengthProperty.Unit.MM);
      path.setProperty(PathModel.LENGTH, lengthProperty);
    }

    //Validate the maximum velocity
    if (((Double) maxVelocityProperty.getValue()) < 0) {
      LOG.warn("{}: Max. velocity property is {} but has to be >= 0. Setting it to 0.",
               path.getName(),
               maxVelocityProperty.getValue());
      maxVelocityProperty.setValueAndUnit(0, SpeedProperty.Unit.MM_S);
      path.setProperty(PathModel.MAX_VELOCITY, maxVelocityProperty);
    }

    //Validate the maximum reverse velocity
    if (((Double) maxRevVelocityProperty.getValue()) < 0) {
      LOG.warn("{}: Max. reverse velocity property is {} but has to be >= 0. Setting it to 0.",
               path.getName(),
               maxRevVelocityProperty.getValue());
      maxRevVelocityProperty.setValueAndUnit(0, SpeedProperty.Unit.MM_S);
      path.setProperty(PathModel.MAX_VELOCITY, maxRevVelocityProperty);
    }

    //Validate the connection type of this path
    boolean found = false;
    for (PathModel.LinerType type : PathModel.LinerType.values()) {
      if (type.equals(typeProperty.getValue())) {
        found = true;
      }
    }
    //Record error if property value is not present in the enum
    if (!found) {
      errorOccurred(path, "ModelValidator.error.path.invalidType",
                    typeProperty.getComparableValue());
      valid = false;
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
      errorOccurred(locationType, "ModelValidator.error.locationType.missingAllowedOperations");
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
    CoordinateProperty xCoordProperty
        = (CoordinateProperty) location.getProperty(LocationModel.MODEL_X_POSITION);
    if (xCoordProperty == null) {
      errorOccurred(location, "ModelValidator.error.location.missingModelX");
      return false;
    }
    try {
      Double.parseDouble(xCoordProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(location, "ModelValidator.error.location.invalidModelX",
                    xCoordProperty.getValue());
      valid = false;
    }

    //Validate the y position in the model view
    CoordinateProperty yCoordProperty
        = (CoordinateProperty) location.getProperty(LocationModel.MODEL_Y_POSITION);
    if (yCoordProperty == null) {
      errorOccurred(location, "ModelValidator.error.location.missingModelY");
      return false;
    }
    try {
      Double.parseDouble(yCoordProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(location, "ModelValidator.error.location.invalidModelY",
                    yCoordProperty.getValue());
      valid = false;
    }

    //Validate the x position in the course model
    StringProperty xPosProperty = (StringProperty) location.getProperty(ElementPropKeys.LOC_POS_X);
    if (xPosProperty == null || xPosProperty.getText() == null) {
      errorOccurred(location, "ModelValidator.error.location.missingPositionX");
      return false;
    }
    try {
      Integer.parseInt(xPosProperty.getText());
    }
    catch (NumberFormatException e) {
      errorOccurred(location, "ModelValidator.error.location.invalidPositionX",
                    xPosProperty.getText());
      valid = false;
    }

    //Validate the y position in the course model
    StringProperty yPosProperty = (StringProperty) location.getProperty(ElementPropKeys.LOC_POS_Y);
    if (yPosProperty == null || yPosProperty.getText() == null) {
      errorOccurred(location, "ModelValidator.error.location.missingPositionY");
      return false;
    }
    try {
      Integer.parseInt(yPosProperty.getText());
    }
    catch (NumberFormatException e) {
      errorOccurred(location,
                    "ModelValidator.error.location.invalidPositionY",
                    yPosProperty.getText());
      valid = false;
    }

    //Validate the location type
    boolean found = false;
    LocationTypeProperty locTypeProperty
        = (LocationTypeProperty) location.getProperty(LocationModel.TYPE);
    if (locTypeProperty == null) {
      errorOccurred(location, "ModelValidator.error.location.missingType");
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
      errorOccurred(location, "ModelValidator.error.location.invalidType",
                    locTypeProperty.getValue());
      valid = false;
    }

    //Validate the labels offset x-coordinate for the model view
    StringProperty labelOffsetXProperty
        = (StringProperty) location.getProperty(ElementPropKeys.LOC_LABEL_OFFSET_X);
    if (labelOffsetXProperty == null) {
      errorOccurred(location, "ModelValidator.error.location.missingLabelOffsetX");
      return false;
    }

    //Validate the labels offset y-coordinate for the model view
    StringProperty labelOffsetYProperty
        = (StringProperty) location.getProperty(ElementPropKeys.LOC_LABEL_OFFSET_Y);
    if (labelOffsetYProperty == null) {
      errorOccurred(location, "ModelValidator.error.location.missingLabelOffsetY");
      return false;
    }

    //Validate the label orientation angle for the model view
    StringProperty labelOrientationAngleProperty
        = (StringProperty) location.getProperty(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);
    if (labelOrientationAngleProperty == null) {
      errorOccurred(location, "ModelValidator.error.location.missingLabelOrientation");
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
      errorOccurred(link, "ModelValidator.error.link.missingStartProperty");
      return false; //Return because the next if would be a NPE
    }
    if (isNullOrEmpty(startProperty.getText())) {
      errorOccurred(link, "ModelValidator.error.link.invalidStartProperty",
                    startProperty.getText());
      valid = false;
    }

    //Validate the end component of the link
    StringProperty endProperty = (StringProperty) link.getProperty(LinkModel.END_COMPONENT);
    if (endProperty == null) {
      errorOccurred(link, "ModelValidator.error.link.missingEndProperty");
      return false; //Return because the next if would be a NPE
    }
    if (isNullOrEmpty(endProperty.getText())) {
      errorOccurred(link, "ModelValidator.error.link.invalidEndProperty", endProperty.getText());
      valid = false;
    }
    if (!valid) {
      return valid;
    }
    //Validate whether the start component exists
    if (!nameExists(model, startProperty.getText())) {
      errorOccurred(link, "ModelValidator.error.link.notExistingStart",
                    startProperty.getText());
      valid = false;
    }
    //Validate whether the point exists
    if (!nameExists(model, endProperty.getText())) {
      errorOccurred(link, "ModelValidator.error.link.notExistingEnd",
                    endProperty.getText());
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
      errorOccurred(block, "ModelValidator.error.block.nullElementsProperty");
      return false; //Return because the next if would be a NPE
    }

    Set<String> elements = new HashSet<>();
    for (String element : elementsProperty.getItems()) {
      if (elements.contains(element)) {
        errorOccurred(block, "ModelValidator.error.block.duplicateElement", element);
        valid = false;
      }
      elements.add(element);
      if (!nameExists(model, element)) {
        errorOccurred(block, "ModelValidator.error.block.notExistingElement", element);
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
      errorOccurred(group, "ModelValidator.error.group.nullElementsProperty");
      return false; //Return because the next if would be a NPE
    }
    Set<String> elements = new HashSet<>();
    for (String element : elementsProperty.getItems()) {
      if (elements.contains(element)) {
        errorOccurred(group, "ModelValidator.error.group.duplicateElement", element);
        valid = false;
      }
      elements.add(element);
      if (!nameExists(model, element)) {
        errorOccurred(group, "ModelValidator.error.group.notExistingElement", element);
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
    StringSetProperty elementsProperty
        = (StringSetProperty) staticRoute.getProperty(StaticRouteModel.ELEMENTS);
    if (elementsProperty == null) {
      errorOccurred(staticRoute, "ModelValidator.error.staticRoute.nullElementsProperty");
      return false; //Return because the next if would be a NPE
    }
    Set<String> elements = new HashSet<>();
    for (String element : elementsProperty.getItems()) {
      if (elements.contains(element)) {
        errorOccurred(staticRoute, "ModelValidator.error.staticRoute.duplicateElement", element);
        valid = false;
      }
      elements.add(element);
      if (!nameExists(model, element)) {
        errorOccurred(staticRoute, "ModelValidator.error.staticRoute.notExistingElement", element);
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
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingLengthProperty");
      valid = false;
    }

    PercentProperty energyCriticalProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL_CRITICAL);
    if (energyCriticalProperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingEnergyCritical");
      valid = false;
    }

    PercentProperty energyGoodProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL_GOOD);
    if (energyGoodProperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingEnergyGood");
      valid = false;
    }

    PercentProperty energyLevelProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL);
    if (energyLevelProperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingEnergyLevel");
      valid = false;
    }

    AbstractProperty energyStateProperty
        = (AbstractProperty) vehicle.getProperty(VehicleModel.ENERGY_STATE);
    if (energyStateProperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingEnergyState");
      valid = false;
    }

    BooleanProperty loadedProperty = (BooleanProperty) vehicle.getProperty(VehicleModel.LOADED);
    if (loadedProperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingLoaded");
      valid = false;
    }

    AbstractProperty stateProperty = (AbstractProperty) vehicle.getProperty(VehicleModel.STATE);
    if (stateProperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingState");
      valid = false;
    }

    AbstractProperty procStateproperty
        = (AbstractProperty) vehicle.getProperty(VehicleModel.PROC_STATE);
    if (procStateproperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingProcState");
      valid = false;
    }

    AbstractProperty integrationStateProperty
        = (AbstractProperty) vehicle.getProperty(VehicleModel.INTEGRATION_LEVEL);
    if (integrationStateProperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingIntegrationLevel");
      valid = false;
    }

    StringProperty currentPointProperty = (StringProperty) vehicle.getProperty(VehicleModel.POINT);
    if (currentPointProperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingCurrentPoint");
      valid = false;
    }

    StringProperty nextPointProperty
        = (StringProperty) vehicle.getProperty(VehicleModel.NEXT_POINT);
    if (nextPointProperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingNextPoint");
      valid = false;
    }

    TripleProperty precisePositionProperty
        = (TripleProperty) vehicle.getProperty(VehicleModel.PRECISE_POSITION);
    if (precisePositionProperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingPrecisePosition");
      valid = false;
    }

    AngleProperty orientationProperty
        = (AngleProperty) vehicle.getProperty(VehicleModel.ORIENTATION_ANGLE);
    if (orientationProperty == null) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.missingOrientation");
      valid = false;
    }

    //If some required properties do not exist, return to avoid NPE for semantic validation
    if (!valid) {
      return valid;
    }

    //Validate the length
    if (((Double) lengthProperty.getValue()) < 1) {
      LOG.warn("{}: Length property is {} but has to be > 0. Setting it to 1.",
               vehicle.getName(),
               lengthProperty.getValue());
      lengthProperty.setValueAndUnit(1, LengthProperty.Unit.MM);
      vehicle.setProperty(VehicleModel.LENGTH, lengthProperty);
    }

    //Validate the critical energy level
    if (((int) energyCriticalProperty.getValue()) < 0
        || ((int) energyCriticalProperty.getValue()) > 100) {
      LOG.warn("{}: Energy level critical is {} but has to be in range of [0..100]. Setting it to 0.",
               vehicle.getName(),
               energyCriticalProperty.getValue());
      energyCriticalProperty.setValueAndUnit(0, PercentProperty.Unit.PERCENT);
      vehicle.setProperty(VehicleModel.ENERGY_LEVEL_CRITICAL, energyCriticalProperty);
    }

    //Validate the good energy level
    if (((int) energyGoodProperty.getValue()) < 0 || ((int) energyGoodProperty.getValue()) > 100) {
      LOG.warn("{}: Energy level good is {} but has to be in range of [0..100]. Setting it to 100.",
               vehicle.getName(),
               energyGoodProperty.getValue());
      energyGoodProperty.setValueAndUnit(100, PercentProperty.Unit.PERCENT);
      vehicle.setProperty(VehicleModel.ENERGY_LEVEL_GOOD, energyGoodProperty);
    }

    //Validate that the good energy level is greater equals than the critical energy level
    if (((int) energyGoodProperty.getValue()) < ((int) energyCriticalProperty.getValue())) {
      LOG.warn("{}: Energy level good('{}') has to be >= energy level critical('{}'). Setting it to {}.",
               vehicle.getName(),
               energyGoodProperty.getValue(),
               energyCriticalProperty.getValue(),
               energyCriticalProperty.getValue());
      energyGoodProperty
          .setValueAndUnit(energyCriticalProperty.getValueByUnit(PercentProperty.Unit.PERCENT),
                           PercentProperty.Unit.PERCENT);
      vehicle.setProperty(VehicleModel.ENERGY_LEVEL_GOOD, energyGoodProperty);
    }

    //Validate the current energy level
    if (((int) energyLevelProperty.getValue()) < 0 || ((int) energyLevelProperty.getValue()) > 100) {
      LOG.warn("{}: Energy level is {} but has to be in range of [0..100]. Setting it to 50.");
      energyLevelProperty.setValueAndUnit(50, PercentProperty.Unit.PERCENT);
      vehicle.setProperty(VehicleModel.ENERGY_LEVEL, energyLevelProperty);
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
      errorOccurred(model, "ModelValidator.error.vehicle.invalidEnergyState",
                    energyStateProperty.getValue());
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
      errorOccurred(model, "ModelValidator.error.vehicle.invalidProcState",
                    procStateproperty.getValue());
      valid = false;
    }

    //Validate the precise position happens in the property converter
    if (((Double) orientationProperty.getValue()) < 0) {
      LOG.warn("{}: Orientation angle is {} but has to be >= 0. Setting it to 0.");
      orientationProperty.setValueAndUnit(0, AngleProperty.Unit.DEG);
      vehicle.setProperty(VehicleModel.ORIENTATION_ANGLE, orientationProperty);
    }
    //Validate whether the current point exists
    String currentPoint = currentPointProperty.getText();
    if (!isNullOrEmptyPoint(currentPoint) && !nameExists(model, currentPoint)) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.notExistingCurrentPoint",
                    currentPointProperty.getText());
      valid = false;
    }
    //Validate whether the next point exists
    String nextPoint = nextPointProperty.getText();
    if (!isNullOrEmptyPoint(nextPoint) && !nameExists(model, nextPoint)) {
      errorOccurred(vehicle, "ModelValidator.error.vehicle.notExistingNextPoint",
                    nextPointProperty.getText());
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

  /**
   * Returns true if the given name is null or empty or equals the string "null", because that's
   * a valid content in a model file for no point.
   *
   * @param name The point name
   * @return True if the given name is null or empty or equals the string "null"
   */
  private boolean isNullOrEmptyPoint(String name) {
    return isNullOrEmpty(name) || name.equals("null");
  }
}
