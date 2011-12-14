package org.realityforge.spydle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Main
{

  public static final ServiceDescriptor SERVICE = new ServiceDescriptor( "127.0.0.1", 1105 );
  public static final InetSocketAddress GRAPHITE_ADDRESS =
    new InetSocketAddress( "192.168.0.10", 2003 );
  public static final String GLOBAL_PREFIX = "PD42.SS";

  public static void main( final String[] args )
    throws Exception

  {
    final JMXConnector connector = initConnector( SERVICE );

    final MBeanServerConnection mBeanServer = connector.getMBeanServerConnection();

    final QueryDescriptor query1 =
      new QueryDescriptor( new ObjectName( "java.lang:type=OperatingSystem" ),
                           null,
                           "Service1" );
    final HashSet<String> attributeNames = new HashSet<String>();
    attributeNames.add( "FreePhysicalMemorySize" );
    final QueryDescriptor query2 =
      new QueryDescriptor( new ObjectName( "java.lang:type=OperatingSystem" ),
                           attributeNames,
                           "Service2" );
    final ArrayList<String> nameComponents = new ArrayList<String>();
    nameComponents.add( "type" );
    nameComponents.add( QueryDescriptor.ATTRIBUTE_COMPONENT );
    nameComponents.add( QueryDescriptor.DOMAIN_COMPONENT );
    final QueryDescriptor query3 =
      new QueryDescriptor( new ObjectName( "java.lang:type=OperatingSystem" ),
                           attributeNames,
                           "Service3",
                           nameComponents );
    final QueryDescriptor query4 =
      new QueryDescriptor( new ObjectName( "java.lang:type=OperatingSystem" ),
                           attributeNames,
                           "Service4",
                           new ArrayList<String>() );
    final QueryDescriptor query5 =
      new QueryDescriptor( new ObjectName( "java.lang:type=OperatingSystem" ),
                           null,
                           null );
    final QueryDescriptor query6 =
      new QueryDescriptor( new ObjectName( "java.lang:type=*" ),
                           null,
                           null );
    final ArrayList<QueryDescriptor> queries = new ArrayList<QueryDescriptor>();
    queries.add( query1 );
    queries.add( query2 );
    queries.add( query3 );
    queries.add( query4 );
    queries.add( query5 );
    queries.add( query6 );

    for( int i = 0; i < 10000000; i++ )
    {
      final MetricHandler handler = new GraphiteMetricHandler( GRAPHITE_ADDRESS, GLOBAL_PREFIX );
      handler.open();
      for( final QueryDescriptor query : queries )
      {
        collectQueryResults( mBeanServer, handler, query );
      }
      handler.close();
      System.out.println( "." );
      Thread.sleep( 1000 );
    }

    connector.close();
  }

  private static void collectQueryResults( final MBeanServerConnection mBeanServer,
                                           final MetricHandler handler,
                                           final QueryDescriptor query )
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
                                           final QueryDescriptor query,
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
          final long numericValue = ( (Number) value ).longValue();
          System.out.println( key + " = " + numericValue );
          handler.metric( key, System.currentTimeMillis(), numericValue );
        }
      }
    }
  }

  private static JMXConnector initConnector( final ServiceDescriptor service )
    throws IOException
  {
    final JMXServiceURL url = new JMXServiceURL( service.getURL() );
    return JMXConnectorFactory.connect( url, service.getEnvironment() );
  }
}
