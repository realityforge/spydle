package org.realityforge.spydle;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricValue;
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

  public void metric( final MetricValue metric )
    throws IOException
  {
    final StringBuilder sb = new StringBuilder();
    final String prefix = _graphiteService.getDescriptor().getPrefix();
    if( null != prefix )
    {
      sb.append( prefix );
      if( sb.length() > 0 )
      {
        sb.append( '.' );
      }
    }
    sb.append( metric.getKey() );
    sb.append( ' ' );
    sb.append( metric.getValue() );
    sb.append( ' ' );
    sb.append( toUnixEpoch( metric.getCollectedAt() ) );
    sb.append( '\n' );
    _graphiteService.acquireConnection().write( sb.toString().getBytes( "US-ASCII" ) );
  }

  private long toUnixEpoch( final long timeInMillis )
  {
    return timeInMillis / 1000;
  }
}
