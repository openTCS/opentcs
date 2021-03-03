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

/**
 * A wrapper to wrap a list of strings and write them with JAXB.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
@XmlRootElement(name = "stringList")
public class StringListWrapper
    extends ArrayList<String> {

  @XmlElement(name = "string")
  public List<String> getEntries() {
    return this;
  }
}
