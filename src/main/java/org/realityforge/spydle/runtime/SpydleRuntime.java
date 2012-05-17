package org.realityforge.spydle.runtime;

import java.io.File;
import java.io.IOException;
import org.realityforge.spydle.util.ConfigScanner;

/**
 * A class responsible for coordinating the components that make up the Spydle service.
 */
public class SpydleRuntime
{
  private final ExecutionEngine _executionEngine = new ExecutionEngine();
  private final TimeScheduler _scheduler = new TimeScheduler( _executionEngine );
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
    _scanner = new ConfigScanner( getDataStore(), _configDirectory );
    _scanner.start();
  }

  public void stop()
  {
    if( null != _configDirectory )
    {
      getDataStore().close();
      _scanner.close();
      _configDirectory = null;
    }
  }

  public ConfigScanner getScanner()
  {
    return _scanner;
  }

  public ExecutionEngine getExecutionEngine()
  {
    return _executionEngine;
  }

  public TimeScheduler getScheduler()
  {
    return _scheduler;
  }

  public MonitorDataStore getDataStore()
  {
    return _dataStore;
  }
}
