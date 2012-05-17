package org.realityforge.spydle.store;

import java.util.Comparator;

/**
 * Utility class to help sort SourceEntry according to due time.
 */
final class SourceEntrySchedulingComparator
  implements Comparator<SourceEntry>
{
  static final SourceEntrySchedulingComparator COMPARATOR = new SourceEntrySchedulingComparator();

  @Override
  public int compare( final SourceEntry o1, final SourceEntry o2 )
  {
    if( null == o1 && null == o2 )
    {
      return 0;
    }
    else if( null == o1 )
    {
      return 1;
    }
    else if( null == o2 )
    {
      return -1;
    }
    else
    {
      return (int) ( o1.getNextPollTime() - o2.getNextPollTime() );
    }
  }
}
