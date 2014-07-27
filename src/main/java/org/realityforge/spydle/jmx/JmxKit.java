package org.realityforge.spydle.jmx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.json.JsonObject;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Utility class to interact with the JmxSource.
 */
public final class JmxKit
{
  private JmxKit()
  {
  }

  public static JmxService build( final JsonObject config )
    throws Exception
  {
    return new JmxService( parse( config ) );
  }

  private static JmxTaskDescriptor parse( final JsonObject config )
    throws Exception
  {
    final String host = config.getString( "host" );
    final int port = config.getInt( "port" );

    final String username = config.getString( "username", null );
    final String password = config.getString( "password", null );

    final JmxConnectionDescriptor connectionDescriptor = new JmxConnectionDescriptor( host, port, username, password );

    final List<JmxProbeDescriptor> probes = new ArrayList<>();

    if ( config.containsKey( "probes" ) )
    {
      for ( final Object queryConfig : config.getJsonArray( "probes" ) )
      {
        probes.add( parseQuery( (JsonObject) queryConfig ) );
      }
    }

    return new JmxTaskDescriptor( connectionDescriptor, probes );
  }

  private static JmxProbeDescriptor parseQuery( final JsonObject config )
    throws MalformedObjectNameException
  {
    final String objectName = config.getString( "object_name" );
    final HashSet<String> attributeNames;
    if ( config.containsKey( "attribute_names" ) )
    {
      attributeNames = new HashSet<>();
      for ( final Object attributeName : config.getJsonArray( "attribute_names" ) )
      {
        attributeNames.add( attributeName.toString() );
      }
    }
    else
    {
      attributeNames = null;
    }
    final String namespace = config.getString( "namespace", null );
    final ArrayList<String> nameComponents;
    if ( config.containsKey( "name_components" ) )
    {
      nameComponents = new ArrayList<>();
      for ( final Object nameComponent : config.getJsonArray( "name_components" ) )
      {
        nameComponents.add( nameComponent.toString() );
      }
    }
    else
    {
      nameComponents = null;
    }
    return new JmxProbeDescriptor( new ObjectName( objectName ), attributeNames, namespace, nameComponents );
  }
}
