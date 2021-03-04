/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.gradle.plugin.integration.builder;

import java.nio.file.Path;
import javax.annotation.Nullable;
import org.gradle.api.Project;

/**
 * The base class for all project builders.
 * 
 * @author Martin Grzenia (Fraunhofer IML)
 */
public abstract class AbstractProjectBuilder {
  
  /**
   * The path where to output the created project.
   */
  private Path outputPath;
  /**
   * The project the created project is based on.
   */
  @Nullable
  private final Project projectBase;

  /**
   * Creates a new instance.
   * 
   * @param outputPath The path where to output the created project.
   * @param projectBase The project the created project is based on.
   */
  public AbstractProjectBuilder(Path outputPath, @Nullable Project projectBase) {
    this.outputPath = outputPath;
    this.projectBase = projectBase;
  }

  public Path getOutputPath() {
    return outputPath;
  }

  public void setOutputPath(Path outputPath) {
    this.outputPath = outputPath;
  }

  public Project getProjectBase() {
    return projectBase;
  }

  /**
   * Builds/creates the project.
   * 
   * @throws Exception If an error occurs during the building process.
   */
  public abstract void build()
      throws Exception;
}
