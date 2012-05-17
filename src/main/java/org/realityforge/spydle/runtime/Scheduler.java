package org.realityforge.spydle.runtime;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public class Scheduler
{
  public static final int MAX_SLEEP_TIME = 1000;

  private static final Logger LOG = Logger.getLogger( Scheduler.class.getName() );

  @Nonnull
  private final ExecutionEngine _executionEngine;
  private final HashMap<String, TimeEntry> _entryMap = new HashMap<>();
  private final PriorityQueue<TimeEntry> _queue = new PriorityQueue<>( 10, TimeEntryComparator.COMPARATOR );

  public Scheduler( @Nonnull final ExecutionEngine executionEngine )
  {
    _executionEngine = executionEngine;
  }

  public synchronized void addTrigger( @Nonnull final String name,
                                       @Nonnull final String stage,
                                       @Nonnull final TimeTrigger trigger,
                                       @Nonnull final Runnable target )
  {
    removeTrigger( name );
    final TimeEntry entry = new TimeEntry( name, stage, trigger, target );
    _entryMap.put( name, entry );
    scheduleEntry( entry );
  }

  public synchronized void removeTrigger( @Nonnull final String name )
  {
    final TimeEntry entry = _entryMap.remove( name );
    if( null != entry )
    {
      _queue.remove( entry );
    }
  }

  public synchronized long tick( final long now )
  {
    TimeEntry entry;
    //noinspection LoopStatementThatDoesntLoop
    while( null != ( entry = _queue.peek() ) )
    {
      final long nextPollTime = entry.getNextTime();
      if( nextPollTime < now )
      {
        // Remove top entry from queue
        _queue.poll();
        run( entry );
      }
      else
      {
        return Math.min( MAX_SLEEP_TIME, nextPollTime - now );
      }
    }
    return MAX_SLEEP_TIME;
  }

  private void run( final TimeEntry entry )
  {
    _executionEngine.execute( entry.getStage(), new Runnable()
    {
      @Override
      public void run()
      {
        executeEntry( entry );
      }
    } );
  }

  private void executeEntry( final TimeEntry entry )
  {
    try
    {
      entry.getTarget().run();
    }
    catch( final Throwable t )
    {
      LOG.log( Level.WARNING, "Problem executing scheduled task " + entry.getName(), t );
    }
    scheduleEntry( entry );
  }

  private synchronized void scheduleEntry( final TimeEntry entry )
  {
    _queue.add( entry );
  }
}
