package org.realityforge.spydle.runtime;

import org.realityforge.spydle.TestMetricSource;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Utility class to help sort SourceEntry according to due time.
 */
public final class SourceEntryTest
{
  @Test
  public void sourceEntry()
  {
    final long now = System.currentTimeMillis();
    final TestMetricSource source = new TestMetricSource();
    final int period = 100;
    final SourceEntry entry1 = new SourceEntry( new MonitorDataStore( new TimeScheduler( new ExecutionEngine() ) ), source, period );

    assertEquals( entry1.getSource(), source );
    assertEquals( entry1.getPeriod(), period );

    entry1.poll( now );

    assertEquals( entry1.getFailCount(), 0 );
    assertEquals( entry1.getLastPollTime(), now );
    assertEquals( entry1.getNextPollTime(), now + period );

    final long failTime = now + 10;
    entry1.fail( failTime );

    assertEquals( entry1.getFailCount(), 1 );
    assertEquals( entry1.getLastPollTime(), now );
    assertEquals( entry1.getLastFailTime(), failTime );
    assertEquals( entry1.getNextPollTime(), failTime + period );

    final long fail2Time = now + period;
    entry1.fail( fail2Time );

    assertEquals( entry1.getFailCount(), 2 );
    assertEquals( entry1.getLastPollTime(), now );
    assertEquals( entry1.getLastFailTime(), fail2Time );
    assertEquals( entry1.getNextPollTime(), fail2Time + (period * 2) );
  }
}
