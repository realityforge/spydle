package org.realityforge.spydle.scheduler;

public class TestTimeTrigger
  implements TimeTrigger
{
  private long _time;

  public TestTimeTrigger( final long time )
  {
    _time = time;
  }

  public TestTimeTrigger()
  {
    this( System.currentTimeMillis() );
  }

  @Override
  public long getTimeAfter( final long moment )
  {
    return _time;
  }
}
