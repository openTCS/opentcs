/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model.elements;

import org.jhotdraw.draw.Figure;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.tree.elements.PointUserObject;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Basisimplementierung für einen Knoten.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class PointModel
    extends AbstractFigureComponent {

  /**
   * Die bevorzugte Winkelausrichtung eines Fahrzeugs auf diesem Punkt.
   */
  public static final String VEHICLE_ORIENTATION_ANGLE = "vehicleOrientationAngle";
  /**
   * Der Schlüssel für den Typ.
   */
  public static final String TYPE = "Type";

  /**
   * Creates a new instance.
   */
  public PointModel() {
    super();
    createProperties();
  }

  /**
   * Creates a new instance.
   *
   * @param figure The point's figure.
   */
  public PointModel(Figure figure) {
    super(figure);
    createProperties();
  }

  /**
   * Setzt den Namen des Knotens.
   *
   * @param name der neue Knotenname
   */
  public void setName(String name) {
    StringProperty p = (StringProperty) getProperty(ModelComponent.NAME);
    p.setText(name);
  }

  @Override // Comparable
  public int compareTo(AbstractFigureComponent o) {
    return getName().compareTo(o.getName());
  }

  @Override // AbstractFigureComponent
  public PointUserObject createUserObject() {
    fUserObject = new PointUserObject(this);

    return (PointUserObject) fUserObject;
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
    CoordinateProperty pPosX = new CoordinateProperty(this, true);
    pPosX.setDescription(bundle.getString("point.x.text"));
    pPosX.setHelptext(bundle.getString("point.x.helptext"));
    setProperty(MODEL_X_POSITION, pPosX);
    // Model y position
    CoordinateProperty pPosY = new CoordinateProperty(this, false);
    pPosY.setDescription(bundle.getString("point.y.text"));
    pPosY.setHelptext(bundle.getString("point.y.helptext"));
    setProperty(MODEL_Y_POSITION, pPosY);
    // Todo: Position z bzw. Layer
    // Die bevorzugte Ausrichtung eines Fahrzeugs auf diesem Punkt
    AngleProperty pPhi = new AngleProperty(this);
    pPhi.setDescription(bundle.getString("point.phi.text"));
    pPhi.setHelptext(bundle.getString("point.phi.helptext"));
    setProperty(VEHICLE_ORIENTATION_ANGLE, pPhi);

    // Typ: Haltepunkt, Meldepunkt oder Parkposition
    SelectionProperty pType = new SelectionProperty(this, PointType.values(), PointType.values()[0]);
    pType.setDescription(bundle.getString("point.type.text"));
    pType.setHelptext(bundle.getString("point.type.helptext"));
    // Diese Property soll für mehrere Punkte gemeinsam editert werden können
    pType.setCollectiveEditable(true);
    setProperty(TYPE, pType);
    // Miscellaneous properties
    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("point.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("point.miscellaneous.helptext"));
    // Diese Property soll für mehrere Punkte gemeinsam editert werden können
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
