/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.type;

import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.components.properties.panel.SymbolPropertyEditorPanel;
import org.opentcs.guing.model.ModelComponent;

/**
 * Ein grafisches Symbol.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SymbolProperty
    extends AbstractComplexProperty {

  /**
   * Der Dateiname des grafischen Symbols.
   */
  private LocationRepresentation fFile;

  /**
   * Creates a new instance of SymbolProperty
   * @param model
   */
  public SymbolProperty(ModelComponent model) {
    super(model, SymbolPropertyEditorPanel.class);
  }
  
  @Override
  public Object getComparableValue() {
    return fFile;
  }

  /**
   * Setzt den Namen der Datei, welche das grafische Symbol enthält.
   *
   * @param filename der Name der Bilddatei
   */
  public void setLocationRepresentation(LocationRepresentation filename) {
    fFile = filename;
  }

  /**
   * Liefert den Namen der Bilddatei mit dem grafischen Symbol.
   *
   * @return den Namen der Bilddatei
   */
  public LocationRepresentation getLocationRepresentation() {
    return fFile;
  }

  @Override // java.lang.Object
  public String toString() {
    if (fValue != null) {
      return fValue.toString();
    }
    
    return fFile == null ? "" : fFile.name();
  }

  @Override // AbstractProperty
  public void copyFrom(Property property) {
    SymbolProperty symbolProperty = (SymbolProperty) property;
    symbolProperty.setValue(null); // Text "<different Values>" löschen
    setLocationRepresentation(symbolProperty.getLocationRepresentation());
  }
}
