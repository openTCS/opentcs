/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange.adapter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.common.model.SystemModel;

/**
 * An adapter for <code>Links</code>.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LinkAdapter
    extends AbstractProcessAdapter {

  @Override
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService) {
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    return plantModel.withLocations(
        plantModel.getLocations().stream()
            .map(loc -> mapLocation((LinkModel) modelComponent, loc))
            .collect(Collectors.toList())
    );
  }

  private LocationCreationTO mapLocation(LinkModel model, LocationCreationTO location) {
    if (!Objects.equals(location.getName(), model.getLocation().getName())) {
      return location;
    }
    return location.withLink(model.getPoint().getName(), getAllowedOperations(model));
  }

  private Set<String> getAllowedOperations(LinkModel model) {
    return new HashSet<>(model.getPropertyAllowedOperations().getItems());
  }

}
