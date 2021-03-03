/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs;

import static java.util.Arrays.asList;
import java.util.List;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStoryMaps;
import org.jbehave.core.reporters.StoryReporterBuilder;

/**
 * Generates a "Story Maps"-view that filters the stories according to their
 * meta data.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class StoryMaps
    extends JUnitStoryMaps {

  public StoryMaps() {
    configuredEmbedder().embedderControls()
        .doGenerateViewAfterStories(true)
        .doIgnoreFailureInStories(true);
    configuredEmbedder().useMetaFilters(metaFilters());
  }

  @Override
  public Configuration configuration() {
    return new MostUsefulConfiguration()
        .useStoryReporterBuilder(new StoryReporterBuilder()
            .withCodeLocation(
                CodeLocations.codeLocationFromPath(
                    System.getProperty("opentcs.reports") + "/jbehave/")
            )
            .withRelativeDirectory("jbehave")
        );
  }

  @Override
  protected List<String> metaFilters() {
    return asList("+component kernel",
                  "+component plantoverview",
                  "+component baselib");
  }

  @Override
  protected List<String> storyPaths() {
    return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
  }
}
