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
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
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
    }
    else if (component instanceof PointModel) {
      valid = validatePoint(model, (PointModel) component);
    }
    else if (component instanceof PathModel) {
      valid = validatePath(model, (PathModel) component);
    }
    else if (component instanceof LocationTypeModel) {
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
    return ResourceBundleUtil.getBundle(I18nPlantOverview.VALIDATOR_PATH)
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

    //Validate the start component of this path
    StringProperty startProperty = (StringProperty) path.getProperty(PathModel.START_COMPONENT);
    if (!nameExists(model, startProperty.getText())) {
      errorOccurred(model,
                    "modelValidator.error_pathStartComponentNotExisting.text",
                    startProperty.getText());
      valid = false;
    }

    //Validate the end component of this path
    StringProperty endProperty = (StringProperty) path.getProperty(PathModel.END_COMPONENT);
    if (!nameExists(model, endProperty.getText())) {
      errorOccurred(model,
                    "modelValidator.error_pathEndComponentNotExisting.text",
                    endProperty.getText());
      valid = false;
    }

    //Validate the length of the path
    LengthProperty lengthProperty = (LengthProperty) path.getProperty(PathModel.LENGTH);
    if (((Double) lengthProperty.getValue()) < 1) {
      LOG.warn("{}: Length property is {} but has to be > 0. Setting it to 1.",
               path.getName(),
               lengthProperty.getValue());
      lengthProperty.setValueAndUnit(1, LengthProperty.Unit.MM);
      path.setProperty(PathModel.LENGTH, lengthProperty);
    }

    //Validate the max velocity
    SpeedProperty maxVelocityProperty = (SpeedProperty) path.getProperty(PathModel.MAX_VELOCITY);
    if (((Double) maxVelocityProperty.getValue()) < 0) {
      LOG.warn("{}: Max. velocity property is {} but has to be >= 0. Setting it to 0.",
               path.getName(),
               maxVelocityProperty.getValue());
      maxVelocityProperty.setValueAndUnit(0, SpeedProperty.Unit.MM_S);
      path.setProperty(PathModel.MAX_VELOCITY, maxVelocityProperty);
    }

    //Validate the maximum reverse velocity
    SpeedProperty maxRevVelocityProperty
        = (SpeedProperty) path.getProperty(PathModel.MAX_REVERSE_VELOCITY);
    if (((Double) maxRevVelocityProperty.getValue()) < 0) {
      LOG.warn("{}: Max. reverse velocity property is {} but has to be >= 0. Setting it to 0.",
               path.getName(),
               maxRevVelocityProperty.getValue());
      maxRevVelocityProperty.setValueAndUnit(0, SpeedProperty.Unit.MM_S);
      path.setProperty(PathModel.MAX_VELOCITY, maxRevVelocityProperty);
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

    //Validate the location type
    LocationTypeProperty locTypeProperty
        = (LocationTypeProperty) location.getProperty(LocationModel.TYPE);
    boolean locTypeExists = model.getLocationTypeModels().stream()
        .map(type -> type.getName())
        .anyMatch(typeName -> typeName.equals(locTypeProperty.getValue()));
    if (!locTypeExists) {
      errorOccurred(location, "modelValidator.error_locationTypeInvalid.text",
                    locTypeProperty.getValue());
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

    //Validate whether the start component exists
    StringProperty startProperty = (StringProperty) link.getProperty(LinkModel.START_COMPONENT);
    if (!nameExists(model, startProperty.getText())) {
      errorOccurred(link, "modelValidator.error_linkStartComponentNotExisting.text",
                    startProperty.getText());
      valid = false;
    }
    //Validate whether the point exists
    StringProperty endProperty = (StringProperty) link.getProperty(LinkModel.END_COMPONENT);
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

    //Validate that all members of the block exists
    StringSetProperty elementsProperty = (StringSetProperty) block.getProperty(BlockModel.ELEMENTS);
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
    if (((Double) lengthProperty.getValue()) < 1) {
      LOG.warn("{}: Length property is {} but has to be > 0. Setting it to 1.",
               vehicle.getName(),
               lengthProperty.getValue());
      lengthProperty.setValueAndUnit(1, LengthProperty.Unit.MM);
      vehicle.setProperty(VehicleModel.LENGTH, lengthProperty);
    }

    //Validate the critical energy level
    PercentProperty energyCriticalProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL_CRITICAL);
    if (((int) energyCriticalProperty.getValue()) < 0
        || ((int) energyCriticalProperty.getValue()) > 100) {
      LOG.warn("{}: Energy level critical is {} but has to be in range of [0..100]. Setting it to 0.",
               vehicle.getName(),
               energyCriticalProperty.getValue());
      energyCriticalProperty.setValueAndUnit(0, PercentProperty.Unit.PERCENT);
      vehicle.setProperty(VehicleModel.ENERGY_LEVEL_CRITICAL, energyCriticalProperty);
    }

    //Validate the good energy level
    PercentProperty energyGoodProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL_GOOD);
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
    PercentProperty energyLevelProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL);
    if (((int) energyLevelProperty.getValue()) < 0 || ((int) energyLevelProperty.getValue()) > 100) {
      LOG.warn("{}: Energy level is {} but has to be in range of [0..100]. Setting it to 50.");
      energyLevelProperty.setValueAndUnit(50, PercentProperty.Unit.PERCENT);
      vehicle.setProperty(VehicleModel.ENERGY_LEVEL, energyLevelProperty);
    }

    //Validate the precise position happens in the property converter
    AngleProperty orientationProperty
        = (AngleProperty) vehicle.getProperty(VehicleModel.ORIENTATION_ANGLE);
    if (((Double) orientationProperty.getValue()) < 0) {
      LOG.warn("{}: Orientation angle is {} but has to be >= 0. Setting it to 0.");
      orientationProperty.setValueAndUnit(0, AngleProperty.Unit.DEG);
      vehicle.setProperty(VehicleModel.ORIENTATION_ANGLE, orientationProperty);
    }

    //Validate whether the current point exists
    StringProperty currentPointProperty = (StringProperty) vehicle.getProperty(VehicleModel.POINT);
    String currentPoint = currentPointProperty.getText();
    if (!isNullOrEmptyPoint(currentPoint) && !nameExists(model, currentPoint)) {
      errorOccurred(vehicle, "modelValidator.error_vehicleCurrentPointNotExisting.text",
                    currentPointProperty.getText());
      valid = false;
    }
    //Validate whether the next point exists
    StringProperty nextPointProperty = (StringProperty) vehicle.getProperty(VehicleModel.NEXT_POINT);
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
