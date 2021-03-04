/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.persistence;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opentcs.guing.persistence.CourseObjectProperty.KeyValueCourseProperty;

/**
 * A wrapper to wrap the entries of a <code>KeyValueSetProperty</code>
 * in a list and write them with JAXB.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
@XmlRootElement(name = "keyValueSetEntries")
public class KeyValueSetListWrapper
    extends ArrayList<KeyValueCourseProperty> {

  @XmlElement(name = "keyValueProperty")
  public List<KeyValueCourseProperty> getEntries() {
    return this;
  }
}
