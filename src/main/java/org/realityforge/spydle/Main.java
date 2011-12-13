package org.realityforge.spydle;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
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
    final JMXServiceURL url =
      new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://127.0.0.1:1105/jmxrmi" );
    final JMXConnector connector = JMXConnectorFactory.connect( url, getEnvironment( null, null ) );

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
    final InetSocketAddress socketAddress = new InetSocketAddress( "192.168.0.11", 2003 );
    for( int i = 0; i < 10000000; i++ )
    {
      final Socket socket = new Socket();
      socket.connect( socketAddress );
      final OutputStream outputStream = socket.getOutputStream();
      for( final MBeanAttributeInfo attribute : attributes )
      {
        final Object value = mBeanServer.getAttribute( objectName, attribute.getName() );
        if( value instanceof Number )
        {
          final long time = System.currentTimeMillis() / 1000;
          final String line = "PD42.SS." + attribute.getName() + " " + value + " " + time + "\n";
          outputStream.write( line.getBytes( "US-ASCII" ) );
        }
      }
      System.out.println( "." );
      socket.close();
      Thread.sleep( 1000 );
    }

    connector.close();
  }

  private static Map<String, String[]> getEnvironment( @Nullable final String username,
                                                       @Nullable final String password )
  {
    final Map<String, String[]> environment = new HashMap<String, String[]>();
    if( null != username && null != password )
    {
      environment.put( JMXConnector.CREDENTIALS, new String[]{ username, password } );
    }
    return environment;
  }
}
