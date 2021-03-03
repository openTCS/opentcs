/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model.elements;

import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LocationThemeProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.VehicleThemeProperty;
import org.opentcs.guing.components.tree.elements.LayoutUserObject;
import org.opentcs.guing.model.CompositeModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Basisimplementierung für ein Layout.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class LayoutModel
    extends CompositeModelComponent {

  public static final String SCALE_X = "scaleX";
  public static final String SCALE_Y = "scaleY";
  public static final String LOCATION_THEME = "locTheme";
  public static final String VEHICLE_THEME = "vehTheme";

  /**
   * Creates a new instance.
   */
  public LayoutModel() {
    super();
    createProperties();
  }

  /**
   * Creates a new instance.
   *
   * @param name The name of the model.
   */
  public LayoutModel(String name) {
    super(name);
    createProperties();
  }

  @Override
  public LayoutUserObject createUserObject() {
    fUserObject = new LayoutUserObject(this);

    return (LayoutUserObject) fUserObject;
  }

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    // Name
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("layout.name.text"));
    pName.setHelptext(bundle.getString("layout.name.helptext"));
    setProperty(NAME, pName);
    // Scale of x-axis
    LengthProperty pScaleX = new LengthProperty(this);
    pScaleX.setDescription(bundle.getString("layout.scaleX.text"));
    pScaleX.setHelptext(bundle.getString("layout.scaleX.helptext"));
    setProperty(SCALE_X, pScaleX);
    // Scale of y-axis
    LengthProperty pScaleY = new LengthProperty(this);
    pScaleY.setDescription(bundle.getString("layout.scaleY.text"));
    pScaleY.setHelptext(bundle.getString("layout.scaleY.helptext"));
    setProperty(SCALE_Y, pScaleY);
    // LocationTheme
    LocationThemeProperty pLocationTheme = new LocationThemeProperty(this);
    pLocationTheme.setDescription(bundle.getString("locationTheme.text"));
    pLocationTheme.setHelptext(bundle.getString("locationTheme.helptext"));
    pLocationTheme.setOperatingEditable(true);
    setProperty(LOCATION_THEME, pLocationTheme);
    // VehicleTheme
    VehicleThemeProperty pVehicleTheme = new VehicleThemeProperty(this);
    pVehicleTheme.setDescription(bundle.getString("vehicleTheme.text"));
    pVehicleTheme.setHelptext(bundle.getString("vehicleTheme.helptext"));
    pVehicleTheme.setOperatingEditable(true);
    setProperty(VEHICLE_THEME, pVehicleTheme);
    // Miscellaneous
    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("layout.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("layout.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }
}
