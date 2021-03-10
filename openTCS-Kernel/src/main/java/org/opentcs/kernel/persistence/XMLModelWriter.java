/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nullable;
import org.opentcs.kernel.workingset.Model;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface XMLModelWriter {
  
  /**
   * Writes a model to a file as XML.
   *
   * @param model The model to be written.
   * @param name If not <code>null</code>, the model will be saved with this
   * name instead of it's current one.
   * @param file The file the model is to be written to.
   * @throws IOException If there was a problem writing the model.
   */
  void writeXMLModel(Model model, @Nullable String name, File file)
  throws IOException;
}
