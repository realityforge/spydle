package org.realityforge.spydle.scheduler;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public class TimeScheduler
{
  public static final int DEFAULT_SLEEP_TIME = 1000;

  private static final Logger LOG = Logger.getLogger( TimeScheduler.class.getName() );

  private final HashMap<String, ThreadPoolExecutor> _executors = new HashMap<>();
  private final HashMap<String, TimeEntry> _entryMap = new HashMap<>();
  private final PriorityBlockingQueue<TimeEntry> _queue =
    new PriorityBlockingQueue<>( 10, TimeEntryComparator.COMPARATOR );
  private boolean _running;

  public synchronized void addTrigger( @Nonnull final String name,
                                       @Nonnull final String stage,
                                       @Nonnull final TimeTrigger trigger,
                                       @Nonnull final Runnable target )
  {
    removeTrigger( name );
    final TimeEntry entry = new TimeEntry( name, stage, trigger, target );
    _entryMap.put( name, entry );
    scheduleEntry( entry );
    notify();
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
        return nextPollTime - now;
      }
    }
    return DEFAULT_SLEEP_TIME;
  }

  private void run( final TimeEntry entry )
  {
    getExecutorForStage( entry.getStage() ).execute( new Runnable()
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

  private ThreadPoolExecutor getExecutorForStage( final String stage )
  {
    ThreadPoolExecutor executor = _executors.get( stage );
    if( null == executor )
    {
      executor = newExecutor();
      _executors.put( stage, executor );
    }
    return executor;
  }

  private ThreadPoolExecutor newExecutor()
  {
    return new ThreadPoolExecutor( 1,
                                   4,
                                   0L,
                                   TimeUnit.MILLISECONDS,
                                   new LinkedBlockingQueue<Runnable>() );
  }

  public void run()
  {
    _running = true;
    while( _running )
    {
      final long sleepTime = tick( System.currentTimeMillis() );

      try
      {
        synchronized( this )
        {
          wait( sleepTime );
        }
      }
      catch( final InterruptedException ie )
      {
        //Ignored
      }
    }
  }
}
