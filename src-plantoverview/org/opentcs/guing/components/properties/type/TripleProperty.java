/**
 * (c): IML.
 *
 */
package org.opentcs.guing.components.properties.type;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.data.model.Triple;
import org.opentcs.guing.model.ModelComponent;

/**
 * Ein Property für einen 3D-Punkt. Der Datentyp für die Koordinaten des Punktes
 * ist <code>long</code>.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class TripleProperty
    extends AbstractProperty {

  /**
   * Der Punkt.
   */
  protected Triple fTriple;

  /**
   * Creates a new instance of PointProperty.
   *
   * @param model
   */
  public TripleProperty(ModelComponent model) {
    this(model, null);
  }

  /**
   * Konstruktor, dem ein 3D-Punkt übergeben wird.
   *
   * @param model
   * @param triple
   */
  public TripleProperty(ModelComponent model, Triple triple) {
    super(model);
    fTriple = triple;
  }

  @Override
  public Object getComparableValue() {
    return fTriple.getX() + "," + fTriple.getY() + "," + fTriple.getZ();
  }

  /**
   * Setzt für das Attribut einen neuen Wert und eine neue Maßeinheit. Eine
   * Ausnahme wird ausgelöst, wenn es sich bei der Maßeinheit um keine mögliche
   * Einheit handelt.
   *
   * @param triple
   */
  public void setValue(Triple triple) {
    fTriple = triple;
  }

  @Override
  public Triple getValue() {
    return fTriple;
  }

  @Override
  public String toString() {
    return fTriple == null ? "null"
        : String.format("(%d, %d, %d)", fTriple.getX(), fTriple.getY(), fTriple.getZ());
  }

  @Override
  public void copyFrom(Property property) {
    TripleProperty tripleProperty = (TripleProperty) property;

    try {
      Triple foreignTriple = tripleProperty.getValue();
      setValue(foreignTriple.clone());
    }
    catch (Exception e) {
      Logger.getLogger(TripleProperty.class.getName()).log(Level.SEVERE, "Exception: ", e);
    }
  }
}
