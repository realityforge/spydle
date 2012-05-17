package org.realityforge.spydle.runtime;

import java.io.File;
import java.io.IOException;

/**
 * A class responsible for coordinating the components that make up the Spydle service.
 */
public class SpydleRuntime
{
  private final ExecutionEngine _executionEngine = new ExecutionEngine();
  private final Scheduler _scheduler = new Scheduler( _executionEngine );
  private final MonitorDataStore _dataStore = new MonitorDataStore( _scheduler );

  private File _configDirectory;
  private ConfigScanner _scanner;

  public void start( final File configDirectory )
    throws IOException
  {
    if( null != _configDirectory )
    {
      stop();
    }
    _configDirectory = configDirectory;
    _scanner = new ConfigScanner( _dataStore, _configDirectory );
    _scanner.start();
    _scheduler.addTrigger( "Scanner", "system", new PeriodicTimeTrigger( 200 ), new Runnable()
    {
      @Override
      public void run()
      {
        _scanner.scan();
      }
    } );

    _scheduler.addTrigger( "GC", "system", new PeriodicTimeTrigger( 1000 ), new Runnable()
    {
      @Override
      public void run()
      {
        System.gc();
      }
    } );
  }

  public void stop()
  {
    if( null != _configDirectory )
    {
      _dataStore.close();
      _executionEngine.close();
      _scanner.close();
      _configDirectory = null;
    }
  }

  public long tick()
  {
    return _scheduler.tick( System.currentTimeMillis() );
  }
}
