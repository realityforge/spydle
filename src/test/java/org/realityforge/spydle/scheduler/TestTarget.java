package org.realityforge.spydle.scheduler;

public class TestTarget
  implements Runnable
{
  private int _runCount;

  public int getRunCount()
  {
    return _runCount;
  }

  @Override
  public void run()
  {
    _runCount++;
  }
}
