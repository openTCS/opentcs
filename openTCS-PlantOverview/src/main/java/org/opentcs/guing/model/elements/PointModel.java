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
    super();
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

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    // Name
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("point.name.text"));
    pName.setHelptext(bundle.getString("point.name.helptext"));
    setProperty(NAME, pName);
    // Model x position
    CoordinateProperty pPosX = new CoordinateProperty(this);
    pPosX.setDescription(bundle.getString("point.x.text"));
    pPosX.setHelptext(bundle.getString("point.x.helptext"));
    setProperty(MODEL_X_POSITION, pPosX);
    // Model y position
    CoordinateProperty pPosY = new CoordinateProperty(this);
    pPosY.setDescription(bundle.getString("point.y.text"));
    pPosY.setHelptext(bundle.getString("point.y.helptext"));
    setProperty(MODEL_Y_POSITION, pPosY);
    // Todo: Position z 
    // Prefered angle of a vehicle on this point
    AngleProperty pPhi = new AngleProperty(this);
    pPhi.setDescription(bundle.getString("point.phi.text"));
    pPhi.setHelptext(bundle.getString("point.phi.helptext"));
    setProperty(VEHICLE_ORIENTATION_ANGLE, pPhi);

    // Type: Park, Report or Halt
    SelectionProperty<PointType> pType = new SelectionProperty<>(this, Arrays.asList(PointType.values()), PointType.values()[0]);
    pType.setDescription(bundle.getString("point.type.text"));
    pType.setHelptext(bundle.getString("point.type.helptext"));
    pType.setCollectiveEditable(true);
    setProperty(TYPE, pType);
    // Miscellaneous properties
    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("point.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("point.miscellaneous.helptext"));
    // HH 2014-02-17: Miscellaneous Properties vorerst nicht collective editable
//  pMiscellaneous.setCollectiveEditable(true);
    setProperty(MISCELLANEOUS, pMiscellaneous);
    // Das zugehörige Model-Layout-Element
    // Position x im Layout
    StringProperty pPointPosX = new StringProperty(this);
    pPointPosX.setDescription(bundle.getString("element.pointPosX.text"));
    pPointPosX.setHelptext(bundle.getString("element.pointPosX.helptext"));
    // Die Position kann nur in der Drawing verschoben werden.
    // TODO: Auch in der Tabelle editieren?
    pPointPosX.setModellingEditable(false);
    setProperty(ElementPropKeys.POINT_POS_X, pPointPosX);
    // Position y im Layout
    StringProperty pPointPosY = new StringProperty(this);
    pPointPosY.setDescription(bundle.getString("element.pointPosY.text"));
    pPointPosY.setHelptext(bundle.getString("element.pointPosY.helptext"));
    pPointPosY.setModellingEditable(false);
    setProperty(ElementPropKeys.POINT_POS_Y, pPointPosY);
    // Position x des zugehörigen Labels im Layout
    StringProperty pPointLabelOffsetX = new StringProperty(this);
    pPointLabelOffsetX.setDescription(bundle.getString("element.pointLabelOffsetX.text"));
    pPointLabelOffsetX.setHelptext(bundle.getString("element.pointLabelOffsetX.helptext"));
    pPointLabelOffsetX.setModellingEditable(false);
    setProperty(ElementPropKeys.POINT_LABEL_OFFSET_X, pPointLabelOffsetX);
    // Position y des zugehörigen Labels im Layout
    StringProperty pPointLabelOffsetY = new StringProperty(this);
    pPointLabelOffsetY.setDescription(bundle.getString("element.pointLabelOffsetY.text"));
    pPointLabelOffsetY.setHelptext(bundle.getString("element.pointLabelOffsetY.helptext"));
    pPointLabelOffsetY.setModellingEditable(false);
    setProperty(ElementPropKeys.POINT_LABEL_OFFSET_Y, pPointLabelOffsetY);
    // Winkelausrichtung des zugehörigen Labels im Layout
    StringProperty pPointLabelOrientationAngle = new StringProperty(this);
    pPointLabelOrientationAngle.setDescription(bundle.getString("element.pointLabelOrientationAngle.text"));
    pPointLabelOrientationAngle.setHelptext(bundle.getString("element.pointLabelOrientationAngle.helptext"));
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
