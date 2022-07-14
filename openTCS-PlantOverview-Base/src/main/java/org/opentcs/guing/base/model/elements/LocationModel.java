/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model.elements;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LocationRepresentation;
import static org.opentcs.guing.base.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.base.components.layer.NullLayerWrapper;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.base.components.properties.type.BooleanProperty;
import org.opentcs.guing.base.components.properties.type.CoordinateProperty;
import org.opentcs.guing.base.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.base.components.properties.type.LayerWrapperProperty;
import org.opentcs.guing.base.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.base.components.properties.type.StringProperty;
import org.opentcs.guing.base.components.properties.type.SymbolProperty;
import org.opentcs.guing.base.model.AbstractConnectableModelComponent;
import org.opentcs.guing.base.model.AbstractModelComponent;
import org.opentcs.guing.base.model.FigureDecorationDetails;
import org.opentcs.guing.base.model.PositionableModelComponent;

/**
 * Basic implementation for every kind of location.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LocationModel
    extends AbstractConnectableModelComponent
    implements AttributesChangeListener,
               PositionableModelComponent,
               FigureDecorationDetails {

  /**
   * The property key for the location's type.
   */
  public static final String TYPE = "Type";
  /**
   * Key for the locked state.
   */
  public static final String LOCKED = "locked";
  /**
   * Key for the reservation token.
   */
  public static final String PERIPHERAL_RESERVATION_TOKEN = "peripheralReservationToken";
  /**
   * Key for the peripheral state.
   */
  public static final String PERIPHERAL_STATE = "peripheralState";
  /**
   * Key for the peripheral processing state.
   */
  public static final String PERIPHERAL_PROC_STATE = "peripheralProcState";
  /**
   * Key for the peripheral job.
   */
  public static final String PERIPHERAL_JOB = "peripheralJob";
  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * The set of vehicle models for which this model component's figure is to be decorted to
   * indicate that it is part of the route of the respective vehicles.
   */
  private Set<VehicleModel> vehicles
      = new TreeSet<>((v1, v2) -> v1.getName().compareTo(v2.getName()));
  /**
   * The set of block models for which this model component's figure is to be decorated to indicate
   * that it is part of the respective block.
   */
  private Set<BlockModel> blocks
      = new TreeSet<>((b1, b2) -> b1.getName().compareTo(b2.getName()));
  /**
   * The model of the type.
   */
  private transient LocationTypeModel fLocationType;
  /**
   * The location for this model.
   */
  private Location location;

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

  public BooleanProperty getPropertyLocked() {
    return (BooleanProperty) getProperty(LOCKED);
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

  public StringProperty getPropertyPeripheralReservationToken() {
    return (StringProperty) getProperty(PERIPHERAL_RESERVATION_TOKEN);
  }

  public StringProperty getPropertyPeripheralState() {
    return (StringProperty) getProperty(PERIPHERAL_STATE);
  }

  public StringProperty getPropertyPeripheralProcState() {
    return (StringProperty) getProperty(PERIPHERAL_PROC_STATE);
  }

  public StringProperty getPropertyPeripheralJob() {
    return (StringProperty) getProperty(PERIPHERAL_JOB);
  }

  @Override
  public void addVehicleModel(VehicleModel model) {
    vehicles.add(model);
  }

  @Override
  public void removeVehicleModel(VehicleModel model) {
    vehicles.remove(model);
  }

  @Override
  public Set<VehicleModel> getVehicleModels() {
    return vehicles;
  }

  @Override
  public void addBlockModel(BlockModel model) {
    blocks.add(model);
  }

  @Override
  public void removeBlockModel(BlockModel model) {
    blocks.remove(model);
  }

  @Override
  public Set<BlockModel> getBlockModels() {
    return blocks;
  }

  @Override
  @SuppressWarnings("unchecked")
  public AbstractModelComponent clone()
      throws CloneNotSupportedException {
    LocationModel clone = (LocationModel) super.clone();
    clone.setVehicleModels((Set<VehicleModel>) ((TreeSet<VehicleModel>) vehicles).clone());
    clone.setBlockModels((Set<BlockModel>) ((TreeSet<BlockModel>) blocks).clone());

    return clone;
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

    BooleanProperty pLocked = new BooleanProperty(this);
    pLocked.setDescription(bundle.getString("locationModel.property_locked.description"));
    pLocked.setHelptext(bundle.getString("locationModel.property_locked.helptext"));
    pLocked.setCollectiveEditable(true);
    pLocked.setOperatingEditable(true);
    setProperty(LOCKED, pLocked);

    SymbolProperty pSymbol = new SymbolProperty(this);
    pSymbol.setLocationRepresentation(LocationRepresentation.DEFAULT);
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

    LayerWrapperProperty pLayerWrapper = new LayerWrapperProperty(this, new NullLayerWrapper());
    pLayerWrapper.setDescription(bundle.getString("locationModel.property_layerWrapper.description"));
    pLayerWrapper.setHelptext(bundle.getString("locationModel.property_layerWrapper.helptext"));
    pLayerWrapper.setModellingEditable(false);
    setProperty(LAYER_WRAPPER, pLayerWrapper);

    StringProperty peripheralReservationTokenProperty = new StringProperty(this);
    peripheralReservationTokenProperty.setDescription(bundle.getString("locationModel.property_peripheralReservationToken.description"));
    peripheralReservationTokenProperty.setHelptext(bundle.getString("locationModel.property_peripheralReservationToken.helptext"));
    peripheralReservationTokenProperty.setOperatingEditable(false);
    peripheralReservationTokenProperty.setModellingEditable(false);
    setProperty(PERIPHERAL_RESERVATION_TOKEN, peripheralReservationTokenProperty);

    StringProperty peripheralStateProperty = new StringProperty(this);
    peripheralStateProperty.setDescription(bundle.getString("locationModel.property_peripheralState.description"));
    peripheralStateProperty.setHelptext(bundle.getString("locationModel.property_peripheralState.helptext"));
    peripheralStateProperty.setOperatingEditable(false);
    peripheralStateProperty.setModellingEditable(false);
    setProperty(PERIPHERAL_STATE, peripheralStateProperty);

    StringProperty peripheralProcState = new StringProperty(this);
    peripheralProcState.setDescription(bundle.getString("locationModel.property_peripheralProcState.description"));
    peripheralProcState.setHelptext(bundle.getString("locationModel.property_peripheralProcState.helptext"));
    peripheralProcState.setOperatingEditable(false);
    peripheralProcState.setModellingEditable(false);
    setProperty(PERIPHERAL_PROC_STATE, peripheralProcState);

    StringProperty peripheralJob = new StringProperty(this);
    peripheralJob.setDescription(bundle.getString("locationModel.property_peripheralJob.description"));
    peripheralJob.setHelptext(bundle.getString("locationModel.property_peripheralJob.helptext"));
    peripheralJob.setOperatingEditable(false);
    peripheralJob.setModellingEditable(false);
    setProperty(PERIPHERAL_JOB, peripheralJob);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("locationModel.property_miscellaneous.description"));
    pMiscellaneous.setHelptext(bundle.getString("locationModel.property_miscellaneous.helptext"));
    pMiscellaneous.setOperatingEditable(true);
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }

  private void setVehicleModels(Set<VehicleModel> vehicles) {
    this.vehicles = vehicles;
  }

  private void setBlockModels(Set<BlockModel> blocks) {
    this.blocks = blocks;
  }

  public void setLocation(@Nonnull Location location) {
    this.location = requireNonNull(location, "location");
  }

  public Location getLocation() {
    return location;
  }
}
