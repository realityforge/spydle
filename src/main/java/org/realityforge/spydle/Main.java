package org.realityforge.spydle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.realityforge.cli.CLArgsParser;
import org.realityforge.cli.CLOption;
import org.realityforge.cli.CLOptionDescriptor;
import org.realityforge.cli.CLUtil;
import org.realityforge.spydle.descriptors.graphite.GraphiteServiceDescriptor;
import org.realityforge.spydle.descriptors.jdbc.JdbcQuery;
import org.realityforge.spydle.descriptors.jdbc.JdbcServiceDescriptor;
import org.realityforge.spydle.descriptors.jdbc.JdbcTaskDescriptor;
import org.realityforge.spydle.descriptors.jmx.JmxQuery;
import org.realityforge.spydle.descriptors.jmx.JmxServiceDescriptor;
import org.realityforge.spydle.descriptors.jmx.JmxTaskDescriptor;
import org.realityforge.spydle.runtime.MetricName;
import org.realityforge.spydle.runtime.MetricValue;
import org.realityforge.spydle.runtime.Namespace;
import org.realityforge.spydle.runtime.graphite.GraphiteService;
import org.realityforge.spydle.runtime.jdbc.JdbcService;
import org.realityforge.spydle.runtime.jmx.JmxService;

public class Main
{
  private static final int HELP_OPT = 1;
  private static final int HOST_CONFIG_OPT = 'h';
  private static final int PORT_CONFIG_OPT = 'p';
  private static final int VERBOSE_OPT = 'v';

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
  };

  private static final int SUCCESS_EXIT_CODE = 0;
  private static final int ERROR_PARSING_ARGS_EXIT_CODE = 1;

  private static boolean c_verbose;
  private static String c_graphiteHost = "127.0.0.1";
  private static int c_graphitePort = 2003;


  public static void main( final String[] args )
    throws Exception
  {
    if( !processOptions( args ) )
    {
      System.exit( ERROR_PARSING_ARGS_EXIT_CODE );
      return;
    }

    final GraphiteService graphiteService =
      new GraphiteService( new GraphiteServiceDescriptor( c_graphiteHost, c_graphitePort, "PD42.SS" ) );
    final JmxTaskDescriptor task = defineJobDescriptor();
    final JmxService jmxService = new JmxService( task.getService() );

    final JdbcTaskDescriptor jdbcTask = defineJdbcJobDescriptor();
    final JdbcService jdbcService = new JdbcService( jdbcTask.getService() );

    for( int i = 0; i < 10000000; i++ )
    {
      final MetricHandler handler =
        new MultiMetricWriter( new MetricHandler[]{ new GraphiteMetricHandler( graphiteService ),
                                                    new PrintStreamMetricHandler() } );
      for( final JmxQuery query : task.getQueries() )
      {
        collectQueryResults( jmxService.acquireConnection(), handler, query );
      }
      for( final JdbcQuery query : jdbcTask.getQueries() )
      {
        collectJdbcQueryResults( jdbcService.acquireConnection(), handler, query );
      }
      Thread.sleep( task.getDelay() );
    }
    graphiteService.close();
    jmxService.close();
    jdbcService.close();

    System.exit( SUCCESS_EXIT_CODE );
  }

  private static void collectJdbcQueryResults( final Connection connection, final MetricHandler handler, final JdbcQuery query )
    throws SQLException, IOException
  {
    final Statement statement = connection.createStatement();
    final ResultSet resultSet = statement.executeQuery( query.getQuery() );
    final HashMap<String, Integer> columns = new HashMap<String, Integer>();
    final ResultSetMetaData metaData = resultSet.getMetaData();
    final int columnCount = metaData.getColumnCount();
    for( int i = 1; i <= columnCount; i++ )
    {
      final int columnType = metaData.getColumnType( i );
      if( Types.TINYINT == columnType ||
          Types.DECIMAL == columnType ||
          Types.DOUBLE == columnType ||
          Types.FLOAT == columnType ||
          Types.INTEGER == columnType ||
          Types.SMALLINT == columnType ||
          Types.NUMERIC == columnType ||
          Types.BIGINT == columnType )
      {
        columns.put( metaData.getColumnName( i ), i );
      }
    }
    while( resultSet.next() )
    {
      final String key;
      if( null != query.getKeyColumn() )
      {
        key = resultSet.getObject( query.getKeyColumn() ).toString();
      }
      else
      {
        key = null;
      }
      for( final Map.Entry<String, Integer> entry : columns.entrySet() )
      {
        final String columnName = entry.getKey();
        final Object value = resultSet.getObject( columnName );
        final MetricValue metricValue =
          new MetricValue( query.generateKey( key, entry.getKey() ), (Number) value, System.currentTimeMillis() );
        handler.metric( metricValue );
      }
    }
    resultSet.close();
    statement.close();
  }

  private static JdbcTaskDescriptor defineJdbcJobDescriptor()
  {
    final JdbcQuery query1 =
      new JdbcQuery( "CALL 1", null, newNamespace( "Service1" ) );
    final ArrayList<JdbcQuery> queries = new ArrayList<JdbcQuery>();
    queries.add( query1 );

    final JdbcServiceDescriptor service = new JdbcServiceDescriptor( "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:aname", "sa", "" );
    return new JdbcTaskDescriptor( service, queries, 1000 );
  }

  private static JmxTaskDescriptor defineJobDescriptor()
    throws MalformedObjectNameException
  {
    final JmxQuery query1 =
      new JmxQuery( new ObjectName( "java.lang:type=OperatingSystem" ),
                    null,
                    newNamespace( "Service1" ) );
    final HashSet<String> attributeNames = new HashSet<String>();
    attributeNames.add( "FreePhysicalMemorySize" );
    final JmxQuery query2 =
      new JmxQuery( new ObjectName( "java.lang:type=OperatingSystem" ),
                    attributeNames,
                    newNamespace( "Service2" ) );
    final ArrayList<String> nameComponents = new ArrayList<String>();
    nameComponents.add( "type" );
    nameComponents.add( JmxQuery.ATTRIBUTE_COMPONENT );
    nameComponents.add( JmxQuery.DOMAIN_COMPONENT );
    final JmxQuery query3 =
      new JmxQuery( new ObjectName( "java.lang:type=OperatingSystem" ),
                    attributeNames,
                    newNamespace( "Service3"),
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
    final ArrayList<JmxQuery> queries = new ArrayList<JmxQuery>();
    queries.add( query1 );
    queries.add( query2 );
    queries.add( query3 );
    queries.add( query4 );
    queries.add( query5 );
    queries.add( query6 );

    final JmxServiceDescriptor service = new JmxServiceDescriptor( "127.0.0.1", 1105 );
    return new JmxTaskDescriptor( service, queries, 1000 );
  }

  private static Namespace newNamespace( final String serviceName )
  {
    final LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
    map.put( "Service", serviceName );
    return new Namespace( map );
  }

  private static void collectQueryResults( final MBeanServerConnection mBeanServer,
                                           final MetricHandler handler,
                                           final JmxQuery query )
    throws Exception
  {
    final ObjectName objectName = query.getObjectName();
    if( objectName.isPattern() )
    {
      final Set<ObjectName> objectNames = mBeanServer.queryNames( objectName, null );
      for( final ObjectName candidate : objectNames )
      {
        collectQueryResults( mBeanServer, handler, query, candidate );
      }
    }
    else
    {
      collectQueryResults( mBeanServer, handler, query, objectName );
    }
  }

  private static void collectQueryResults( final MBeanServerConnection mBeanServer,
                                           final MetricHandler handler,
                                           final JmxQuery query,
                                           final ObjectName objectName )
    throws Exception
  {
    final MBeanInfo info = mBeanServer.getMBeanInfo( objectName );
    for( final MBeanAttributeInfo attribute : info.getAttributes() )
    {
      final String attributeName = attribute.getName();
      final Set<String> attributeNames = query.getAttributeNames();
      if( null == attributeNames ||
          attributeNames.contains( attributeName ) )
      {
        final Object value = mBeanServer.getAttribute( objectName, attributeName );
        if( value instanceof Number )
        {
          final MetricName name = query.generateKey( objectName, attributeName );
          final MetricValue metricValue =
            new MetricValue( name, (Number) value, System.currentTimeMillis() );
          handler.metric( metricValue );
        }
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
        case HOST_CONFIG_OPT:
          c_graphiteHost = option.getArgument();
          break;
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
