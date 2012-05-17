package org.realityforge.spydle.jdbc;

import java.util.ArrayList;
import java.util.List;
import javax.management.MalformedObjectNameException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.realityforge.spydle.Namespace;
import org.realityforge.spydle.util.ConfigUtil;

/**
 * Utility class to interact with the JdbcSource.
 */
public final class JdbcKit
{
  private JdbcKit()
  {
  }

  public static JdbcService build( final JSONObject config )
    throws Exception
  {
    return new JdbcService( parse( config ) );
  }

  private static JdbcTaskDescriptor parse( final JSONObject config )
    throws Exception
  {
    final String driver = ConfigUtil.getValue( config, "driver", String.class );
    final String jdbcUrl = ConfigUtil.getValue( config, "url", String.class );

    final String username = ConfigUtil.getValue( config, "username", String.class, false );
    final String password = ConfigUtil.getValue( config, "password", String.class, false );

    final JdbcConnectionDescriptor connectionDescriptor = new JdbcConnectionDescriptor( driver, jdbcUrl, username, password );

    final List<JdbcProbeDescriptor> probes = new ArrayList<>();

    final JSONArray queryArray = ConfigUtil.getValue( config, "probes", JSONArray.class );
    for( final Object queryConfig : queryArray )
    {
      probes.add( parseQuery( (JSONObject) queryConfig ) );
    }

    return new JdbcTaskDescriptor( connectionDescriptor, probes );
  }

  private static JdbcProbeDescriptor parseQuery( final JSONObject config )
    throws MalformedObjectNameException
  {
    final String query = ConfigUtil.getValue( config, "query", String.class );
    final String keyColumn = ConfigUtil.getValue( config, "key_column", String.class, false );
    final Namespace namespace = ConfigUtil.parseNamespace( config );
    return new JdbcProbeDescriptor( query, keyColumn, namespace );
  }
}
