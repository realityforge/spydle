package org.realityforge.spydle;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.realityforge.cli.CLArgsParser;
import org.realityforge.cli.CLOption;
import org.realityforge.cli.CLOptionDescriptor;
import org.realityforge.cli.CLUtil;
import org.realityforge.spydle.runtime.MetricSink;
import org.realityforge.spydle.runtime.MetricValueSet;
import org.realityforge.spydle.runtime.MulticastMetricSink;
import org.realityforge.spydle.runtime.Namespace;
import org.realityforge.spydle.runtime.PrintStreamMetricSink;
import org.realityforge.spydle.runtime.graphite.GraphiteService;
import org.realityforge.spydle.runtime.graphite.GraphiteServiceDescriptor;
import org.realityforge.spydle.runtime.jdbc.JdbcQuery;
import org.realityforge.spydle.runtime.jdbc.JdbcService;
import org.realityforge.spydle.runtime.jdbc.JdbcServiceDescriptor;
import org.realityforge.spydle.runtime.jdbc.JdbcTaskDescriptor;
import org.realityforge.spydle.runtime.jmx.JmxQuery;
import org.realityforge.spydle.runtime.jmx.JmxService;
import org.realityforge.spydle.runtime.jmx.JmxServiceDescriptor;
import org.realityforge.spydle.runtime.jmx.JmxTaskDescriptor;

public class Main
{
  private static final String DEFAULT_CONFIG_DIRECTORY = "./conf.d";

  private static final int HELP_OPT = 1;
  private static final int HOST_CONFIG_OPT = 'h';
  private static final int PORT_CONFIG_OPT = 'p';
  private static final int VERBOSE_OPT = 'v';
  private static final int CONFIG_DIRECTORY_CONFIG_OPT = 'd';

  private static final CLOptionDescriptor[] OPTIONS = new CLOptionDescriptor[]{
    new CLOptionDescriptor( "help",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            HELP_OPT,
                            "print this message and exit" ),
    new CLOptionDescriptor( "host",
                            CLOptionDescriptor.ARGUMENT_REQUIRED,
                            HOST_CONFIG_OPT,
                            "the host of the graphite server. Defaults to the local host." ),
    new CLOptionDescriptor( "port",
                            CLOptionDescriptor.ARGUMENT_REQUIRED,
                            PORT_CONFIG_OPT,
                            "the port of the graphite server. Defaults to " + GraphiteServiceDescriptor.DEFAULT_PORT ),
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
  private static String c_graphiteHost = "127.0.0.1";
  private static int c_graphitePort = GraphiteServiceDescriptor.DEFAULT_PORT;
  private static File c_configDirectory = new File( DEFAULT_CONFIG_DIRECTORY );

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

    final GraphiteService graphiteService =
      new GraphiteService( new GraphiteServiceDescriptor( c_graphiteHost, c_graphitePort, "PD42.SS" ) );
    final JmxTaskDescriptor task = defineJobDescriptor();
    final JmxService jmxService = new JmxService( task );

    final JdbcService jdbcService = new JdbcService( defineJdbcJobDescriptor() );

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
            if( StandardWatchEventKinds.ENTRY_CREATE == kind )
            {
              System.out.println( "File added: " + pathEvent.context().getFileName() );
            }
            else if( StandardWatchEventKinds.ENTRY_DELETE == kind )
            {
              System.out.println( "File removed: " + pathEvent.context().getFileName() );
            }
            else if( StandardWatchEventKinds.ENTRY_MODIFY == kind )
            {
              System.out.println( "File modified: " + pathEvent.context().getFileName() );
            }
          }
        }
      }
      final MetricSink sink =
        new MulticastMetricSink( new MetricSink[]{ graphiteService, new PrintStreamMetricSink( System.out ) } );

      MetricValueSet poll = jmxService.poll();
      if( null != poll )
      {
        sink.handleMetrics( poll );
      }
      poll = jdbcService.poll();
      if( null != poll )
      {
        sink.handleMetrics( poll );
      }
      Thread.sleep( 100 );
    }
    graphiteService.close();
    jmxService.close();
    jdbcService.close();
    watcher.close();

    System.exit( SUCCESS_EXIT_CODE );
  }


  private static JdbcTaskDescriptor defineJdbcJobDescriptor()
  {
    final JdbcQuery query1 =
      new JdbcQuery( "CALL 1", null, newNamespace( "Service1" ) );
    final ArrayList<JdbcQuery> queries = new ArrayList<>();
    queries.add( query1 );

    final JdbcServiceDescriptor service = new JdbcServiceDescriptor( "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:aname", "sa", "" );
    return new JdbcTaskDescriptor( service, queries );
  }

  private static JmxTaskDescriptor defineJobDescriptor()
    throws MalformedObjectNameException
  {
    final JmxQuery query1 =
      new JmxQuery( new ObjectName( "java.lang:type=OperatingSystem" ),
                    null,
                    newNamespace( "Service1" ) );
    final HashSet<String> attributeNames = new HashSet<>();
    attributeNames.add( "FreePhysicalMemorySize" );
    final JmxQuery query2 =
      new JmxQuery( new ObjectName( "java.lang:type=OperatingSystem" ),
                    attributeNames,
                    newNamespace( "Service2" ) );
    final ArrayList<String> nameComponents = new ArrayList<>();
    nameComponents.add( "type" );
    nameComponents.add( JmxQuery.ATTRIBUTE_COMPONENT );
    nameComponents.add( JmxQuery.DOMAIN_COMPONENT );
    final JmxQuery query3 =
      new JmxQuery( new ObjectName( "java.lang:type=OperatingSystem" ),
                    attributeNames,
                    newNamespace( "Service3" ),
                    nameComponents );
    final JmxQuery query4 =
      new JmxQuery( new ObjectName( "java.lang:type=OperatingSystem" ),
                    attributeNames,
                    newNamespace( "Service4" ),
                    new ArrayList<String>() );
    final JmxQuery query5 =
      new JmxQuery( new ObjectName( "java.lang:type=OperatingSystem" ),
                    null,
                    null );
    final JmxQuery query6 =
      new JmxQuery( new ObjectName( "java.lang:type=*" ),
                    null,
                    null );
    final ArrayList<JmxQuery> queries = new ArrayList<>();
    queries.add( query1 );
    queries.add( query2 );
    queries.add( query3 );
    queries.add( query4 );
    queries.add( query5 );
    queries.add( query6 );

    final JmxServiceDescriptor service = new JmxServiceDescriptor( "127.0.0.1", 1105 );
    return new JmxTaskDescriptor( service, queries );
  }

  private static Namespace newNamespace( final String serviceName )
  {
    final LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put( "Service", serviceName );
    return new Namespace( map );
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
        case HOST_CONFIG_OPT:
        {
          c_graphiteHost = option.getArgument();
          break;
        }
        case PORT_CONFIG_OPT:
        {
          final String port = option.getArgument();
          try
          {
            c_graphitePort = Integer.parseInt( port );
          }
          catch( final NumberFormatException nfe )
          {
            error( "parsing port: " + port );
            return false;
          }
          break;
        }
        case CONFIG_DIRECTORY_CONFIG_OPT:
        {
          c_configDirectory = new File( option.getArgument() );
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
      info( "Server Host: " + c_graphiteHost );
      info( "Server Port: " + c_graphitePort );
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
