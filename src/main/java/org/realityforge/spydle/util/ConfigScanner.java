package org.realityforge.spydle.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.realityforge.spydle.runtime.graphite.GraphiteKit;
import org.realityforge.spydle.runtime.jdbc.JdbcKit;
import org.realityforge.spydle.runtime.jmx.JmxKit;
import org.realityforge.spydle.runtime.print.PrintKit;
import org.realityforge.spydle.store.MonitorDataStore;

/**
 * Utility class that monitors a configuration directory and updates data store when configuration changes.
 */
public final class ConfigScanner
  implements Closeable
{
  private static final Logger LOG = Logger.getLogger( ConfigScanner.class.getName() );

  @Nonnull
  private final MonitorDataStore _dataStore;
  @Nonnull
  private final File _configDirectory;

  private WatchService _watcher;

  public ConfigScanner( @Nonnull final MonitorDataStore dataStore,
                        @Nonnull final File configDirectory )
  {
    _dataStore = dataStore;
    _configDirectory = configDirectory;
  }

  @Override
  public void close()
  {
    try
    {
      _watcher.close();
    }
    catch( final Throwable t )
    {
      LOG.log( Level.WARNING, "Error closing watcher", t );
    }
  }

  public void start()
    throws IOException
  {
    _dataStore.clear();
    _watcher = FileSystems.getDefault().newWatchService();

    _configDirectory.toPath().
      register( _watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY );

    final File[] files = _configDirectory.listFiles();
    if( null != files )
    {
      for( final File file : files )
      {
        loadConfiguration( file );
      }
    }
  }

  public void scan()
  {
    final WatchKey key = _watcher.poll();
    if( null != key )
    {
      for( final WatchEvent<?> event : key.pollEvents() )
      {
        final WatchEvent.Kind<?> kind = event.kind();
        if( StandardWatchEventKinds.OVERFLOW != kind )
        {
          @SuppressWarnings( "unchecked" )
          final WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
          final Path context = pathEvent.context();
          final File file =
            _configDirectory.toPath().resolve( context ).toFile().toPath().toFile();
          if( file.getName().endsWith( ".json" ) )
          {
            if( StandardWatchEventKinds.ENTRY_CREATE == kind )
            {
              LOG.info( "Configuration file added: " + file );
              loadConfiguration( file );
            }
            else if( StandardWatchEventKinds.ENTRY_DELETE == kind )
            {
              LOG.info( "Configuration file removed: " + file );
              _dataStore.deregisterSource( file.toString() );
            }
            else if( StandardWatchEventKinds.ENTRY_MODIFY == kind )
            {
              LOG.info( "Configuration file modified: " + file );
              loadConfiguration( file );
            }
          }
        }
      }
      key.reset();
    }
  }

  private void loadConfiguration( final File file )
  {
    try
    {
      final JSONObject config = (JSONObject) JSONValue.parse( new FileReader( file ) );
      final String type = ConfigUtil.getValue( config, "type", String.class );
      final int pollPeriod = 1000 * 10;
      switch( type )
      {
        case "in:jmx":
          _dataStore.registerSource( file.toString(), JmxKit.build( config ), pollPeriod );
          break;
        case "in:jdbc":
          _dataStore.registerSource( file.toString(), JdbcKit.build( config ), pollPeriod );
          break;
        case "out:graphite":
          _dataStore.registerSink( file.toString(), GraphiteKit.build( config ) );
          break;
        case "out:print":
          _dataStore.registerSink( file.toString(), PrintKit.build( config ) );
          break;
        default:
          throw new IllegalArgumentException( "Unknown type '" + type + "' in configuration: " + config );
      }
    }
    catch( final Throwable t )
    {
      LOG.log( Level.WARNING, "Error parsing configuration file: " + file, t );
    }
  }
}

