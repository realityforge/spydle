package org.realityforge.spydle.scheduler;

import java.util.Arrays;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Utility class to help sort SourceEntry according to due time.
 */
public final class TimeEntryComparatorTest
{
  @Test
  public void sorting()
  {
    final long now = System.currentTimeMillis();
    final TimeEntry entry1 = new TimeEntry( "NameA","StageA",new TestTimeTrigger(), new TestTarget() );
    final TimeEntry entry2 = new TimeEntry( "NameB","StageB",new TestTimeTrigger(), new TestTarget() );

    final TimeEntry[] elements = new TimeEntry[]{ entry1, entry2, null, null };

    entry1.setNextTime( now - 10 );
    entry2.setNextTime( now - 5 );

    Arrays.sort( elements, TimeEntryComparator.COMPARATOR );
    assertEquals( elements, new TimeEntry[]{ entry1, entry2, null, null } );

    entry1.setNextTime( now - 5 );
    entry2.setNextTime( now - 10 );

    Arrays.sort( elements, TimeEntryComparator.COMPARATOR );
    assertEquals( elements, new TimeEntry[]{ entry2, entry1, null, null } );

    entry1.setNextTime( now - 5 );
    entry2.setNextTime( now - 5 );

    Arrays.sort( elements, TimeEntryComparator.COMPARATOR );
    assertEquals( elements, new TimeEntry[]{ entry2, entry1, null, null } );
  }
}
