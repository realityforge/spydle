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
{
  private static final Logger LOG = Logger.getLogger( MonitorDataStore.class.getName() );

  private final Map<String, MetricSource> _sources = new HashMap<>();
  private final Map<String, MetricSink> _sinks = new HashMap<>();

  public void registerSource( @Nonnull final String key, @Nonnull final MetricSource sink )
  {
    deregisterSource( key );
    _sources.put( key, sink );
  }

  public Collection<MetricSource> sources()
  {
    return _sources.values();
  }

  public void deregisterSource( @Nonnull final String key )
  {
    final MetricSource existing = _sources.remove( key );
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
