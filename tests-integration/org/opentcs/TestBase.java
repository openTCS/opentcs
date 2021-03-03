/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;

/**
 * Base class for JBehave tests.
 * Defines a common configuration.
 * Subclasses should override storyPaths() and stepsFactory() to specify
 * the stories and corresponding mappings to steps.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public abstract class TestBase
    extends JUnitStories {

  public TestBase() {
    super();
    configuredEmbedder().embedderControls()
        .doGenerateViewAfterStories(true)
        .doIgnoreFailureInStories(true);
  }

  @Override
  public Configuration configuration() {
    Configuration config =  new MostUsefulConfiguration();
    config.usePendingStepStrategy(new PassingUponPendingStep());
    config.useStoryReporterBuilder(
        new StoryReporterBuilder()
            .withFormats(Format.XML, Format.HTML, Format.STATS)
            .withFailureTrace(false)
            .withCodeLocation(
                // Note: The last part of the CodeLocation-path is appearantly
                // stripped, therefore we set the redundant RelativeDirectory
                // which is relative to the CodeLocation.
                CodeLocations.codeLocationFromPath(
                    System.getProperty("opentcs.reports") + "/jbehave/"))
            .withRelativeDirectory("jbehave"));
    return config;
  }
}
