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
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for <code>Links</code>.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LinkAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LinkAdapter.class);

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model component.
   * @param eventDispatcher The event dispatcher.
   */
  @Inject
  public LinkAdapter(@Assisted LinkModel model,
                     @Assisted EventDispatcher eventDispatcher) {
    super(model, eventDispatcher);
  }

  @Override
  public LinkModel getModel() {
    return (LinkModel) super.getModel();
  }

  @Override
  public void updateModelProperties(Kernel kernel,
                                    TCSObject<?> tcsObject,
                                    @Nullable ModelLayoutElement layoutElement) {
  }

  @Override
  public void storeToPlantModel(PlantModelCreationTO plantModel) {
    try {
      PointModel point = getModel().getPoint();
      LocationModel location = getModel().getLocation();

      plantModel.getLocations().stream()
          .filter(loc -> Objects.equals(loc.getName(), location.getName()))
          .findFirst()
          .ifPresent(loc -> loc.getLinks().put(point.getName(), getAllowedOperations()));
    }
    catch (ObjectUnknownException | CredentialsException e) {
      LOG.warn("", e);
    }
  }

  private Set<String> getAllowedOperations() {
    StringSetProperty pOperations = (StringSetProperty) getModel().getProperty(LinkModel.ALLOWED_OPERATIONS);
    return new HashSet<>(pOperations.getItems());
  }
}
