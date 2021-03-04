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

import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.GroupCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.GroupModel;

/**
 * An adapter for Groups.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class GroupAdapter
    extends AbstractProcessAdapter {

  @Override  // OpenTCSProcessAdapter
  public void updateModelProperties(TCSObject<?> tcsObject, ModelComponent modelComponent, SystemModel systemModel, TCSObjectService objectService, @Nullable
      ModelLayoutElement layoutElement) {
    Group group = requireNonNull((Group) tcsObject, "tcsObject");
    GroupModel model = (GroupModel) modelComponent;

    model.getPropertyName().setText(group.getName());

    updateMiscModelProperties(model, group);
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    return plantModel.withGroup(
        new GroupCreationTO(modelComponent.getName())
            .withMemberNames(getMemberNames((GroupModel) modelComponent))
            .withProperties(getKernelProperties(modelComponent))
    );
  }

  private Set<String> getMemberNames(GroupModel groupModel) {
    Set<String> result = new HashSet<>();
    for (ModelComponent model : groupModel.getChildComponents()) {
      result.add(model.getName());
    }

    return result;
  }
}
