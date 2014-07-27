package org.realityforge.spydle.runtime;

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
import javax.json.Json;
import javax.json.JsonObject;
import org.realityforge.spydle.graphite.GraphiteKit;
import org.realityforge.spydle.jdbc.JdbcKit;
import org.realityforge.spydle.jmx.JmxKit;
import org.realityforge.spydle.print.PrintKit;

/**
 * Utility class that monitors a configuration directory and updates data store when configuration changes.
 */
final class ConfigScanner
  implements Closeable
{
  private static final Logger LOG = Logger.getLogger( ConfigScanner.class.getName() );
  public static final int DEFAULT_PERIOD = 1000 * 10;

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
    catch ( final Throwable t )
    {
      LOG.log( Level.WARNING, "Error closing watcher", t );
    }
  }

  void start()
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
    if ( null != files )
    {
      for ( final File file : files )
      {
        loadConfiguration( file );
      }
    }
  }

  void scan()
  {
    final WatchKey key = _watcher.poll();
    if ( null != key )
    {
      for ( final WatchEvent<?> event : key.pollEvents() )
      {
        final WatchEvent.Kind<?> kind = event.kind();
        if ( StandardWatchEventKinds.OVERFLOW != kind )
        {
          @SuppressWarnings("unchecked")
          final WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
          final Path context = pathEvent.context();
          final File file =
            _configDirectory.toPath().resolve( context ).toFile().toPath().toFile();
          if ( file.getName().endsWith( ".json" ) )
          {
            if ( StandardWatchEventKinds.ENTRY_CREATE == kind )
            {
              LOG.info( "Configuration file added: " + file );
              loadConfiguration( file );
            }
            else if ( StandardWatchEventKinds.ENTRY_DELETE == kind )
            {
              LOG.info( "Configuration file removed: " + file );
              unloadConfiguration( file );
            }
            else if ( StandardWatchEventKinds.ENTRY_MODIFY == kind )
            {
              LOG.info( "Configuration file modified: " + file );
              unloadConfiguration( file );
              loadConfiguration( file );
            }
          }
        }
      }
      key.reset();
    }
  }

  private void unloadConfiguration( @Nonnull final File file )
  {
    final String key = file.toString();
    if ( _dataStore.isSourceRegistered( key ) )
    {
      _dataStore.deregisterSource( key );
    }
    else if ( _dataStore.isSinkRegistered( key ) )
    {
      _dataStore.deregisterSink( key );
    }
  }

  private void loadConfiguration( @Nonnull final File file )
  {
    try
    {
      final JsonObject config = Json.createReader( new FileReader( file ) ).readObject();
      final String type = config.getString( "type" );

      // Period really only makes sense for sources
      final int refreshPeriod = config.getInt( "period", -1 );
      final int pollPeriod = -1 == refreshPeriod ? refreshPeriod : DEFAULT_PERIOD;
      final String stage = config.getString( "stage", type );
      final JsonObject subConfig =
        config.containsKey( "config" ) ? config.getJsonObject( "config" ) : Json.createObjectBuilder().build();
      switch ( type )
      {
        case "in:jmx":
          _dataStore.registerSource( file.toString(), JmxKit.build( subConfig ), stage, pollPeriod );
          break;
        case "in:jdbc":
          _dataStore.registerSource( file.toString(), JdbcKit.build( subConfig ), stage, pollPeriod );
          break;
        case "out:graphite":
          _dataStore.registerSink( file.toString(), GraphiteKit.build( subConfig ), stage );
          break;
        case "out:print":
          _dataStore.registerSink( file.toString(), PrintKit.build( subConfig ), stage );
          break;
        default:
          throw new IllegalArgumentException( "Unknown type '" + type + "' in configuration: " + config );
      }
    }
    catch ( final Throwable t )
    {
      LOG.log( Level.WARNING, "Error parsing configuration file: " + file, t );
    }
  }
}
