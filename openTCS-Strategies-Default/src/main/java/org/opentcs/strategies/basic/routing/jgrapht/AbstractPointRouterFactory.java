/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.Iterator;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.routing.PointRouter;
import org.opentcs.strategies.basic.routing.PointRouterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates {@link PointRouter} instances with algorithm implementations created by subclasses.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class AbstractPointRouterFactory
    implements PointRouterFactory {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractPointRouterFactory.class);
  /**
   * The kernel providing the model data.
   */
  private final LocalKernel kernel;
  /**
   * Maps the plant model to a graph.
   */
  private final ModelGraphMapper mapper;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel providing model data.
   * @param mapper Maps the plant model to a graph.
   */
  public AbstractPointRouterFactory(@Nonnull LocalKernel kernel,
                                    @Nonnull ModelGraphMapper mapper) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.mapper = requireNonNull(mapper, "mapper");
  }

  @Override
  public PointRouter createPointRouter(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    long timeStampBefore = System.currentTimeMillis();

    Set<Point> points = kernel.getTCSObjects(Point.class);
    WeightedGraph<String, ModelEdge> graph = mapper.translateModel(points,
                                                                   kernel.getTCSObjects(Path.class),
                                                                   vehicle);

    PointRouter router = new ShortestPathPointRouter(createShortestPathAlgorithm(graph), points);
    // Make a single request for a route from one point to a different one to make sure the
    // point router is primed. (Some implementations are initialized lazily.)
    if (points.size() >= 2) {
      Iterator<Point> pointIter = points.iterator();
      router.getRouteSteps(pointIter.next(), pointIter.next());
    }

    LOG.debug("Created point router for {} in {} milliseconds.",
              vehicle.getName(),
              System.currentTimeMillis() - timeStampBefore);

    return router;
  }

  /**
   * Returns a shortest path algorithm implementation working on the given graph.
   *
   * @param graph The graph.
   * @return A shortest path algorithm implementation working on the given graph.
   */
  protected abstract ShortestPathAlgorithm<String, ModelEdge> createShortestPathAlgorithm(
      WeightedGraph<String, ModelEdge> graph);
}
