package org.realityforge.spydle;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.realityforge.spydle.descriptors.graphite.GraphiteServiceDescriptor;
import org.realityforge.spydle.descriptors.jmx.JmxServiceDescriptor;
import org.realityforge.spydle.descriptors.jmx.JmxTaskDescriptor;
import org.realityforge.spydle.descriptors.jmx.Query;
import org.realityforge.spydle.runtime.MetricValue;
import org.realityforge.spydle.runtime.graphite.GraphiteService;
import org.realityforge.spydle.runtime.jmx.JmxService;

public class Main
{

  public static final InetSocketAddress GRAPHITE_ADDRESS = new InetSocketAddress( "192.168.0.14", 2003 );

  public static void main( final String[] args )
    throws Exception
  {
    final GraphiteService graphiteService =
      new GraphiteService( new GraphiteServiceDescriptor( GRAPHITE_ADDRESS, "PD42.SS" ) );
    final JmxTaskDescriptor task = defineJobDescriptor();
    final JmxService jmxService = new JmxService( task.getService() );

    for( int i = 0; i < 10000000; i++ )
    {
      final MetricHandler handler =
        new MultiMetricWriter( new MetricHandler[]{ new GraphiteMetricHandler( graphiteService ),
                                                    new PrintStreamMetricHandler() } );
      for( final Query query : task.getQueries() )
      {
        collectQueryResults( jmxService.acquireConnection(), handler, query );
      }
      Thread.sleep( task.getDelay() );
    }
    graphiteService.close();
    jmxService.close();
  }

  private static JmxTaskDescriptor defineJobDescriptor()
    throws MalformedObjectNameException
  {
    final Query query1 =
      new Query( new ObjectName( "java.lang:type=OperatingSystem" ),
                 null,
                 "Service1" );
    final HashSet<String> attributeNames = new HashSet<String>();
    attributeNames.add( "FreePhysicalMemorySize" );
    final Query query2 =
      new Query( new ObjectName( "java.lang:type=OperatingSystem" ),
                 attributeNames,
                 "Service2" );
    final ArrayList<String> nameComponents = new ArrayList<String>();
    nameComponents.add( "type" );
    nameComponents.add( Query.ATTRIBUTE_COMPONENT );
    nameComponents.add( Query.DOMAIN_COMPONENT );
    final Query query3 =
      new Query( new ObjectName( "java.lang:type=OperatingSystem" ),
                 attributeNames,
                 "Service3",
                 nameComponents );
    final Query query4 =
      new Query( new ObjectName( "java.lang:type=OperatingSystem" ),
                 attributeNames,
                 "Service4",
                 new ArrayList<String>() );
    final Query query5 =
      new Query( new ObjectName( "java.lang:type=OperatingSystem" ),
                 null,
                 null );
    final Query query6 =
      new Query( new ObjectName( "java.lang:type=*" ),
                 null,
                 null );
    final ArrayList<Query> queries = new ArrayList<Query>();
    queries.add( query1 );
    queries.add( query2 );
    queries.add( query3 );
    queries.add( query4 );
    queries.add( query5 );
    queries.add( query6 );

    final JmxServiceDescriptor service = new JmxServiceDescriptor( "127.0.0.1", 1105 );
    return new JmxTaskDescriptor( service, queries, 1000 );
  }

  private static void collectQueryResults( final MBeanServerConnection mBeanServer,
                                           final MetricHandler handler,
                                           final Query query )
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
                                           final Query query,
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
          final String key = query.generateKey( objectName, attributeName );
          final MetricValue metricValue =
            new MetricValue( key, (Number) value, System.currentTimeMillis() );
          handler.metric( metricValue );
        }
      }
    }
  }
}
