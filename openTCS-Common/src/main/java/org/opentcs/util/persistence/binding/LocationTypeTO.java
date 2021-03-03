/**
 * Copyright (c) 2017 Fraunhofer IML
 */
package org.opentcs.util.persistence.binding;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name", "id", "locationNamePrefix", "allowedOperations", "properties"})
public class LocationTypeTO
    extends PlantModelElementTO {

  private String locationNamePrefix;
  private List<AllowedOperationTO> allowedOperations = new ArrayList<>();

  @XmlAttribute
  public String getLocationNamePrefix() {
    return locationNamePrefix;
  }

  public LocationTypeTO setLocationNamePrefix(String locationNamePrefix) {
    this.locationNamePrefix = locationNamePrefix;
    return this;
  }

  @XmlElement(name = "allowedOperation")
  public List<AllowedOperationTO> getAllowedOperations() {
    return allowedOperations;
  }

  public LocationTypeTO setAllowedOperations(@Nonnull List<AllowedOperationTO> allowedOperations) {
    requireNonNull(allowedOperations, "allowedOperations");
    this.allowedOperations = allowedOperations;
    return this;
  }
}
