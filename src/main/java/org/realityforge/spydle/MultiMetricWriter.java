package org.realityforge.spydle;

import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricValueSet;

public final class MultiMetricWriter
  implements MetricHandler
{
  private final MetricHandler[] _handlers;

  public MultiMetricWriter( @Nonnull final MetricHandler[] handlers )
  {
    _handlers = handlers;
  }

  public void metrics( final MetricValueSet metrics )
  {
    for( final MetricHandler handler : _handlers )
    {
      handler.metrics( metrics );
    }
  }
}
