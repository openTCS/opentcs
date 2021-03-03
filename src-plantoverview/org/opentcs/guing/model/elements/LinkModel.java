/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model.elements;

import org.opentcs.guing.components.properties.panel.LinkActionsEditorPanel;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.components.tree.elements.LinkUserObject;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Standardausführung für Link. Verfügt über keine Attribute.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LinkModel
    extends AbstractConnection {

  /**
   * Der Schlüssel für die möglichen Aktionen an der Station in Abhängigkeit vom
   * Meldepunkt.
   */
  public static final String ALLOWED_OPERATIONS = "AllowedOperations";

  /**
   * Creates a new instance.
   */
  public LinkModel() {
    super();
    createProperties();
  }

  /**
   *
   * @return The model of the connected Point
   */
  public PointModel getPoint() {
    if (getStartComponent() instanceof PointModel) {
      return (PointModel) getStartComponent();
    }

    if (getEndComponent() instanceof PointModel) {
      return (PointModel) getEndComponent();
    }

    return null;
  }

  /**
   *
   * @return The model of the connected Location
   */
  public LocationModel getLocation() {
    if (getStartComponent() instanceof LocationModel) {
      return (LocationModel) getStartComponent();
    }

    if (getEndComponent() instanceof LocationModel) {
      return (LocationModel) getEndComponent();
    }

    return null;
  }

  @Override // AbstractFigureComponent
  public LinkUserObject createUserObject() {
    fUserObject = new LinkUserObject(this);

    return (LinkUserObject) fUserObject;
  }

  @Override // AbstractModelComponent
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("link.description");
  }

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    // Name
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("link.name.text"));
    pName.setHelptext(bundle.getString("link.name.helptext"));

    // The name of a link cannot be changed because it is not stored in the kernel model
    pName.setModellingEditable(false);  // ??? Test HH 2014-03-21

    setProperty(NAME, pName);
    // Allowed operations
    StringSetProperty pOperations = new StringSetProperty(this);
    pOperations.setPropertyEditor(LinkActionsEditorPanel.class);
    pOperations.setDescription(bundle.getString("link.action.text"));
    pOperations.setHelptext(bundle.getString("link.action.helptext"));
    setProperty(ALLOWED_OPERATIONS, pOperations);
  }
}
