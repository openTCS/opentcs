/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.io.IOException;
import java.io.InputStream;
import org.opentcs.kernel.workingset.Model;

/**
 * An interface for an XML model reader.
 * 
 * @author Stefan Walter (Fraunhofer IML)
 */
interface XMLModelReader {
  /**
   * Returns the version an implementation supports as a String.
   *
   * @return The version an implementation supports as a String.
   */
  String getVersionString();
  
  /**
   * Reads an XML document and transforms its data to a proper
   * <code>Model</code>.
   *
   * @param inStream The input stream from which the model is to be read.
   * @param model The <code>Model</code> instance into which the data read is
   * written.
   * @throws InvalidModelException If there was a problem interpreting the
   * model.
   */
  void readXMLModel(InputStream inStream, Model model)
  throws IOException, InvalidModelException;
}
