package org.realityforge.spydle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
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
    new InetSocketAddress( "192.168.0.14", 2003 );
  public static final String GLOBAL_PREFIX = "PD42.SS";

  public static void main( final String[] args )
    throws Exception

  {
    final JMXConnector connector = initConnector( SERVICE );

    final MBeanServerConnection mBeanServer = connector.getMBeanServerConnection();

    final QueryDescriptor query1 =
      new QueryDescriptor( new ObjectName( "java.lang:type=OperatingSystem" ),
                           "OperatingSystem",
                           null );
    final HashSet<String> attributeNames = new HashSet<String>();
    attributeNames.add( "FreePhysicalMemorySize" );
    final QueryDescriptor query2 =
      new QueryDescriptor( new ObjectName( "java.lang:type=OperatingSystem" ),
                           "OperatingSystem2",
                           attributeNames );
    final ArrayList<QueryDescriptor> queries = new ArrayList<QueryDescriptor>();
    queries.add( query1 );
    queries.add( query2 );

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
    final MBeanInfo info = mBeanServer.getMBeanInfo( query.getObjectName() );
    for( final MBeanAttributeInfo attribute : info.getAttributes() )
    {
      final String attributeName = attribute.getName();
      if( query.getAttributeNames().contains( attributeName ) ||
          0 == query.getAttributeNames().size() )
      {
        final String key = query.getMetricPrefix() + "." + attribute.getName();
        final Object value = mBeanServer.getAttribute( query.getObjectName(), attributeName );
        if( value instanceof Number )
        {
          handler.metric( key, System.currentTimeMillis(), ( (Number) value ).longValue() );
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
