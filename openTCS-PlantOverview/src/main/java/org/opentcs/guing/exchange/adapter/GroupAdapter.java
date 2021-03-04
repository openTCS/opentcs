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
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.to.model.GroupCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.GroupModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOG = LoggerFactory.getLogger(GroupAdapter.class);

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

    StringProperty name
        = (StringProperty) getModel().getProperty(ModelComponent.NAME);
    name.setText(group.getName());

    updateMiscModelProperties(group);
  }

  @Override  // OpenTCSProcessAdapter
  public void storeToPlantModel(PlantModelCreationTO plantModel) {
    plantModel.getGroups().add(new GroupCreationTO(getModel().getName())
        .setMemberNames(getMemberNames())
        .setProperties(getKernelProperties()));
  }

  private Set<String> getMemberNames() {
    Set<String> result = new HashSet<>();
    for (ModelComponent model : getModel().getChildComponents()) {
      if (getEventDispatcher().findProcessAdapter(model) != null) {
        result.add(model.getName());
      }
    }

    return result;
  }
}
