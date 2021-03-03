/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model.elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.components.tree.elements.LocationUserObject;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Basisimplementierung für alle Arten von Stationen (auch Aufzüge, Drehteller
 * und dergleichen).
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LocationModel
    extends AbstractFigureComponent
    implements AttributesChangeListener {

  /**
   * The property key for the location's type.
   */
  public static final String TYPE = "Type";
////	// Schlüssel für das Label -> TODO: in ObjectPropConstants definieren
////	public static final String LABEL = "Label";
  /**
   * Der Stationstyp.
   */
  private transient LocationTypeModel fLocationType;

  /**
   * Creates a new instance.
   */
  public LocationModel() {
    super();
    createProperties();
  }

  @Override // AbstractFigureComponent
  public LocationUserObject createUserObject() {
    fUserObject = new LocationUserObject(this);

    return (LocationUserObject) fUserObject;
  }

  @Override
  public LabeledLocationFigure getFigure() {
    return (LabeledLocationFigure) super.getFigure();
  }

  /**
   * Setzt den Stationstyp.
   *
   * @param type der Stationstyp
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
   * Liefert den Stationstyp.
   *
   * @return den Stationstyp
   */
  public LocationTypeModel getLocationType() {
    return fLocationType;
  }

  /**
   * Aktualisiert den Namen der Location.
   */
  protected void updateName() {
    StringProperty property = (StringProperty) getProperty(NAME);
    String oldName = property.getText();
    String newName = getName();
    property.setText(newName);

    if (!newName.equals(oldName)) {
      property.markChanged();
    }

    propertiesChanged(this);
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

  @Override // AbstractModelComponent
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("location.description");
  }

  @Override // AttributesChangeListener
  public void propertiesChanged(AttributesChangeEvent e) {
    if (fLocationType.getProperty(ModelComponent.NAME).hasChanged()) {
      updateName();
    }

    if (fLocationType.getProperty(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION).hasChanged()) {
      propertiesChanged(this);
    }
  }

  /**
   *
   * @param types
   */
  public void updateTypeProperty(List<LocationTypeModel> types) {
    List<String> possibleValues = new ArrayList<>();
    String value = null;
    Iterator<LocationTypeModel> eLocationTypes = types.iterator();

    while (eLocationTypes.hasNext()) {
      LocationTypeModel type = eLocationTypes.next();
      possibleValues.add(type.getName());

      if (type == fLocationType) {
        value = type.getName();
      }
    }

    SelectionProperty pType = (SelectionProperty) getProperty(LocationModel.TYPE);
    pType.setPossibleValues(possibleValues.toArray());
    pType.setValue(value);
    pType.markChanged();
  }

  @Override
  public int compareTo(AbstractFigureComponent o) {
    return getName().compareTo(o.getName());
  }

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    // Name
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("location.name.text"));
    pName.setHelptext(bundle.getString("location.name.helptext"));
    setProperty(NAME, pName);
    // Position x
    CoordinateProperty pPosX = new CoordinateProperty(this, true);
    pPosX.setDescription(bundle.getString("location.x.text"));
    pPosX.setHelptext(bundle.getString("location.x.helptext"));
    setProperty(MODEL_X_POSITION, pPosX);
    // Position y
    CoordinateProperty pPosY = new CoordinateProperty(this, false);
    pPosY.setDescription(bundle.getString("location.y.text"));
    pPosY.setHelptext(bundle.getString("location.y.helptext"));
    setProperty(MODEL_Y_POSITION, pPosY);
    // Location type
    SelectionProperty pType = new SelectionProperty(this);
    pType.setDescription(bundle.getString("location.type.text"));
    pType.setHelptext(bundle.getString("location.type.helptext"));
    setProperty(TYPE, pType);
//		// Beschriftung
//    StringProperty pLabel = new StringProperty(this);
//		pLabel.setDescription(bundle.getString("location.label.text"));
//		pLabel.setHelptext(bundle.getString("location.label.helptext"));
//		pLabel.setCollectiveEditable(true);
//		setProperty(LABEL, pLabel);
    // Symbol - der gehörige Enum-Wert wird unter Miscellaneous Properies gespeichert
    SymbolProperty pSymbol = new SymbolProperty(this);
    pSymbol.setDescription(bundle.getString("location.symbol.text"));
    pSymbol.setHelptext(bundle.getString("location.symbol.helptext"));
    pSymbol.setCollectiveEditable(true);
    pSymbol.setOperatingEditable(true);
    setProperty(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION, pSymbol);
    // Das zugehörige Model-Layout-Element
    // Die Position kann nur in der Drawing verschoben werden.
    // TODO: Auch in der Tabelle editieren?
    // Position x im Layout
    StringProperty pLocPosX = new StringProperty(this);
    pLocPosX.setDescription(bundle.getString("element.locPosX.text"));
    pLocPosX.setHelptext(bundle.getString("element.locPosX.helptext"));
    pLocPosX.setModellingEditable(false);
    setProperty(ElementPropKeys.LOC_POS_X, pLocPosX);
    // Position y im Layout
    StringProperty pLocPosY = new StringProperty(this);
    pLocPosY.setDescription(bundle.getString("element.locPosY.text"));
    pLocPosY.setHelptext(bundle.getString("element.locPosY.helptext"));
    pLocPosY.setModellingEditable(false);
    setProperty(ElementPropKeys.LOC_POS_Y, pLocPosY);
    // Position x des zugehörigen Labels im Layout
    StringProperty pLocLabelOffsetX = new StringProperty(this);
    pLocLabelOffsetX.setDescription(bundle.getString("element.locLabelOffsetX.text"));
    pLocLabelOffsetX.setHelptext(bundle.getString("element.locLabelOffsetX.helptext"));
    pLocLabelOffsetX.setModellingEditable(false);
    setProperty(ElementPropKeys.LOC_LABEL_OFFSET_X, pLocLabelOffsetX);
    // Position y des zugehörigen Labels im Layout
    StringProperty pLocLabelOffsetY = new StringProperty(this);
    pLocLabelOffsetY.setDescription(bundle.getString("element.locLabelOffsetY.text"));
    pLocLabelOffsetY.setHelptext(bundle.getString("element.locLabelOffsetY.helptext"));
    pLocLabelOffsetY.setModellingEditable(false);
    setProperty(ElementPropKeys.LOC_LABEL_OFFSET_Y, pLocLabelOffsetY);
    // Winkelausrichtung des zugehörigen Labels im Layout
    StringProperty pLocLabelOrientationAngle = new StringProperty(this);
    pLocLabelOrientationAngle.setDescription(bundle.getString("element.locLabelOrientationAngle.text"));
    pLocLabelOrientationAngle.setHelptext(bundle.getString("element.locLabelOrientationAngle.helptext"));
    pLocLabelOrientationAngle.setModellingEditable(false);
    setProperty(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE, pLocLabelOrientationAngle);
    // Miscellaneous Properties 
    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("location.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("location.miscellaneous.helptext"));
    // HH 2014-02-17: Miscellaneous Properties vorerst nicht collective editable
//  pMiscellaneous.setCollectiveEditable(true);
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }
}
