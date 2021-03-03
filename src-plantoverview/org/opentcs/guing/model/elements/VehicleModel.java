/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model.elements;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.CoursePointProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.TripleProperty;
import org.opentcs.guing.components.tree.elements.VehicleUserObject;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Standardausführung eines Fahrzeugs. Ein Fahrzeug besitzt eine eindeutige
 * Nummer. Ein Fahrzeug ist immer Kindkomponente eines Fahrzeugtyps.
 * <p>
 * <b>Entwurfsmuster:</b> Kompositum. Vehicle ist eine konkrete Blattkomponente.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class VehicleModel
    extends AbstractFigureComponent {

  /**
   * Diese Größen können in der Modellierung geändert werden.
   */
  public static final String LENGTH = "Length";
  public static final String ENERGY_LEVEL_CRITICAL = "EnergyLevelCritical";
  public static final String ENERGY_LEVEL_GOOD = "EnergyLevelGood";
  /**
   * Diese Größen werden vom jeweiligen Fahrzeugtreiber gesetzt und in der GUI
   * nur angezeigt.
   */
  public static final String LOADED = "Loaded";
  public static final String STATE = "State";
  public static final String PROC_STATE = "ProcState";
  public static final String POINT = "Point";
  public static final String NEXT_POINT = "NextPoint";
  public static final String PRECISE_POSITION = "PrecisePosition";
  public static final String INITIAL_POSITION = "InitialPosition";
  public static final String ORIENTATION_ANGLE = "OrientationAngle";
  public static final String ENERGY_LEVEL = "EnergyLevel";
  /**
   * Der Batterieladezustand wird aus dem aktuellen Energy-Level und den
   * Schwellwerten bestimmmt.
   */
  public static final String ENERGY_STATE = "EnergyState";

  /**
   * Der Punkt, auf dem das Fahrzeug steht.
   */
  private PointModel fPoint;
  /**
   * Der Punkt, der als nächstes von dem Fahrzeug angefahren wird.
   */
  private PointModel fNextPoint;
  /**
   * Die aktuelle Position (x,y,z), die der Fahrzeugtreiber gemeldet hat.
   */
  private Triple fPrecisePosition;
  /**
   * Die aktuelle Winkelausrichtung (-360 .. +360 deg).
   */
  private double fOrientationAngle;
  /**
   * Der aktuelle Fahrauftrag des Fahrzeugs.
   */
  private List<FigureComponent> fDriveOrderComponents;
  /*
   * Die Farbe, in welcher gegebenenfalls der aktuelle Fahrauftrag des Fahrzeugs
   * dargestellt werden soll.
   */
  private Color fDriveOrderColor = Color.BLACK;
  /**
   * Flag, ob Farben heller oder dunkler werden.
   */
  private boolean driveOrderColorDesc = false;
  /**
   *
   */
  private TransportOrder.State fDriveOrderState;
  /**
   * Bei
   * <code>true</code> wird der jeweils aktuelle Fahrauftrag visualisiert.
   */
  private boolean fDisplayDriveOrders;
  /**
   * Bei
   * <code>true</code> folgt die Fahrkursansicht dem Fahrzeug.
   */
  private boolean fViewFollows;
  /**
   * A reference to the vehicle.
   */
  private TCSObjectReference<Vehicle> reference;

  /**
   * Creates a new instance.
   */
  public VehicleModel() {
    super();
    createProperties();
  }

  /**
   * Setzt den Namen des Fahrzeugs.
   *
   * @param name der Name des Fahrzeugs
   */
  public void setName(String name) {
    StringProperty p = (StringProperty) getProperty(NAME);
    p.setText(name);
  }

  /**
   * Setzt den Punkt, auf dem sich das Fahrzeug befindet.
   *
   * @param point der Punkt
   */
  public void placeOnPoint(PointModel point) {
    fPoint = point;
  }

  /**
   * @return Der Punkt, auf dem sich das Fahrzeug befindet.
   */
  public PointModel getPoint() {
    return fPoint;
  }

  /**
   * @return Der Punkt, den das Fahrzeug als nächstes anfährt.
   */
  public PointModel getNextPoint() {
    return fNextPoint;
  }

  /**
   * Setzt den Punkt, den das Fahrzeug als nächstes anfährt.
   *
   * @param point der Punkt
   */
  public void setNextPoint(PointModel point) {
    fNextPoint = point;
  }

  /**
   * @return Die aktuelle Position (x,y,z)
   */
  public Triple getPrecisePosition() {
    return fPrecisePosition;
  }

  public void setPrecisePosition(Triple position) {
    fPrecisePosition = position;
  }

  /**
   *
   * @return die aktuelle Winkelausrichtung (-360 .. +360 deg)
   */
  public double getOrientationAngle() {
    return fOrientationAngle;
  }

  public void setOrientationAngle(double angle) {
    fOrientationAngle = angle;
  }

  /**
   * Liefert die Liste der Komponenten entlang des aktuellen Fahrauftrags des
   * Fahrzeugs.
   *
   * @return den Fahrauftrag
   */
  public List<FigureComponent> getDriveOrderComponents() {
    return fDriveOrderComponents;
  }

  /**
   * Setzt die Liste der Komponenten entlang des aktuellen Fahrauftrags des
   * Fahrzeugs.
   *
   * @param driveOrderComponents den Fahrauftrag
   */
  public void setDriveOrderComponents(List<FigureComponent> driveOrderComponents) {
    if (fDriveOrderComponents == null && driveOrderComponents != null) {
      updateDriveOrderColor();
    }
    fDriveOrderComponents = driveOrderComponents;
  }

  /**
   * Macht fDriveOrderColor heller oder dunkler.
   */
  private void updateDriveOrderColor() {
    int red = fDriveOrderColor.getRed();
    int green = fDriveOrderColor.getGreen();
    int blue = fDriveOrderColor.getBlue();

    if (red >= 240 || green >= 240 || blue >= 240) {
      driveOrderColorDesc = true;
    }
    else if (red < 30 && red > 0 || green < 30 && green > 0 || blue < 30 && blue > 0) {
      driveOrderColorDesc = false;
    }

    if (driveOrderColorDesc) {
      red *= 0.8;
      green *= 0.8;
      blue *= 0.8;
      fDriveOrderColor = new Color(red, green, blue);
    }
    else {
      red *= 1.2;
      green *= 1.2;
      blue *= 1.2;

      if (red > 240) {
        red = 240;
      }

      if (green > 240) {
        green = 240;
      }

      if (blue > 240) {
        blue = 240;
      }

      fDriveOrderColor = new Color(red, green, blue);
    }
  }

  /**
   * Liefert die Farbe, in welcher gegebenenfalls der aktuelle Fahrauftrag des
   * Fahrzeugs dargestellt werden soll.
   *
   * @return die Farbe
   */
  public Color getDriveOrderColor() {
    return fDriveOrderColor;
  }

  /**
   * Setzt die Farbe, in welcher gegebenenfalls der aktuelle Fahrauftrag des
   * Fahrzeugs dargestellt werden soll.
   *
   * @param color die Farbe
   */
  public void setDriveOrderColor(Color color) {
    fDriveOrderColor = Objects.requireNonNull(color, "color is null");
  }

  /**
   *
   * @return
   */
  public TransportOrder.State getDriveOrderState() {
    return fDriveOrderState;
  }

  public void setDriveOrderState(TransportOrder.State driveOrderState) {
    fDriveOrderState = driveOrderState;
  }

  /**
   * Teilt mit, ob der jeweils aktuelle Fahrauftrag angezeigt werden soll oder
   * nicht.
   *
   * @param state
   * <code>true</code>, wenn der jeweils aktuelle Fahrauftrag visualisiert
   * werden soll
   */
  public void setDisplayDriveOrders(boolean state) {
    fDisplayDriveOrders = state;
  }

  /**
   * Gibt Auskunft darüber, ob der jeweils aktuelle Fahrauftrag angezeigt wird.
   *
   * @return <code>true</code>, wenn der jeweils aktuelle Fahrauftrag
   * visualisiert wird
   */
  public boolean getDisplayDriveOrders() {
    return fDisplayDriveOrders;
  }

  /**
   * @return
   */
  public boolean isViewFollows() {
    return fViewFollows;
  }

  public void setViewFollows(boolean viewFollows) {
    this.fViewFollows = viewFollows;
  }

  public TCSObjectReference<Vehicle> getReference() {
    return reference;
  }

  public void setReference(TCSObjectReference<Vehicle> reference) {
    this.reference = reference;
  }

  @Override
  public int compareTo(AbstractFigureComponent o) {
    StringProperty property = (StringProperty) getProperty(NAME);
    String name = property.getText();
    property = (StringProperty) o.getProperty(NAME);
    String otherName = property.getText();
   
    return name.compareTo(otherName);
  }

  @Override // AbstractModelComponent
  public String getTreeViewName() {
    String treeViewName = getDescription() + " " + getName();
    
    return treeViewName;
  }

  @Override // AbstractModelComponent
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("vehicle.description");
  }

  @Override // AbstractFigureComponent
  public VehicleUserObject createUserObject() {
    fUserObject = new VehicleUserObject(this);

    return (VehicleUserObject) fUserObject;
  }

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    // Name
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("vehicle.name.text"));
    pName.setHelptext(bundle.getString("vehicle.name.helptext"));
    setProperty(NAME, pName);
    // Fahrzeuglänge
    LengthProperty pLength = new LengthProperty(this);
    pLength.setDescription(bundle.getString("vehicle.length.text"));
    pLength.setHelptext(bundle.getString("vehicle.length.helptext"));
    setProperty(LENGTH, pLength);
    // Unterer Schwellwert für "kritischen" Batterie-Ladezustand
    PercentProperty pEnergyLevelCritical = new PercentProperty(this, true);
    pEnergyLevelCritical.setDescription(bundle.getString("vehicle.energyLevelCritical.text"));
    pEnergyLevelCritical.setHelptext(bundle.getString("vehicle.energyLevelCritical.helptext"));
    setProperty(ENERGY_LEVEL_CRITICAL, pEnergyLevelCritical);
    // Obererer Schwellwert für "guten" Batterie-Ladezustand
    PercentProperty pEnergyLevelGood = new PercentProperty(this, true);
    pEnergyLevelGood.setDescription(bundle.getString("vehicle.energyLevelGood.text"));
    pEnergyLevelGood.setHelptext(bundle.getString("vehicle.energyLevelGood.helptext"));
    setProperty(ENERGY_LEVEL_GOOD, pEnergyLevelGood);
    // Initiale Fahrzeugposition
    CoursePointProperty pInitialPosition = new CoursePointProperty(this);
    pInitialPosition.setDescription(bundle.getString("vehicle.initialPosition.text"));
    pInitialPosition.setHelptext(bundle.getString("vehicle.initialPosition.helptext"));
    setProperty(INITIAL_POSITION, pInitialPosition);
    // Diese Größen werden vom Fahrzeugtreiber gesetzt und sind nicht editierbar
    // Aktueller Batterie-Ladezustand
    PercentProperty pEnergyLevel = new PercentProperty(this, true);
    pEnergyLevel.setDescription(bundle.getString("vehicle.energyLevel.text"));
    pEnergyLevel.setHelptext(bundle.getString("vehicle.energyLevel.helptext"));
    pEnergyLevel.setModellingEditable(false);
    setProperty(ENERGY_LEVEL, pEnergyLevel);
    // Bewertung: Gut, ausreichend, kritisch
    SelectionProperty pEnergyState = new SelectionProperty(this, EnergyState.values(), EnergyState.CRITICAL);
    pEnergyState.setDescription(bundle.getString("vehicle.energyState.text"));
    pEnergyState.setHelptext(bundle.getString("vehicle.energyState.helptext"));
    pEnergyState.setModellingEditable(false);
    setProperty(ENERGY_STATE, pEnergyState);
    // Ist mindestens ein LAM beladen?
    BooleanProperty pLoaded = new BooleanProperty(this);
    pLoaded.setDescription(bundle.getString("vehicle.loaded.text"));
    pLoaded.setHelptext(bundle.getString("vehicle.loaded.helptext"));
    pLoaded.setModellingEditable(false);
    setProperty(LOADED, pLoaded);
    // State
    SelectionProperty pState = new SelectionProperty(this, Vehicle.State.values(), Vehicle.State.UNKNOWN);
    pState.setDescription(bundle.getString("vehicle.state.text"));
    pState.setHelptext(bundle.getString("vehicle.state.helptext"));
    pState.setModellingEditable(false);
    setProperty(STATE, pState);
    // Process state
    SelectionProperty pProcState = new SelectionProperty(this, Vehicle.ProcState.values(), Vehicle.ProcState.UNAVAILABLE);
    pProcState.setDescription(bundle.getString("vehicle.procState.text"));
    pProcState.setHelptext("vehicle.procState.helptext");
    pProcState.setModellingEditable(false);
    setProperty(PROC_STATE, pProcState);
    // Position: Current Point
    StringProperty pPoint = new StringProperty(this);
    pPoint.setDescription(bundle.getString("vehicle.point.text"));
    pPoint.setHelptext(bundle.getString("vehicle.point.helptext"));
    pPoint.setModellingEditable(false);
    setProperty(POINT, pPoint);
    // Position: Next Point
    StringProperty pNextPoint = new StringProperty(this);
    pNextPoint.setDescription(bundle.getString("vehicle.nextPoint.text"));
    pNextPoint.setHelptext(bundle.getString("vehicle.nextPoint.helptext"));
    pNextPoint.setModellingEditable(false);
    setProperty(NEXT_POINT, pNextPoint);
    // Precise position
    TripleProperty pPrecisePosition = new TripleProperty(this);
    pPrecisePosition.setDescription(bundle.getString("vehicle.precisePosition.text"));
    pPrecisePosition.setHelptext(bundle.getString("vehicle.precisePosition.helptext"));
    pPrecisePosition.setModellingEditable(false);
    setProperty(PRECISE_POSITION, pPrecisePosition);
    // Angle orientation
    AngleProperty pOrientationAngle = new AngleProperty(this);
    pOrientationAngle.setDescription(bundle.getString("vehicle.orientationAngle.text"));
    pOrientationAngle.setHelptext(bundle.getString("vehicle.orientationAngle.helptext"));
    pOrientationAngle.setModellingEditable(false);
    setProperty(ORIENTATION_ANGLE, pOrientationAngle);
    // Miscellaneous properties
    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("vehicle.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("vehicle.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }

  public enum EnergyState {

    CRITICAL,
    DEGRADED,
    GOOD
  }
}
