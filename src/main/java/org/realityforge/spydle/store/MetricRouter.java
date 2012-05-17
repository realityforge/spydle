package org.realityforge.spydle.store;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricSink;
import org.realityforge.spydle.runtime.MetricSource;
import org.realityforge.spydle.runtime.MetricValueSet;

public final class MetricRouter
{
  private static final Logger LOG = Logger.getLogger( MetricRouter.class.getName() );

  @Nonnull
  private final MonitorDataStore _dataStore;

  public MetricRouter( @Nonnull final MonitorDataStore dataStore )
  {
    _dataStore = dataStore;
  }

  public void tick()
  {
    for( final MetricSource source : _dataStore.sources() )
    {
      handleMetrics( source.poll() );
    }
  }

  private void handleMetrics( final MetricValueSet metrics )
  {
    if( null != metrics )
    {
      for( final MetricSink sink : _dataStore.sinks() )
      {
        try
        {
          sink.handleMetrics( metrics );
        }
        catch( final Throwable t )
        {
          LOG.log( Level.WARNING, "Problem sending metric to sink " + sink, t );
        }
      }
    }
  }
}
