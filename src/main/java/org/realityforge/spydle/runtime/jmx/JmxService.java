package org.realityforge.spydle.runtime.jmx;

import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.realityforge.spydle.descriptors.jmx.JmxServiceDescriptor;

public class JmxService
  implements Closeable
{
  private final JmxServiceDescriptor _descriptor;
  private JMXConnector _connector;

  public JmxService( @Nonnull final JmxServiceDescriptor descriptor )
  {
    _descriptor = descriptor;
  }

  public MBeanServerConnection acquireConnection()
    throws IOException
  {
    if( null != _connector )
    {
      try
      {
        //Attempt to ping connection and if it fails null out the connector to force a reconnect
        _connector.getConnectionId();
      }
      catch( final IOException ioe )
      {
        _connector = null;
      }
    }
    if( null == _connector )
    {
      final JMXServiceURL url = new JMXServiceURL( _descriptor.getURL() );
      _connector = JMXConnectorFactory.connect( url, _descriptor.getEnvironment() );
    }
    return _connector.getMBeanServerConnection();
  }

  public void close()
  {
    if( null != _connector )
    {
      try
      {
        _connector.close();
      }
      catch( final IOException ioe )
      {
        //Ignored
      }
      _connector = null;
    }
  }
}
