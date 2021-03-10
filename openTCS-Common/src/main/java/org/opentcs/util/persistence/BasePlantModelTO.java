/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The base class for a plant model transfer object.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.PROPERTY)
public class BasePlantModelTO {

  private String version = "";

  @XmlAttribute(required = true)
  public String getVersion() {
    return version;
  }

  public BasePlantModelTO setVersion(@Nonnull String version) {
    requireNonNull(version, "version");
    this.version = version;
    return this;
  }
}
