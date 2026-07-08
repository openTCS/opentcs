// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.model.Location;

/**
 * A transfer object representing a {@link Location.Link} instance.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class LinkTO {

  @Nonnull
  private String location;
  @Nonnull
  private String point;
  @Nonnull
  private List<String> allowedOperations;
}
// CHECKSTYLE:ON
