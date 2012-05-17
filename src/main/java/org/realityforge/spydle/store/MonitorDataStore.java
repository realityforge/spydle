package org.realityforge.spydle.store;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricSink;
import org.realityforge.spydle.runtime.MetricSource;

/**
 * Simple store for sources and sinks of monitoring data.
 */
public final class MonitorDataStore
  implements Closeable
{
  private static final Logger LOG = Logger.getLogger( MonitorDataStore.class.getName() );

  private final Map<String, MetricSource> _sources = new HashMap<>();
  private final Map<String, MetricSink> _sinks = new HashMap<>();

  @Override
  public void close()
  {
    LOG.info( "Closing router" );
    for( final Map.Entry<String, MetricSource> entry : _sources.entrySet() )
    {
      doClose( entry.getKey(), entry.getValue() );
    }
    _sources.clear();
    for( final Map.Entry<String, MetricSink> entry : _sinks.entrySet() )
    {
      doClose( entry.getKey(), entry.getValue() );
    }
    _sinks.clear();
  }

  public void registerSource( @Nonnull final String key, @Nonnull final MetricSource source )
  {
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.registerSource(" + key + "," + source + ")" );
    }
    deregisterSource( key );
    _sources.put( key, source );
  }

  public Collection<MetricSource> sources()
  {
    return _sources.values();
  }

  public void deregisterSource( @Nonnull final String key )
  {
    final MetricSource existing = _sources.remove( key );
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.deregisterSource(" + key + ") => " + existing );
    }
    doClose( key, existing );
  }

  private void doClose( final String key, final MetricSource existing )
  {
    if( existing instanceof Closeable )
    {
      try
      {
        ( (Closeable) existing ).close();
      }
      catch( final IOException e )
      {
        LOG.log( Level.FINE, "Error closing existing source for key " + key, e );
      }
    }
  }

  public void registerSink( @Nonnull final String key, @Nonnull final MetricSink sink )
  {
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.registerSink(" + key + "," + sink + ")" );
    }
    deregisterSink( key );
    _sinks.put( key, sink );
  }

  public Collection<MetricSink> sinks()
  {
    return _sinks.values();
  }

  public void deregisterSink( @Nonnull final String key )
  {
    final MetricSink existing = _sinks.remove( key );
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.deregisterSink(" + key + ") => " + existing );
    }
    doClose( key, existing );
  }

  private void doClose( final String key, final MetricSink existing )
  {
    if( existing instanceof Closeable )
    {
      try
      {
        ( (Closeable) existing ).close();
      }
      catch( final IOException e )
      {
        LOG.log( Level.FINE, "Error closing existing sink for key " + key, e );
      }
    }
  }
}
