package org.realityforge.spydle.jdbc;

import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.management.MalformedObjectNameException;

/**
 * Utility class to interact with the JdbcSource.
 */
public final class JdbcKit
{
  private JdbcKit()
  {
  }

  public static JdbcService build( final JsonObject config )
    throws Exception
  {
    return new JdbcService( parse( config ) );
  }

  private static JdbcTaskDescriptor parse( final JsonObject config )
    throws Exception
  {
    final String driver = config.getString( "driver" );
    final String jdbcUrl = config.getString( "url" );

    final String username = config.getString( "username", null );
    final String password = config.getString( "password", null );

    final JdbcConnectionDescriptor connectionDescriptor =
      new JdbcConnectionDescriptor( driver, jdbcUrl, username, password );

    final List<JdbcProbeDescriptor> probes = new ArrayList<>();

    final JsonArray queryArray = config.containsKey( "probes" ) ? config.getJsonArray( "probes" ) : null;
    if ( null != queryArray )
    {
      for ( final Object queryConfig : queryArray )
      {
        probes.add( parseQuery( (JsonObject) queryConfig ) );
      }
    }

    return new JdbcTaskDescriptor( connectionDescriptor, probes );
  }

  private static JdbcProbeDescriptor parseQuery( final JsonObject config )
    throws MalformedObjectNameException
  {
    final String query = config.getString( "query" );
    final String keyColumn = config.getString( "key_column", null );
    final String namespace = config.getString( "namespace", null );
    return new JdbcProbeDescriptor( query, keyColumn, namespace );
  }
}
