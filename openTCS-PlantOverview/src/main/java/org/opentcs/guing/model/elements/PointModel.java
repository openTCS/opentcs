/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import java.util.Arrays;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Basic implementation of a point.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class PointModel
    extends AbstractFigureComponent {

  /**
   * Key for the prefered angle of a vehicle on this point.
   */
  public static final String VEHICLE_ORIENTATION_ANGLE = "vehicleOrientationAngle";
  /**
   * Key for the type.
   */
  public static final String TYPE = "Type";

  /**
   * Creates a new instance.
   */
  public PointModel() {
    createProperties();
  }

  @Override // AbstractModelComponent
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("point.description");
  }

  @Override
  public LabeledPointFigure getFigure() {
    return (LabeledPointFigure) super.getFigure();
  }

  public CoordinateProperty getPropertyModelPositionX() {
    return (CoordinateProperty) getProperty(MODEL_X_POSITION);
  }

  public CoordinateProperty getPropertyModelPositionY() {
    return (CoordinateProperty) getProperty(MODEL_Y_POSITION);
  }

  public AngleProperty getPropertyVehicleOrientationAngle() {
    return (AngleProperty) getProperty(VEHICLE_ORIENTATION_ANGLE);
  }

  @SuppressWarnings("unchecked")
  public SelectionProperty<PointType> getPropertyType() {
    return (SelectionProperty<PointType>) getProperty(TYPE);
  }

  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }

  public StringProperty getPropertyLayoutPosX() {
    return (StringProperty) getProperty(ElementPropKeys.POINT_POS_X);
  }

  public StringProperty getPropertyLayoutPosY() {
    return (StringProperty) getProperty(ElementPropKeys.POINT_POS_Y);
  }

  public StringProperty getPropertyPointLabelOffsetX() {
    return (StringProperty) getProperty(ElementPropKeys.POINT_LABEL_OFFSET_X);
  }

  public StringProperty getPropertyPointLabelOffsetY() {
    return (StringProperty) getProperty(ElementPropKeys.POINT_LABEL_OFFSET_Y);
  }

  public StringProperty getPropertyPointLabelOrientationAngle() {
    return (StringProperty) getProperty(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);
  }

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("point.name.text"));
    pName.setHelptext(bundle.getString("point.name.helptext"));
    setProperty(NAME, pName);

    CoordinateProperty pPosX = new CoordinateProperty(this);
    pPosX.setDescription(bundle.getString("point.x.text"));
    pPosX.setHelptext(bundle.getString("point.x.helptext"));
    setProperty(MODEL_X_POSITION, pPosX);

    CoordinateProperty pPosY = new CoordinateProperty(this);
    pPosY.setDescription(bundle.getString("point.y.text"));
    pPosY.setHelptext(bundle.getString("point.y.helptext"));
    setProperty(MODEL_Y_POSITION, pPosY);

    AngleProperty pPhi = new AngleProperty(this);
    pPhi.setDescription(bundle.getString("point.phi.text"));
    pPhi.setHelptext(bundle.getString("point.phi.helptext"));
    setProperty(VEHICLE_ORIENTATION_ANGLE, pPhi);

    SelectionProperty<PointType> pType = new SelectionProperty<>(this,
                                                                 Arrays.asList(PointType.values()),
                                                                 PointType.values()[0]);
    pType.setDescription(bundle.getString("point.type.text"));
    pType.setHelptext(bundle.getString("point.type.helptext"));
    pType.setCollectiveEditable(true);
    setProperty(TYPE, pType);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("point.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("point.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);

    StringProperty pPointPosX = new StringProperty(this);
    pPointPosX.setDescription(bundle.getString("element.pointPosX.text"));
    pPointPosX.setHelptext(bundle.getString("element.pointPosX.helptext"));
    // The position can only be changed in the drawing.
    pPointPosX.setModellingEditable(false);
    setProperty(ElementPropKeys.POINT_POS_X, pPointPosX);

    StringProperty pPointPosY = new StringProperty(this);
    pPointPosY.setDescription(bundle.getString("element.pointPosY.text"));
    pPointPosY.setHelptext(bundle.getString("element.pointPosY.helptext"));
    // The position can only be changed in the drawing.
    pPointPosY.setModellingEditable(false);
    setProperty(ElementPropKeys.POINT_POS_Y, pPointPosY);

    StringProperty pPointLabelOffsetX = new StringProperty(this);
    pPointLabelOffsetX.setDescription(bundle.getString("element.pointLabelOffsetX.text"));
    pPointLabelOffsetX.setHelptext(bundle.getString("element.pointLabelOffsetX.helptext"));
    pPointLabelOffsetX.setModellingEditable(false);
    setProperty(ElementPropKeys.POINT_LABEL_OFFSET_X, pPointLabelOffsetX);

    StringProperty pPointLabelOffsetY = new StringProperty(this);
    pPointLabelOffsetY.setDescription(bundle.getString("element.pointLabelOffsetY.text"));
    pPointLabelOffsetY.setHelptext(bundle.getString("element.pointLabelOffsetY.helptext"));
    pPointLabelOffsetY.setModellingEditable(false);
    setProperty(ElementPropKeys.POINT_LABEL_OFFSET_Y, pPointLabelOffsetY);

    StringProperty pPointLabelOrientationAngle = new StringProperty(this);
    pPointLabelOrientationAngle.setDescription(
        bundle.getString("element.pointLabelOrientationAngle.text"));
    pPointLabelOrientationAngle.setHelptext(
        bundle.getString("element.pointLabelOrientationAngle.helptext"));
    pPointLabelOrientationAngle.setModellingEditable(false);
    setProperty(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE, pPointLabelOrientationAngle);
  }

  /**
   * The supported point types.
   */
  public enum PointType {

    /**
     * A halting position.
     */
    HALT,
    /**
     * A reporting position.
     */
    REPORT,
    /**
     * A parking position.
     */
    PARK;

    @Override
    public String toString() {
      return ResourceBundleUtil.getBundle().getString("point.type." + name() + ".text");
    }
  }
}
