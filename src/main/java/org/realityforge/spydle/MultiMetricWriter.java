package org.realityforge.spydle;

import java.io.IOException;
import javax.annotation.Nonnull;

public final class MultiMetricWriter
  implements MetricHandler
{
  private final MetricHandler[] _handlers;

  public MultiMetricWriter( @Nonnull final MetricHandler[] handlers )
  {
    _handlers = handlers;
  }

  public void metric( final String key, final long timeInMillis, final long value )
    throws IOException
  {
    for( final MetricHandler handler : _handlers )
    {
      handler.metric( key, timeInMillis, value );
    }
  }
}
