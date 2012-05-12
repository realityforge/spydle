package org.realityforge.spydle.runtime.jmx;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.realityforge.spydle.runtime.MetricName;
import org.realityforge.spydle.runtime.MetricSource;
import org.realityforge.spydle.runtime.MetricValue;
import org.realityforge.spydle.runtime.MetricValueSet;

public final class JmxService
  implements MetricSource, Closeable
{
  private static final Logger LOG = Logger.getLogger( JmxService.class.getName() );

  private final JmxTaskDescriptor _descriptor;
  private JMXConnector _connector;
  private boolean _closed;

  public JmxService( @Nonnull final JmxTaskDescriptor descriptor )
  {
    _descriptor = descriptor;
  }

  private MBeanServerConnection acquireConnection()
    throws IOException
  {
    if( _closed )
    {
      throw new EOFException();
    }
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
      final JMXServiceURL url = new JMXServiceURL( _descriptor.getConnection().getURL() );
      _connector = JMXConnectorFactory.connect( url, _descriptor.getConnection().getEnvironment() );
    }
    return _connector.getMBeanServerConnection();
  }

  public void close()
  {
    _closed = true;
    doClose();
  }

  private void doClose()
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

  @Override
  @Nullable
  public MetricValueSet poll()
  {
    final LinkedList<MetricValue> metrics = new LinkedList<>();
    for( final JmxProbeDescriptor probe : _descriptor.getProbes() )
    {
      try
      {
        collectQueryResults( metrics, acquireConnection(), probe );
      }
      catch( final Exception e )
      {
        LOG.log( Level.FINE, "Error querying MBeanServer: " + _descriptor.getConnection() + " Query: " + probe, e );
        doClose();
        return null;
      }
    }

    return new MetricValueSet( metrics, System.currentTimeMillis() );
  }


  private void collectQueryResults( final LinkedList<MetricValue> metrics,
                                    final MBeanServerConnection mBeanServer,
                                    final JmxProbeDescriptor probe )
    throws Exception
  {
    final ObjectName objectName = probe.getObjectName();
    if( objectName.isPattern() )
    {
      final Set<ObjectName> objectNames = mBeanServer.queryNames( objectName, null );
      for( final ObjectName candidate : objectNames )
      {
        collectQueryResults( metrics, mBeanServer, probe, candidate );
      }
    }
    else
    {
      collectQueryResults( metrics, mBeanServer, probe, objectName );
    }
  }

  private void collectQueryResults( final LinkedList<MetricValue> metrics,
                                    final MBeanServerConnection mBeanServer,
                                    final JmxProbeDescriptor probe,
                                    final ObjectName objectName )
    throws Exception
  {
    final MBeanInfo info = mBeanServer.getMBeanInfo( objectName );
    for( final MBeanAttributeInfo attribute : info.getAttributes() )
    {
      final String attributeName = attribute.getName();
      final Set<String> attributeNames = probe.getAttributeNames();
      if( null == attributeNames ||
          attributeNames.contains( attributeName ) )
      {
        final Object value = mBeanServer.getAttribute( objectName, attributeName );
        if( value instanceof Number )
        {
          final MetricName name = probe.generateKey( objectName, attributeName );
          final MetricValue metricValue = new MetricValue( name, (Number) value );
          metrics.add( metricValue );
        }
      }
    }
  }
}
