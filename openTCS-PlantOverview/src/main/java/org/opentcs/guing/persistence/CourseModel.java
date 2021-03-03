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
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * JAXB class for a <code>SystemModel</code>
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
@XmlRootElement(name = "model")
@XmlSeeAlso({KeyValueSetListWrapper.class,
             StringListWrapper.class})
public class CourseModel {

  /**
   * Set of course elements in the model.
   */
  private List<CourseElement> elements = new ArrayList<>();

  /**
   * Create instance.
   */
  public CourseModel() {
  }

  /**
   * Add a course element to the model.
   *
   * @param element The course element.
   * @return true if the model did not already contain the element.
   */
  public boolean add(CourseElement element) {
    return elements.add(element);
  }

  public void setCourseElements(List<CourseElement> elements) {
    this.elements = elements == null ? new ArrayList<>() : elements;
  }

  @XmlElement(name = "element", required = false)
  public List<CourseElement> getCourseElements() {
    return elements;
  }
}
