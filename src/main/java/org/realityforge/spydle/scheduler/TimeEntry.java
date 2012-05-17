package org.realityforge.spydle.scheduler;

import javax.annotation.Nonnull;

final class TimeEntry
{
  @Nonnull
  private final String _name;
  @Nonnull
  private final TimeTrigger _trigger;
  @Nonnull
  private final Runnable _target;

  //cached version of time from TimeTrigger class
  private long _nextTime;

  TimeEntry( @Nonnull final String name,
             @Nonnull final TimeTrigger trigger,
             @Nonnull final Runnable target )
  {
    _name = name;
    _trigger = trigger;
    _target = target;
    _nextTime = _trigger.getTimeAfter( System.currentTimeMillis() );
  }

  @Nonnull
  String getName()
  {
    return _name;
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

