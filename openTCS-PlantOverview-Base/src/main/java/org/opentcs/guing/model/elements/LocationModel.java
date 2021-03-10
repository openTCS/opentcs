/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.visualization.ElementPropKeys;
import static org.opentcs.guing.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.model.AbstractConnectableModelComponent;
import org.opentcs.guing.model.PositionableModelComponent;

/**
 * Basic implementation for every kind of location.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LocationModel
    extends AbstractConnectableModelComponent
    implements AttributesChangeListener,
               PositionableModelComponent {

  /**
   * The property key for the location's type.
   */
  public static final String TYPE = "Type";
  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * The model of the type.
   */
  private transient LocationTypeModel fLocationType;

  /**
   * Creates a new instance.
   */
  public LocationModel() {
    createProperties();
  }

  /**
   * Sets the location type.
   *
   * @param type The model of the type.
   */
  public void setLocationType(LocationTypeModel type) {
    if (fLocationType != null) {
      fLocationType.removeAttributesChangeListener(this);
    }

    if (type != null) {
      fLocationType = type;
      fLocationType.addAttributesChangeListener(this);
    }
  }

  /**
   * Returns the location type.
   *
   * @return The type.
   */
  public LocationTypeModel getLocationType() {
    return fLocationType;
  }

  /**
   * Refreshes the name of this location.
   */
  protected void updateName() {
    StringProperty property = getPropertyName();
    String oldName = property.getText();
    String newName = getName();
    property.setText(newName);

    if (!newName.equals(oldName)) {
      property.markChanged();
    }

    propertiesChanged(this);
  }

  @Override // AbstractModelComponent
  public String getDescription() {
    return bundle.getString("locationModel.description");
  }

  @Override // AttributesChangeListener
  public void propertiesChanged(AttributesChangeEvent e) {
    if (fLocationType.getPropertyName().hasChanged()) {
      updateName();
    }

    if (fLocationType.getPropertyDefaultRepresentation().hasChanged()) {
      propertiesChanged(this);
    }
  }

  public void updateTypeProperty(List<LocationTypeModel> types) {
    requireNonNull(types, "types");

    List<String> possibleValues = new ArrayList<>();
    String value = null;

    for (LocationTypeModel type : types) {
      possibleValues.add(type.getName());

      if (type == fLocationType) {
        value = type.getName();
      }
    }

    getPropertyType().setPossibleValues(possibleValues);
    getPropertyType().setValue(value);
    getPropertyType().markChanged();
  }

  public CoordinateProperty getPropertyModelPositionX() {
    return (CoordinateProperty) getProperty(MODEL_X_POSITION);
  }

  public CoordinateProperty getPropertyModelPositionY() {
    return (CoordinateProperty) getProperty(MODEL_Y_POSITION);
  }

  public LocationTypeProperty getPropertyType() {
    return (LocationTypeProperty) getProperty(TYPE);
  }

  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }

  public SymbolProperty getPropertyDefaultRepresentation() {
    return (SymbolProperty) getProperty(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION);
  }

  public StringProperty getPropertyLayoutPositionX() {
    return (StringProperty) getProperty(ElementPropKeys.LOC_POS_X);
  }

  public StringProperty getPropertyLayoutPositionY() {
    return (StringProperty) getProperty(ElementPropKeys.LOC_POS_Y);
  }

  public StringProperty getPropertyLabelOffsetX() {
    return (StringProperty) getProperty(ElementPropKeys.LOC_LABEL_OFFSET_X);
  }

  public StringProperty getPropertyLabelOffsetY() {
    return (StringProperty) getProperty(ElementPropKeys.LOC_LABEL_OFFSET_Y);
  }

  public StringProperty getPropertyLabelOrientationAngle() {
    return (StringProperty) getProperty(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);
  }

  private void createProperties() {
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("locationModel.property_name.description"));
    pName.setHelptext(bundle.getString("locationModel.property_name.helptext"));
    setProperty(NAME, pName);

    CoordinateProperty pPosX = new CoordinateProperty(this);
    pPosX.setDescription(bundle.getString("locationModel.property_modelPositionX.description"));
    pPosX.setHelptext(bundle.getString("locationModel.property_modelPositionX.helptext"));
    setProperty(MODEL_X_POSITION, pPosX);

    CoordinateProperty pPosY = new CoordinateProperty(this);
    pPosY.setDescription(bundle.getString("locationModel.property_modelPositionY.description"));
    pPosY.setHelptext(bundle.getString("locationModel.property_modelPositionY.helptext"));
    setProperty(MODEL_Y_POSITION, pPosY);

    LocationTypeProperty pType = new LocationTypeProperty(this);
    pType.setDescription(bundle.getString("locationModel.property_type.description"));
    pType.setHelptext(bundle.getString("locationModel.property_type.helptext"));
    setProperty(TYPE, pType);

    SymbolProperty pSymbol = new SymbolProperty(this);
    pSymbol.setDescription(bundle.getString("locationModel.property_symbol.description"));
    pSymbol.setHelptext(bundle.getString("locationModel.property_symbol.helptext"));
    pSymbol.setCollectiveEditable(true);
    setProperty(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION, pSymbol);

    StringProperty pLocPosX = new StringProperty(this);
    pLocPosX.setDescription(bundle.getString("locationModel.property_positionX.description"));
    pLocPosX.setHelptext(bundle.getString("locationModel.property_positionX.helptext"));
    pLocPosX.setModellingEditable(false);
    setProperty(ElementPropKeys.LOC_POS_X, pLocPosX);

    StringProperty pLocPosY = new StringProperty(this);
    pLocPosY.setDescription(bundle.getString("locationModel.property_positionY.description"));
    pLocPosY.setHelptext(bundle.getString("locationModel.property_positionY.helptext"));
    pLocPosY.setModellingEditable(false);
    setProperty(ElementPropKeys.LOC_POS_Y, pLocPosY);

    StringProperty pLocLabelOffsetX = new StringProperty(this);
    pLocLabelOffsetX.setDescription(bundle.getString("locationModel.property_labelOffsetX.description"));
    pLocLabelOffsetX.setHelptext(bundle.getString("locationModel.property_labelOffsetX.helptext"));
    pLocLabelOffsetX.setModellingEditable(false);
    setProperty(ElementPropKeys.LOC_LABEL_OFFSET_X, pLocLabelOffsetX);

    StringProperty pLocLabelOffsetY = new StringProperty(this);
    pLocLabelOffsetY.setDescription(bundle.getString("locationModel.property_labelOffsetY.description"));
    pLocLabelOffsetY.setHelptext(bundle.getString("locationModel.property_labelOffsetY.helptext"));
    pLocLabelOffsetY.setModellingEditable(false);
    setProperty(ElementPropKeys.LOC_LABEL_OFFSET_Y, pLocLabelOffsetY);

    StringProperty pLocLabelOrientationAngle = new StringProperty(this);
    pLocLabelOrientationAngle.setDescription(bundle.getString("locationModel.property_labelOrientationAngle.description"));
    pLocLabelOrientationAngle.setHelptext(bundle.getString("locationModel.property_labelOrientationAngle.helptext"));
    pLocLabelOrientationAngle.setModellingEditable(false);
    setProperty(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE, pLocLabelOrientationAngle);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("locationModel.property_miscellaneous.description"));
    pMiscellaneous.setHelptext(bundle.getString("locationModel.property_miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }
}
