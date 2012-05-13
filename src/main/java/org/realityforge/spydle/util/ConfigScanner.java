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
import javax.annotation.Nonnull;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.realityforge.spydle.runtime.graphite.GraphiteKit;
import org.realityforge.spydle.runtime.jdbc.JdbcKit;
import org.realityforge.spydle.runtime.jmx.JmxKit;
import org.realityforge.spydle.runtime.print.PrintKit;
import org.realityforge.spydle.store.MonitorDataStore;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: 13/05/12
 * Time: 10:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigScanner
  implements Closeable
{
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
    throws IOException
  {
    _watcher.close();
  }

  public void start()
    throws IOException
  {
    _dataStore.close();
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
    throws IOException
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
          final Path file = _configDirectory.toPath().resolve( context ).toFile().getCanonicalFile().toPath();
          if( StandardWatchEventKinds.ENTRY_CREATE == kind )
          {
            System.out.println( "File added: " + file );
            loadConfiguration( file.toFile() );
          }
          else if( StandardWatchEventKinds.ENTRY_DELETE == kind )
          {
            System.out.println( "File removed: " + file );
            _dataStore.deregisterSource( file.toString() );
          }
          else if( StandardWatchEventKinds.ENTRY_MODIFY == kind )
          {
            System.out.println( "File modified: " + file );
            loadConfiguration( file.toFile() );
          }
        }
      }
      key.reset();
    }
  }

  private void loadConfiguration( final File file )
  {
    if( file.getName().endsWith( ".json" ) )
    {
      try
      {
        final JSONObject config = (JSONObject) JSONValue.parse( new FileReader( file ) );
        final String type = ConfigUtil.getValue( config, "type", String.class );
        switch( type )
        {
          case "in:jmx":
            _dataStore.registerSource( file.toString(), JmxKit.build( config ) );
            break;
          case "in:jdbc":
            _dataStore.registerSource( file.toString(), JdbcKit.build( config ) );
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
      catch( Exception e )
      {
        e.printStackTrace();
      }
    }
  }
}
