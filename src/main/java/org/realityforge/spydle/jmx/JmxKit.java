package org.realityforge.spydle.jmx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.realityforge.spydle.Namespace;
import org.realityforge.spydle.util.ConfigUtil;

/**
 * Utility class to interact with the JmxSource.
 */
public final class JmxKit
{
  private JmxKit()
  {
  }

  public static JmxService build( final JSONObject config )
    throws Exception
  {
    return new JmxService( parse( config ) );
  }

  private static JmxTaskDescriptor parse( final JSONObject config )
    throws Exception
  {
    final String host = ConfigUtil.getValue( config, "host", String.class );
    final int port = ConfigUtil.getValue( config, "port", Number.class ).intValue();

    final String username = ConfigUtil.getValue( config, "username", String.class, false );
    final String password = ConfigUtil.getValue( config, "password", String.class, false );

    final JmxConnectionDescriptor connectionDescriptor = new JmxConnectionDescriptor( host, port, username, password );

    final List<JmxProbeDescriptor> probes = new ArrayList<>();

    final JSONArray queryArray = ConfigUtil.getValue( config, "probes", JSONArray.class );
    for( final Object queryConfig : queryArray )
    {
      probes.add( parseQuery( (JSONObject) queryConfig ) );
    }

    return new JmxTaskDescriptor( connectionDescriptor, probes );
  }

  private static JmxProbeDescriptor parseQuery( final JSONObject config )
    throws MalformedObjectNameException
  {
    final String objectName = ConfigUtil.getValue( config, "object_name", String.class );
    final JSONArray queryArray = ConfigUtil.getValue( config, "attribute_names", JSONArray.class, false );
    final HashSet<String> attributeNames;
    if( null != queryArray )
    {
      attributeNames = new HashSet<>();
      for( final Object attributeName : queryArray )
      {
        attributeNames.add( attributeName.toString() );
      }
    }
    else
    {
      attributeNames = null;
    }
    final Namespace namespace = ConfigUtil.parseNamespace( config );
    final JSONArray nameComponentsArray = ConfigUtil.getValue( config, "name_components", JSONArray.class, false );
    final ArrayList<String> nameComponents;
    if( null != nameComponentsArray )
    {
      nameComponents = new ArrayList<>();
      for( final Object nameComponent : nameComponentsArray )
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
