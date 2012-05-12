package org.realityforge.spydle.runtime.jdbc;

import java.util.ArrayList;
import java.util.List;
import javax.management.MalformedObjectNameException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.realityforge.spydle.runtime.Namespace;
import org.realityforge.spydle.runtime.util.ConfigUtil;

/**
 * Utility class to interact with the JdbcSource.
 */
public final class JdbcKit
{
  private JdbcKit()
  {
  }

  public static JdbcTaskDescriptor parse( final JSONObject config )
    throws Exception
  {
    final String driver = ConfigUtil.getValue( config, "driver", String.class );
    final String jdbcUrl = ConfigUtil.getValue( config, "url", String.class );

    final String username = ConfigUtil.getValue( config, "username", String.class, false );
    final String password = ConfigUtil.getValue( config, "password", String.class, false );

    final JdbcServiceDescriptor serviceDescriptor = new JdbcServiceDescriptor( driver, jdbcUrl, username, password );

    final List<JdbcQuery> queries = new ArrayList<>();

    final JSONArray queryArray = ConfigUtil.getValue( config, "probes", JSONArray.class );
    for( final Object queryConfig : queryArray )
    {
      queries.add( parseQuery( (JSONObject) queryConfig ) );
    }

    return new JdbcTaskDescriptor( serviceDescriptor, queries );
  }

  private static JdbcQuery parseQuery( final JSONObject config )
    throws MalformedObjectNameException
  {
    final String query = ConfigUtil.getValue( config, "query", String.class );
    final String keyColumn = ConfigUtil.getValue( config, "key_column", String.class, false );
    final Namespace namespace = ConfigUtil.parseNamespace( config );
    return new JdbcQuery( query, keyColumn, namespace );
  }
}
