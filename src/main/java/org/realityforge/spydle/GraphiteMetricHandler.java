package org.realityforge.spydle;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricValue;
import org.realityforge.spydle.runtime.MetricValueSet;
import org.realityforge.spydle.runtime.graphite.GraphiteService;

/**
 * A simple handler that writes to graphite.
 */
public final class GraphiteMetricHandler
  implements MetricHandler
{
  private final GraphiteService _graphiteService;

  public GraphiteMetricHandler( @Nonnull final GraphiteService graphiteService )
  {
    _graphiteService = graphiteService;
  }

  public void metrics( final MetricValueSet metrics )
  {
    _graphiteService.writeMetric( metrics );
  }
}
