// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.persistence;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opentcs.data.model.Couple;
import org.opentcs.guing.base.components.properties.type.AngleProperty;
import org.opentcs.guing.base.components.properties.type.BoundingBoxProperty;
import org.opentcs.guing.base.components.properties.type.EnergyLevelThresholdSetProperty;
import org.opentcs.guing.base.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.base.components.properties.type.PercentProperty;
import org.opentcs.guing.base.components.properties.type.Property;
import org.opentcs.guing.base.components.properties.type.SpeedProperty;
import org.opentcs.guing.base.components.properties.type.StringProperty;
import org.opentcs.guing.base.components.properties.type.StringSetProperty;
import org.opentcs.guing.base.model.BoundingBoxModel;
import org.opentcs.guing.base.model.EnergyLevelThresholdSetModel;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;
import org.opentcs.modeleditor.util.TextAreaDialog;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator for a {@link SystemModel} and its {@link ModelComponent}s.
 * Validates if the model component can safely be added to a system model.
 */
public class ModelValidator {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ModelValidator.class);
  /**
   * This class' resource bundle.
   */
  private final ResourceBundleUtil bundle
      = ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.VALIDATOR_PATH);
  /**
   * The collection of errors which happened after the last reset.
   */
  private final List<String> errors = new ArrayList<>();

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
    return new ArrayList<>(errors);
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
      errorOccurred(
          component,
          "modelValidator.error_componentNameInvalid.text",
          component.getName()
      );
      return false;
    }
    if (nameExists(model, component)) {
      errorOccurred(
          component,
          "modelValidator.error_componentNameExists.text",
          component.getName()
      );
      return false;
    }
    //Validate the miscellaneous property of the component
    //TODO: Seems to be optional in some models?!
    KeyValueSetProperty miscellaneous
        = (KeyValueSetProperty) component.getProperty(ModelComponent.MISCELLANEOUS);
    /*
     * if (miscellaneous == null) {
     * errorOccurred(component, "Miscellaneous key-value-set does not exist.");
     * return false;
     * }
     */
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
        = new TextAreaDialog(
            parent,
            true,
            bundle.getString("modelValidator.dialog_validationWarning.message.loadingError")
        );
    panel.setContent(content);
    panel.setTitle(bundle.getString("modelValidator.dialog_validationWarning.title"));
    panel.setLocationRelativeTo(null);
    panel.setVisible(true);
  }

  public void showSavingValidationWarning(Component parent, Collection<String> content) {
    TextAreaDialog panel
        = new TextAreaDialog(
            parent,
            true,
            bundle.getString("modelValidator.dialog_validationWarning.message.savingError")
        );
    panel.setContent(content);
    panel.setTitle(bundle.getString("modelValidator.dialog_validationWarning.title"));
    panel.setLocationRelativeTo(null);
    panel.setVisible(true);
  }

  public String formatDeserializationErrors(ModelComponent component, Collection<String> errors) {
    return ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.VALIDATOR_PATH)
        .getFormatted(
            "modelValidator.error_deserialization.text",
            component.getName(),
            errors
        );
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
      LOG.warn(
          "{}: Orientation angle property ('{}') is not a number. Setting it to 0.",
          point.getName(),
          orientationProperty.getValue()
      );
      orientationProperty.setValueAndUnit(0, AngleProperty.Unit.DEG);
    }
    else {
      Double angle = (Double) orientationProperty.getValue();
      if (angle < 0) {
        LOG.warn(
            "{}: Orientation angle property is {} but has to be > 0."
                + " Transforming to positive angle.",
            point.getName(),
            orientationProperty.getValue()
        );
        orientationProperty.setValueAndUnit(360 + angle % 360, AngleProperty.Unit.DEG);
      }
    }
    point.setProperty(PointModel.VEHICLE_ORIENTATION_ANGLE, orientationProperty);

    // Validate the maximum vehicle bounding box
    BoundingBoxProperty boundingBoxProperty
        = (BoundingBoxProperty) point.getProperty(PointModel.MAX_VEHICLE_BOUNDING_BOX);
    if (boundingBoxProperty.getValue().getLength() < 1
        || boundingBoxProperty.getValue().getWidth() < 1
        || boundingBoxProperty.getValue().getHeight() < 1) {
      LOG.warn(
          "{}: Some bounding box property dimensions are smaller than 1 but have to be > 0. "
              + "Setting them to 1.",
          point.getName()
      );
      boundingBoxProperty.setValue(
          new BoundingBoxModel(
              Math.max(boundingBoxProperty.getValue().getLength(), 1),
              Math.max(boundingBoxProperty.getValue().getWidth(), 1),
              Math.max(boundingBoxProperty.getValue().getHeight(), 1),
              new Couple(
                  boundingBoxProperty.getValue().getReferenceOffset().getX(),
                  boundingBoxProperty.getValue().getReferenceOffset().getY()
              )
          )
      );
      point.setProperty(PointModel.MAX_VEHICLE_BOUNDING_BOX, boundingBoxProperty);
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

    //Validate the start component of this path
    StringProperty startProperty = (StringProperty) path.getProperty(PathModel.START_COMPONENT);
    if (!nameExists(model, startProperty.getText())) {
      errorOccurred(
          model,
          "modelValidator.error_pathStartComponentNotExisting.text",
          startProperty.getText()
      );
      valid = false;
    }

    //Validate the end component of this path
    StringProperty endProperty = (StringProperty) path.getProperty(PathModel.END_COMPONENT);
    if (!nameExists(model, endProperty.getText())) {
      errorOccurred(
          model,
          "modelValidator.error_pathEndComponentNotExisting.text",
          endProperty.getText()
      );
      valid = false;
    }

    //Validate the length of the path
    LengthProperty lengthProperty = (LengthProperty) path.getProperty(PathModel.LENGTH);
    if (((Double) lengthProperty.getValue()) < 1) {
      LOG.warn(
          "{}: Length property is {} but has to be > 0. Setting it to 1.",
          path.getName(),
          lengthProperty.getValue()
      );
      lengthProperty.setValueAndUnit(1, LengthProperty.Unit.MM);
      path.setProperty(PathModel.LENGTH, lengthProperty);
    }

    //Validate the max velocity
    SpeedProperty maxVelocityProperty = (SpeedProperty) path.getProperty(PathModel.MAX_VELOCITY);
    if (((Double) maxVelocityProperty.getValue()) < 0) {
      LOG.warn(
          "{}: Max. velocity property is {} but has to be >= 0. Setting it to 0.",
          path.getName(),
          maxVelocityProperty.getValue()
      );
      maxVelocityProperty.setValueAndUnit(0, SpeedProperty.Unit.MM_S);
      path.setProperty(PathModel.MAX_VELOCITY, maxVelocityProperty);
    }

    //Validate the maximum reverse velocity
    SpeedProperty maxRevVelocityProperty
        = (SpeedProperty) path.getProperty(PathModel.MAX_REVERSE_VELOCITY);
    if (((Double) maxRevVelocityProperty.getValue()) < 0) {
      LOG.warn(
          "{}: Max. reverse velocity property is {} but has to be >= 0. Setting it to 0.",
          path.getName(),
          maxRevVelocityProperty.getValue()
      );
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
      errorOccurred(
          location, "modelValidator.error_locationTypeInvalid.text",
          locTypeProperty.getValue()
      );
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
      errorOccurred(
          link, "modelValidator.error_linkStartComponentNotExisting.text",
          startProperty.getText()
      );
      valid = false;
    }
    //Validate whether the point exists
    StringProperty endProperty = (StringProperty) link.getProperty(LinkModel.END_COMPONENT);
    if (!nameExists(model, endProperty.getText())) {
      errorOccurred(
          link, "modelValidator.error_linkEndComponentNotExisting.text",
          endProperty.getText()
      );
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
   * Validates the properties of a vehicle model.
   *
   * @param model the system model to validate against
   * @param vehicle the vehicle model to validate
   * @return true if the vehicle model is valid, false otherwise
   */
  private boolean validateVehicle(SystemModel model, VehicleModel vehicle) {
    boolean valid = true;

    //Validate that all properties needed exist
    BoundingBoxProperty boundingBoxProperty
        = (BoundingBoxProperty) vehicle.getProperty(VehicleModel.BOUNDING_BOX);
    if (boundingBoxProperty.getValue().getLength() < 1
        || boundingBoxProperty.getValue().getWidth() < 1
        || boundingBoxProperty.getValue().getHeight() < 1) {
      LOG.warn(
          "{}: Some bounding box property dimensions are smaller than 1 but have to be > 0. "
              + "Setting them to 1.",
          vehicle.getName()
      );
      boundingBoxProperty.setValue(
          new BoundingBoxModel(
              Math.max(boundingBoxProperty.getValue().getLength(), 1),
              Math.max(boundingBoxProperty.getValue().getWidth(), 1),
              Math.max(boundingBoxProperty.getValue().getHeight(), 1),
              new Couple(
                  boundingBoxProperty.getValue().getReferenceOffset().getX(),
                  boundingBoxProperty.getValue().getReferenceOffset().getY()
              )
          )
      );
      vehicle.setProperty(VehicleModel.BOUNDING_BOX, boundingBoxProperty);
    }

    EnergyLevelThresholdSetProperty energyLevelThresholdSetProperty
        = (EnergyLevelThresholdSetProperty) vehicle.getProperty(
            VehicleModel.ENERGY_LEVEL_THRESHOLD_SET
        );
    // Validate the critical energy level
    if (energyLevelThresholdSetProperty.getValue().getEnergyLevelCritical() < 0
        || energyLevelThresholdSetProperty.getValue().getEnergyLevelCritical() > 100) {
      LOG.warn(
          "{}: Energy level critical is {}, should be in [0..100]. Setting to 0.",
          vehicle.getName(),
          energyLevelThresholdSetProperty.getValue().getEnergyLevelCritical()
      );
      energyLevelThresholdSetProperty.setValue(
          new EnergyLevelThresholdSetModel(
              0,
              energyLevelThresholdSetProperty.getValue().getEnergyLevelGood(),
              energyLevelThresholdSetProperty.getValue().getEnergyLevelSufficientlyRecharged(),
              energyLevelThresholdSetProperty.getValue().getEnergyLevelFullyRecharged()
          )
      );
      vehicle.setProperty(VehicleModel.ENERGY_LEVEL_THRESHOLD_SET, energyLevelThresholdSetProperty);
    }

    // Validate the good energy level
    if (energyLevelThresholdSetProperty.getValue().getEnergyLevelGood() < 0
        || energyLevelThresholdSetProperty.getValue().getEnergyLevelGood() > 100) {
      LOG.warn(
          "{}: Energy level good is {}, should be in [0..100]. Setting to 100.",
          vehicle.getName(),
          energyLevelThresholdSetProperty.getValue().getEnergyLevelGood()
      );
      energyLevelThresholdSetProperty.setValue(
          new EnergyLevelThresholdSetModel(
              energyLevelThresholdSetProperty.getValue().getEnergyLevelCritical(),
              100,
              energyLevelThresholdSetProperty.getValue().getEnergyLevelSufficientlyRecharged(),
              energyLevelThresholdSetProperty.getValue().getEnergyLevelFullyRecharged()
          )
      );
      vehicle.setProperty(VehicleModel.ENERGY_LEVEL_THRESHOLD_SET, energyLevelThresholdSetProperty);
    }

    // Validate that the good energy level is greater than or equals the critical energy level
    if (energyLevelThresholdSetProperty.getValue().getEnergyLevelGood()
        < energyLevelThresholdSetProperty.getValue().getEnergyLevelCritical()) {
      LOG.warn(
          "{}: Energy level good ({}) not >= energy level critical ({}). Setting to {}.",
          vehicle.getName(),
          energyLevelThresholdSetProperty.getValue().getEnergyLevelGood(),
          energyLevelThresholdSetProperty.getValue().getEnergyLevelCritical(),
          energyLevelThresholdSetProperty.getValue().getEnergyLevelCritical()
      );
      energyLevelThresholdSetProperty.setValue(
          new EnergyLevelThresholdSetModel(
              energyLevelThresholdSetProperty.getValue().getEnergyLevelCritical(),
              energyLevelThresholdSetProperty.getValue().getEnergyLevelCritical(),
              energyLevelThresholdSetProperty.getValue().getEnergyLevelSufficientlyRecharged(),
              energyLevelThresholdSetProperty.getValue().getEnergyLevelFullyRecharged()
          )
      );
      vehicle.setProperty(VehicleModel.ENERGY_LEVEL_THRESHOLD_SET, energyLevelThresholdSetProperty);
    }

    // Validate that the fully recharged energy level is greater than or equals the sufficiently
    // recharged energy level
    if (energyLevelThresholdSetProperty.getValue().getEnergyLevelFullyRecharged()
        < energyLevelThresholdSetProperty.getValue().getEnergyLevelSufficientlyRecharged()) {
      LOG.warn(
          "{}: Energy level fully recharged ({}) not >= energy level sufficiently recharged ({})."
              + " Setting to {}.",
          vehicle.getName(),
          energyLevelThresholdSetProperty.getValue().getEnergyLevelFullyRecharged(),
          energyLevelThresholdSetProperty.getValue().getEnergyLevelSufficientlyRecharged(),
          energyLevelThresholdSetProperty.getValue().getEnergyLevelSufficientlyRecharged()
      );
      energyLevelThresholdSetProperty.setValue(
          new EnergyLevelThresholdSetModel(
              energyLevelThresholdSetProperty.getValue().getEnergyLevelCritical(),
              energyLevelThresholdSetProperty.getValue().getEnergyLevelGood(),
              energyLevelThresholdSetProperty.getValue().getEnergyLevelSufficientlyRecharged(),
              energyLevelThresholdSetProperty.getValue().getEnergyLevelSufficientlyRecharged()
          )
      );
      vehicle.setProperty(VehicleModel.ENERGY_LEVEL_THRESHOLD_SET, energyLevelThresholdSetProperty);
    }

    //Validate the current energy level
    PercentProperty energyLevelProperty
        = (PercentProperty) vehicle.getProperty(VehicleModel.ENERGY_LEVEL);
    if (((int) energyLevelProperty.getValue()) < 0
        || ((int) energyLevelProperty.getValue()) > 100) {
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
      errorOccurred(
          vehicle, "modelValidator.error_vehicleCurrentPointNotExisting.text",
          currentPointProperty.getText()
      );
      valid = false;
    }
    //Validate whether the next point exists
    StringProperty nextPointProperty
        = (StringProperty) vehicle.getProperty(VehicleModel.NEXT_POINT);
    String nextPoint = nextPointProperty.getText();
    if (!isNullOrEmptyPoint(nextPoint) && !nameExists(model, nextPoint)) {
      errorOccurred(
          vehicle, "modelValidator.error_vehicleNextPointNotExisting.text",
          nextPointProperty.getText()
      );
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
