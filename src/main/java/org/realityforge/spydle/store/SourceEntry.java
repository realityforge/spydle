package org.realityforge.spydle.store;

import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricSource;

public final class SourceEntry
{
  public static final int MAX_BACK_OFF_FACTOR = 5;
  @Nonnull
  private final MetricSource _source;
  private final long _period;
  private long _lastPollTime;
  private long _lastFailTime;
  private long _nextPollTime;
  private int _failCount;

  SourceEntry( @Nonnull final MetricSource source, final long period )
  {
    _source = source;
    _period = period;
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

  public int getFailCount()
  {
    return _failCount;
  }

  @Nonnull
  public MetricSource getSource()
  {
    return _source;
  }

  public long getPeriod()
  {
    return _period;
  }

  public long getLastPollTime()
  {
    return _lastPollTime;
  }

  public long getLastFailTime()
  {
    return _lastFailTime;
  }

  public long getNextPollTime()
  {
    return _nextPollTime;
  }
}
