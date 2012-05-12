package org.realityforge.spydle;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricValue;
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
    throws IOException
  {
    for( final MetricHandler handler : _handlers )
    {
      handler.metrics( metrics );
    }
  }
}
