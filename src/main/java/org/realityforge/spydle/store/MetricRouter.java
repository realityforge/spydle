package org.realityforge.spydle.store;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricSink;
import org.realityforge.spydle.runtime.MetricSource;
import org.realityforge.spydle.runtime.MetricValueSet;

public final class MetricRouter
  implements Closeable
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

  public void close()
    throws IOException
  {
    IOException problem = null;
    LOG.info( "Closing router" );
    for( final MetricSink sink : _dataStore.sinks() )
    {
      if( sink instanceof Closeable )
      {
        try
        {
          ( (Closeable) sink ).close();
        }
        catch( final IOException ioe )
        {
          ioe.fillInStackTrace();
          LOG.log( Level.WARNING, "Problem closing sink " + sink, ioe );
          problem = ioe;
        }
      }
    }
    for( final MetricSource source : _dataStore.sources() )
    {
      if( source instanceof Closeable )
      {
        try
        {
          ( (Closeable) source ).close();
        }
        catch( final IOException ioe )
        {
          ioe.fillInStackTrace();
          LOG.log( Level.WARNING, "Problem closing source " + source, ioe );
          problem = ioe;
        }
      }
    }
    if( null != problem )
    {
      throw problem;
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
