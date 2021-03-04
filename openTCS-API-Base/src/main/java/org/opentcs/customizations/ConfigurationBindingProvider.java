/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customizations;

import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A provider to get bindings (implementations) for configuration interfaces.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 * @deprecated Use {@link org.opentcs.configuration.ConfigurationBindingProvider} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public interface ConfigurationBindingProvider
    extends org.opentcs.configuration.ConfigurationBindingProvider {

}
