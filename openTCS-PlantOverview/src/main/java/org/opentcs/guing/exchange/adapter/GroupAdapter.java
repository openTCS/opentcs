/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import com.google.inject.assistedinject.Assisted;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.storage.PlantModelCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNull;

/**
 * An adapter for Groups.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class GroupAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(GroupAdapter.class);

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model component.
   * @param eventDispatcher The event dispatcher.
   */
  @Inject
  public GroupAdapter(@Assisted GroupModel model,
                      @Assisted EventDispatcher eventDispatcher) {
    super(model, eventDispatcher);
  }

  @Override
  public GroupModel getModel() {
    return (GroupModel) super.getModel();
  }

  @Override  // OpenTCSProcessAdapter
  public void updateModelProperties(Kernel kernel,
                                    TCSObject<?> tcsObject,
                                    @Nullable ModelLayoutElement layoutElement) {
    Group group = requireNonNull((Group) tcsObject, "tcsObject");

    try {
      StringProperty name
          = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      name.setText(group.getName());

      updateMiscModelProperties(group);
    }
    catch (CredentialsException e) {
      log.warn("", e);
    }
  }

  @Override  // OpenTCSProcessAdapter
  public void updateProcessProperties(Kernel kernel, PlantModelCache plantModel) {
    Group group = kernel.createGroup();
    TCSObjectReference<Group> reference = group.getReference();

    StringProperty pName
        = (StringProperty) getModel().getProperty(ModelComponent.NAME);
    String name = pName.getText();

    try {
      // Neuer Name
      kernel.renameTCSObject(reference, name);

      updateProcessGroup(kernel, group, plantModel);

      updateMiscProcessProperties(kernel, reference);
    }
    catch (KernelRuntimeException e) {
      log.warn("", e);
    }
  }

  private void updateProcessGroup(Kernel kernel, Group group, PlantModelCache plantModel)
      throws ObjectUnknownException, CredentialsException {
    for (TCSObjectReference<?> resRef : group.getMembers()) {
      kernel.removeGroupMember(group.getReference(), resRef);
    }

    for (ModelComponent model : getModel().getChildComponents()) {
      TCSObjectReference<?> memberRef;
      if (model instanceof PointModel) {
        memberRef = plantModel.getPoints().get(model.getName()).getReference();
      }
      else if (model instanceof PathModel) {
        memberRef = plantModel.getPaths().get(model.getName()).getReference();
      }
      else if (model instanceof LocationModel) {
        memberRef = plantModel.getLocations().get(model.getName()).getReference();
      }
      else {
        throw new IllegalArgumentException("Unhandled model type "
            + model.getClass().getName());
      }
      ProcessAdapter adapter = getEventDispatcher().findProcessAdapter(model);

      if (adapter != null) {
        kernel.addGroupMember(group.getReference(), memberRef);
      }
    }

    for (VisualLayout layout : plantModel.getVisualLayouts()) {
      updateLayoutElement(layout, group.getReference());
    }
  }

  /**
   * Refreshes the properties of the layout element and saves it in the kernel.
   *
   * @param layout The VisualLayout.
   */
  private void updateLayoutElement(VisualLayout layout,
                                   TCSObjectReference<?> ref) {
    ModelLayoutElement layoutElement = new ModelLayoutElement(ref);

    // TODO...
////    GroupModel model = (GroupModel) getModel();
////    Map<String, String> layoutProperties = fLayoutElement.getProperties();
////    ColorProperty pColor = (ColorProperty) model.getProperty(ElementPropKeys.BLOCK_COLOR);
////    int rgb = pColor.getColor().getRGB() & 0x00FFFFFF;  // mask alpha bits
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
