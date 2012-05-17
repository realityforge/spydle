package org.realityforge.spydle.store;

import java.util.Arrays;
import org.realityforge.spydle.runtime.TestMetricSource;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Utility class to help sort SourceEntry according to due time.
 */
public final class SourceEntrySchedulingComparatorTest
{
  @Test
  public void sorting()
  {
    final long now = System.currentTimeMillis();
    final SourceEntry entry1 = new SourceEntry( new TestMetricSource(), 100 );
    final SourceEntry entry2 = new SourceEntry( new TestMetricSource(), 100 );

    final SourceEntry[] elements = new SourceEntry[]{ entry1, entry2, null, null };

    entry1.poll( now - 10 );
    entry2.poll( now - 5 );

    Arrays.sort( elements, SourceEntrySchedulingComparator.COMPARATOR );
    assertEquals( elements, new SourceEntry[]{ entry1, entry2, null, null } );

    entry1.poll( now - 5 );
    entry2.poll( now - 10 );

    Arrays.sort( elements, SourceEntrySchedulingComparator.COMPARATOR );
    assertEquals( elements, new SourceEntry[]{ entry2, entry1, null, null } );

    entry1.poll( now - 5 );
    entry2.poll( now - 5 );

    Arrays.sort( elements, SourceEntrySchedulingComparator.COMPARATOR );
    assertEquals( elements, new SourceEntry[]{ entry2, entry1, null, null } );
  }
}
