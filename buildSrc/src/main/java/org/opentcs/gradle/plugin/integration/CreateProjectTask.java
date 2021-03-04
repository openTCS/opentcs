/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.gradle.plugin.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import static org.opentcs.gradle.plugin.integration.Constants.PROJECT_PREFIX;
import static org.opentcs.gradle.plugin.integration.Constants.PROJECT_PREFIX_INTEGRATION;
import org.opentcs.gradle.plugin.integration.builder.ProjectBuilder;
import org.opentcs.gradle.plugin.integration.util.ProjectPropertyReader;

/**
 * The task to create an integration project.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class CreateProjectTask
    extends DefaultTask {

  public CreateProjectTask() {
  }

  @TaskAction
  public void createProject()
      throws IOException {
    // Get the project name
    String projectName = getProjectName();

    // Get the output directory
    String outputDir = "build/";
    String projectFolderPath = outputDir + PROJECT_PREFIX + PROJECT_PREFIX_INTEGRATION + projectName;
    Path outputPath = getProject().mkdir(new File(projectFolderPath)).toPath();

    // Build the project
    new ProjectBuilder(projectName, outputPath, getProject()).build();
  }

  private String getProjectName() {
    return ProjectPropertyReader.getProjectProperty(getProject(),
                                                    Constants.PROPKEY_PROJECTNAME,
                                                    Constants.PROPVAL_PROJECTNAME_DEFAULT);
  }
}
