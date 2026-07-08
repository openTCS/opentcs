// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.awt.Color;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A transfer object representing a {@link Color} instance.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class ColorTO {

  private int red;
  private int green;
  private int blue;
}
// CHECKSTYLE:ON
