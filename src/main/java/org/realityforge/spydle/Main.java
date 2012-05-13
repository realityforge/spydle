package org.realityforge.spydle;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.realityforge.cli.CLArgsParser;
import org.realityforge.cli.CLOption;
import org.realityforge.cli.CLOptionDescriptor;
import org.realityforge.cli.CLUtil;
import org.realityforge.spydle.runtime.MetricSink;
import org.realityforge.spydle.runtime.MetricSource;
import org.realityforge.spydle.runtime.MetricValueSet;
import org.realityforge.spydle.runtime.graphite.GraphiteKit;
import org.realityforge.spydle.runtime.jdbc.JdbcKit;
import org.realityforge.spydle.runtime.jmx.JmxKit;
import org.realityforge.spydle.runtime.print.PrintKit;
import org.realityforge.spydle.store.MonitorDataStore;
import org.realityforge.spydle.util.ConfigUtil;

public class Main
{
  private static final String DEFAULT_CONFIG_DIRECTORY = "./conf.d";

  private static final int HELP_OPT = 1;
  private static final int VERBOSE_OPT = 'v';
  private static final int CONFIG_DIRECTORY_CONFIG_OPT = 'd';

  private static final CLOptionDescriptor[] OPTIONS = new CLOptionDescriptor[]{
    new CLOptionDescriptor( "help",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            HELP_OPT,
                            "print this message and exit" ),
    new CLOptionDescriptor( "verbose",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            VERBOSE_OPT,
                            "print verbose message while sending the message." ),
    new CLOptionDescriptor( "config-directory",
                            CLOptionDescriptor.ARGUMENT_REQUIRED,
                            CONFIG_DIRECTORY_CONFIG_OPT,
                            "the directory in which configuration is read from. Defaults to " + DEFAULT_CONFIG_DIRECTORY ),
  };

  private static final int SUCCESS_EXIT_CODE = 0;
  private static final int ERROR_PARSING_ARGS_EXIT_CODE = 1;

  private static boolean c_verbose;
  private static File c_configDirectory = new File( DEFAULT_CONFIG_DIRECTORY ).getAbsoluteFile();
  private static MonitorDataStore c_dataStore = new MonitorDataStore();

  public static void main( final String[] args )
    throws Exception
  {
    if( !processOptions( args ) )
    {
      System.exit( ERROR_PARSING_ARGS_EXIT_CODE );
      return;
    }


    final WatchService watcher = FileSystems.getDefault().newWatchService();
    final Path path = c_configDirectory.toPath();

    path.register( watcher,
                   StandardWatchEventKinds.ENTRY_CREATE,
                   StandardWatchEventKinds.ENTRY_DELETE,
                   StandardWatchEventKinds.ENTRY_MODIFY );

    final File[] files = c_configDirectory.listFiles();
    if( null != files )
    {
      for( final File file : files )
      {
        loadConfiguration( file );
      }
    }

    for( int i = 0; i < 10000000; i++ )
    {
      final WatchKey key = watcher.poll();
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
            final Path file = path.resolve( context ).toFile().getCanonicalFile().toPath();
            if( StandardWatchEventKinds.ENTRY_CREATE == kind )
            {
              System.out.println( "File added: " + file );
              loadConfiguration( file.toFile() );
            }
            else if( StandardWatchEventKinds.ENTRY_DELETE == kind )
            {
              System.out.println( "File removed: " + file );
              c_dataStore.deregisterSource( file.toString() );
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

      for( final MetricSource source : c_dataStore.sources() )
      {
        handleMetrics( source.poll() );
      }
      Thread.sleep( 100 );
    }

    for( final MetricSink sink : c_dataStore.sinks() )
    {
      if( sink instanceof Closeable )
      {
        ( (Closeable) sink ).close();
      }
    }
    for( final MetricSource source : c_dataStore.sources() )
    {
      if( source instanceof Closeable )
      {
        ( (Closeable) source ).close();
      }
    }
    watcher.close();

    System.exit( SUCCESS_EXIT_CODE );
  }

  private static void handleMetrics( final MetricValueSet metrics )
  {
    if( null != metrics )
    {
      for( final MetricSink sink : c_dataStore.sinks() )
      {
        sink.handleMetrics( metrics );
      }
    }
  }

  private static void loadConfiguration( final File file )
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
            c_dataStore.registerSource( file.toString(), JmxKit.build( config ) );
            break;
          case "in:jdbc":
            c_dataStore.registerSource( file.toString(), JdbcKit.build( config ) );
            break;
          case "out:graphite":
            c_dataStore.registerSink( file.toString(), GraphiteKit.build( config ) );
            break;
          case "out:print":
            c_dataStore.registerSink( file.toString(), PrintKit.build( config ) );
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

  private static boolean processOptions( final String[] args )
  {
    // Parse the arguments
    final CLArgsParser parser = new CLArgsParser( args, OPTIONS );

    //Make sure that there was no errors parsing arguments
    if( null != parser.getErrorString() )
    {
      error( parser.getErrorString() );
      return false;
    }

    // Get a list of parsed options
    @SuppressWarnings( "unchecked" ) final List<CLOption> options = parser.getArguments();
    for( final CLOption option : options )
    {
      switch( option.getId() )
      {
        case CLOption.TEXT_ARGUMENT:
        {
          error( "Unknown argument specified: " + option.getArgument() );
          return false;
        }
        case CONFIG_DIRECTORY_CONFIG_OPT:
        {
          c_configDirectory = new File( option.getArgument() ).getAbsoluteFile();
          break;
        }
        case VERBOSE_OPT:
        {
          c_verbose = true;
          break;
        }
        case HELP_OPT:
        {
          printUsage();
          return false;
        }
      }
    }

    if( !c_configDirectory.exists() )
    {
      error( "Config directory does not exist: " + c_configDirectory );
      return false;
    }

    if( !c_configDirectory.isDirectory() )
    {
      error( "Config directory is not a directory: " + c_configDirectory );
      return false;
    }

    if( c_verbose )
    {
      info( "Config Directory: " + c_configDirectory );
    }

    return true;
  }

  /**
   * Print out a usage statement
   */
  private static void printUsage()
  {
    final String lineSeparator = System.getProperty( "line.separator" );

    final StringBuilder msg = new StringBuilder();

    msg.append( "java " );
    msg.append( Main.class.getName() );
    msg.append( " [options] message" );
    msg.append( lineSeparator );
    msg.append( "Options: " );
    msg.append( lineSeparator );

    msg.append( CLUtil.describeOptions( OPTIONS ).toString() );

    info( msg.toString() );
  }

  private static void info( final String message )
  {
    System.out.println( message );
  }

  private static void error( final String message )
  {
    System.out.println( "Error: " + message );
  }
}
