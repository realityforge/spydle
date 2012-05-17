package org.realityforge.spydle.scheduler;

public final class PeriodicTimeTrigger
  implements TimeTrigger
{
  protected final long _offset;
  protected final long _period;
  private long _triggerTime;

  public PeriodicTimeTrigger( final int offset, final int period )
  {
    _offset = offset;
    _period = period;

    reset();
  }

  public long getTimeAfter( final long moment )
  {
    if( moment <= _triggerTime )
    {
      return _triggerTime;
    }
    else
    {
      if( -1 == _period )
      {
        return -1;
      }

      final long over = moment - _triggerTime;
      final long remainder = over % _period;

      return moment + ( _period - remainder );
    }
  }

  public long getOffset()
  {
    return _offset;
  }

  public long getPeriod()
  {
    return _period;
  }

  private void reset()
  {
    final long current = System.currentTimeMillis();
    if( -1 == _offset )
    {
      _triggerTime = current;
    }
    else
    {
      _triggerTime = current + _offset;
    }
  }

  public String toString()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append( "PeriodicTimeTrigger[ " );

    sb.append( "trigger time=" );
    sb.append( _triggerTime );
    sb.append( " " );

    sb.append( "offset=" );
    sb.append( _offset );
    sb.append( " " );

    if( -1 != _period )
    {
      sb.append( "period=" );
      sb.append( _period );
      sb.append( " " );
    }

    sb.append( "]" );

    return sb.toString();
  }
}



