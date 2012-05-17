package org.realityforge.spydle.store;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricSink;
import org.realityforge.spydle.runtime.MetricSource;
import org.realityforge.spydle.runtime.MetricValueSet;
import org.realityforge.spydle.scheduler.TimeScheduler;

/**
 * Simple store for sources and sinks of monitoring data.
 */
public final class MonitorDataStore
  implements Closeable
{
  private static final Logger LOG = Logger.getLogger( MonitorDataStore.class.getName() );

  @Nonnull
  private final TimeScheduler _scheduler;
  private final Map<String, SourceEntry> _sources = new HashMap<>();
  private final Map<String, SinkEntry> _sinks = new HashMap<>();

  public MonitorDataStore( @Nonnull final TimeScheduler scheduler )
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

  public synchronized void clear()
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

  public synchronized void registerSource( @Nonnull final String key,
                                           @Nonnull final MetricSource source,
                                           @Nonnull final String stage,
                                           final int pollPeriod )
  {
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.registerSource(" + key + "," + source + ")" );
    }
    deregisterSource( key );
    final SourceEntry entry = new SourceEntry( this, source, pollPeriod );
    _sources.put( key, entry );
    _scheduler.addTrigger( key, stage, entry, entry );
  }

  public synchronized void deregisterSource( @Nonnull final String key )
  {
    final SourceEntry existing = _sources.remove( key );
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.deregisterSource(" + key + ") => " + existing );
    }
    if( null != existing )
    {
      _scheduler.removeTrigger( key );
      doClose( key, existing.getSource() );
    }
  }

  public synchronized void registerSink( @Nonnull final String key,
                                         @Nonnull final MetricSink sink,
                                         @Nonnull final String stage )
  {
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.registerSink(" + key + "," + sink + ")" );
    }
    deregisterSink( key );
    _sinks.put( key, new SinkEntry( sink, stage ) );
  }

  public synchronized void deregisterSink( @Nonnull final String key )
  {
    final SinkEntry existing = _sinks.remove( key );
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.deregisterSink(" + key + ") => " + existing );
    }
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
