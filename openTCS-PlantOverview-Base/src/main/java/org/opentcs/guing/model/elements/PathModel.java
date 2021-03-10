/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import java.util.Arrays;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import static org.opentcs.guing.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LinerTypeProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;

/**
 * A connection between two points.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class PathModel
    extends AbstractConnection {

  /**
   * Key for the length.
   */
  public static final String LENGTH = "length";
  /**
   * Key for routing costs.
   */
  public static final String ROUTING_COST = "cost";
  /**
   * Key for maximum forward velocity.
   */
  public static final String MAX_VELOCITY = "maxVelocity";
  /**
   * Key for maximum reverse velocity.
   */
  public static final String MAX_REVERSE_VELOCITY = "maxReverseVelocity";
  /**
   * Key for the locked state.
   */
  public static final String LOCKED = "locked";
  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);

  /**
   * Creates a new instance.
   */
  public PathModel() {
    createProperties();
  }

  @Override
  public String getDescription() {
    return bundle.getString("pathModel.description");
  }

  public LengthProperty getPropertyLength() {
    return (LengthProperty) getProperty(LENGTH);
  }

  public IntegerProperty getPropertyRoutingCost() {
    return (IntegerProperty) getProperty(ROUTING_COST);
  }

  public SpeedProperty getPropertyMaxVelocity() {
    return (SpeedProperty) getProperty(MAX_VELOCITY);
  }

  public SpeedProperty getPropertyMaxReverseVelocity() {
    return (SpeedProperty) getProperty(MAX_REVERSE_VELOCITY);
  }

  public BooleanProperty getPropertyLocked() {
    return (BooleanProperty) getProperty(LOCKED);
  }

  @SuppressWarnings("unchecked")
  public SelectionProperty<Type> getPropertyPathConnType() {
    return (SelectionProperty<Type>) getProperty(ElementPropKeys.PATH_CONN_TYPE);
  }

  public StringProperty getPropertyPathControlPoints() {
    return (StringProperty) getProperty(ElementPropKeys.PATH_CONTROL_POINTS);
  }

  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }

  private void createProperties() {
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("pathModel.property_name.description"));
    pName.setHelptext(bundle.getString("pathModel.property_name.helptext"));
    setProperty(NAME, pName);

    LengthProperty pLength = new LengthProperty(this);
    pLength.setDescription(bundle.getString("pathModel.property_length.description"));
    pLength.setHelptext(bundle.getString("pathModel.property_length.helptext"));
    setProperty(LENGTH, pLength);

    IntegerProperty pCost = new IntegerProperty(this, 1);
    pCost.setDescription(bundle.getString("pathModel.property_routingCost.descritpion"));
    pCost.setHelptext(bundle.getString("pathModel.property_routingCost.helptext"));
    setProperty(ROUTING_COST, pCost);

    SpeedProperty pMaxVelocity = new SpeedProperty(this, 1.0, SpeedProperty.Unit.M_S);
    pMaxVelocity.setDescription(bundle.getString("pathModel.property_maximumVelocity.description"));
    pMaxVelocity.setHelptext(bundle.getString("pathModel.property_maximumVelocity.helptext"));
    setProperty(MAX_VELOCITY, pMaxVelocity);

    SpeedProperty pMaxReverseVelocity = new SpeedProperty(this, 0.0, SpeedProperty.Unit.M_S);
    pMaxReverseVelocity.setDescription(bundle.getString("pathModel.property_maximumReverseVelocity.description"));
    pMaxReverseVelocity.setHelptext(bundle.getString("pathModel.property_maximumReverseVelocity.helptext"));
    setProperty(MAX_REVERSE_VELOCITY, pMaxReverseVelocity);

    LinerTypeProperty pPathConnType = new LinerTypeProperty(this, Arrays.asList(Type.values()), Type.values()[0]);
    pPathConnType.setDescription(bundle.getString("pathModel.property_pathConnectionType.description"));
    pPathConnType.setHelptext(bundle.getString("pathModel.property_pathConnectionType.helptext"));
    pPathConnType.setCollectiveEditable(true);
    setProperty(ElementPropKeys.PATH_CONN_TYPE, pPathConnType);

    StringProperty pPathControlPoints = new StringProperty(this);
    pPathControlPoints.setDescription(bundle.getString("pathModel.property_pathControlPoints.description"));
    pPathControlPoints.setHelptext(bundle.getString("pathModel.property_pathControlPoints.helptext"));
    // Control points may only be moved in the drawing.
    pPathControlPoints.setModellingEditable(false);
    setProperty(ElementPropKeys.PATH_CONTROL_POINTS, pPathControlPoints);

    StringProperty startComponent = new StringProperty(this);
    startComponent.setDescription(bundle.getString("pathModel.property_startComponent.description"));
    startComponent.setModellingEditable(false);
    startComponent.setOperatingEditable(false);
    setProperty(START_COMPONENT, startComponent);
    StringProperty endComponent = new StringProperty(this);
    endComponent.setDescription(bundle.getString("pathModel.property_endComponent.description"));
    endComponent.setModellingEditable(false);
    endComponent.setOperatingEditable(false);
    setProperty(END_COMPONENT, endComponent);

    BooleanProperty pLocked = new BooleanProperty(this);
    pLocked.setDescription(bundle.getString("pathModel.property_locked.description"));
    pLocked.setHelptext(bundle.getString("pathModel.property_locked.helptext"));
    pLocked.setCollectiveEditable(true);
    pLocked.setOperatingEditable(true);
    setProperty(LOCKED, pLocked);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("pathModel.property_miscellaneous.description"));
    pMiscellaneous.setHelptext(bundle.getString("pathModel.property_miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }

  /**
   * The supported liner types for connections.
   */
  public enum Type {

    /**
     * A direct connection.
     */
    DIRECT(ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.direct.description"),
           ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.direct.helptext")),
    /**
     * An elbow connection.
     */
    ELBOW(ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.elbow.description"),
          ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.elbow.helptext")),
    /**
     * A slanted connection.
     */
    SLANTED(ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.slanted.description"),
            ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.slanted.helptext")),
    /**
     * A polygon path with any number of vertecies.
     */
    POLYPATH(ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.polypath.description"),
             ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.polypath.helptext")),

    /**
     * A bezier curve with 2 control points.
     */
    BEZIER(ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.bezier.description"),
           ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.bezier.helptext")),
    /**
     * A bezier curve with 3 control points.
     */
    BEZIER_3(ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.bezier3.description"),
             ResourceBundle.getBundle(BUNDLE_PATH).getString("pathModel.type.bezier3.helptext"));

    private final String description;

    private final String helptext;

    private Type(String description, String helptext) {
      this.description = requireNonNull(description, "description");
      this.helptext = requireNonNull(helptext, "helptext");
    }

    public String getDescription() {
      return description;
    }

    public String getHelptext() {
      return helptext;
    }

    /**
     * Returns the <code>Type</code> constant matching the name in the
     * given input. This method permits extraneous whitespace around the name
     * and is case insensitive, which makes it a bit more liberal than the
     * default <code>valueOf</code> that all enums provide.
     *
     * @param input The name of the enum constant to return.
     * @return The enum constant matching the given name.
     * @throws IllegalArgumentException If this enum has no constant with the
     * given name.
     */
    public static Type valueOfNormalized(String input)
        throws IllegalArgumentException {
      String normalizedInput = requireNonNull(input, "input is null").trim();

      for (Type curType : values()) {
        if (normalizedInput.equalsIgnoreCase(curType.name())) {
          return curType;
        }
      }

      throw new IllegalArgumentException("No match for '" + input + "'");
    }
  }
}
