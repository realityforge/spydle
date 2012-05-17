package org.realityforge.spydle.runtime;

import javax.annotation.Nonnull;

final class TimeEntry
{
  @Nonnull
  private final String _name;
  @Nonnull
  private final String _stage;
  @Nonnull
  private final TimeTrigger _trigger;
  @Nonnull
  private final Runnable _target;

  //cached version of time from TimeTrigger class
  private long _nextTime;

  TimeEntry( @Nonnull final String name,
             @Nonnull final String stage,
             @Nonnull final TimeTrigger trigger,
             @Nonnull final Runnable target )
  {
    _name = name;
    _stage = stage;
    _trigger = trigger;
    _target = target;
    _nextTime = getTimeTrigger().getTimeAfter( System.currentTimeMillis() );
  }

  @Nonnull
  String getName()
  {
    return _name;
  }

  @Nonnull
  String getStage()
  {
    return _stage;
  }

  @Nonnull
  Runnable getTarget()
  {
    return _target;
  }

  @Nonnull
  TimeTrigger getTimeTrigger()
  {
    return _trigger;
  }

  long getNextTime()
  {
    return _nextTime;
  }

  void setNextTime( final long nextTime )
  {
    _nextTime = nextTime;
  }


  public String toString()
  {
    return "TimeEntry[ name=" + _name + " time=" + _nextTime + " ]";
  }
}

