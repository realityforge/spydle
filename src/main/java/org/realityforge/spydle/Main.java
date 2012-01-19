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
import java.util.Map;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
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
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;

public class Main
{

  public static final InetSocketAddress GRAPHITE_ADDRESS = new InetSocketAddress( "192.168.0.16", 2003 );

  public static void main( final String[] args )
    throws Exception
  {
    final GraphiteService graphiteService =
      new GraphiteService( new GraphiteServiceDescriptor( GRAPHITE_ADDRESS, "PD42.SS" ) );
    final JmxTaskDescriptor task = defineJobDescriptor();
    final JmxService jmxService = new JmxService( task.getService() );

    final JdbcTaskDescriptor jdbcTask = defineJdbcJobDescriptor();
    final JdbcService jdbcService = new JdbcService( jdbcTask.getService() );

    // first, define the RRD
    final RrdDef rrdDef = new RrdDef( "target/foo.rrd", 300 );
    rrdDef.addDatasource( "inbytes", DsType.GAUGE, 600, 0, Double.NaN );
    rrdDef.addDatasource( "outbytes", DsType.GAUGE, 600, 0, Double.NaN );
    rrdDef.addArchive( ConsolFun.AVERAGE, 0.5, 1, 600 ); // 1 step, 600 rows
    rrdDef.addArchive( ConsolFun.AVERAGE, 0.5, 6, 700 ); // 6 steps, 700 rows
    rrdDef.addArchive( ConsolFun.MAX, 0.5, 1, 600 );


    final RrdDb rrdDb = new RrdDb( rrdDef );
    Sample sample = rrdDb.createSample();

    for( int i = 0; i < 10000000; i++ )
    {
      sample.setTime( System.currentTimeMillis() );
      sample.setValue( "inbytes", 23 );
      sample.setValue( "outbytes", 42 );
      sample.update();
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
    rrdDb.close();
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
}
