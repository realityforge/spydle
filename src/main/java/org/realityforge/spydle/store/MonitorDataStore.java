package org.realityforge.spydle.store;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricSink;
import org.realityforge.spydle.runtime.MetricSource;
import org.realityforge.spydle.runtime.MetricValueSet;

/**
 * Simple store for sources and sinks of monitoring data.
 */
public final class MonitorDataStore
  implements Closeable
{
  private static final Logger LOG = Logger.getLogger( MonitorDataStore.class.getName() );
  public static final int DEFAULT_SLEEP_TIME = 1000;

  private final Map<String, SourceEntry> _sources = new HashMap<>();
  private final PriorityQueue<SourceEntry> _sourceQueue =
    new PriorityQueue<>( 10, SourceEntrySchedulingComparator.COMPARATOR );
  private final Map<String, MetricSink> _sinks = new HashMap<>();

  public long tick( final long now )
  {
    SourceEntry entry;
    //noinspection LoopStatementThatDoesntLoop
    while( null != ( entry = _sourceQueue.peek() ) )
    {
      final long nextPollTime = entry.getNextPollTime();
      if( nextPollTime < now )
      {
        // Remove top entry from queue
        _sourceQueue.poll();
        queuePoll( now, entry );
      }
      else
      {
        return nextPollTime - now;
      }
    }
    return DEFAULT_SLEEP_TIME;
  }

  private void queuePoll( final long now, final SourceEntry entry )
  {
    final MetricValueSet metrics = entry.getSource().poll();
    if( null == metrics )
    {
      entry.fail( now );
    }
    else
    {
      entry.poll( now );
      queueRoute( metrics );
    }
    //Re add entry into queue
    _sourceQueue.add( entry );
  }

  private void queueRoute( final MetricValueSet metrics )
  {
    for( final MetricSink sink : _sinks.values() )
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

  public synchronized void clear()
  {
    for( final Map.Entry<String, SourceEntry> entry : _sources.entrySet() )
    {
      doClose( entry.getKey(), entry.getValue().getSource() );
    }
    _sources.clear();
    _sourceQueue.clear();
    for( final Map.Entry<String, MetricSink> entry : _sinks.entrySet() )
    {
      doClose( entry.getKey(), entry.getValue() );
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
    final SourceEntry entry = new SourceEntry( source, stage, pollPeriod );
    _sources.put( key, entry );
    _sourceQueue.add( entry );
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
      _sourceQueue.remove( existing );
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
    _sinks.put( key, sink );
  }

  public synchronized void deregisterSink( @Nonnull final String key )
  {
    final MetricSink existing = _sinks.remove( key );
    if( LOG.isLoggable( Level.FINE ) )
    {
      LOG.fine( "MonitorDataStore.deregisterSink(" + key + ") => " + existing );
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
