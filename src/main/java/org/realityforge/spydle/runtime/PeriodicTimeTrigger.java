package org.realityforge.spydle.runtime;

import javax.annotation.Nonnegative;

final class PeriodicTimeTrigger
  implements TimeTrigger
{
  private final int _period;
  private long _triggerTime;

  PeriodicTimeTrigger( @Nonnegative final int period )
  {
    _period = period;
    _triggerTime = System.currentTimeMillis();
  }

  public long getTimeAfter( final long moment )
  {
    if( moment <= _triggerTime )
    {
      return _triggerTime;
    }
    else
    {
      final long over = moment - _triggerTime;
      final long remainder = over % _period;
      return moment + ( _period - remainder );
    }
  }
}



