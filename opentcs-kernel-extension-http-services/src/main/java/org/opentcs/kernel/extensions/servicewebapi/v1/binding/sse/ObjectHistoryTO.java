// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.ObjectHistory;

/**
 * A transfer object (to be used with the SSE API) representing an {@link ObjectHistory} instance.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class ObjectHistoryTO {

  private List<ObjectHistoryEntryTO> entries;

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class ObjectHistoryEntryTO {

    private Instant timestamp;
    private String eventCode;
    private List<String> supplements;
  }
}
// CHECKSTYLE:ON
