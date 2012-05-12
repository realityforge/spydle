package org.realityforge.spydle.runtime.jmx;

import java.io.Closeable;
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

  public JmxService( @Nonnull final JmxTaskDescriptor descriptor )
  {
    _descriptor = descriptor;
  }

  private MBeanServerConnection acquireConnection()
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
      final JMXServiceURL url = new JMXServiceURL( _descriptor.getService().getURL() );
      _connector = JMXConnectorFactory.connect( url, _descriptor.getService().getEnvironment() );
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

  @Override
  @Nullable
  public MetricValueSet poll()
  {
    final LinkedList<MetricValue> metrics = new LinkedList<>();
    for( final JmxQuery query : _descriptor.getQueries() )
    {
      try
      {
        collectQueryResults( metrics, acquireConnection(), query );
      }
      catch( final Exception e )
      {
        LOG.log( Level.FINE, "Error querying MBeanServer: " + _descriptor.getService() + " Query: " + query, e );
        close();
        return null;
      }
    }

    return new MetricValueSet( metrics, System.currentTimeMillis() );
  }


  private void collectQueryResults( final LinkedList<MetricValue> metrics,
                                    final MBeanServerConnection mBeanServer,
                                    final JmxQuery query )
    throws Exception
  {
    final ObjectName objectName = query.getObjectName();
    if( objectName.isPattern() )
    {
      final Set<ObjectName> objectNames = mBeanServer.queryNames( objectName, null );
      for( final ObjectName candidate : objectNames )
      {
        collectQueryResults( metrics, mBeanServer, query, candidate );
      }
    }
    else
    {
      collectQueryResults( metrics, mBeanServer, query, objectName );
    }
  }

  private void collectQueryResults( final LinkedList<MetricValue> metrics,
                                    final MBeanServerConnection mBeanServer,
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
          final MetricValue metricValue = new MetricValue( name, (Number) value );
          metrics.add( metricValue );
        }
      }
    }
  }
}
