package org.realityforge.spydle.runtime;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.realityforge.spydle.MetricSink;
import org.realityforge.spydle.MetricSource;
import org.realityforge.spydle.MetricValueSet;

/**
 * Simple store for sources and sinks of monitoring data.
 */
final class MonitorDataStore
  implements Closeable
{
  private static final Logger LOG = Logger.getLogger( MonitorDataStore.class.getName() );

  @Nonnull
  private final Scheduler _scheduler;
  private final Map<String, SourceEntry> _sources = new HashMap<>();
  private final Map<String, SinkEntry> _sinks = new HashMap<>();

  MonitorDataStore( @Nonnull final Scheduler scheduler )
  {
    _scheduler = scheduler;
  }

  void queueRoute( final MetricValueSet metrics )
  {
    for( final SinkEntry sink : _sinks.values() )
    {
      try
      {
        sink.getSink().handleMetrics( metrics );
      }
      catch( final Throwable t )
      {
        LOG.log( Level.WARNING, "Problem sending metric to sink " + sink, t );
      }
    }
  }

  synchronized void clear()
  {
    for( final Map.Entry<String, SourceEntry> entry : _sources.entrySet() )
    {
      doClose( entry.getKey(), entry.getValue().getSource() );
      _scheduler.removeTrigger( entry.getKey() );
    }
    _sources.clear();
    for( final Map.Entry<String, SinkEntry> entry : _sinks.entrySet() )
    {
      doClose( entry.getKey(), entry.getValue().getSink() );
    }
    _sinks.clear();
  }

  @Override
  public synchronized void close()
  {
    LOG.info( "Closing router" );
    clear();
  }

  synchronized void registerSource( @Nonnull final String key,
                                    @Nonnull final MetricSource source,
                                    @Nonnull final String stage,
                                    final int pollPeriod )
  {
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.registerSource(" + key + "," + source + ")" );
    }
    doDeregisterSource( key, _sources.remove( key ) );
    final SourceEntry entry = new SourceEntry( this, source, pollPeriod );
    _sources.put( key, entry );
    _scheduler.addTrigger( key, stage, entry, entry );
  }

  synchronized boolean isSourceRegistered( @Nonnull final String key )
  {
    return null != _sources.remove( key );
  }

  synchronized void deregisterSource( @Nonnull final String key )
  {
    final SourceEntry existing = _sources.remove( key );
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.deregisterSource(" + key + ") => " + existing );
    }
    doDeregisterSource( key, existing );
  }

  private void doDeregisterSource( final String key, final SourceEntry existing )
  {
    if( null != existing )
    {
      _scheduler.removeTrigger( key );
      doClose( key, existing.getSource() );
    }
  }

  synchronized void registerSink( @Nonnull final String key,
                                  @Nonnull final MetricSink sink,
                                  @Nonnull final String stage )
  {
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.registerSink(" + key + "," + sink + ")" );
    }
    doDeregisterSink( key, _sinks.remove( key ) );
    _sinks.put( key, new SinkEntry( sink, stage ) );
  }

  synchronized boolean isSinkRegistered( @Nonnull final String key )
  {
    return null != _sinks.remove( key );
  }

  synchronized void deregisterSink( @Nonnull final String key )
  {
    final SinkEntry existing = _sinks.remove( key );
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.deregisterSink(" + key + ") => " + existing );
    }
    doDeregisterSink( key, existing );
  }

  private void doDeregisterSink( final String key, final SinkEntry existing )
  {
    if( null != existing )
    {
      doClose( key, existing.getSink() );
    }
  }

  private void doClose( final String key, final MetricSource existing )
  {
    doClose( "source", key, existing );
  }

  private void doClose( final String key, final MetricSink existing )
  {
    doClose( "sink", key, existing );
  }

  private void doClose( final String type, final String key, final Object object )
  {
    if( object instanceof Closeable )
    {
      try
      {
        ( (Closeable) object ).close();
      }
      catch( final IOException e )
      {
        LOG.log( Level.FINE, "Error closing " + type + " for key " + key, e );
      }
    }
  }
}
