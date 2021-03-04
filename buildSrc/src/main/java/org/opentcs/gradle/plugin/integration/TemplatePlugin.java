/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.gradle.plugin.integration;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * A gradle plugin to create integration projects.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class TemplatePlugin
    implements Plugin<Project> {

  /**
   * The name of the task this plugin provides.
   */
  private static final String TASK_NAME = "createIntegrationProject";

  @Override
  public void apply(Project project) {
    project.getTasks().create(TASK_NAME, CreateProjectTask.class);
  }
}
