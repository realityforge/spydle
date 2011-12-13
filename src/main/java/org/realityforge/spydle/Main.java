package org.realityforge.spydle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Main
{
  public static void main( final String[] args )
    throws Exception

  {
    final JMXConnector connector = initConnector( new ServiceDescriptor( "127.0.0.1", 1105 ) );

    final MBeanServerConnection mBeanServer = connector.getMBeanServerConnection();
    final ObjectName objectName = new ObjectName( "java.lang:type=OperatingSystem" );
    final MBeanInfo info = mBeanServer.getMBeanInfo( objectName );
    final MBeanAttributeInfo[] attributes = info.getAttributes();
    for( final MBeanAttributeInfo attribute : attributes )
    {
      final Object value = mBeanServer.getAttribute( objectName, attribute.getName() );
      System.out.println( attribute.getType() + " " + attribute.getName() + "=" + value );
    }

    try
    {
      final Object value = mBeanServer.getAttribute( objectName, "foo" );
      System.out.println( " foo=" + value );
    }
    catch( final AttributeNotFoundException anfe )
    {
      //Ignored
    }
    final Set<ObjectName> objectNames = mBeanServer.queryNames( objectName, null );
    final InetSocketAddress socketAddress = new InetSocketAddress( "192.168.0.11", 2003 );
    for( int i = 0; i < 10000000; i++ )
    {
      final MetricHandler handler = new GraphiteMetricHandler( socketAddress, "PD42.SS" );
      handler.open();
      for( final MBeanAttributeInfo attribute : attributes )
      {
        final String key = attribute.getName();
        final String attributeName = attribute.getName();
        final Object value = mBeanServer.getAttribute( objectName, attributeName );
        if( value instanceof Number )
        {
          handler.metric( key, System.currentTimeMillis(), ( (Number) value ).longValue() );
        }
      }
      handler.close();
      System.out.println( "." );
      Thread.sleep( 1000 );
    }

    connector.close();
  }

  private static JMXConnector initConnector( final ServiceDescriptor service )
    throws IOException
  {
    final JMXServiceURL url = new JMXServiceURL( service.getURL() );
    return JMXConnectorFactory.connect( url, service.getEnvironment() );
  }
}
