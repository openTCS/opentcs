/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.util;

import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the PlantOverview application (in modeling mode).
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(PlantOverviewModelingApplicationConfiguration.PREFIX)
public interface PlantOverviewModelingApplicationConfiguration {
  
  /**
   * This configuration's prefix.
   */
  String PREFIX = "plantoverviewapp";

  @ConfigurationEntry(
      type = "String",
      description = {"The plant overview application's locale, as a BCP 47 language tag.",
                     "Examples: 'en', 'de', 'zh'"},
      orderKey = "0_init_0")
  String locale();

  @ConfigurationEntry(
      type = "Class name",
      description = {
        "The name of the class to be used for the location theme.",
        "Must be a class extending org.opentcs.components.plantoverview.LocationTheme"
      },
      orderKey = "3_themes_0"
  )
  Class<? extends LocationTheme> locationThemeClass();
}
