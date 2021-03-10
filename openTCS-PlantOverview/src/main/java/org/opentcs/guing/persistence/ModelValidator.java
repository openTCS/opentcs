/**
 * Copyright (c) 2016 Fraunhofer IML
 */
package org.opentcs.guing.persistence;

import com.google.common.base.Strings;
import static com.google.common.base.Strings.isNullOrEmpty;
import java.awt.Component;
import java.util.Collection;
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
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.TextAreaDialog;
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
  private final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.VALIDATOR_PATH);

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
      errorOccurred(model, "modelValidator.error_modelNull.text");
      return false;
    }
    if (component == null) {
      errorOccurred(component, "modelValidator.error_componentNull.text");
      return false;
    }
    //Validate the name of the component
    if (Strings.isNullOrEmpty(component.getName())) {
      errorOccurred(component, "modelValidator.error_componentNameInvalid.text", component.getName());
      return false;
    }
    if (nameExists(model, component)) {
      errorOccurred(component, "modelValidator.error_componentNameExists.text", component.getName());
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
    else if (component instanceof VehicleModel) {
      valid = validateVehicle(model, (VehicleModel) component);
    }
    else {
      LOG.warn("Unknown model component {} - skipping validation.", component.getClass());
    }
    return valid;
  }

  public void showLoadingValidationWarning(Component parent, Collection<String> content) {
    TextAreaDialog panel
        = new TextAreaDialog(parent,
                             true,
                             bundle.getString("modelValidator.dialog_validationWarning.message.loadingError"));
    panel.setContent(content);
    panel.setTitle(bundle.getString("modelValidator.dialog_validationWarning.title"));
    panel.setLocationRelativeTo(null);
    panel.setVisible(true);
  }

  public void showSavingValidationWarning(Component parent, Collection<String> content) {
    TextAreaDialog panel
        = new TextAreaDialog(parent,
                             true,
                             bundle.getString("modelValidator.dialog_validationWarning.message.savingError"));
    panel.setContent(content);
    panel.setTitle(bundle.getString("modelValidator.dialog_validationWarning.title"));
    panel.setLocationRelativeTo(null);
    panel.setVisible(true);
  }
  
  public String formatDeserializationErrors(ModelComponent component, Collection<String> errors) {
    return ResourceBundleUtil.getBundle(I18nPlantOverview.MISC_PATH)
          .getFormatted("modelValidator.error_deserialization.text",
                        component.getName(),
                        errors);
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
      errorOccurred(layoutModel, "modelValidator.error_layoutScaleXMissing.text");
      return false; //Return because the next if would be a NPE
    }
    double scaleX = 0;
    try {
      scaleX = Double.parseDouble(scaleXProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(layoutModel, "modelValidator.error_layoutScaleXInvalid.text",
                    scaleXProperty.getValue());
      valid = false;
    }
    if (scaleX < 0) {
      errorOccurred(layoutModel, "modelValidator.error_layoutScaleXNegative.text",
                    scaleXProperty.getValue());
      valid = false;
    }

    //validate the y-scale in the model view
    LengthProperty scaleYProperty = (LengthProperty) layoutModel.getProperty(LayoutModel.SCALE_Y);
    if (scaleYProperty == null || scaleYProperty.getValue() == null) {
      errorOccurred(layoutModel, "modelValidator.error_layoutScaleYMissing.text");
      return false; //Return because the next if would be a NPE
    }
    double scaleY = 0;
    try {
      scaleY = Double.parseDouble(scaleYProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(layoutModel, "modelValidator.error_layoutScaleYInvalid.text",
                    scaleYProperty.getValue());
      valid = false;
    }
    if (scaleY < 0) {
      errorOccurred(layoutModel, "modelValidator.error_layoutScaleYNegative.text",
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
      errorOccurred(point, "modelValidator.error_pointVehicleOrientationMissing.text");
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
      errorOccurred(point, "modelValidator.error_pointTypeMissing.text");
      return false; //Return because the next if would be a NPE
    }

    boolean found = false;
    for (PointModel.Type type : PointModel.Type.values()) {
      if (typeProperty.getValue().equals(type)) {
        found = true;
      }
    }
    if (!found) {
      errorOccurred(point, "modelValidator.error_pointTypeInvalid.text",
                    typeProperty.getComparableValue());
      valid = false;
    }

    //Validate the x position in the model view
    CoordinateProperty xCoordProperty
        = (CoordinateProperty) point.getProperty(PointModel.MODEL_X_POSITION);
    if (xCoordProperty == null || xCoordProperty.getValue().toString().equals("")) {
      errorOccurred(point, "modelValidator.error_pointModelPositionXMissing.text");
      valid = false;
    }

    //Validate the y position in the model view
    CoordinateProperty yCoordProperty
        = (CoordinateProperty) point.getProperty(PointModel.MODEL_Y_POSITION);
    if (yCoordProperty == null
        || yCoordProperty.getValue() == null
        || yCoordProperty.getValue().toString().equals("")) {
      errorOccurred(point, "modelValidator.error_pointModelPositionYMissing.text");
      valid = false;
    }

    //Validate the x position in the course model
    StringProperty xPosProperty = (StringProperty) point.getProperty(ElementPropKeys.POINT_POS_X);
    if (xPosProperty == null
        || xPosProperty.getText() == null
        || xPosProperty.getText().equals("")) {
      errorOccurred(point, "modelValidator.error_pointPositionXMissing.text");
      valid = false;
    }

    //Validate the y position in the course model
    StringProperty yPosProperty = (StringProperty) point.getProperty(ElementPropKeys.POINT_POS_Y);
    if (yPosProperty == null || isNullOrEmpty(yPosProperty.getText())) {
      errorOccurred(point, "modelValidator.error_pointPositionYMissing.text");
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
      errorOccurred(path, "modelValidator.error_pathLengthMissing.text");
      return false; //Return because the next if would be a NPE
    }

    //Validate the routing costs
    IntegerProperty costProperty = (IntegerProperty) path.getProperty(PathModel.ROUTING_COST);
    if (costProperty == null || costProperty.getValue() == null) {
      errorOccurred(path, "modelValidator.error_pathRoutingCostMissing.text");
      return false; //Return because the next if would be a NPE
    }

    //Validate the max velocity
    SpeedProperty maxVelocityProperty = (SpeedProperty) path.getProperty(PathModel.MAX_VELOCITY);
    if (maxVelocityProperty == null || maxVelocityProperty.getValue() == null) {
      errorOccurred(path, "modelValidator.error_pathMaximumVelocityMissing.text");
      return false; //Return because the next if would be a NPE
    }

    //Validate the max reverse velocity
    SpeedProperty maxRevVelocityProperty
        = (SpeedProperty) path.getProperty(PathModel.MAX_REVERSE_VELOCITY);
    if (maxRevVelocityProperty == null || maxRevVelocityProperty.getValue() == null) {
      errorOccurred(path, "modelValidator.error_pathMaximumReverseVelocityMissing.text");
      return false; //Return because the next if would be a NPE
    }

    //Validate the connection type of this path
    AbstractProperty typeProperty
        = (AbstractProperty) path.getProperty(ElementPropKeys.PATH_CONN_TYPE);
    if (typeProperty == null) {
      errorOccurred(path, "modelValidator.error_pathConnectionTypeMissing.text");
      return false; //Return because the next if would be a NPE
    }

    //Validate the control points of the path
    StringProperty controlPointsProperty
        = (StringProperty) path.getProperty(ElementPropKeys.PATH_CONTROL_POINTS);
    if (controlPointsProperty == null) {
      errorOccurred(path, "modelValidator.error_pathControlPointsMissing.text");
      return false; //Return because the next if would be a NPE
    }

    if ((typeProperty.getValue().equals(PathModel.Type.BEZIER)
         || typeProperty.getValue().equals(PathModel.Type.BEZIER_3))
        && (isNullOrEmpty(controlPointsProperty.getText()))) {
      errorOccurred(path, "modelValidator.error_pathControlPointsInvalid.text",
                    controlPointsProperty.getText());
      valid = false;
    }

    //Validate the start component of this path
    StringProperty startProperty = (StringProperty) path.getProperty(PathModel.START_COMPONENT);
    if (startProperty == null) {
      errorOccurred(path, "modelValidator.error_pathStartComponentMissing.text");
      return false; //Return because the next if would be a NPE
    }

    if (isNullOrEmpty(startProperty.getText())) {
      errorOccurred(path, "modelValidator.error_pathStartComponentInvalid.text", startProperty.getText());
      valid = false;
    }

    if (!nameExists(model, startProperty.getText())) {
      errorOccurred(model, "modelValidator.error_pathStartComponentNotExisting.text", startProperty.getText());
      valid = false;
    }

    //Validate the end component of this path
    StringProperty endProperty = (StringProperty) path.getProperty(PathModel.END_COMPONENT);
    if (endProperty == null) {
      errorOccurred(path, "modelValidator.error_pathEndComponentMissing.text");
      return false; //Return because the next if would be a NPE
    }

    if (endProperty.getText() == null
        || endProperty.getText().equals("")) {
      errorOccurred(path, "modelValidator.error_pathEndComponentInvalid.text", endProperty.getText());
      valid = false;
    }

    if (!nameExists(model, endProperty.getText())) {
      errorOccurred(model, "modelValidator.error_pathEndComponentNotExisting.text", endProperty.getText());
      valid = false;
    }

    //Validate the existence of the locked property
    BooleanProperty lockedProperty = (BooleanProperty) path.getProperty(PathModel.LOCKED);
    if (lockedProperty == null) {
      errorOccurred(path, "modelValidator.error_pathLockedMissing.text");
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
    for (PathModel.Type type : PathModel.Type.values()) {
      if (type.equals(typeProperty.getValue())) {
        found = true;
      }
    }
    //Record error if property value is not present in the enum
    if (!found) {
      errorOccurred(path, "modelValidator.error_pathTypeInvalid.text",
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
      errorOccurred(locationType, "modelValidator.error_locationTypeAllowedOperationsMissing.text");
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
      errorOccurred(location, "modelValidator.error_locationModelPositionXMissing.text");
      return false;
    }
    try {
      Double.parseDouble(xCoordProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(location, "modelValidator.error_locationModelPositionXInvalid.text",
                    xCoordProperty.getValue());
      valid = false;
    }

    //Validate the y position in the model view
    CoordinateProperty yCoordProperty
        = (CoordinateProperty) location.getProperty(LocationModel.MODEL_Y_POSITION);
    if (yCoordProperty == null) {
      errorOccurred(location, "modelValidator.error_locationModelPositionYMissing.text");
      return false;
    }
    try {
      Double.parseDouble(yCoordProperty.getValue().toString());
    }
    catch (NumberFormatException e) {
      errorOccurred(location, "modelValidator.error_locationModelPositionYInvalid.text",
                    yCoordProperty.getValue());
      valid = false;
    }

    //Validate the x position in the course model
    StringProperty xPosProperty = (StringProperty) location.getProperty(ElementPropKeys.LOC_POS_X);
    if (xPosProperty == null || xPosProperty.getText() == null) {
      errorOccurred(location, "modelValidator.error_locationPositionXMissing.text");
      return false;
    }
    try {
      Integer.parseInt(xPosProperty.getText());
    }
    catch (NumberFormatException e) {
      errorOccurred(location, "modelValidator.error_locationPositionXInvalid.text",
                    xPosProperty.getText());
      valid = false;
    }

    //Validate the y position in the course model
    StringProperty yPosProperty = (StringProperty) location.getProperty(ElementPropKeys.LOC_POS_Y);
    if (yPosProperty == null || yPosProperty.getText() == null) {
      errorOccurred(location, "modelValidator.error_locationPositionYMissing.text");
      return false;
    }
    try {
      Integer.parseInt(yPosProperty.getText());
    }
    catch (NumberFormatException e) {
      errorOccurred(location,
                    "modelValidator.error_locationPositionYInvalid.text",
                    yPosProperty.getText());
      valid = false;
    }

    //Validate the location type
    boolean found = false;
    LocationTypeProperty locTypeProperty
        = (LocationTypeProperty) location.getProperty(LocationModel.TYPE);
    if (locTypeProperty == null) {
      errorOccurred(location, "modelValidator.error_locationTypeMissing.text");
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
      errorOccurred(location, "modelValidator.error_locationTypeInvalid.text",
                    locTypeProperty.getValue());
      valid = false;
    }

    //Validate the labels offset x-coordinate for the model view
    StringProperty labelOffsetXProperty
        = (StringProperty) location.getProperty(ElementPropKeys.LOC_LABEL_OFFSET_X);
    if (labelOffsetXProperty == null) {
      errorOccurred(location, "modelValidator.error_locationLabelOffsetXMissing.text");
      return false;
    }

    //Validate the labels offset y-coordinate for the model view
    StringProperty labelOffsetYProperty
        = (StringProperty) location.getProperty(ElementPropKeys.LOC_LABEL_OFFSET_Y);
    if (labelOffsetYProperty == null) {
      errorOccurred(location, "modelValidator.error_locationLabelOffsetYMissing.text");
      return false;
    }

    //Validate the label orientation angle for the model view
    StringProperty labelOrientationAngleProperty
        = (StringProperty) location.getProperty(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);
    if (labelOrientationAngleProperty == null) {
      errorOccurred(location, "modelValidator.error_locationOrientationAngleMissing.text");
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
      errorOccurred(link, "modelValidator.error_linkStartComponentMissing.text");
      return false; //Return because the next if would be a NPE
    }
    if (isNullOrEmpty(startProperty.getText())) {
      errorOccurred(link, "modelValidator.error_linkStartComponentInvalid.text",
                    startProperty.getText());
      valid = false;
    }

    //Validate the end component of the link
    StringProperty endProperty = (StringProperty) link.getProperty(LinkModel.END_COMPONENT);
    if (endProperty == null) {
      errorOccurred(link, "modelValidator.error_linkEndComponentMissing.text");
      return false; //Return because the next if would be a NPE
    }
    if (isNullOrEmpty(endProperty.getText())) {
      errorOccurred(link, "modelValidator.error_linkEndComponentInvalid.text", endProperty.getText());
      valid = false;
    }
    if (!valid) {
      return valid;
    }
    //Validate whether the start component exists
    if (!nameExists(model, startProperty.getText())) {
      errorOccurred(link, "modelValidator.error_linkStartComponentNotExisting.text",
                    startProperty.getText());
      valid = false;
    }
    //Validate whether the point exists
    if (!nameExists(model, endProperty.getText())) {
      errorOccurred(link, "modelValidator.error_linkEndComponentNotExisting.text",
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

    //Validate the block type
    AbstractProperty typeProperty = (AbstractProperty) block.getProperty(BlockModel.TYPE);
    if (typeProperty == null || typeProperty.getComparableValue() == null) {
      errorOccurred(block, "modelValidator.error_blockTypeMissing.text");
    }

    //Validate that all members of the block exists
    StringSetProperty elementsProperty = (StringSetProperty) block.getProperty(BlockModel.ELEMENTS);
    if (elementsProperty == null) {
      errorOccurred(block, "modelValidator.error_blockElementsMissing.text");
      return false; //Return because the next if would be a NPE
    }

    Set<String> elements = new HashSet<>();
    for (String element : elementsProperty.getItems()) {
      if (elements.contains(element)) {
        errorOccurred(block, "modelValidator.error_blockElementsDuplicate.text", element);
        valid = false;
      }
      elements.add(element);
      if (!nameExists(model, element)) {
        errorOccurred(block, "modelValidator.error_blockElementsBotExisting.text", element);
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
      errorOccurred(group, "modelValidator.error_groupElementsMissing.text");
      return false; //Return because the next if would be a NPE
    }
    Set<String> elements = new HashSet<>();
    for (String element : elementsProperty.getItems()) {
      if (elements.contains(element)) {
        errorOccurred(group, "modelValidator.error_groupElementsDuplicate.text", element);
        valid = false;
      }
      elements.add(element);
      if (!nameExists(model, element)) {
        errorOccurred(group, "modelValidator.error_groupElementsNotExisting.text", element);
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
      errorOccurred(vehicle, "modelValidator.error_vehicleLengthMissing.text");
      valid = false;
    }

    PercentProperty energyCriticalProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL_CRITICAL);
    if (energyCriticalProperty == null) {
      errorOccurred(vehicle, "modelValidator.error_vehicleEnergyLevelCriticalMissing.text");
      valid = false;
    }

    PercentProperty energyGoodProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL_GOOD);
    if (energyGoodProperty == null) {
      errorOccurred(vehicle, "modelValidator.error_vehicleEnergyLevelGoodMissing.text");
      valid = false;
    }

    PercentProperty energyLevelProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL);
    if (energyLevelProperty == null) {
      errorOccurred(vehicle, "modelValidator.error_vehicleEnergyLevelMissing.text");
      valid = false;
    }

    BooleanProperty loadedProperty = (BooleanProperty) vehicle.getProperty(VehicleModel.LOADED);
    if (loadedProperty == null) {
      errorOccurred(vehicle, "modelValidator.error_vehicleLoadedMissing.text");
      valid = false;
    }

    AbstractProperty stateProperty = (AbstractProperty) vehicle.getProperty(VehicleModel.STATE);
    if (stateProperty == null) {
      errorOccurred(vehicle, "modelValidator.error_vehicleStateMissing.text");
      valid = false;
    }

    AbstractProperty procStateproperty
        = (AbstractProperty) vehicle.getProperty(VehicleModel.PROC_STATE);
    if (procStateproperty == null) {
      errorOccurred(vehicle, "modelValidator.error_vehicleProcessingStateMissing.text");
      valid = false;
    }

    AbstractProperty integrationStateProperty
        = (AbstractProperty) vehicle.getProperty(VehicleModel.INTEGRATION_LEVEL);
    if (integrationStateProperty == null) {
      errorOccurred(vehicle, "modelValidator.error_vehicleIntegrationLevelMissing.text");
      valid = false;
    }

    StringProperty currentPointProperty = (StringProperty) vehicle.getProperty(VehicleModel.POINT);
    if (currentPointProperty == null) {
      errorOccurred(vehicle, "modelValidator.error_vehicleCurrentPointMissing.text");
      valid = false;
    }

    StringProperty nextPointProperty
        = (StringProperty) vehicle.getProperty(VehicleModel.NEXT_POINT);
    if (nextPointProperty == null) {
      errorOccurred(vehicle, "modelValidator.error_vehicleNextPointMissing.text");
      valid = false;
    }

    TripleProperty precisePositionProperty
        = (TripleProperty) vehicle.getProperty(VehicleModel.PRECISE_POSITION);
    if (precisePositionProperty == null) {
      errorOccurred(vehicle, "modelValidator.error_vehiclePrecisePositionMissing.text");
      valid = false;
    }

    AngleProperty orientationProperty
        = (AngleProperty) vehicle.getProperty(VehicleModel.ORIENTATION_ANGLE);
    if (orientationProperty == null) {
      errorOccurred(vehicle, "modelValidator.error_vehicleOrientationAngleMissing.text");
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

    //Validate that the processing state is a valid/known value
    boolean found = false;
    for (Vehicle.ProcState procState : Vehicle.ProcState.values()) {
      if (procState.equals(procStateproperty.getValue())) {
        found = true;
        break;
      }
    }
    if (!found) {
      errorOccurred(model, "modelValidator.error_vehicleProcessingStateInvalid.text",
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
      errorOccurred(vehicle, "modelValidator.error_vehicleCurrentPointNotExisting.text",
                    currentPointProperty.getText());
      valid = false;
    }
    //Validate whether the next point exists
    String nextPoint = nextPointProperty.getText();
    if (!isNullOrEmptyPoint(nextPoint) && !nameExists(model, nextPoint)) {
      errorOccurred(vehicle, "modelValidator.error_vehicleNextPointNotExisting.text",
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
