/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.queries;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opentcs.access.Kernel;

/**
 * A query for all script files available on the kernel side.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@Availability(Kernel.State.OPERATING)
public class QueryAvailableScriptFiles
    extends Query<QueryAvailableScriptFiles>
    implements Serializable {

  /**
   * The list of available script files.
   */
  private final List<String> fileNames;

  /**
   * Creates a new instance.
   *
   * @param fileNames The list of available script files.
   */
  public QueryAvailableScriptFiles(List<String> fileNames) {
    this.fileNames = Collections.unmodifiableList(
        Objects.requireNonNull(fileNames, "fileNames is null"));
  }

  /**
   * Returns the list of available script files.
   *
   * @return The list of available script files.
   */
  public List<String> getFileNames() {
    return fileNames;
  }
}
