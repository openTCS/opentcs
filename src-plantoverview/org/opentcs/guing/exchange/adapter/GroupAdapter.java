/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.GroupModel;

/**
 * An adapter for Groups.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class GroupAdapter
    extends OpenTCSProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger log = Logger.getLogger(GroupAdapter.class.getName());

  /**
   * Creates a new instance.
   */
  public GroupAdapter() {
    super();
  }

  @Override
  @SuppressWarnings("unchecked")
  public TCSObjectReference<Group> getProcessObject() {
    return (TCSObjectReference<Group>) super.getProcessObject();
  }

  @Override
  public GroupModel getModel() {
    return (GroupModel) super.getModel();
  }

  @Override
  public void setModel(ModelComponent model) {
    if (!GroupModel.class.isInstance(model)) {
      throw new IllegalArgumentException(model + " is not a GroupModel");
    }
    super.setModel(model);
  }

  @Override	// AbstractProcessAdapter
  public Group createProcessObject() throws KernelRuntimeException {
    Group group = kernel().createGroup();
    setProcessObject(group.getReference());
    nameToModel(group);
    register();

    return group;
  }

  @Override	// AbstractProcessAdapter
  public void releaseProcessObject() {
    try {
      kernel().removeTCSObject(getProcessObject());
      super.releaseProcessObject(); // also delete the Adapter
    }
    catch (KernelRuntimeException e) {
      log.log(Level.WARNING, null, e);
    }
  }

  @Override	// OpenTCSProcessAdapter
  public void propertiesChanged(AttributesChangeEvent event) {
    if (event.getInitiator() != this) {
      updateProcessProperties(false);
    }
  }

  @Override	// OpenTCSProcessAdapter
  public void updateModelProperties() {
    TCSObjectReference<Group> reference = getProcessObject();

    synchronized (reference) {
      try {
        Group group = kernel().getTCSObject(Group.class, reference);
        if (group == null) {
          return;
        }

        StringProperty name = (StringProperty) getModel().getProperty(ModelComponent.NAME);
        name.setText(group.getName());

        updateMiscModelProperties(group);
      }
      catch (CredentialsException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  @Override	// OpenTCSProcessAdapter
  public void updateProcessProperties(boolean updateAllProperties) {
    super.updateProcessProperties(updateAllProperties);
    TCSObjectReference<Group> reference = getProcessObject();

    if (isInTransition()) {
      return;
    }

    synchronized (reference) {
      StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      String name = pName.getText();

      try {
        // Neuer Name
        if (updateAllProperties || pName.hasChanged()) {
          kernel().renameTCSObject(reference, name);
        }

        Group group = kernel().getTCSObject(Group.class, reference);
        updateProcessGroup(group, reference);

        updateMiscProcessProperties(updateAllProperties);
      }
      catch (ObjectExistsException e) {
        undo(name, e);
      }
      catch (CredentialsException | ObjectUnknownException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  private void updateProcessGroup(Group group,
                                  TCSObjectReference<Group> reference)
      throws ObjectUnknownException, CredentialsException {
    if (group == null) {
      return;
    }
    for (TCSObjectReference<?> resRef : group.getMembers()) {
      kernel().removeGroupMember(reference, resRef);
    }

    for (ModelComponent model : getModel().getChildComponents()) {
      ProcessAdapter adapter = getEventDispatcher().findProcessAdapter(model);

      if (adapter != null && adapter.getProcessObject() != null) {
        kernel().addGroupMember(
            reference, (TCSResourceReference) adapter.getProcessObject());
      }
    }

    for (VisualLayout layout : kernel().getTCSObjects(VisualLayout.class)) {
      updateLayoutElement(layout);
    }
  }

  /**
   * Refreshes the properties of the layout element and saves it in the kernel.
   *
   * @param layout The VisualLayout.
   */
  private void updateLayoutElement(VisualLayout layout) {
    // Beim ersten Aufruf ein neues Model-Layout-Element erzeugen
    if (fLayoutElement == null) {
      fLayoutElement = new ModelLayoutElement(getProcessObject());
    }
    // TODO...
////    GroupModel model = (GroupModel) getModel();
////    Map<String, String> layoutProperties = fLayoutElement.getProperties();

////    ColorProperty pColor = (ColorProperty) model.getProperty(ElementPropKeys.BLOCK_COLOR);
////    int rgb = pColor.getColor().getRGB() & 0x00FFFFFF;	// mask alpha bits
////    layoutProperties.put(ElementPropKeys.BLOCK_COLOR, String.format("#%06X", rgb));
////    fLayoutElement.setProperties(layoutProperties);
////
////    Set<LayoutElement> layoutElements = layout.getLayoutElements();
////    for (LayoutElement element : layoutElements) {
////      ModelLayoutElement mle = (ModelLayoutElement) element;
////
////      if (mle.getVisualizedObject().getId() == fLayoutElement.getVisualizedObject().getId()) {
////        layoutElements.remove(element);
////        break;
////      }
////    }
////
////    layoutElements.add(fLayoutElement);
////    kernel().setVisualLayoutElements(layout.getReference(), layoutElements);
  }
}
