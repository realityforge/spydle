package org.realityforge.spydle.runtime;

import javax.annotation.Nonnull;
import org.realityforge.spydle.MetricSource;
import org.realityforge.spydle.MetricValueSet;

final class SourceEntry
  implements Runnable, TimeTrigger
{
  static final int MAX_BACK_OFF_FACTOR = 5;
  @Nonnull
  private final MonitorDataStore _dataStore;
  @Nonnull
  private final MetricSource _source;
  @Nonnull
  private final int _period;
  private long _lastPollTime;
  private long _lastFailTime;
  private long _nextPollTime;
  private int _failCount;

  SourceEntry( @Nonnull final MonitorDataStore dataStore, @Nonnull final MetricSource source, final int period )
  {
    _dataStore = dataStore;
    _source = source;
    _period = period;
    poll( System.currentTimeMillis() );
  }

  void poll( final long time )
  {
    //Time should be in the last 100ms
    assert time > System.currentTimeMillis() - 100;
    _lastPollTime = time;
    _failCount = 0;
    _nextPollTime = time + _period;
  }

  void fail( final long time )
  {
    //Time should be in the last 100ms
    assert time > System.currentTimeMillis() - 100;
    _lastFailTime = time;
    _failCount = ( _failCount < MAX_BACK_OFF_FACTOR ) ? _failCount + 1 : MAX_BACK_OFF_FACTOR;
    _nextPollTime = time + (long) ( _period * Math.pow( 2, (_failCount - 1) ) );
  }

  int getFailCount()
  {
    return _failCount;
  }

  @Nonnull
  MetricSource getSource()
  {
    return _source;
  }

  @Override
  public void run()
  {
    final long now = System.currentTimeMillis();
    final MetricValueSet metrics = getSource().poll();
    if( null == metrics )
    {
      fail( now );
    }
    else
    {
      poll( now );
      _dataStore.queueRoute( metrics );
    }
  }

  @Override
  public long getTimeAfter( final long moment )
  {
    return _nextPollTime;
  }

  int getPeriod()
  {
    return _period;
  }

  long getLastPollTime()
  {
    return _lastPollTime;
  }

  long getLastFailTime()
  {
    return _lastFailTime;
  }

  long getNextPollTime()
  {
    return _nextPollTime;
  }
}
