/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * A sequence of vertices that, when connected in their defined order, represent the area that may
 * be occupied by an object.
 * <p>
 * Since an envelope represents a closed geometry, it is expected that the last vertex matches the
 * first one.
 * <p>
 * Note that an envelope with less than four vertices is not reasonable, since it cannot span a
 * two-dimensional plane. Such envelopes are therefore considered empty.
 */
public class Envelope
    implements Serializable {

  private final List<Couple> vertices;

  /**
   * Creates a new instance.
   *
   * @param vertices The sequence of vertices the envelope consists of.
   * @throws IllegalArgumentException If the sequence of vertices is empty or if the last vertext
   * in the sequence does not match the first one.
   */
  public Envelope(@Nonnull List<Couple> vertices) {
    this.vertices = requireNonNull(vertices, "vertices");
    checkArgument(!vertices.isEmpty(), "An envelope must contain some vertices.");
    checkArgument(Objects.equals(vertices.get(0), vertices.get(vertices.size() - 1)),
                  "An envelope's last vertex must match the first one.");
  }

  /**
   * Returns the sequence of vertices the envelope consists of.
   *
   * @return The sequence of vertices the envelope consists of.
   */
  public List<Couple> getVertices() {
    return vertices;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 89 * hash + Objects.hashCode(this.vertices);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Envelope)) {
      return false;
    }

    Envelope other = (Envelope) obj;
    return Objects.equals(this.vertices, other.vertices);
  }

  @Override
  public String toString() {
    return "Envelope{" + "vertices=" + vertices + '}';
  }
}
