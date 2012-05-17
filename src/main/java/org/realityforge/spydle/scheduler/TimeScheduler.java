package org.realityforge.spydle.scheduler;

import java.util.HashMap;
import java.util.PriorityQueue;
import javax.annotation.Nonnull;

public class TimeScheduler
{
  private final HashMap<String, TimeEntry> _entryMap = new HashMap<>();
  private final PriorityQueue<TimeEntry> _queue =
    new PriorityQueue<>( 10, TimeEntryComparator.COMPARATOR );

  public synchronized void addTrigger( @Nonnull final String name,
                                       @Nonnull final String stage,
                                       @Nonnull final TimeTrigger trigger,
                                       @Nonnull final Runnable target )
  {
    removeTrigger( name );
    final TimeEntry entry = new TimeEntry( name, stage, trigger, target );
    _entryMap.put( name, entry );
    _queue.add( entry );
  }

  public synchronized void removeTrigger( @Nonnull final String name )
  {
    final TimeEntry entry = _entryMap.remove( name );
    if( null != entry )
    {
      _queue.remove( entry );
    }
  }
}
