package org.realityforge.spydle.store;

import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricSource;

final class SourceEntry
{
  static final int MAX_BACK_OFF_FACTOR = 5;
  @Nonnull
  private final MetricSource _source;
  @Nonnull
  private final String _stage;
  private final long _period;
  private long _lastPollTime;
  private long _lastFailTime;
  private long _nextPollTime;
  private int _failCount;

  SourceEntry( @Nonnull final MetricSource source, @Nonnull final String stage, final long period )
  {
    _source = source;
    _stage = stage;
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

  @Nonnull
  String getStage()
  {
    return _stage;
  }

  long getPeriod()
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
