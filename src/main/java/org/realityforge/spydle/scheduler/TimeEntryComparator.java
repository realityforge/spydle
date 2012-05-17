package org.realityforge.spydle.scheduler;

import java.util.Comparator;

/**
 * Utility class to help sort SourceEntry according to due time.
 */
final class TimeEntryComparator
  implements Comparator<TimeEntry>
{
  static final TimeEntryComparator COMPARATOR = new TimeEntryComparator();

  @Override
  public int compare( final TimeEntry o1, final TimeEntry o2 )
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
      return (int) ( o1.getNextTime() - o2.getNextTime() );
    }
  }
}
