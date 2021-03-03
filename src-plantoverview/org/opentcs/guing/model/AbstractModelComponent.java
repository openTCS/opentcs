/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.tree.TreeViewManager;
import org.opentcs.guing.components.tree.elements.UserObject;

/**
 * Standardimplementierung für eine Blatt-Komponente des Systemmodells.
 * <p>
 * <b>Entwurfsmuster:</b> Kompositum. AbstractModelComponent ist die Komponente.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class AbstractModelComponent
    implements ModelComponent {

  /**
   * Das UserObject, das im Tree dargestellt wird;
   */
  protected UserObject fUserObject;
  /**
   * Der Name der Komponente, wie er im TreeView erscheint.
   */
  private String fTreeViewName;
  /**
   * Ob die Komponente im TreeView angezeigt werden soll.
   */
  private boolean fTreeViewVisibility = true;
  /**
   * Die direkte Elternkomponente.
   */
  private transient ModelComponent fParent;
  /**
   * Die Objekte, die an Änderungen der Attribute interessiert sind.
   */
  private transient List<AttributesChangeListener> fAttributesChangeListeners;
  /**
   * The actual parent of this component. PropertiesCollection e.g.
   * overwrites it.
   */
  private ModelComponent actualParent;
  /**
   * The component's attributes.
   */
  private Map<String, Property> fProperties;

  /**
   * Creates a new instance.
   */
  public AbstractModelComponent() {
    this("");
  }

  /**
   * Creates a new instance with the given name.
   *
   * @param treeViewName The name.
   */
  public AbstractModelComponent(String treeViewName) {
    fTreeViewName = Objects.requireNonNull(treeViewName, "treeViewName is null");
    fProperties = new LinkedHashMap<>();
    fAttributesChangeListeners = new CopyOnWriteArrayList<>();
  }

  @Override
  public abstract UserObject createUserObject();

  @Override
  public UserObject getUserObject() {
    return fUserObject;
  }

  @Override
  public void treeRestore(ModelComponent parent, TreeViewManager treeViewManager) {
    treeViewManager.addItem(parent, this);
  }

  @Override
  public void add(ModelComponent component) {
  }

  @Override
  public void remove(ModelComponent component) {
  }

  @Override
  public List<ModelComponent> getChildComponents() {
    return new ArrayList<>();
  }

  @Override
  public String getTreeViewName() {
    return fTreeViewName;
  }

  @Override
  public boolean contains(ModelComponent component) {
    return false;
  }

  @Override
  public ModelComponent getParent() {
    return fParent;
  }

  @Override
  public ModelComponent getActualParent() {
    return actualParent;
  }

  @Override
  public void setParent(ModelComponent parent) {
    if (parent instanceof PropertiesCollection) {
      actualParent = fParent;
    }
    fParent = parent;
  }

  @Override
  public boolean isTreeViewVisible() {
    return fTreeViewVisibility;
  }

  @Override
  public final void setTreeViewVisibility(boolean visibility) {
    fTreeViewVisibility = visibility;
  }

  @Override // ModelComponent
  public String getDescription() {
    return "";
  }

  @Override
  public String getName() {
    StringProperty property = (StringProperty) getProperty(NAME);

    if (property != null) {
      return property.getText();
    }
    else {
      return new String();
    }
  }

  @Override
  public Property getProperty(String name) {
    return fProperties.get(name);
  }

  @Override
  public Map<String, Property> getProperties() {
    return fProperties;
  }

  @Override
  public void setProperty(String name, Property property) {
    fProperties.put(name, property);
  }

  @Override
  public void addAttributesChangeListener(AttributesChangeListener listener) {
    if (fAttributesChangeListeners == null) {
      fAttributesChangeListeners = new CopyOnWriteArrayList<>();
    }

    if (!fAttributesChangeListeners.contains(listener)) {
      fAttributesChangeListeners.add(listener);
    }
  }

  @Override
  public void removeAttributesChangeListener(AttributesChangeListener listener) {
    if (fAttributesChangeListeners != null) {
      fAttributesChangeListeners.remove(listener);
    }
  }

  @Override
  public boolean containsAttributesChangeListener(AttributesChangeListener listener) {
    if (fAttributesChangeListeners == null) {
      return false;
    }

    return fAttributesChangeListeners.contains(listener);
  }

  @Override
  public void propertiesChanged(AttributesChangeListener initiator) {
    if (fAttributesChangeListeners != null) {
      for (AttributesChangeListener listener : fAttributesChangeListeners) {
        if (initiator != listener) {
          listener.propertiesChanged(new AttributesChangeEvent(initiator, this));
        }
      }
    }
    unmarkAllPropertyChanges();
  }

  @Override
  public AbstractModelComponent clone() throws CloneNotSupportedException {
    AbstractModelComponent clonedModelComponent = (AbstractModelComponent) super.clone();
    clonedModelComponent.fAttributesChangeListeners = new CopyOnWriteArrayList<>();
    // "Shallow" copy of the Map
    clonedModelComponent.fProperties = new LinkedHashMap<>();
    // "Deep" copy: clone all properties
    Map<String, Property> map = getProperties();
    Set<Map.Entry<String, Property>> entrySet = map.entrySet();
    Iterator<Map.Entry<String, Property>> iMap = entrySet.iterator();
    Map.Entry<String, Property> entry;
    String propertyName;
    Property clonedProperty;

    while (iMap.hasNext()) {
      entry = iMap.next();
      propertyName = entry.getKey();
      clonedProperty = (Property) entry.getValue().clone();
      // Don't clone the name - the kernel will create a unique new name
      if (propertyName.equals(NAME)) {
        ((StringProperty) clonedProperty).setText("");
      }

      clonedProperty.setModel(clonedModelComponent);
      clonedModelComponent.setProperty(propertyName, clonedProperty);
    }

    return clonedModelComponent;
  }

  private void unmarkAllPropertyChanges() {
    for (Property property : fProperties.values()) {
      property.unmarkChanged();
    }
  }
}
