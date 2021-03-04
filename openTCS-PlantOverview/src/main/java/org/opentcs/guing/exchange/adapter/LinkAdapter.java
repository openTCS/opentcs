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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.LinkModel;

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
                                    TCSObjectService objectService,
                                    @Nullable ModelLayoutElement layoutElement) {
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
